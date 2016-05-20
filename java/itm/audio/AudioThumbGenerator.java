package itm.audio;

import java.io.ByteArrayInputStream;

/*******************************************************************************
 This file is part of the ITM course 2016
 (c) University of Vienna 2009-2016
*******************************************************************************/


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * 
 * This class creates acoustic thumbnails from various types of audio files. It
 * can be called with 3 parameters, an input filename/directory and an output
 * directory, and the desired length of the thumbnail in seconds. It will read
 * MP3 or OGG encoded input audio files(s), cut the contained audio data to a
 * given length (in seconds) and saves the acoustic thumbnails to a certain
 * length.
 * 
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */

public class AudioThumbGenerator {

	private int thumbNailLength = 10; // 10 sec default

	/**
	 * Constructor.
	 */
	public AudioThumbGenerator(int thumbNailLength) {
		this.thumbNailLength = thumbNailLength;
	}

	/**
	 * Processes the passed input audio file / audio file directory and stores
	 * the processed files to the output directory.
	 * 
	 * @param input
	 *            a reference to the input audio file / input directory
	 * @param output
	 *            a reference to the output directory
	 */
	public ArrayList<File> batchProcessAudioFiles(File input, File output)
			throws IOException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		ArrayList<File> ret = new ArrayList<File>();

		if (input.isDirectory()) {
			File[] files = input.listFiles();
			for (File f : files) {

				String ext = f.getName().substring(
						f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
					try {
						File result = processAudio(f, output);
						System.out.println("converted " + f + " to " + result);
						ret.add(result);
					} catch (Exception e0) {
						System.err.println("Error converting " + f + " : "
								+ e0.toString());
					}

				}

			}
		} else {
			String ext = input.getName().substring(
					input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
				try {
					File result = processAudio(input, output);
					System.out.println("converted " + input + " to " + result);
					ret.add(result);
				} catch (Exception e0) {
					System.err.println("Error converting " + input + " : "
							+ e0.toString());
				}

			}

		}
		return ret;
	}

	/**
	 * Processes the passed audio file and stores the processed file to the
	 * output directory.
	 * 
	 * @param input
	 *            a reference to the input audio File
	 * @param output
	 *            a reference to the output directory
	 * @throws UnsupportedAudioFileException 
	 */
	protected File processAudio(File input, File output) throws IOException,
			IllegalArgumentException, UnsupportedAudioFileException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		File outputFile = new File(output, input.getName() + ".wav");
		

		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************


		// load the input audio file
		AudioInputStream in = AudioSystem.getAudioInputStream(input);
		AudioFormat af = in.getFormat();
		
		//Array in dem die Daten des thumbnails gespeichert werden
	    byte[] data;
	    
	    //berechnung der groesse vo dem byte array "daten"
		 // bytes = seconds * sample rate * channels * (bits per sample / 8)
	    if(af.getFrameSize() == -1)
	    	data = new byte[Math.round(thumbNailLength * af.getSampleRate() * af.getChannels() * 
	    			((af.getSampleSizeInBits()==-1? 2f/8f : af.getSampleSizeInBits() / 8f)) )];
	    else //bytes = seconds * sample rate * frame size
	    	data = new byte[Math.round(thumbNailLength * af.getSampleRate() * af.getFrameSize() )];
	 
	    // Choose a buffer of 100 KB
	    byte[] buffer = new byte[102400];

	    //gesamte anzahl an derzeit gelesenen bytes von der audiodatei
	    int numOfBytesReadTotal = 0;
	    //die aktuelle anzahl an bits, die gerade gelesen werden von der audio datei
	    int numOfBytesRead = 0;

	    //lese solange von der audiodatei, bis sie endet
	    while ((numOfBytesRead = in.read(buffer)) != -1) {

	    	//sobald wir die maximale anzahl an zu lesende daten ueberschritten haben, hoeren wir mi  der schleife auf, 
	    	//dann haben wir alle benoetigten Daten um das thumbnail zu schreiben
	      if (numOfBytesReadTotal + numOfBytesRead >= data.length)
	      {
	    	  //daen werden von dem akuellen buffer in den daa array kopier um sie spaeter weguspeichern
	        System.arraycopy(buffer, 0, data, numOfBytesReadTotal, data.length - numOfBytesReadTotal);
	        break;
	      }

    	  //daen werden von dem akuellen buffer in den daa array kopier um sie spaeter weguspeichern
	      System.arraycopy(buffer, 0, data, numOfBytesReadTotal, numOfBytesRead);
	      numOfBytesReadTotal += numOfBytesRead;
	    }

		//erzeugen eines neuen inpusream, der aus den vorhin gelesenen daen ereiug wird 
		InputStream is = new ByteArrayInputStream(data);
		//mittels den inputstream koennen wir nun einen neuen AudioInpuSream ereugen, den wir anschliessend physisch wegspeichern 
		AudioInputStream towrite = new AudioInputStream(is, af, (long)data.length);
		AudioSystem.write(towrite, Type.WAVE, outputFile);

		return outputFile;
	}
	

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		//args = new String[]{"./media/audio", "./media/audio", "5"};
		
		if (args.length < 3) {
			System.out
					.println("usage: java itm.audio.AudioThumbGenerator <input-audioFile> <output-directory> <length>");
			System.out
					.println("usage: java itm.audio.AudioThumbGenerator <input-directory> <output-directory> <length>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		Integer length = new Integer(args[2]);
		AudioThumbGenerator audioThumb = new AudioThumbGenerator(length.intValue());
		audioThumb.batchProcessAudioFiles(fi, fo);
	}

}
