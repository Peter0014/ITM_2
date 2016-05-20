package itm.video;

/*******************************************************************************
 This file is part of the ITM course 2016
 (c) University of Vienna 2009-2016
 *******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.xuggle.xuggler.*;

/**
 * 
 * This class creates JPEG thumbnails from from video frames grabbed from the
 * middle of a video stream It can be called with 2 parameters, an input
 * filename/directory and an output directory.
 * 
 * If the input file or the output directory do not exist, an exception is
 * thrown.
 */

public class VideoFrameGrabber {

	/**
	 * Constructor.
	 */
	public VideoFrameGrabber() {
	}

	/**
	 * Processes the passed input video file / video file directory and stores
	 * the processed files in the output directory.
	 * 
	 * @param input
	 *            a reference to the input video file / input directory
	 * @param output
	 *            a reference to the output directory
	 */
	public ArrayList<File> batchProcessVideoFiles(File input, File output) throws IOException {
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
				if (f.isDirectory())
					continue;

				String ext = f.getName().substring(f.getName().lastIndexOf(".") + 1).toLowerCase();
				if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv")
						|| ext.equals("mp4")) {
					File result = processVideo(f, output);
					System.out.println("converted " + f + " to " + result);
					ret.add(result);
				}

			}

		} else {
			String ext = input.getName().substring(input.getName().lastIndexOf(".") + 1).toLowerCase();
			if (ext.equals("avi") || ext.equals("swf") || ext.equals("asf") || ext.equals("flv") || ext.equals("mp4")) {
				File result = processVideo(input, output);
				System.out.println("converted " + input + " to " + result);
				ret.add(result);
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
	 */
	protected File processVideo(File input, File output) throws IOException, IllegalArgumentException {
		if (!input.exists())
			throw new IOException("Input file " + input + " was not found!");
		if (input.isDirectory())
			throw new IOException("Input file " + input + " is a directory!");
		if (!output.exists())
			throw new IOException("Output directory " + output + " not found!");
		if (!output.isDirectory())
			throw new IOException(output + " is not a directory!");

		File outputFile = new File(output, input.getName() + "_thumb.jpg");
		// load the input video file

		// ***************************************************************
		// Fill in your code here!
		// ***************************************************************

		// Sources:
		// see https://goo.gl/ZBAcmC and https://goo.gl/BTyrJN (JavaCodeGeeks,
		// on 14.05.2016)
		// see https://goo.gl/DMAObO (Xuggler Demo File, on 14.05.2016)

		// Create Streamcoder, Container and load video into it
		IContainer container = IContainer.make();
		IStreamCoder coder = null;
		container.open(input.getAbsolutePath(), IContainer.Type.READ, null);

		long frames = 0; // Max amount of frames in the video stream
		int numStreams = container.getNumStreams(); // Total number of streams
		int videoStreamIndex = -1; // Correct number of the video stream, -1 if
									// none found

		// Loop to find correct video stream, if not found an exception will be
		// thrown. Also to extract the amount of frames from the stream
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
		// Call the resampler with 
		// .make(targetWidth, targetHeight, targetPixelFormat,
		//       srcWidth, srcHeight, srcPixelFormat)
		IVideoResampler resampler = IVideoResampler.make(
				width, height, IPixelFormat.Type.BGR24,
				width, height, coder.getPixelType());

		IPacket packet = IPacket.make(); // Packets that will be loaded and
											// analyzed further

		// Jump to the correct place in the video stream (in this case at halftime)
		container.seekKeyFrame(
				videoStreamIndex, // Stream Id
				0,	// Starting point
				frames / 2,	// Point to seek to
				frames,	// End point
				0	// Type of seeker (0 = None/Any)
				);

		// VideoPicture that will be decoded and saved if its complete
		IVideoPicture picture = IVideoPicture.make(coder.getPixelType(), width, height);
		boolean success = false; // Check if a KeyFrame was found

		// read out the contents of the media file
		while (container.readNextPacket(packet) >= 0) {
			if (picture.isComplete()) // Finish if the picture is complete
				break;

			if (packet.isKeyPacket() || success) { 
				if (packet.getStreamIndex() == videoStreamIndex) {
					success = true;
					coder.open(null, null);
					int byteOffset = 0; // bytes that have been already decoded
					
					// Go through the whole package until its done or the picture is complete
					while (byteOffset < packet.getSize()) { 
						byteOffset += coder.decodeVideo(picture, packet, byteOffset);
						
						if (picture.isComplete()) {
							// Resample picture if its necessary and save it locally
							if (coder.getPixelType() != IPixelFormat.Type.BGR24) {
								IVideoPicture resampled = IVideoPicture.make(
										resampler.getOutputPixelFormat(),
										width,
										height
										);
								resampler.resample(resampled, picture);
								ImageIO.write(Utils.videoPictureToImage(resampled), "jpg", outputFile);
							} else {
								ImageIO.write(Utils.videoPictureToImage(picture), "jpg", outputFile);
							}
							break;
						}
					} // Packet is done
					coder.close(); // Cleanup
				} // if (videoStream) end
			} // if (Packet.isKey) end
		} 

		// Cleanup
		container.close();
		container = null;
		coder = null;

		return outputFile;

	}

	/**
	 * Main method. Parses the commandline parameters and prints usage
	 * information if required.
	 */
	public static void main(String[] args) throws Exception {

		// args = new String[] { "./media/video/", "./test" };

		if (args.length < 2) {
			System.out.println("usage: java itm.video.VideoFrameGrabber <input-videoFile> <output-directory>");
			System.out.println("usage: java itm.video.VideoFrameGrabber <input-directory> <output-directory>");
			System.exit(1);
		}
		File fi = new File(args[0]);
		File fo = new File(args[1]);
		VideoFrameGrabber grabber = new VideoFrameGrabber();
		grabber.batchProcessVideoFiles(fi, fo);
	}

}
