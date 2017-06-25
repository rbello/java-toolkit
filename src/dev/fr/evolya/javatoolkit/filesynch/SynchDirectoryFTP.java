package fr.evolya.javatoolkit.filesynch;

import java.io.File;
import java.net.SocketException;
import java.util.Date;
import java.util.TimeZone;

import fr.evolya.javatoolkit.code.Cache;
import fr.evolya.javatoolkit.code.Logs;
import fr.evolya.javatoolkit.code.utils.Utils;
import fr.evolya.javatoolkit.net.ftp.FTPDataTransferListener;
import fr.evolya.javatoolkit.net.ftp.FTPFile;
import fr.evolya.javatoolkit.net.ftp.FTPServerReference;

public class SynchDirectoryFTP implements SynchDirectory {

	private FTPServerReference _server;
	private String _path;
	private TimeZone _timezone;
	private boolean _ready = false;
	private Cache<String, FTPFile[]> cache;

	public SynchDirectoryFTP(FTPServerReference server, String path) {
		this(server, path, TimeZone.getTimeZone("GMT"));
	}
	
	public SynchDirectoryFTP(FTPServerReference server, String path, TimeZone timezone) {
		_server = server;
		_path = path;
		_timezone = timezone;
		cache = new Cache<String, FTPFile[]>(1) {
			@Override
			protected FTPFile[] updateValue(String path) throws Exception {
				return _server.list(path);
			}
		};
	}
	
	public String[] list(String path) {
		
		// Clean
		path = Utils.clean_path(_path + "/" + path);
		
		// Get value
		FTPFile[] list = cache.getValue(path);
		
		// Méfiance...
		if (list == null) return null;
		
		// Fetch list
		String[] names = new String[list.length];
		int i = 0;
		for (FTPFile file : list) {
			names[i++] = file.getName();
		}
		
		// Return the list of file names
		return names;
	}

	@Override
	public String toString() {
		return "ftp:/" + _server.getHost() + ":" + _server.getPort()
				+ "@" + _server.getUsername() + " (" + _timezone.getID() + ")";
	}

	public boolean isFile(String path) {
		return exists(this._path + "/" + path, FTPFile.TYPE_FILE);
	}

	public boolean isDirectory(String path) {
		return exists(this._path + "/" + path, FTPFile.TYPE_DIRECTORY);
	}

	private boolean exists(String path, int type) {
		
		// Clean
		path = Utils.clean_path(path);
		
		// Si un cache existe pour ce chemin, c'est qu'il s'agit d'un répertoire
		if (cache.isCachedAndValid(path)) {
			return true;
		}
		
		// On recherche le parent de ce répertoire
		String parent = Utils.dirname(path);
		String filename = new File(path).getName();
		
		// On demande au cache le listing des fichiers
		FTPFile[] list = cache.getValue(parent);
		
		// Le parent n'existe pas, donc ce fichier non plus
		if (list == null) {
			return false;
		}

		// On parcours le cache à la recherche du nom du fichier
		for (FTPFile file : list) {
			if (file.getType() != type)
				continue;
			if (file.getName().equals(filename))
				return true;
		}
		
		// Sinon ce n'est pas un dossier
		return false;
	}

	@Override
	public long getSize(String path) {
		try {
			return _server.getSize(this._path + "/" + path);
		} catch (Exception e) {
			handleException(e);
			return -1;
		}
	}

	@Override
	public void mkdir(String path) {
		try {
			path = Utils.clean_path(path);
			_server.mkdir(path);
			cache.invalidate(Utils.dirname(path));
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public void open() throws Exception {
		_server.connect();
		_server.changeDirectory(_path);
		_ready = true;
		// TODO Déterminer la timezone
	}

	@Override
	public void close() throws Exception {
		try {
			_server.close();
		}
		finally {
			_ready = false;
		}
	}

	@Override
	public Date getDeletedDate(String path) {
		// Non supporté par cette implémentation
		return null;
	}

	@Override
	public Date getCreatedDate(String path) {
		try {
			return Utils.toGMT(_server.getCreatedDate(path), this._timezone);
		} catch (Exception e) {
			handleException(e);
			return null;
		}
	}

	@Override
	public Date getModifiedDate(String path) {
		try {
			return Utils.toGMT(_server.getModifiedDate(path), this._timezone);
		} catch (Exception e) {
			handleException(e);
			return null;
		}
	}

	@Override
	public boolean copyFileTo(String path, SynchDirectory target) {
		if (target instanceof SynchDirectoryFilesystem) {
			SynchDirectoryFilesystem fs = (SynchDirectoryFilesystem) target;
			File file = fs.getFile(path);
			try {
				_server.download(file, path, new TouchLocalFileFromRemote(fs, path, file));
			} catch (Exception e) {
				handleException(e);
			}
			return true;
			
		}
		return false;
	}

	@Override
	public boolean copyFileFrom(String path, SynchDirectory source) {
		if (source instanceof SynchDirectoryFilesystem) {
			SynchDirectoryFilesystem fs = (SynchDirectoryFilesystem) source;
			File file = fs.getFile(path);
			try {
				_server.upload(file, path, new TouchLocalFileFromRemote(fs, path, file));
			} catch (Exception e) {
				handleException(e);
			}
			return true;
		}
		return false;
	}

	@Override
	public TimeZone getTimeZone() {
		return _timezone;
	}

	@Override
	public boolean isReady() {
		return _ready;
	}
	
	private void handleException(Exception ex) {
		if (ex instanceof SocketException) {
			_ready = false;
			if (FileSynchService.LOGGER.isLoggable(Logs.INFO)) {
				FileSynchService.LOGGER.log(Logs.INFO, "SynchDirectoryFTP disconnected: " + this);
			}
			cache.invalidate();
			_server.interrupt();
		}
	}
	
	private final class TouchLocalFileFromRemote implements FTPDataTransferListener {
		
		private final SynchDirectoryFilesystem fs;
		private final String path;
		private final File file;

		private TouchLocalFileFromRemote(SynchDirectoryFilesystem fs, String path, File file) {
			this.fs = fs;
			this.path = path;
			this.file = file;
		}

		public void transferred(int length) { }
		public void started() { }
		public void failed() { }
		public void aborted() { }

		public void completed() {
			try {
				// On recupère la date de modification du fichier sur le serveur en GMT
				Date modified = Utils.toGMT(_server.getModifiedDate(path), _timezone);
				// On modifie cette date pour qu'elle soit comme celle du système de fichier d'origine
				modified = Utils.toTimeZone(modified, fs.getTimeZone());
				// On modifie cette date sur le système local
				file.setLastModified(modified.getTime());
			} catch (Exception e) {
				handleException(e);
			}
		}
	}
	
}
