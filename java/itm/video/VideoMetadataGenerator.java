package itm.video;

/*******************************************************************************
 This file is part of the ITM course 2016
 (c) University of Vienna 2009-2016
 *******************************************************************************/

import itm.model.MediaFactory;
import itm.model.VideoMedia;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.Processor;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.DataSource;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.io.IURLProtocolHandler;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec.Type;

/**
 * This class reads video files, extracts metadata for both the audio and the
 * video track, and writes these metadata to a file.
 * 
 * It can be called with 3 parameters, an input filename/directory, an output
 * directory and an "overwrite" flag. It will read the input video file(s),
 * retrieve the metadata and write it to a text file in the output directory.
 * The overwrite flag indicates whether the resulting output file should be
 * overwritten or not.
 * 
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */
public class VideoMetadataGenerator {

	/**
	 * Constructor.
	 */
	public VideoMetadataGenerator() {
	}

	/**
	 * Processes a video file directory in a batch process.
	 * 
	 * @param input
	 *            a reference to the video file directory
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing metadata files should be
	 *            overwritten or not
	 * @return a list of the created media objects (videos)
	 */
	public ArrayList<VideoMedia> batchProcessVideoFiles(File input, File output, boolean overwrite) throws IOException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		ArrayList<VideoMedia> ret = new ArrayList<VideoMedia>();

		if (input.isDirectory()) {
			File[] files = input.listFiles();
			for (File f : files) {

				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv")
						|| ext.equals("mp4"))
					try {
						VideoMedia result = processVideo(f, output, overwrite);
						System.out.println("created metadata for file " + f + " in " + output);
						ret.add(result);
					} catch (Exception e0) {
						System.err.println("Error when creating metadata from file " + input + " : " + e0.toString());
					}

			}
		} else {

			String ext = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv") || ext.equals("mp4"))
				try {
					VideoMedia result = processVideo(input, output, overwrite);
					System.out.println("created metadata for file " + input + " in " + output);
					ret.add(result);
				} catch (Exception e0) {
					System.err.println("Error when creating metadata from file " + input + " : " + e0.toString());
				}

		}
		return ret;
	}

	/**
	 * Processes the passed input video file and stores the extracted metadata
	 * to a textfile in the output directory.
	 * 
	 * @param input
	 *            a reference to the input video file
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing metadata files should be
	 *            overwritten or not
	 * @return the created video media object
	 */
	protected VideoMedia processVideo(File input, File output, boolean overwrite) throws Exception {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		// create outputfilename and check whether thumb already exists.
		// all video metadata files have to start with "vid_" - this is used by
		// the
		// mediafactory!
		File outputFile = new File(output, "vid_" + input.getName() + ".txt");
		if (outputFile.exists())
			if (!overwrite) {
				// load from file
				VideoMedia media = new VideoMedia();
				media.readFromFile(outputFile);
				return media;
			}

		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************
		
		// create video media object
		VideoMedia media = (VideoMedia) MediaFactory.createMedia(input);

		// set video and audio stream metadata 
		 // Create a Xuggler container object
		//der conainer ist vorerst leer enthael dann aber alle video daten
	    IContainer container = IContainer.make();
	    //oeffnen des containers mit dem gewuenschten video 
	    container.open(input.getAbsolutePath(), IContainer.Type.READ, null);
	    
	    // der container kann mehrere streams enthalten wie z.b. einen video und einen audio sream
	    int numStreams = container.getNumStreams();
	    
	    //nun gehn wir alle streams durch und reagieren je nachdem drauf
	    for(int i = 0; i < numStreams; i++)
	    {
	    	//holen uns nacheinander jeden sream einmal raus und bearbeien ihn
	      IStream stream = container.getStream(i);

	      //enheal die encodieren DAen des sreams
	      IStreamCoder coder = stream.getStreamCoder();

	      //setzen der wetre des mediaobjeks
	      media.setVideoCodec(coder.getCodecType().toString());
	      media.setVideoCodecID(coder.getCodecID().toString());
	     
	      media.setVideoLength(container.getDuration() / Global.DEFAULT_PTS_PER_SECOND);
	      
	      //nun muessen wir checken ob der stream gerade ein video bw ein audio sream is und speichern
	      //je nachdem verschiedene werte in das objekt
	      if (coder.getCodecType() == Type.CODEC_TYPE_AUDIO)
	      {
		      media.setAudioCodec(coder.getCodecType().toString());
		      media.setAudioCodecID(coder.getCodecID().toString());
		      media.setAudioChannels(coder.getChannels());
		      media.setAudioSampleRate(coder.getSampleRate());
		      media.setAudioBitRate(coder.getBitRate());
	      } 
	      else if (coder.getCodecType() == Type.CODEC_TYPE_VIDEO)
	      {
		      media.setVideoHeight(coder.getHeight());
		      media.setVideoWidth(coder.getWidth());
		      media.setVideoFrameRate(coder.getFrameRate().getDouble());
	      }
	    }
	    
		// add video tag
	    media.addTag("video");
	    System.out.println(media.serializeObject());

		// write metadata
		media.writeToFile(outputFile);
	    
		return media;
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		// args = new String[] {"./media/video", "./media/md"};

		if (args.length < 2) {
			System.out.println("usage: java itm.video.VideoMetadataGenerator <input-video> <output-directory>");
			System.out.println("usage: java itm.video.VideoMetadataGenerator <input-directory> <output-directory>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		VideoMetadataGenerator videoMd = new VideoMetadataGenerator();
		videoMd.batchProcessVideoFiles(fi, fo, true);
	}
}
