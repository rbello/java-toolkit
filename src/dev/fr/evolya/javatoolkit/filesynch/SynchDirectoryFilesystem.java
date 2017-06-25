package fr.evolya.javatoolkit.filesynch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.TimeZone;

import fr.evolya.javatoolkit.code.utils.Utils;

public class SynchDirectoryFilesystem implements SynchDirectory {

	private File base;
	private TimeZone timezone;

	public SynchDirectoryFilesystem(String path) {
		this(new File(path));
	}
	
	public SynchDirectoryFilesystem(File path) {
		this(path, TimeZone.getDefault());
	}
	
	public SynchDirectoryFilesystem(String path, TimeZone timezone) {
		this(new File(path), timezone);
	}
	
	public SynchDirectoryFilesystem(File path, TimeZone timezone) {
		// Méfiance...
		if (!path.exists()) {
			throw new InvalidPathException(path.toString(),
					"Given path doesn't exists");
		}
		if (!path.isDirectory()) {
			throw new InvalidPathException(path.toString(),
					"Given file is not a directory");
		}
		// On conserve les références
		this.base = path;
		this.timezone = timezone;
	}
	
	@Override
	public String toString() {
		return "file:/" + base.getAbsolutePath()
				+ " (" + timezone.getID() + ")";
	}

	@Override
	public String[] list(String path) {
		/*
		 * http://docs.oracle.com/javase/6/docs/api/java/io/File.html#list()
		 * 
		 * Returns:
		 * An array of strings naming the files and directories in the
		 * directory denoted by this abstract pathname. The array will
		 * be empty if the directory is empty. Returns null if this
		 * abstract pathname does not denote a directory, or if an
		 * I/O error occurs.
		 * 
		 * Throws:
		 * SecurityException - If a security manager exists and its
		 * SecurityManager.checkRead(java.lang.String) method denies
		 * read access to the directory.
		 */
		return new File(this.base, path).list();
	}

	@Override
	public boolean isDirectory(String path) {
		return new File(this.base, path).isDirectory();
	}

	@Override
	public boolean isFile(String path) {
		return new File(this.base, path).isFile();
	}
	
	@Override
	public long getSize(String path) {
		return new File(base, path).length();
	}

	@Override
	public void mkdir(String path) {
		File file = new File(base, path);
		if (!file.mkdir()) {
			throw new IllegalStateException();
		}
	}

	@Override
	public void open() throws Exception { }

	@Override
	public void close() throws Exception { }

	@Override
	public Date getDeletedDate(String path) {
		// Non supporté par cette implémentation
		return null;
	}

	@Override
	public Date getCreatedDate(String path) {
		// On recupère le chemin vers le dossier
		Path p = FileSystems.getDefault().getPath(
				base.getAbsolutePath(), path);
		// On tente de lire les attributs du système de fichier
		try {
			BasicFileAttributes attr = Files.readAttributes(
					p, BasicFileAttributes.class);
			// On arrondi à la seconde, car la plupart des systèmes de fichiers
			// ne gèrent pas les millisecondes
			long millis = (long) Math.floor(attr.creationTime().toMillis() / 1000) * 1000;
			return Utils.toGMT(new Date(millis), this.timezone);
		}
		catch (Exception ex) {
			return null;
		}
	}

	@Override
	public Date getModifiedDate(String path) {
		// On fabrique le fichier
		File file = new File(base, path);
		// Le fichier n'existe pas
		if (!file.exists()) return null;
		// C'est un fichier, on renvoie la date de dernière
		// modification.
		return getLastModified(file);
	}
	
	public Date getLastModified(File file) {
		// On arrondi à la seconde, car la plupart des systèmes de fichiers
		// ne gèrent pas les millisecondes
		long millis = (long) Math.floor(file.lastModified() / 1000) * 1000;
		return Utils.toGMT(new Date(millis), timezone); 
	}

	@Override
	public boolean copyFileTo(String path, SynchDirectory target) throws Exception {
		if (target instanceof SynchDirectoryFilesystem) {
			File src = getFile(path);
			File desc = ((SynchDirectoryFilesystem)target).getFile(path);
			copyFileUsingStream(src, desc);
			desc.setLastModified(src.lastModified());
			return true;
		}
		return false;
	}

	@Override
	public boolean copyFileFrom(String path, SynchDirectory source) throws Exception {
		if (source instanceof SynchDirectoryFilesystem) {
			File src = ((SynchDirectoryFilesystem)source).getFile(path);
			File desc = getFile(path);
			copyFileUsingStream(src, desc);
			desc.setLastModified(src.lastModified());
			return true;
		}
		return false;
	}
	
	 public File getFile(String path) {
		return new File(this.base, path);
	}

	private static void copyFileUsingStream(File source, File dest) throws IOException {
	        InputStream is = null;
	        OutputStream os = null;
	        try {
	            is = new FileInputStream(source);
	            os = new FileOutputStream(dest);
	            byte[] buffer = new byte[1024];
	            int length;
	            while ((length = is.read(buffer)) > 0) {
	                os.write(buffer, 0, length);
	            }
	        } finally {
	            is.close();
	            os.close();
	        }
	    }

	@Override
	public TimeZone getTimeZone() {
		return timezone;
	}

	@Override
	public boolean isReady() {
		return true;
	}

}
