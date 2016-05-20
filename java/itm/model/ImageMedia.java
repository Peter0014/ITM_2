package itm.model;

/*******************************************************************************
    This file is part of the ITM course 2016
    (c) University of Vienna 2009-2016
*******************************************************************************/

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import javax.imageio.ImageIO;

import com.sun.corba.se.impl.orbutil.closure.Constant;

/**
    This class describes an image. 
*/
public class ImageMedia extends AbstractMedia 
{

    public final static int ORIENTATION_LANDSCAPE = 0;
    public final static int ORIENTATION_PORTRAIT = 1;

    // ***************************************************************
    //  Fill in your code here!
    // ***************************************************************

    // add required properties (scope: protected!)
    private final String PROPNAME_width="width";
    private final String PROPNAME_height="height";
    private final String PROPNAME_numOfImgComp="numOfImgComp";
    private final String PROPNAME_numOfImgColComp="numOfImgColComp";
    private final String PROPNAME_transparency="transparency";
    private final String PROPNAME_pixelSize="pixelSize";
    private final String PROPNAME_colorSpaceType="colorSpaceType";
    private final String PROPNAME_oriantation="oriantation";
    private final String PROPNAME_seperator=": ";
    
    protected int width;
    protected int height;
    protected int numOfImgComp;
    protected int numOfImgColComp;
    protected int transparency;
    protected int pixelSize;
    protected int colorSpaceType;
    protected int oriantation;
    // add get/set methods for the properties

    public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getNumOfImgComp() {
		return numOfImgComp;
	}

	public void setNumOfImgComp(int numOfImgComp) {
		this.numOfImgComp = numOfImgComp;
	}

	public int getNumOfImgColComp() {
		return numOfImgColComp;
	}

	public void setNumOfImgColComp(int numOfImgColComp) {
		this.numOfImgColComp = numOfImgColComp;
	}

	public int getTransparency() {
		return transparency;
	}

	public void setTransparency(int alpha) {
		this.transparency = alpha;
	}

	public int getPixelSize() {
		return pixelSize;
	}

	public void setPixelSize(int pixelSize) {
		this.pixelSize = pixelSize;
	}

	public int getColorSpaceType() {
		return colorSpaceType;
	}

	public void setColorSpaceType(int colorSpaceType) {
		this.colorSpaceType = colorSpaceType;
	}

	public int getOriantation() {
		return oriantation;
	}

	public void setOriantation(int orianztaion) {
		this.oriantation = orianztaion;
	}

    /**
        Constructor.
    */
    public ImageMedia()
    {
        super();
    }


	/**
        Constructor.
    */
    public ImageMedia( File instance )
    {
        super( instance );
    }


    /**
        Converts a color space type to a human readable string
        @return a string describing the passed colorspace
    */
    protected String serializeCSType( int cstype )
    {
        switch ( cstype ) {
            case ColorSpace.CS_CIEXYZ: return "CS_CIEXYZ"; 
            case ColorSpace.CS_GRAY: return "CS_GRAY"; 
            case ColorSpace.CS_LINEAR_RGB: return "CS_LINEAR_RGB"; 
            case ColorSpace.CS_PYCC: return "CS_PYCC"; 
            case ColorSpace.CS_sRGB: return "CS_sRGB"; 
            case ColorSpace.TYPE_CMY: return "TYPE_CMY"; 
            case ColorSpace.TYPE_CMYK: return "TYPE_CMYK"; 
            case ColorSpace.TYPE_GRAY: return "TYPE_GRAY"; 
            case ColorSpace.TYPE_RGB: return "TYPE_RGB"; 
            case ColorSpace.TYPE_HLS: return "TYPE_HLS"; 
            default: return ""+cstype; 
        }
    }

    /**
        Converts a human readable string string to a color space type
        @return the colorspace corresponding to the passed string
    */
    protected int deserializeCSType( String cstype )
    {
        if ( cstype.equals( "CS_CIEXYZ" ) ) {
            return ColorSpace.CS_CIEXYZ;
        }
        if ( cstype.equals( "CS_GRAY" ) ) {
            return ColorSpace.CS_GRAY;
        }
        if ( cstype.equals( "CS_LINEAR_RGB" ) ) {
            return ColorSpace.CS_LINEAR_RGB;
        }
        if ( cstype.equals( "CS_PYCC" ) ) {
            return ColorSpace.CS_PYCC;
        }
        if ( cstype.equals( "CS_sRGB" ) ) {
            return ColorSpace.CS_sRGB;
        }
        if ( cstype.equals( "TYPE_CMY" ) ) { 
            return ColorSpace.TYPE_CMY;
        }
        if ( cstype.equals( "TYPE_CMYK" ) ) {
            return ColorSpace.TYPE_CMYK;
        }
        if ( cstype.equals( "TYPE_GRAY" ) ) {
            return ColorSpace.TYPE_GRAY;
        }
        if ( cstype.equals( "TYPE_RGB" ) ) {
            return ColorSpace.TYPE_RGB;
        }
        if ( cstype.equals( "TYPE_HLS" ) ) {
            return ColorSpace.TYPE_HLS;
        }

        return Integer.parseInt( cstype );
    }
        
        
    /**
        Serializes this object to a string buffer.
        @return a StringBuffer containing a serialized version of this object.
    */
    @Override
    public StringBuffer serializeObject() throws IOException
    {
        StringWriter data = new StringWriter();
        // print writer for creating the output
        PrintWriter out = new PrintWriter( data );
        // print type
        out.println( "type: image" );
        StringBuffer sup = super.serializeObject();
        // print the serialization of the superclass (AbstractMedia)
        out.print( sup );

        // ***************************************************************
        //  Fill in your code here!
        // ***************************************************************
        BufferedImage img = ImageIO.read(instance);
        
        // print properties
        out.println(PROPNAME_width+PROPNAME_seperator+img.getWidth());
        
        out.println(PROPNAME_height+PROPNAME_seperator+img.getHeight());
        
        out.println(PROPNAME_numOfImgComp+PROPNAME_seperator +img.getColorModel().getNumComponents());
        
        out.println(PROPNAME_numOfImgColComp+PROPNAME_seperator+img.getColorModel().getNumColorComponents());
        
        out.println(PROPNAME_transparency+PROPNAME_seperator+img.getColorModel().getTransparency());
        
        out.println(PROPNAME_pixelSize+PROPNAME_seperator+img.getColorModel().getPixelSize());
        
        out.println(PROPNAME_oriantation+PROPNAME_seperator+img.getColorModel().getColorSpace().getType());
        

        return data.getBuffer();
    }



    /**
        Deserializes this object from the passed string buffer.
    */
    @Override
    public void deserializeObject( String data ) throws IOException
    {
        super.deserializeObject( data );
        
        StringReader sr = new StringReader( data );
        BufferedReader br = new BufferedReader( sr );
        String line = null;
        while ( ( line = br.readLine() ) != null ) {

            // ***************************************************************
            //  Fill in your code here!
            // ***************************************************************
            
            // read and set properties
        	 if ( line.startsWith( PROPNAME_width+PROPNAME_seperator ) ) {
                 setWidth( Integer.parseInt(line.substring( (PROPNAME_width+PROPNAME_seperator).length() )) );
                 }
        	 else if ( line.startsWith( PROPNAME_height+PROPNAME_seperator ) ) {
                 setHeight( Integer.parseInt(line.substring( (PROPNAME_height+PROPNAME_seperator).length() )) );
                 } 
        	 else if ( line.startsWith( PROPNAME_numOfImgComp+PROPNAME_seperator ) ) {
                 setNumOfImgComp( Integer.parseInt(line.substring( (PROPNAME_numOfImgComp+PROPNAME_seperator).length() )) );
                 } 
        	 else if ( line.startsWith( PROPNAME_numOfImgColComp+PROPNAME_seperator ) ) {
                 setNumOfImgColComp( Integer.parseInt(line.substring( (PROPNAME_numOfImgColComp+PROPNAME_seperator).length() )) );
                 } 
        	 else if ( line.startsWith( PROPNAME_pixelSize+PROPNAME_seperator ) ) {
                 setPixelSize( Integer.parseInt(line.substring( (PROPNAME_pixelSize+PROPNAME_seperator).length() )) );
                 } 
        	 else if ( line.startsWith( PROPNAME_colorSpaceType+PROPNAME_seperator ) ) {
                 setColorSpaceType( Integer.parseInt(line.substring( (PROPNAME_colorSpaceType+PROPNAME_seperator).length() )) );
                 } 
        	 else if ( line.startsWith( PROPNAME_transparency+PROPNAME_seperator ) ) {
                 setTransparency( Integer.parseInt(line.substring( (PROPNAME_transparency+PROPNAME_seperator).length() )) );
                 } 
        	 else if ( line.startsWith( PROPNAME_oriantation+PROPNAME_seperator ) ) {
                 setOriantation( Integer.parseInt(line.substring( (PROPNAME_oriantation+PROPNAME_seperator).length() )) );
                 } 
        }
    }
}


