package fr.evolya.javatoolkit.filesynch;

import java.util.Date;
import java.util.TimeZone;

public interface SynchDirectory {

	public String[] list(String path);

	public boolean isDirectory(String path);

	public boolean isFile(String path);
	
	public Date getDeletedDate(String path);

	public Date getCreatedDate(String path);

	public Date getModifiedDate(String path);
	
	public long getSize(String path);

	public void open() throws Exception;

	public void close() throws Exception;

	public boolean copyFileTo(String path, SynchDirectory target) throws Exception;

	public boolean copyFileFrom(String path, SynchDirectory source) throws Exception;
	
	public void mkdir(String path) throws Exception;

	public TimeZone getTimeZone();

	public boolean isReady();

}
