package fr.evolya.javatoolkit.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import fr.evolya.javatoolkit.audio.awt.PlayerThread;

public class Audio {
	
	private final static int BUFFER_SIZE = 128000;
	private static final String MP3_SUFFIX = ".mp3";
	
    public static void playWav(File file)
    		throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    	playWav(new FileInputStream(file));
    }
    
    public static void playWav(InputStream stream)
    		throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    	playWav(AudioSystem.getAudioInputStream(new BufferedInputStream(stream)));
    }
    
    public static void playWav(AudioInputStream stream)
    		throws UnsupportedAudioFileException, IOException, LineUnavailableException {

    	AudioFormat audioFormat = stream.getFormat();

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);

        SourceDataLine sourceLine = null;
        sourceLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceLine.open(audioFormat);
        sourceLine.start();

        int nBytesRead = 0;
        byte[] abData = new byte[BUFFER_SIZE];
        while (nBytesRead != -1) {
            try {
                nBytesRead = stream.read(abData, 0, abData.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nBytesRead >= 0) {
                @SuppressWarnings("unused")
                int nBytesWritten = sourceLine.write(abData, 0, nBytesRead);
            }
        }

        sourceLine.drain();
        sourceLine.close();
    }
    
    public static boolean isMp3(File f) {
        return f.getName().toLowerCase().endsWith(MP3_SUFFIX);
    }
    
    public static Decoder playMp3(File file) {
    	//PlayerThread.startPlaying(null, file, null);
    	try {
	    	FileInputStream in = new FileInputStream(file);
	        BufferedInputStream bin = new BufferedInputStream(in, 128 * 1024);
	        Decoder decoder = new Decoder();
	        decoder.play(file.getName(), bin);
	        return decoder;
    	}
    	catch (Exception ex) {
    		throw new RuntimeException(ex);
    	}
    }

}
