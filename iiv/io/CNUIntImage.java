package iiv.io;
import iiv.data.*;
import iiv.script.*;
import java.io.*;
import java.util.zip.*;
import java.util.*;
import java.awt.*;
import java.awt.image.*;
/**
 * Converts an image to and from an int array.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		java.awt.image.ImageFilter
 * @see		java.awt.image.ImageProducer
 * @see		java.awt.image.ImageConsumer
 * @since	iiV1.153
 */
public class CNUIntImage implements ImageConsumer, ImageProducer {
  public final static int MAGIC_NUMBER = 'C'<<24 | 'N'<<16 | 'U'<<8 | '2';
  private Object imageLock = new Object();
  private int[] imagebuffer = null;
  private boolean imagedone = false;
  private boolean productionwait = false;

  private IOException ioe=null;

  private Object IP_consumerLock = new Object();
  private Vector<ImageConsumer> consumers = new Vector<ImageConsumer>();
  private ImageProducer ip = null;

  /**
   * Creates a new instance of CNUIntImage.
   *
   */
  public CNUIntImage(ImageProducer ip) { this.ip = ip; }
  /**
   * Creates a new instance of CNUIntImage.
   *
   */
  public CNUIntImage(int[] imagebuffer) {
    setImageBuffer(imagebuffer);
  }
  /**
   * Creates a new instance of CNU}IntImage based on an encoded string.
   *
   */
  public CNUIntImage(String encodedImage) {
    try {
      setImageBuffer(uncompressIn(new ByteArrayInputStream(encodedImage.getBytes())));
    } catch (IOException ioe) {
      System.out.println("CNUIntImage constructor caught IOException");
      ioe.printStackTrace();
    }
  }
  /**
   * Create a script for recreating this object.
   */
  public String toScript(CNUScriptObjects scriptedObjects) {
    String classname = getClass().getName();
    StringBuffer sb = new StringBuffer();
    sb.append("// -- start ").append(classname).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
      try {
	variableName = scriptedObjects.addObject(this, "cnuintimg");
	
	ByteArrayOutputStream baos = new ByteArrayOutputStream();
	compressOut(getImageBuffer(), baos);

	String imagebytes = baos.toString();
	int length = imagebytes.length();
	sb.append("cnuintimagebytestmp = \"");
	sb.append(imagebytes);
	sb.append("\";");
	sb.append(variableName).append(" = new ").append(classname).append("(cnuintimagebytestmp);\n");
	sb.append("unset(\"cnuintimagebytestmp\");\n");
      } catch (IOException ioe) {
	System.out.println("CNUIntImage.toScript caught IOException");
	ioe.printStackTrace();
      }
    }
    if(variableName != null) sb.append("script_rtn=").append(variableName).append(";\n");
    else sb.append("script_rtn=null;\n");
    sb.append("// -- end ").append(classname).append(" script\n");
    return sb.toString();
  }
  /**
   * Set the image buffer.
   *
   * @param imagebuffer image buffer
   */
  public void setImageBuffer(int[] imagebuffer) {
    if(imagebuffer[0] != MAGIC_NUMBER)
      System.out.println("CNUIntImage invalid image data");
    else synchronized (imageLock) {
      this.imagebuffer = imagebuffer;
      imagedone = true;
    }
  }
  /**
   * Get the image buffer.
   *
   * @return image buffer if complete otherwise <code>null</code>
   */
  public int[] getImageBuffer() {
    synchronized (imageLock) {
      if(imagedone) return imagebuffer;
      else return null;
    }
  }
  /**
   * Starts consuming and waits until complete.
   */
  public void consume() throws IOException
  {
    synchronized (imageLock) {
      imagedone = false;
      productionwait = true;
      ioe = null;
      ip.startProduction(this);
    }
    synchronized (this) {
      while ( productionwait ) {
	try {
	  wait(1000);
	}
	catch ( InterruptedException e ) {}
	if ( ioe != null ) throw ioe;
      }
    }
    synchronized (imageLock) {
      imagedone = true;
    }
  }
  /**
   * Called by the image producer to set the color model.
   *
   * @param model	color model that producer plans to mostly use
   */
  public void setColorModel(ColorModel model) {}
  /**
   * Called by the image producer to set the image dimensions.
   *
   * @param w the width of the image
   * @param h the height of the image
   */
  public void setDimensions(int w, int h) {
    synchronized (imageLock) {
      imagedone = false;
      imagebuffer = new int[3 + (w * h)];
      imagebuffer[0] = MAGIC_NUMBER;
      imagebuffer[1] = w;
      imagebuffer[2] = h;
    }
  }
  /**
   * Called by the image producer to set hints.
   *
   * @param h hints
   */
  public void setHints(int h) { }
  /**
   * Called by the image producer to set properties.
   *
   * @param props properties
   */
  public void setProperties(Hashtable props) { }
  /**
   * Called by the image producer when the image has been
   * completely sent.
   *
   * @param status of image completion
   */
  public void imageComplete( int status ) {
    synchronized (imageLock) {
      ip.removeConsumer( this );
      if ( status == ImageConsumer.IMAGEABORTED )
	ioe = new IOException( "image aborted" );
      productionwait = false; // allow consume loop to exit
    }
    synchronized (this) {
      notifyAll();  // unblock consume loop wait
    }
  }
  /**
   * Called by the image producer to set the pixels with a int array.
   *
   * @param srcX	start x location in source image
   * @param srcY	start y location in source image
   * @param srcW	width of source data
   * @param srcH	height of source data
   * @param model	color model for source data
   * @param pixels	array containing source data
   * @param srcOff	offset to first word of source data
   * @param srcScan	width of scanlines in the source data
   */
  public void setPixels(int srcX, int srcY, int srcW, int srcH,
    ColorModel model, int pixels[], int srcOff, int srcScan) {
    synchronized (imageLock) {
      int[] outbuffer = imagebuffer;
      if(outbuffer == null) {
        ioe = new IOException("trying to set pixels before setting dimensions");
	return;
      }
      int width = outbuffer[1];
      int height = outbuffer[2];
      int outyoff = 3 + (srcY * width) + srcX;
      int sy = srcOff;
      for (int y = 0; y < srcH; y++, sy += srcScan, outyoff += width) {
	int s = sy;
	int outx = outyoff;
	int outxend = outx + srcW;
	for (; outx < outxend; s++, outx++) {
	  outbuffer[outx] = model.getRGB(pixels[s]);
	}
      }
    }
  }
  /**
   * Called by the image producer to set the pixels with a byte array.
   *
   * @param srcX	start x location in source image
   * @param srcY	start y location in source image
   * @param srcW	width of source data
   * @param srcH	height of source data
   * @param model	color model for source data
   * @param pixels	array containing source data
   * @param srcOff	offset to first word of source data
   * @param srcScan	width of scanlines in the source data
   */
  public void setPixels(int srcX, int srcY, int srcW, int srcH,
    ColorModel model, byte pixels[], int srcOff, int srcScan) {
    synchronized (imageLock) {
      int[] outbuffer = getImageBuffer();
      if(outbuffer == null) {
	ioe = new IOException("trying to set pixels before setting dimensions");
	return;
      }
      int width = outbuffer[1];
      int height = outbuffer[2];
      if(outbuffer == null) return;
      int outyoff = 3 + (srcY * width) + srcX;
      int sy = srcOff;
      for (int y = 0; y < srcH; y++, sy += srcScan, outyoff += width) {
	int s = sy;
	int outx = outyoff;
	int outxend = outx + srcW;
	for (; outx < outxend; s++, outx++) {
	  outbuffer[outx] = model.getRed(pixels[s]);
	}
      }
    }
  }
  /**
   * Registers an image consumer with this producer.
   *
   * @param ic	image consumer
   */
  public void addConsumer(ImageConsumer ic) {
    synchronized (IP_consumerLock) {
      if(! consumers.contains(ic)) consumers.addElement(ic);
    }
  }
  /**
   * Determines if an image consumer is registered.
   *
   * @param ic	image consumer
   * @return	<code>true</code> if image consumer is registered
   */
  public boolean isConsumer(ImageConsumer ic) {
    synchronized (IP_consumerLock) {
      return consumers.contains(ic);
    }
  }
  /**
   * Removes a registered image consumer.
   *
   * @param ic	image consumer to remove
   */
  public void removeConsumer(ImageConsumer ic) {
    synchronized (IP_consumerLock) {
      consumers.removeElement(ic);
    }
  }
  /**
   * Requests pixel data be resent in left to right, top to bottom order.
   *
   * @param ic	image consumer making request
   */
  public void requestTopDownLeftRightResend(ImageConsumer ic) {
    startProduction(ic);
  }
  /**
   * Triggers the delivery of image data.
   *
   * @param ic	image consumer making request
   */
  public void startProduction(ImageConsumer ic) {
    addConsumer(ic);
    produce();
  }
  /**
   * Sends the image to consumers.
   */
  private void produce() {
    int status = ImageConsumer.SINGLEFRAMEDONE;
    int[] imagebuffer = getImageBuffer();
    int width = 0;
    int height = 0;
    if(imagebuffer != null) {
      width = imagebuffer[1]; height = imagebuffer[2];
    }
    synchronized (IP_consumerLock) {
      for(int i=consumers.size()-1; i>=0; i--) {
        ImageConsumer consumer = consumers.elementAt(i);
	if(imagebuffer == null) status = ImageConsumer.IMAGEERROR;
	else {
          consumer.setDimensions(width, height);
	  consumer.setColorModel(ColorModel.getRGBdefault());
	  consumer.setPixels(0, 0, width, height,
			     ColorModel.getRGBdefault(),
			     imagebuffer, 3, width);
	}
	consumer.imageComplete(status);
        if(status == ImageConsumer.STATICIMAGEDONE) {
	  consumers.removeAllElements();
	  consumer = null;
        }
      }
    }
  }
  public final static byte[][] byteEncodeTable = {
    {'!',(byte)130},{'!',(byte)131},{'!',(byte)132},{'!',(byte)133},{'!',(byte)134}, // 4
    {'!',(byte)135},{'!',(byte)136},{'!',(byte)137},{'!',(byte)138},{'!',(byte)139}, // 9
    {'!',(byte)140},{'!',(byte)41},{'!',(byte)142},{'!',(byte)43},{'!',(byte)44}, // 14
    {'!',(byte)145},{'!',(byte)146},{'!',(byte)147},{'!',(byte)148},{'!',(byte)149}, // 19
    {'!',(byte)150},{'!',(byte)151},{'!',(byte)152},{'!',(byte)153},{'!',(byte)154}, // 24
    {'!',(byte)155},{'!',(byte)156},{'!',(byte)47},{'!',(byte)158},{'!',(byte)159}, // 29
    {'!',(byte)160},{'!',(byte)161},{'!',(byte)162},{'!',(byte)163},{'!',(byte)164}, // 34
    {'!',(byte)165},{'!',(byte)166},{'%'},          {'&'},          {'!',(byte)169}, // 39
    {'('},{')'},{'*'},{'+'},{','},  // 44
    {'-'},{'.'},{'/'},{'0'},{'1'},  // 49
    {'2'},{'3'},{'4'},{'5'},{'6'},  // 54
    {'7'},{'8'},{'9'},{':'},{'!',(byte)189},  // 59
    {'<'},{'='},{'>'},{'!',(byte)193},{'@'},  // 64
    {'A'},{'B'},{'C'},{'D'},{'E'},  // 69
    {'F'},{'G'},{'H'},{'I'},{'J'},  // 74
    {'K'},{'L'},{'M'},{'N'},{'O'},  // 79
    {'P'},{'Q'},{'R'},{'S'},{'T'},  // 84
    {'U'},{'V'},{'W'},{'X'},{'Y'},  // 89
    {'Z'},{'['},{'!',(byte)222},{']'},{'^'},  // 94
    {'_'},{'`'},{'a'},{'b'},{'c'},  // 99
    {'d'},{'e'},{'f'},{'g'},{'h'},  // 104
    {'i'},{'j'},{'k'},{'l'},{'m'},  // 109
    {'n'},{'o'},{'p'},{'q'},{'r'},  // 114
    {'s'},{'t'},{'u'},{'v'},{'w'},  // 119
    {'x'},{'y'},{'z'},{'{'},{'|'},  // 124
    {'}'},{'~'},{'!',(byte)126},{(byte)128},{'!',(byte)128},  // 129
    {(byte)130},{(byte)131},{(byte)132},{(byte)133},{(byte)134},  // 134
    {(byte)135},{(byte)136},{(byte)137},{(byte)138},{(byte)139},  // 139
    {(byte)140},{'!',(byte)61},{(byte)142},{'!',73},{'!', (byte) 64},  // 144
    {(byte)145},{(byte)146},{(byte)147},{(byte)148},{(byte)149},  // 149
    {(byte)150},{(byte)151},{(byte)152},{(byte)153},{(byte)154},  // 154
    {(byte)155},{(byte)156},{'!',(byte)247},{(byte)158},{(byte)159},  // 159
    {(byte)160},{(byte)161},{(byte)162},{(byte)163},{(byte)164},  // 164
    {(byte)165},{(byte)166},{(byte)167},{(byte)168},{(byte)169},  // 169
    {(byte)170},{(byte)171},{(byte)172},{(byte)173},{(byte)174},  // 174
    {(byte)175},{(byte)176},{(byte)177},{(byte)178},{(byte)179},  // 179
    {(byte)180},{(byte)181},{(byte)182},{(byte)183},{(byte)184},  // 184
    {(byte)185},{(byte)186},{(byte)187},{(byte)188},{(byte)189},  // 189
    {(byte)190},{(byte)191},{(byte)192},{(byte)193},{(byte)194},  // 194
    {(byte)195},{(byte)196},{(byte)197},{(byte)198},{(byte)199},  // 199
    {(byte)200},{(byte)201},{(byte)202},{(byte)203},{(byte)204},  // 204
    {(byte)205},{(byte)206},{(byte)207},{(byte)208},{(byte)209},  // 209
    {(byte)210},{(byte)211},{(byte)212},{(byte)213},{(byte)214},  // 214
    {(byte)215},{(byte)216},{(byte)217},{(byte)218},{(byte)219},  // 219
    {(byte)220},{(byte)221},{(byte)222},{(byte)223},{(byte)224},  // 224
    {(byte)225},{(byte)226},{(byte)227},{(byte)228},{(byte)229},  // 229
    {(byte)230},{(byte)231},{(byte)232},{(byte)233},{(byte)234},  // 234
    {(byte)235},{(byte)236},{(byte)237},{(byte)238},{(byte)239},  // 239
    {(byte)240},{(byte)241},{(byte)242},{(byte)243},{(byte)244},  // 244
    {(byte)245},{(byte)246},{(byte)247},{(byte)248},{(byte)249},  // 249
    {(byte)250},{(byte)251},{(byte)252},{(byte)253},{(byte)254},  // 254
    {'!',(byte)254}  // 255
  };
  public final static int CNU_COMPRESSED_MAGIC_NUMBER =
    'C'<<24 | 'N'<<16 | 'U'<<8 | 'C';
  public final static void compressOut(int[] array, OutputStream os) 
    throws IOException {

    // magic number not filtered or compressed

    os.write((CNU_COMPRESSED_MAGIC_NUMBER>>24) & 0xFF);
    os.write((CNU_COMPRESSED_MAGIC_NUMBER>>16) & 0xFF);
    os.write((CNU_COMPRESSED_MAGIC_NUMBER>>8) & 0xFF);
    os.write((CNU_COMPRESSED_MAGIC_NUMBER) & 0xFF);
    os.flush();

    FilterOutputStream fos = new FilterOutputStream(os) {
	public void write(int b) throws IOException {
	  byte[] encodedBytes =
	    byteEncodeTable[CNUTypes.UnsignedByteToInt((byte) b)];
	  for(int i=0; i<encodedBytes.length; i++)
	    super.write(encodedBytes[i]);
	}
	public void write(byte bytes[], int off, int len)
	  throws IOException {
	  int end = off + len;
	  for(int j=off; j<end; j++) {
	    byte[] encodedBytes =
	      byteEncodeTable[CNUTypes.UnsignedByteToInt(bytes[j])];
	    for(int i=0; i<encodedBytes.length; i++)
	      super.write(encodedBytes[i]);
	  }
	}
	public void write(byte b[]) throws IOException {
	  write(b, 0, b.length);
	}
      };
    GZIPOutputStream gzipOut = new GZIPOutputStream(fos);

    DataOutputStream dos = new DataOutputStream(gzipOut);
    //    DataOutputStream dos = new DataOutputStream(fos);
    //    dos.writeInt(CNU_COMPRESSED_MAGIC_NUMBER);
    dos.writeLong(array.length);
    for(int i=0; i<array.length; i++ ) dos.writeInt(array[i]);
    dos.flush();

    gzipOut.close();
    fos.flush();
  }
  public final static int[] quotedDecodeTable = {
    -1,-1,-2,-3,-4,-5,-6,-7,-8,-9,                      // 9
    -10,-11,-12,-13,-14,-15,-16,-17,-18,-19,            // 19
    -20,-21,-22,-23,-24,-25,-26,-27,-28,-29,            // 29
    -30,-31,-32,-33,-34,-35,-36,-37,-38,-39,            // 39
    -40,11,-42,13,14,-45,-46,27,-48,-49,                // 49
    -50,-51,-52,-53,-54,-55,-56,-57,-58,-59,            // 59
    -60,141,-62,255,144,-65,-66,-67,-68,-69,            // 49
    -70,-71,-72,143,-74,-75,-76,-77,-78,-79,            // 79
    -80,-81,-82,-83,-84,-85,-86,-87,-88,-89,            // 89
    -90,-91,-92,-93,-94,-95,-96,-97,-98,-99,            // 99
    -100,-101,-102,-103,-104,-105,-106,-107,-108,-109,  // 109
    -111,-111,-112,-113,-114,-115,-116,-117,-118,-119,  // 119
    -129,-121,-122,-123,-124,-125,127,-128,129,-129,    // 129
    0,1,2,3,4,5,6,7,8,9,                                // 139
    10,-141,12,-143,-144,15,16,17,18,19,                // 149
    20,21,22,23,24,25,26,-157,28,29,                    // 159
    30,31,32,33,34,35,36,-167,-168,39,                  // 169
    -170,-171,-172,-173,-174,-175,-176,-177,-178,-179,  // 179
    -180,-181,-182,-183,-184,-185,-186,-187,-188,59,    // 189
    -190,-191,-192,63,-194,-195,-196,-197,-198,-199,    // 199
    -200,-201,-202,-203,-204,-205,-206,-207,-208,-209,  // 209
    -210,-211,-212,-213,-214,-215,-216,-217,-218,-219,  // 219
    -220,-221,92,-223,-224,-225,-226,-227,-228,-229,    // 229
    -230,-231,-232,-233,-234,-235,-236,-237,-238,-239,  // 239
    -240,-241,-242,-243,-244,-245,-246,157,-248,-249,   // 249
    -250,-251,-252,-253,255,  -255                      // 255
  };
  public final static int[] uncompressIn(InputStream is)
      throws IOException {
    // magic number not filtered or compressed
    int magic_number = (is.read()<<24) |  (is.read()<<16) |
      (is.read()<<8) | is.read();
    if(magic_number != CNU_COMPRESSED_MAGIC_NUMBER)
      throw new IOException("file missing CNU magic number");

    FilterInputStream fis = new FilterInputStream(is) {
	public int read() throws IOException {
	  int b=super.read();
	  if(b == '!') {
	    int quoteIndex = super.read();
	    b = quotedDecodeTable[quoteIndex];
	    if (b < 0) {
	      throw new IOException("invalid escaped byte=" + quoteIndex +
				    " or 0" + Integer.toOctalString(quoteIndex) +
				    " maps to " + b );
	    }
	  }
	  return b;
	}
	public int read(byte b[], int off, int len)
	  throws IOException {
	  int end = off + len;
	  for(int i=off; i<end; i++) b[i] = (byte) read();
	  return len;
	}
	public int read(byte b[]) throws IOException {
	  return read(b, 0, b.length);
	}
      };
    GZIPInputStream gzipIn = new GZIPInputStream(fis);
    DataInputStream dis = new DataInputStream(gzipIn);

    //    int magic_number = dis.readInt();
    //    if(magic_number != CNU_COMPRESSED_MAGIC_NUMBER)
    //      throw new IOException("file missing CNU magic number");


    int size = (int) dis.readLong();
    int[] array = new int[size];
    for(int i=0; i<size; i++) array[i] = dis.readInt();
    return array;
  }
}
