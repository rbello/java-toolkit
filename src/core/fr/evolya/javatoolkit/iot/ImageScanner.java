package fr.evolya.javatoolkit.iot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import fr.evolya.javatoolkit.code.KeyValue;
import fr.evolya.javatoolkit.code.funcint.Callback;

public class ImageScanner extends KeyValue<String, String> {
	
	public static final String MODE_COLOR = "Color";
	public static final String MODE_BW = "Lineart";
	public static final String MODE_GRAYSCALE = "Gray";
	public static final String FILE_TIFF = "TIFF";
	
	public static interface DetectionCallback extends Callback<ImageScanner, String> { }
	
	public ImageScanner(String id, String name) {
		super(id, name);
	}

	public static void detectFirstScanner(DetectionCallback callback) {
		detectFirstScannerRoot(null, callback);
	}
	
	public static void detectFirstScannerRoot(String rootPwd, DetectionCallback callback) {
		new Thread(new Runnable() {
			public void run() {
				try {
					String[] cmd = {"scanimage", "-L"};
					if (rootPwd != null) {
						System.out.println("root");
						cmd = new String[]{"/bin/bash","-c","echo "+rootPwd+"| sudo -S scanimage -L"};
					}
					Process ps = Runtime.getRuntime().exec(cmd);
					ps.waitFor();
					if (ps.exitValue() != 0) {
						callback.onFailure("process exited with code = " + ps.exitValue());
						return;
					}
			        InputStream is = ps.getInputStream();
			        BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(is));
			        String str = reader.readLine();
			        is.close();
			        if (str == null) {
			        	callback.onFailure("empty input stream");
			        	return;
			        }
			        if (str.isEmpty()) {
			        	callback.onFailure("no device available");
			        	return;
			        }
			        //System.out.println(str);
			        String[] tokens = str.split("' is a ");
			        callback.onSuccess(new ImageScanner(tokens[0].substring(8), tokens[1]));
				}
				catch (IOException | InterruptedException ex) {
					callback.onFailure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		}).start();
	}
	
	public static void scan(File target, String scannerID, String colorMode, int dpi, String format, Callback<File, String> callback) {
		new Thread(new Runnable() {
			public void run() {
				try {
					ProcessBuilder builder = new ProcessBuilder(
							"scanimage", 
							"-d", scannerID, 
							"--format", format,
							"--resolution", ""+dpi,
							"--mode", colorMode
							//,"--progress"
							);
					builder.redirectOutput(target);
					Process ps = builder.start();
					ps.waitFor();
					if (ps.exitValue() != 0) {
						InputStream is = ps.getErrorStream();
				        BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(is));
				        String str = reader.readLine();
				        is.close();
						callback.onFailure("process exited with code = " + ps.exitValue()
							+ "\nmessage = " + (str == null ? "" : str.trim()));
						return;
					}
					callback.onSuccess(target);
				}
				catch (IOException | InterruptedException ex) {
					callback.onFailure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
				}
			}
		}).start();
	}

}
