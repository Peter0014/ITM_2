package itm.image;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;

/*******************************************************************************
    This file is part of the ITM course 2016
    (c) University of Vienna 2009-2016
*******************************************************************************/

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.text.Position;

/**
    This class converts images of various formats to PNG thumbnails files.
    It can be called with 3 parameters, an input filename/directory, an output directory and a compression quality parameter.
    It will read the input image(s), grayscale and scale it/them and convert it/them to a PNG file(s) that is/are written to the output directory.

    If the input file or the output directory do not exist, an exception is thrown.
*/
public class ImageThumbnailGenerator 
{

    /**
        Constructor.
    */
    public ImageThumbnailGenerator()
    {
    }

    /**
        Processes an image directory in a batch process.
        @param input a reference to the input image file
        @param output a reference to the output directory
        @param rotation
        @param overwrite indicates whether existing thumbnails should be overwritten or not
        @return a list of the created files
    */
    public ArrayList<File> batchProcessImages( File input, File output, double rotation, boolean overwrite ) throws IOException
    {
        if ( ! input.exists() ) {
            throw new IOException( "Input file " + input + " was not found!" );
        }
        if ( ! output.exists() ) {
            throw new IOException( "Output directory " + output + " not found!" );
        }
        if ( ! output.isDirectory() ) {
            throw new IOException( output + " is not a directory!" );
        }

        ArrayList<File> ret = new ArrayList<File>();

        if ( input.isDirectory() ) {
            File[] files = input.listFiles();
            for ( File f : files ) {
                try {
                    File result = processImage( f, output, rotation, overwrite );
                    System.out.println( "converted " + f + " to " + result );
                    ret.add( result );
                } catch ( Exception e0 ) {
                    System.err.println( "Error converting " + input + " : " + e0.toString() );
                }
            }
        } else {
            try {
                File result = processImage( input, output, rotation, overwrite );
                System.out.println( "converted " + input + " to " + result );
                ret.add( result );
            } catch ( Exception e0 ) {
                System.err.println( "Error converting " + input + " : " + e0.toString() );
            }
        } 
        return ret;
    }  

    private AffineTransform findTranslation(AffineTransform at, BufferedImage bi) {
        Point2D p2din, p2dout;

        p2din = new Point2D.Double(0.0, 0.0);
        p2dout = at.transform(p2din, null);
        double ytrans = p2dout.getY();

        p2din = new Point2D.Double(0, bi.getHeight());
        p2dout = at.transform(p2din, null);
        double xtrans = p2dout.getX();

        AffineTransform tat = new AffineTransform();
        tat.translate(-xtrans, -ytrans);
        return tat;
      }
    
    /**
        Processes the passed input image and stores it to the output directory.
        This function should not do anything if the outputfile already exists and if the overwrite flag is set to false.
        @param input a reference to the input image file
        @param output a reference to the output directory
        @param dimx the width of the resulting thumbnail
        @param dimy the height of the resulting thumbnail
        @param overwrite indicates whether existing thumbnails should be overwritten or not
    */
    protected File processImage( File input, File output, double rotation, boolean overwrite ) throws IOException, Exception, IllegalArgumentException
    {
        if ( ! input.exists() ) {
            throw new IOException( "Input file " + input + " was not found!" );
        }
        if ( input.isDirectory() ) {
            throw new IOException( "Input file " + input + " is a directory!" );
        }
        if ( ! output.exists() ) {
            throw new IOException( "Output directory " + output + " not found!" );
        }
        if ( ! output.isDirectory() ) {
            throw new IOException( output + " is not a directory!" );
        }

        // create outputfilename and check whether thumb already exists
        File outputFile = new File( output, input.getName() + ".thumb.png" );
        if ( outputFile.exists() ) {
            if ( ! overwrite ) {
                return outputFile;
            }
        }

        // ***************************************************************
        //  Fill in your code here!
        // ***************************************************************

        try
        {
	        //Rotate quelle: http://www.java2s.com/Code/Java/2D-Graphics-GUI/RotateImage45Degrees.htm
	        //Scaling quelle: http://stackoverflow.com/questions/4216123/how-to-scale-a-bufferedimage
	        //watermark quelle: https://gist.github.com/madan712/4026080
	        
	        // load the input image
	        BufferedImage img = ImageIO.read(input);
	        
	        //copy of image
	        BufferedImage copyImg;
	
	        if(img.getWidth() < 200)
	        	copyImg = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
	        else
	        	copyImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);
	        
	        //draw the image
	        Graphics2D g2D = (Graphics2D)copyImg.getGraphics();
	
	        if(img.getWidth() < 200)
	            g2D.drawImage(img, 100 - img.getWidth()/2, 50-img.getHeight()/2, null);
	        else
	        	g2D.drawImage(img, 0, 0, null);
	        
	        //Draw the watermark
	        g2D.setFont(new Font("Arial", Font.BOLD, 30));
	        String watermark = "NRF";
	        
	
	        if(img.getWidth() < 200)
	            g2D.drawString(watermark, 100, 50);
	        else
	        	g2D.drawString(watermark, img.getHeight() / 2, img.getHeight() / 2 );
	        
	        //G2D no longer needed
	        g2D.dispose();
	        
	        //Create AffineTransformation to rotate and scale if needed
	        AffineTransform at = new AffineTransform();
	
	        if (img.getWidth() < img.getHeight())
	        	at.rotate(90 * Math.PI / 180.0, img.getWidth() / 2.0, img.getHeight() / 2.0);
	
	        //Apply the rotation before scaling down
	        BufferedImageOp filter;
	        filter = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
	        copyImg = filter.filter(copyImg, null);
	
	        //clear transformation to use it again for scaling
	        at = new AffineTransform();
	        if(copyImg.getWidth() > 200)
	        {
	            float iScale = 0.98f;
	            while(copyImg.getWidth()*iScale > 200)
	            	iScale -= 0.02f;
	            at.scale(iScale, iScale);
	        }
	        
	        //apply transformation and save first pic
	        filter = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
	        copyImg = filter.filter(copyImg, null);
	        ImageIO.write(copyImg, "png", outputFile);
	
	        //create new bufferedImage for user input rotation
	        BufferedImage rotatedImage;
	        
	        //if smaller than 200 pixel its used a 200x100 BufferdImage and the image wille be drawn inside it
	        if(img.getWidth() < 200)
	        	rotatedImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
	        else
	        	rotatedImage = new BufferedImage(copyImg.getWidth(), copyImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
	        
	        Graphics2D g2DRot = (Graphics2D)rotatedImage.getGraphics();
	        
	        //if smaller than 200 draw origin pic and draw wattermark again
	        if(img.getWidth() < 200)
	        {
	        	//draw the original pic in center, dont use the edidet cause that is allready 200x100 pixel large
	        	g2DRot.drawImage(img, 100 , 50, null);
	        	
	        	//Draw watermark again, cause only original pic now was drawn
	        	g2DRot.setFont(new Font("Arial", Font.BOLD, 30));
	            g2DRot.drawString(watermark, 100, 50);
	        }
	        else
	            g2DRot.drawImage(copyImg, 0, 0, null);
	        g2DRot.dispose();
	        
	        //new Transformation for user rotation
	        AffineTransform atRot = new AffineTransform();
	        
	        // rotate by the given parameter the image - do not crop image parts!
	        // rotate around image center
	        if(img.getWidth() < 200)
	            atRot.rotate(rotation * Math.PI / 180.0, 100, 50);
	        else
	        {
	        	atRot.rotate(rotation * Math.PI / 180.0, rotatedImage.getWidth() / 2.0, rotatedImage.getHeight() / 2.0);
	
	        	//Check the picture edges to do not cut them off while rotating and apply it to the affineTransformation
	            AffineTransform translationTransform;
	            translationTransform = findTranslation(atRot, rotatedImage);
	            atRot.preConcatenate(translationTransform);
	        }
	       
	        //finnaly apply the filter on the picture
	        BufferedImageOp filterRot;
	        filterRot = new AffineTransformOp(atRot, AffineTransformOp.TYPE_BILINEAR);
	        rotatedImage = filterRot.filter(rotatedImage, null);
	        
	        //save the rotated image as extra file
	        ImageIO.write(rotatedImage, "png", new File( output, input.getName() + ".thumb.rotated.png" ));
	        
	        // encode and save the image  
	        return outputFile;

        }
        catch(Exception ex)
        {
            throw new Exception( "An Error occured: \n" + ex.getMessage());
        }
        /**
            ./ant.sh ImageThumbnailGenerator -Dinput=media/img/ -Doutput=test/ -Drotation=90
        */
    }
    
    /**
        Main method. Parses the commandline parameters and prints usage information if required.
    */
    public static void main( String[] args ) throws Exception
    {
        if ( args.length < 3 ) {
            System.out.println( "usage: java itm.image.ImageThumbnailGenerator <input-image> <output-directory> <rotation degree>" );
            System.out.println( "usage: java itm.image.ImageThumbnailGenerator <input-directory> <output-directory> <rotation degree>" );
            System.exit( 1 );
        }
        File fi = new File( args[0] );
        File fo = new File( args[1] );
        double rotation = Double.parseDouble( args[2] );

        ImageThumbnailGenerator itg = new ImageThumbnailGenerator();
        itg.batchProcessImages( fi, fo, rotation, true );
    }    
}