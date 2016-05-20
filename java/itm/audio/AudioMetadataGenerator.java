package itm.audio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/*******************************************************************************
 This file is part of the ITM course 2016
 (c) University of Vienna 2009-2016
 *******************************************************************************/

import itm.model.AudioMedia;
import itm.model.MediaFactory;

/**
 * This class reads audio files of various formats and stores some basic audio
 * metadata to text files. It can be called with 3 parameters, an input
 * filename/directory, an output directory and an "overwrite" flag. It will read
 * the input audio file(s), retrieve some metadata and write it to a text file
 * in the output directory. The overwrite flag indicates whether the resulting
 * output file should be overwritten or not.
 * 
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */
public class AudioMetadataGenerator {

	/**
	 * Constructor.
	 */
	public AudioMetadataGenerator() {
	}

	/**
	 * Processes an audio file directory in a batch process.
	 * 
	 * @param input
	 *            a reference to the audio file directory
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing metadata files should be
	 *            overwritten or not
	 * @return a list of the created media objects (images)
	 */
	public ArrayList<AudioMedia> batchProcessAudio(File input, File output,
			boolean overwrite) throws IOException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		ArrayList<AudioMedia> ret = new ArrayList<AudioMedia>();

		if (input.isDirectory()) {
			File[] files = input.listFiles();
			for (File f : files) {

				String ext = f.getName().substring(
						f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
					try {
						AudioMedia result = processAudio(f, output, overwrite);
						System.out.println("created metadata for file " + f
								+ " in " + output);
						ret.add(result);
					} catch (Exception e0) {
						System.err
								.println("Error when creating metadata from file "
										+ input + " : " + e0.toString());
					}

				}

			}
		} else {

			String ext = input.getName().substring(
					input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("wav") || ext.equals("mp3") || ext.equals("ogg")) {
				try {
					AudioMedia result = processAudio(input, output, overwrite);
					System.out.println("created metadata for file " + input
							+ " in " + output);
					ret.add(result);
				} catch (Exception e0) {
					System.err
							.println("Error when creating metadata from file "
									+ input + " : " + e0.toString());
				}

			}

		}
		return ret;
	}

	/**
	 * Processes the passed input audio file and stores the extracted metadata
	 * to a textfile in the output directory.
	 * 
	 * @param input
	 *            a reference to the input audio file
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing metadata files should be
	 *            overwritten or not
	 * @return the created image media object
	 * @throws UnsupportedAudioFileException 
	 */
	protected AudioMedia processAudio(File input, File output, boolean overwrite)
			throws IOException, IllegalArgumentException, UnsupportedAudioFileException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		// create outputfilename and check whether thumb already exists. All
		// image metadata files have to start with "aud_" - this is used by the
		// mediafactory!
		File outputFile = new File(output, "aud_" + input.getName() + ".txt");
		if (outputFile.exists())
			if (!overwrite) {
				// load from file
				AudioMedia media = new AudioMedia();
				media.readFromFile(outputFile);
				return media;
			}

		
		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************

		// create an audio metadata object
		AudioMedia media = (AudioMedia) MediaFactory.createMedia(input);		

		// load the input audio file, do not decode
		AudioInputStream in = AudioSystem.getAudioInputStream(input);
		
		// read AudioFormat properties		
		AudioFormat aformat = in.getFormat(); 
		media.setChannels(aformat.getChannels());
		
		//encoding typ setzen sofern nicht null
		if(aformat.getEncoding() != null)
			media.setEncoding(aformat.getEncoding().toString());
		
		// read file-type specific properties
		// sofern die datei eine mpeg datei ist, schreib sie in die media instanz alle werte  
		if(aformat.getEncoding().toString().toLowerCase().contains("mpeg") )
		{
			//lade alle properties in eine eigene hashmap um sie später durchzugehen
			AudioFileFormat aff = AudioSystem.getAudioFileFormat(in);
			Map<String, Object> map = aff.properties();

			//gehe jeden eintrag der map durch 
			for(Entry<String, Object> entry : map.entrySet())
			{
				//sofern der key des eintrages eines der schlüsselworte enthält z.b. author,
				//schreibe den inhalt in das media objekt
				if(entry.getKey().toLowerCase().contains(("author")))
					media.setAuthor(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("title")))
					media.setTitle(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("date")))
					media.setDate(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("comment")))
					media.setComment(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("album")))
					media.setAlbum(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("track")))
					media.setTrack(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("composer")))
					media.setComposer(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("genre")))
					media.setGenre(entry.getValue().toString());
				else if(entry.getKey().toLowerCase().contains(("frequency")))
					media.setFrequency(Float.parseFloat(entry.getValue().toString()));
				else if(entry.getKey().toLowerCase().contains(("duration")))
					media.setDuration(Float.parseFloat(entry.getValue().toString()));
				else if(entry.getKey().toLowerCase().contains(("bitrate")))
					media.setBitrate(Integer.parseInt(entry.getValue().toString()));
			}

		}
		else //sofern es keine mp3 is, is ein nicht unterstützer typ,
			//und setze somit die einträge des media objeks auf leer bzw. -1 
		{
			media.setAuthor("");
			media.setTitle("");
			media.setDate("");
			media.setComment("");
			media.setAlbum("");
			media.setTrack("");
			media.setComposer("");
			media.setGenre("");
			media.setFrequency(-1f);
			media.setDuration(-1f);
			media.setBitrate(-1);
		}
		
		// add a "audio" tag
		media.addTag("audio");
		// System.out.println(media.serializeObject());

		// close the audio and write the md file.
		media.writeToFile(outputFile);
		
		return media;
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		//args = new String[] { "./media/audio", "./media/md" };
		
		if (args.length < 2) {
			System.out
					.println("usage: java itm.image.AudioMetadataGenerator <input-image> <output-directory>");
			System.out
					.println("usage: java itm.image.AudioMetadataGenerator <input-directory> <output-directory>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		AudioMetadataGenerator audioMd = new AudioMetadataGenerator();
		audioMd.batchProcessAudio(fi, fo, true);
	}
}
