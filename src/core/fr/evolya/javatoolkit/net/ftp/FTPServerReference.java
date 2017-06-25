package fr.evolya.javatoolkit.net.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import fr.evolya.javatoolkit.code.utils.Utils;

public class FTPServerReference {
	
	protected String host;
	protected int port;
	protected String username;
	protected String password;
	
	protected FTPClient ftp;
	
	public FTPServerReference(String host) {
		this(host, 21, "anonymous", "anonymous@nodomain.com");
	}
	
	public FTPServerReference(String host, String username, String password) {
		this(host, 21, username, password);
	}
	
	public FTPServerReference(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void connect() throws Exception {
		ftp = new FTPClient();
		ftp.connect(host, port);
		ftp.login(username, password);
	}

	public void changeDirectory(String path) throws Exception {
		ftp.changeDirectory(path);
	}

	public void close() throws Exception {
		try {
			ftp.disconnect(true);
		}
		catch (Exception ex) {
			ftp.disconnect(false);
		}
	}

	/**
	 * This method asks and returns a file size in bytes.
	 * 
	 * @param path
	 *            The path to the file.
	 * @return The file size in bytes.
	 */
	public long getSize(String path) throws Exception {
		return ftp.fileSize(path);
	}

	/**
	 * This method asks and returns the last modification date of a file or
	 * directory.
	 * 
	 * @param path
	 *            The path to the file or the directory.
	 * @return The file/directory last modification date.
	 */
	public Date getModifiedDate(String path) throws Exception {
		return ftp.modifiedDate(path);
	}
	
	public Date getCreatedDate(String path) throws Exception {
		return null;
	}

	public void mkdir(String path) throws Exception {
		ftp.createDirectory(Utils.clean_path(path));
	}
	
	public void upload(File file, String path, FTPDataTransferListener listener) throws Exception {
		ftp.changeDirectory(Utils.dirname(Utils.clean_path(path)));
		ftp.upload(file.getName(), new FileInputStream(file), 0, 0, listener);
	}

	public void download(File file, String path, FTPDataTransferListener listener) throws Exception {
		ftp.changeDirectory(Utils.dirname(Utils.clean_path(path)));
		ftp.download(file.getName(), file, listener);
	}

	public void interrupt() {
		try {
			ftp.disconnect(false);
		} catch (Throwable t) { }
	}

	public FTPFile[] list(String path) throws Exception {
		ftp.changeDirectory(path);
		return ftp.list();
	}

}
