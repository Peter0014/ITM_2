package itm.video;

/*******************************************************************************
 This file is part of the ITM course 2016
 (c) University of Vienna 2009-2016
 *******************************************************************************/

import itm.util.ImageCompare;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;

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
public class VideoThumbnailGenerator {

	/**
	 * Constructor.
	 */
	public VideoThumbnailGenerator() {
	}

	/**
	 * Processes a video file directory in a batch process.
	 * 
	 * @param input
	 *            a reference to the video file directory
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing output files should be overwritten
	 *            or not
	 * @return a list of the created media objects (videos)
	 */
	public ArrayList<File> batchProcessVideoFiles(File input, File output, boolean overwrite, int timespan)
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

				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv")
						|| ext.equals("mp4"))
					try {
						File result = processVideo(f, output, overwrite, timespan);
						System.out.println("processed file " + f + " to " + output);
						ret.add(result);
					} catch (Exception e0) {
						System.err.println("Error processing file " + input + " : " + e0.toString());
					}
			}
		} else {

			String ext = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv") || ext.equals("mp4"))
				try {
					File result = processVideo(input, output, overwrite, timespan);
					System.out.println("processed " + input + " to " + result);
					ret.add(result);
				} catch (Exception e0) {
					System.err.println("Error when creating processing file " + input + " : " + e0.toString());
				}

		}
		return ret;
	}

	/**
	 * Processes the passed input video file and stores a thumbnail of it to the
	 * output directory.
	 * 
	 * @param input
	 *            a reference to the input video file
	 * @param output
	 *            a reference to the output directory
	 * @param overwrite
	 *            indicates whether existing files should be overwritten or not
	 * @return the created video media object
	 */
	protected File processVideo(File input, File output, boolean overwrite, int timespan) throws Exception {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		// create output file and check whether it already exists.
		File outputFile = new File(output, input.getName() + "_thumb.avi");

		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************

		// extract frames from input video

		// add a watermark of your choice and paste it to the image
		// e.g. text or a graphic

		// create a video writer

		// add a stream with the proper width, height and frame rate

		// if timespan is set to zero, compare the frames to use and add
		// only frames with significant changes to the final video

		// loop: get the frame image, encode the image to the video stream

		// Close the writer

		// Sources:
		// see https://goo.gl/BTyrJN (JavaCodeGeeks, on 14.05.2016)
		// see http://mindmeat.blogspot.co.at/2008/07/java-image-comparison.html (on 19.05.2016)

		// START------ SAME CODE AS IN VIDEOFRAMEGRABBER.JAVA ------START
		
		// Create Streamcoder, Container and load video into it
		IContainer container = IContainer.make();
		IStreamCoder coder = null;
		container.open(input.getAbsolutePath(), IContainer.Type.READ, null);

		long frames = 0; // Max amount of frames in the video stream
		long totalDuration = container.getDuration() / Global.DEFAULT_PTS_PER_SECOND; // Total seconds of the clip
		int numStreams = container.getNumStreams(); // Total number of streams
		int videoStreamIndex = -1; // Correct number of the video stream, -1 if
									// none found

		// Loop to find correct video stream, if not found an exception will be
		// thrown.
		for (int i = 0; i < numStreams; i++) {

			IStream stream = container.getStream(i);
			coder = stream.getStreamCoder();

			if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamIndex = i;
				frames = stream.getDuration();
				break;
			}
		}

		if (videoStreamIndex == -1)
			throw new IOException("Input file " + input + " doesn't have a videostream!");

		int width = coder.getWidth(); // Video width
		int height = coder.getHeight(); // Video height

		// Create a resampler in case the video isn't in the right
		// format (BGR24)
		IVideoResampler resampler = IVideoResampler.make(
				width, height, IPixelFormat.Type.BGR24,
				width, height, coder.getPixelType());

		IPacket packet = IPacket.make(); // Packets that will be loaded and
											// analyzed further
		
		// END------- SAME CODE AS IN VIDEOFRAMEGRABBER.JAVA -------END

		// VideoPicture ArrayList that will hold all pictures that are complete
		ArrayList<BufferedImage> pictures = new ArrayList<>();
		IVideoPicture frame = IVideoPicture.make(
				coder.getPixelType(), width, height); // Create initial empty IVideoPicture
		
		long elapsedTime = 0; // Time that was already analyzed
		long framesTimespan = frames / totalDuration * timespan; // Amount of frames in one timespan
		boolean success = false; // Check if a KeyFrame was found

		// read out the contents of the media file
		while (container.readNextPacket(packet) >= 0) {

			// Increase index and add new empty picture placeholder if the picture is complete
			if (frame.isComplete()) {
				frame = IVideoPicture.make(
						coder.getPixelType(), width, height); // Create new empty IVideoPicture
				success = false;
				if (packet.getTimeStamp() > framesTimespan) // Don't raise elapsedTime at frame 0
					elapsedTime += framesTimespan;
			}

			if (packet.isKeyPacket() || success) {
				if (packet.getStreamIndex() == videoStreamIndex) {
					// Just save the first Frame and every other one that's after the set timespan
					if ((packet.getTimeStamp() - elapsedTime) >= framesTimespan || packet.getTimeStamp() == 0) {
						success = true;
						coder.open(null, null);
						int byteOffset = 0; // bytes that have been already
											// decoded

						// Go through the whole package until its done or the
						// picture is complete
						while (byteOffset < packet.getSize()) {
							byteOffset += coder.decodeVideo(frame, packet, byteOffset);

							if (frame.isComplete()) {
								// Resample picture if its necessary and save it
								// into the 'pictures' ArrayList as BufferedImage
								if (coder.getPixelType() != IPixelFormat.Type.BGR24) {
									IVideoPicture resampled = IVideoPicture.make(resampler.getOutputPixelFormat(),
											width, height);
									resampler.resample(resampled, frame);
									pictures.add(Utils.videoPictureToImage(resampled));
								} else 
									pictures.add(Utils.videoPictureToImage(frame));
								break;
							}
						} // Packet is done
						coder.close(); // Cleanup
					} // if (Timespan) end
				} // if (videoStream) end
			} // if (Packet.isKey) end
		}
		
		// Add Watermark like in Assignment 1
		for (BufferedImage pic : pictures) {
			Graphics2D graphic =  pic.createGraphics();
			String text = "Peter.Nico";
			graphic.setFont(graphic.getFont().deriveFont(15f));
			graphic.setColor(new Color(1, 0, 0, 1f));
			graphic.drawString(text, 15, 15);
		}
		
		// If every frame is extracted than kill/remove same looking ones 
		if (timespan == 0){
			for (int i = 1; i < pictures.size(); i++) {
				ImageCompare compare = new ImageCompare(
						pictures.get(i-1),
						pictures.get(i));
				compare.setParameters(
						10,	// # of vertical columns
						10,	// # of horizontal columns
						10, // Brightness threshhold
						10	// Stabilization factor
						);
				compare.compare();	// Let the class compare the pictures
				if (compare.match())
					pictures.remove(i--);
			}
		}
		
		// Create MediaWriter that saves the pictures as videos
		IMediaWriter writer = ToolFactory.makeWriter(outputFile.getAbsolutePath());
		writer.addVideoStream(
				0,	// Index of the saved stream
				0,	// format-dependend id for this stream
				ICodec.ID.CODEC_ID_MPEG4,	// Codec used to encode the data
				IRational.make(1, 1),	// Framerate that the video will encode at (1 per second)
				width,	// width of video frames
				height	// height of video frames
				);
		
		// Encode every picture into a video and save it
		for (int i = 0; i < pictures.size(); i++) {
			writer.encodeVideo(0, pictures.get(i), i, TimeUnit.SECONDS);
		}

		// Cleanup
		container.close();
		container = null;
		coder = null;
		writer.close();

		return outputFile;
	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		args = new String[] { "./media/video/panda.avi", "./test", "" };

		if (args.length < 3) {
			System.out.println(
					"usage: java itm.video.VideoThumbnailGenerator <input-video> <output-directory> <timespan>");
			System.out.println(
					"usage: java itm.video.VideoThumbnailGenerator <input-directory> <output-directory> <timespan>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		int timespan = 5;
		if (args.length == 3)
			timespan = Integer.parseInt(args[2]);

		VideoThumbnailGenerator videoMd = new VideoThumbnailGenerator();
		videoMd.batchProcessVideoFiles(fi, fo, true, timespan);
	}
}