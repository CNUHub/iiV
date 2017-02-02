package iiv.filter;
import iiv.data.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
/**
 * LinearImageFilter sits between an ImageProducer and ImageConsumer
 * applying a linear transform to an image.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		java.awt.image.ImageProducer
 * @see		java.awt.image.ImageConsumer
 * @see		Mapping2D
 * @see		FilterSampling
 * @since	iiV1.0
 */
public class LinearImageFilter implements ImageConsumer,
					  ImageProducer, Mapping2D,
					  FilterSampling {

  private Object parameterLock = new Object();
  private int sampleType = REPLICATE;
  private int inWidth = 0;
  private int inHeight = 0;
  private int outWidth = 0;
  private int outHeight = 0;
  private boolean isIdentity = true;
  private boolean flipV = false;
  private boolean flipH = false;
  private double zoomV = 1;
  private double zoomH = 1;
  private double angle = 0;
  private double rxfm[] = {1, 0, 0, 1};
  private double irxfm[] = {1, 0, 0, 1};
  private double xfm[] = {1, 0, 0, 1};
  private double ixfm[] = {1, 0, 0, 1};
  private double xshift = 0;
  private double yshift = 0;
  private ColorModel sourceColorModel = null;
  private ColorModel produceColorModel = null;
  private Hashtable<Object,Object> properties = null;
  private int hints = 0;

  private int ibuffer[] = null;
  private byte bbuffer[] = null;
  private int ioutbuffer[] = null;
  private byte boutbuffer[] = null;

  private Object IP_consumerLock = new Object();
  private Vector<ImageConsumer> consumers = new Vector<ImageConsumer>();
  private ImageProducer ip = null;
  private ImageConsumer consumer = null;

  /**
   * Creates a clone of this object.
   *
   * @return	clone of this object
   */
  final public Object clone() {
    LinearImageFilter lif = new LinearImageFilter();
    synchronized (parameterLock) {
      lif.setFilterSampleType(sampleType);
      lif.setFlip(flipV, flipH);
      lif.setZoom(zoomV, zoomH);
      lif.setAngle(angle);
    }
    return lif;
  }
  /**
   * Sets the filter sampling type.
   *
   * @param sampleType filter sample type
   */
  public void setFilterSampleType( int sampleType ) {
    synchronized (parameterLock) { this.sampleType = sampleType; }
  }
  /**
   * Gets the filter sampling type.
   *
   * @return filter sample type
   */
  public int getFilterSampleType() { return sampleType; }
  /**
   * Set the flip flags
   *
   * @param flipV if <code>true</code> image will be flipped vertically
   * @param flipH if <code>true</code> image will be flipped horizontally
   */
  public void setFlip(boolean flipV, boolean flipH) {
    synchronized (parameterLock) {
      if((flipV != this.flipV) || (flipH != this.flipH)) {
        this.flipV = flipV;
        this.flipH = flipH;
      }
    }
    updateMatrix();
  }
  /**
   * Sets a single zoom factor.
   *
   * @param zoom	zoom factor for both horizontal an vertical
   */
  public void setZoom(double zoom) {
    setZoom(zoom, zoom);
  }
  /**
   * Sets independent horizontal and vertical zoom factors.
   *
   * @param zoomV	vertical zoom factor
   * @param zoomH	horizontal zoom factor
   */
  public void setZoom(double zoomV, double zoomH) {
    synchronized (parameterLock) {
      if(zoomH <= 0) zoomH = 1;
      if(zoomV <= 0) zoomV = 1;
      if((zoomH != this.zoomH) || (zoomV != this.zoomV)) {
        this.zoomH = zoomH;
        this.zoomV = zoomV;
      }
    }
    updateMatrix();
  }
  /**
   * Set the rotation angle (radians).
   *
   * @param angle	rotation angle in radians
   */
  public void setAngle(double angle) {
    synchronized (parameterLock) {
      if(angle != this.angle) {
        this.angle = angle;
        if(angle != 0) {
          rxfm[0] = Math.cos(angle); rxfm[1] = Math.sin(angle);
          rxfm[2] = -rxfm[1]; rxfm[3] = rxfm[0];
          irxfm[0] = rxfm[0]; irxfm[1] = -rxfm[1];
          irxfm[2] = -rxfm[2]; irxfm[3] = rxfm[3];
        }
        else {
          rxfm[0] = 1; rxfm[1] = 0;
          rxfm[2] = 0; rxfm[3] = 1;
          irxfm[0] = 1; irxfm[1] = 0;
          irxfm[2] = 0; irxfm[3] = 1;
        }
      }
    }
    updateMatrix();
  }
  /**
   * Updates the linear mapping matrix based on zoom and rotation.
   */
  private void updateMatrix() {
    boolean callSetDimensions = false;
    synchronized (parameterLock) {
      // initialize forward and inverse matrices to identity
      isIdentity = true;
      xfm[0] = xfm[3] = ixfm[0] = ixfm[3] = 1;
      xfm[1] = xfm[2] = ixfm[1] = ixfm[2] = 0;
      // apply flips
      if(flipH) {
        isIdentity = false;
        xfm[0] *= -1;
        xfm[1] *= -1;
      }
      if(flipV) {
        isIdentity = false;
        xfm[2] *= -1;
        xfm[3] *= -1;
      }
      // apply horizontal zoom
      if(zoomH != 1) {
        isIdentity = false;
        xfm[0] *= zoomH;
        xfm[1] *= zoomH;
      }
      // apply vertical zoom
      if(zoomV != 1) {
        isIdentity = false;
        xfm[2] *= zoomV;
        xfm[3] *= zoomV;
      }
      // apply angle - last forward operation
      if(angle != 0) {
        isIdentity = false;
        double[] tmpxfm = new double[4];
        // premultiply by rotation matrix since flipping preformed first
        tmpxfm[0] = rxfm[0] * xfm[0] + rxfm[1] * xfm[2];
        tmpxfm[1] = rxfm[0] * xfm[1] + rxfm[1] * xfm[3];
        tmpxfm[2] = rxfm[2] * xfm[0] + rxfm[3] * xfm[2];
        tmpxfm[3] = rxfm[2] * xfm[1] + rxfm[3] * xfm[3];
        for(int i=0; i<4; i++) xfm[i] = tmpxfm[i];

        // apply inverse angle - first inverse operation
        // postmultiply inverse by inverse rotation matrix
        tmpxfm[0] = ixfm[0] * irxfm[0] + ixfm[1] * irxfm[2];
        tmpxfm[1] = ixfm[0] * irxfm[1] + ixfm[1] * irxfm[3];
        tmpxfm[2] = ixfm[2] * irxfm[0] + ixfm[3] * irxfm[2];
        tmpxfm[3] = ixfm[2] * irxfm[1] + ixfm[3] * irxfm[3];
        for(int i=0; i<4; i++) ixfm[i] = tmpxfm[i];
      }
      // Only modify inverse matrix if not identity
      if(! isIdentity) {
        // apply inverse vertical zoom
        if(zoomV != 1) {
          ixfm[2] /= zoomV;
          ixfm[3] /= zoomV;
        }
        // apply inverse horizontal zoom
        if(zoomH != 1) {
          ixfm[0] /= zoomH;
          ixfm[1] /= zoomH;
        }
        // apply inverse flips
        if(flipV) {
          ixfm[2] *= -1;
          ixfm[3] *= -1;
        }
        if(flipH) {
          ixfm[0] *= -1;
          ixfm[1] *= -1;
        }
      }
      // if dimensions previously set call setdimensions
      // to calculate dimensions and set needed shift
      if(inWidth != 0) callSetDimensions = true;
    }
    if(callSetDimensions) setDimensions(inWidth, inHeight);
  }
  /**
   * Gets the isIdentity flag.
   *
   * @return	<code>true</code> if transform matrix is an identity matrix
   */
  public boolean getIsIdentity() {
    synchronized (parameterLock) {
      return(isIdentity);
    }
  }
  /**
   * Maps a precise prefilter location to a precise filtered location.
   *
   * @param in	array containing the input location
   * @param out	array to contain the output location
   */
  public void map(double[] in, double[] out) {
    synchronized (parameterLock) {
      double tmp = xfm[0] * in[0] + xfm[1] * in[1] + xshift;
      out[1] = xfm[2] * in[0] + xfm[3] * in[1] + yshift;
      out[0] = tmp;
    }
  }
  /**
   * Maps a precise prefilter location to a precise filtered location.
   *
   * @param in	array containing the input location
   * @return	a new array containing the output location
   */
  public double[] map(double[] in) {
    double[] out = new double[2];
    map(in, out);
    return out;
  }
  /**
   * Maps a prefilter point to the nearest filtered point.
   */
  public void map(Point inPt, Point outPt) {
    double[] in = new double[2];
    double[] out = new double[2];
    in[0] = inPt.x; in[1] = inPt.y;
    map(in, out);
    outPt.x = (int) Math.round(out[0]);
    outPt.y = (int) Math.round(out[1]);
    return;
  }
  /**
   * Inverts mapping - maps a precise filtered location to the precise
   * prefilter location.
   *
   * @param in	array containing the input location
   * @param out	array to contain the output location
   */
  public void invertMap(double[] in, double[] out) {
    synchronized (parameterLock) {
      double x = in[0] - xshift;
      double y = in[1] - yshift;
      out[0] = ixfm[0] * x + ixfm[1] * y;
      out[1] = ixfm[2] * x + ixfm[3] * y;
    }
  }
  /**
   * Inverts mapping - maps a precise filtered location to the precise
   * prefilter location.
   *
   * @param in	array containing the input location
   * @return	a new array containing the output location
   */
  public double[] invertMap(double[] in) {
    double[] out = new double[2];
    invertMap(in, out);
    return out;
  }
  /**
   * Inverts mapping - maps a precise filtered location to the precise
   * prefilter location.
   *
   * @param inPt	point containing the input location
   * @param outPt	point to contain the output location
   */
  public void invertMap(Point inPt, Point outPt) {
    double[] in = new double[2];
    double[] out = new double[2];
    in[0] = inPt.x; in[1] = inPt.y;
    invertMap(in, out);
    outPt.x = (int) Math.round(out[0]);
    outPt.y = (int) Math.round(out[1]);
  }
  /**
   * Sets the amount of origin shift.
   *
   * @param x	amount of x shift in pixels
   * @param y	amount of y shift in pixels
   */
  private void setShift(double x, double y) {
    synchronized (parameterLock) {
      xshift = x; yshift = y;
    }
  }
  /**
   * Sets the image dimensions - called by the image producer.
   *
   * @param w	width in pixels
   * @param h	heigth in pixels
   */
  public void setDimensions(int w, int h) {
    int newOutWidth;
    int newOutHeight;
    synchronized (parameterLock) {
      if((w != inWidth) || (h != inHeight)) {
        inWidth = w; inHeight = h;
        ibuffer = null; bbuffer = null;
      }
      setShift(0, 0);
      // calculate extremes
      double[] in = {0, 0};
      double[] lt = map(in);
      in[1] = h-1; double[] lb = map(in);
      in[0] = w-1; double[] rb = map(in);
      in[1] = 0;   double[] rt = map(in);
      double xmin = Math.min(Math.min(Math.min(lt[0], lb[0]), rt[0]), rb[0]);
      double ymin = Math.min(Math.min(Math.min(lt[1], lb[1]), rt[1]), rb[1]);
      Point orig = new Point((int) Math.floor(xmin), (int) Math.floor(ymin));
      double xmax = Math.max(Math.max(Math.max(lt[0], lb[0]), rt[0]), rb[0]);
      double ymax = Math.max(Math.max(Math.max(lt[1], lb[1]), rt[1]), rb[1]);
      Point end = new Point((int) Math.ceil(xmax),(int) Math.ceil(ymax));
      setShift(-orig.x, -orig.y);
      newOutWidth = end.x - orig.x + 1;
      newOutHeight = end.y - orig.y + 1;
      if( (newOutWidth != outWidth) || (newOutHeight != outHeight) ) {
        outWidth = newOutWidth; outHeight = newOutHeight;
        ioutbuffer = null; boutbuffer = null;
      }
    }
    // make sure image consumers get dimension as soon as possible
    synchronized (IP_consumerLock) {
      if(consumer != null) consumer.setDimensions(newOutWidth, newOutHeight);
      for(int i=consumers.size()-1; i>=0; i--) {
        ImageConsumer ic = consumers.elementAt(i);
        if(ic != consumer) ic.setDimensions(newOutWidth, newOutHeight);
      }
    }
  }
  /**
   * Sets the image producer.
   *
   * @param ip image producer
   */
  public void setImageProducer(ImageProducer ip) {
    synchronized (IP_consumerLock) {
      this.ip = ip;
    }
  }
  /**
   * Sets the image properties - called by the image producer.
   *
   * @param props	image properties
   */
  public void setProperties(Hashtable<?,?> in_props) {
    synchronized (parameterLock) {
      if(in_props == null) properties = new Hashtable<Object,Object>();
      else properties = new Hashtable<Object,Object>(in_props);
      double rotation = 0;
      Object irm = properties.get("rotate");

      if (irm != null) {
        rotation = ((Double)irm).doubleValue();
      }
      rotation += angle;
      properties.put("rotation", new Double(rotation));
    }
  }
  /**
   * Sets the image hints - called by the image producer.
   *
   * @param h	hints
   */
  public void setHints(int h) {
    synchronized (parameterLock) {
      hints = h;
      hints &= ~ImageConsumer.RANDOMPIXELORDER;
      hints |= ImageConsumer.TOPDOWNLEFTRIGHT;
    }
  }
  /**
   * Sets the most likely color model - called by the image producer.
   * Note - each call to setPixels may send a different color model.
   *
   * @param model	color model
   */
  public void setColorModel(ColorModel model) {
    synchronized (parameterLock) {
      this.sourceColorModel = model;
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
			ColorModel model, byte pixels[], int srcOff,
			int srcScan) {
    synchronized (parameterLock) {
      if(this.sourceColorModel == null) {
	this.sourceColorModel = model;
	System.out.println("LinearImageFilter.setPixels(byte) - warning color model not set prior");
      }
      if(this.sourceColorModel != model) {
	System.out.println("LinearImageFilter.setPixels(byte) - warning different color model");
	System.out.println("Old model=" + this.sourceColorModel);
	System.out.println("New model=" + model);
      }
      if(bbuffer == null) {
        if(inWidth > 0 && inHeight > 0) bbuffer = new byte[inWidth * inHeight];
	else {
	  System.out.println("LinearImageFilter.setPixels(byte) - error width and height not set");
	  return;
	}
      }
      int s = srcOff;
      int d = (srcY * inWidth) + srcX;
      for (int y=0; y<srcH; y++, s += srcScan, d += inWidth) {
        int sx = s;
        int dx = d;
        for (int x=0; x<srcW; x++, sx++, dx++) bbuffer[dx] = pixels[sx];
      }
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
				     ColorModel model, int pixels[],
				     int srcOff, int srcScan) {
    synchronized (parameterLock) {
      if(this.sourceColorModel == null) {
	this.sourceColorModel = model;
	System.out.println("LinearImageFilter.setPixels(int) - warning color model not set prior");
      }
      if(this.sourceColorModel != model) {
	System.out.println("LinearImageFilter.setPixels(int) - warning different color model");
	System.out.println("Old model=" + this.sourceColorModel);
	System.out.println("New model=" + model);
      }
      if(ibuffer == null) {
        if(inWidth > 0 && inHeight > 0) ibuffer = new int[inWidth * inHeight];
	else {
	  System.out.println("LinearImageFilter.setPixels(int) - error width and height not set");
	  return;
	}
      }
      int s = srcOff;
      int d = (srcY * inWidth) + srcX;
      for (int y=0; y<srcH; y++, s += srcScan, d += inWidth) {
        int sx = s;
        int dx = d;
        for (int x=0; x<srcW; x++, sx++, dx++) ibuffer[dx] = pixels[sx];
      }
    }
  }
  /**
   * Requests to resend data in order - called by FilteredImageSource.
   *
   * @param ip	image producer to resend to
   */
  public void resendTopDownLeftRight(ImageProducer ip) {
    setImageProducer(ip);
    produce();
  }
  /**
   * Creates (if needed) and fills the output buffer
   * using nearest pixel replication.
   *
   */
  public void fillBuffer() {
    synchronized (parameterLock) {
      // calculate double precision locations and increments
      double[] outTmp = new double[2];
      outTmp[0] = 0; outTmp[1] = 0;
      double[] inYpos = invertMap(outTmp);
      outTmp[0] = 1; outTmp[1] = 0;
      double[] incX = invertMap(outTmp);
      incX[0] = incX[0] - inYpos[0]; incX[1] = incX[1] - inYpos[1];
      outTmp[0] = 0; outTmp[1] = 1;
      double[] incY = invertMap(outTmp);
      incY[0] = incY[0] - inYpos[0]; incY[1] = incY[1] - inYpos[1];
      double[] inXpos = new double[2];
      // integer locations
      Point out = new Point(0, 0);
      Point in = new Point(0, 0);
      int outIndex = 0;

      produceColorModel = sourceColorModel;
      int outLength = outWidth * outHeight;
      if(bbuffer != null) {
	if((boutbuffer == null) || (boutbuffer.length < outLength))
	  boutbuffer = new byte[outLength];

	for(out.y = 0; out.y < outHeight; out.y++,
	      inYpos[0] += incY[0], inYpos[1] += incY[1]) {
	  inXpos[0] = inYpos[0]; inXpos[1] = inYpos[1];
	  for(out.x = 0; out.x < outWidth; out.x++, outIndex++,
		inXpos[0] += incX[0], inXpos[1] += incX[1]) {
	    in.x = (int) Math.round(inXpos[0]);
	    in.y = (int) Math.round(inXpos[1]);
	    if((in.x < 0) || (in.y < 0) ||
	       (in.x >= inWidth) || (in.y >= inHeight))
	      boutbuffer[outIndex] = 0;
	    else boutbuffer[outIndex] = bbuffer[in.x + (in.y * inWidth)];
	  }
	}
      }
      else if(ibuffer != null) {
	if((ioutbuffer == null) || (ioutbuffer.length < outLength))
	  ioutbuffer = new int[outLength];
	for( out.y = 0; out.y < outHeight; out.y++,
	       inYpos[0] += incY[0], inYpos[1] += incY[1] ) {
	  inXpos[0] = inYpos[0]; inXpos[1] = inYpos[1];
	  for( out.x = 0; out.x < outWidth; out.x++, outIndex++,
		 inXpos[0] += incX[0], inXpos[1] += incX[1] ) {
	    in.x = (int) Math.round(inXpos[0]);
	    in.y = (int) Math.round(inXpos[1]);
	    if((in.x < 0) || (in.y < 0) ||
	       (in.x >= inWidth) || (in.y >= inHeight)) ioutbuffer[outIndex] = 0;
	    else ioutbuffer[outIndex] = ibuffer[in.x + (in.y * inWidth)];
	  }
	}
      }
      else {
	boutbuffer = null;
	ioutbuffer = null;
      }
    }
  }
  /**
   * Fills (creates if needed) an output buffer with
   * weighted average (interpolation) of surrounding pixels.
   *
   */
  public void averageFillBuffer(boolean rgbonly) {
    synchronized (parameterLock) {
      // calculate double precision locations and increments
      double[] outTmp = new double[2];
      outTmp[0] = 0; outTmp[1] = 0;
      double[] inYpos = invertMap(outTmp);
      outTmp[0] = 1;
      double[] incX = invertMap(outTmp);
      incX[0] = incX[0] - inYpos[0]; incX[1] = incX[1] - inYpos[1];
      outTmp[0] = 0; outTmp[1] = 1;
      double[] incY = invertMap(outTmp);
      incY[0] = incY[0] - inYpos[0]; incY[1] = incY[1] - inYpos[1];
      double[] inXpos = new double[2];
      // integer locations
      Point out = new Point(0, 0);
      int outIndex = 0;

      int outLength = outWidth * outHeight;
      Object inarray;
      int intype;
      if(bbuffer != null) {
	inarray = bbuffer;
	intype = CNUTypes.UNSIGNED_BYTE;
      }
      else if(ibuffer != null) {
	inarray = ibuffer;
	intype = CNUTypes.INTEGER;
      }
      else {
	boutbuffer = null;
	ioutbuffer = null;
	return;
      }

      if((! rgbonly) && (sourceColorModel instanceof IndexColorModel)) {
	// average index value and output input color model
	produceColorModel = sourceColorModel;
	int outtype;
	Object outarray;
	if(bbuffer != null) {
	  if((boutbuffer == null) || (boutbuffer.length < outLength))
	    boutbuffer = new byte[outLength];
	  outarray = boutbuffer;
	  outtype = CNUTypes.UNSIGNED_BYTE;
	}
	else {
	  if((ioutbuffer == null) || (ioutbuffer.length < outLength))
	    ioutbuffer = new int[outLength];
	  outarray = ioutbuffer;
	  outtype = CNUTypes.INTEGER;
	}

	for(out.y = 0; out.y < outHeight;
	    out.y++, inYpos[0] += incY[0], inYpos[1] += incY[1] ) {
	  inXpos[0] = inYpos[0]; inXpos[1] = inYpos[1];
	  for(out.x = 0; out.x < outWidth;
	      out.x++, outIndex++, inXpos[0] += incX[0], inXpos[1] += incX[1] ) {
	    if((inXpos[0] < 0) || (inXpos[1] < 0) ||
	       (inXpos[0] >= inWidth) || (inXpos[1] >= inHeight)) {
	      CNUTypes.setArrayValue(0, outarray, outIndex, outtype);
	    }
	    else {
	      int xLesserIndex = (int) inXpos[0];
	      int xGreaterIndex = xLesserIndex + 1;
	      if( xGreaterIndex >= inWidth ) {
		xGreaterIndex = inWidth - 1;
		if( xLesserIndex >= inWidth ) xLesserIndex = inWidth - 1;
	      }
	      double xGreaterFactor = inXpos[0] - ((double) xLesserIndex);
	      double xLesserFactor = 1.0 - xGreaterFactor;
	      int yLesser = (int) inXpos[1];
	      int yGreater = yLesser + 1;
	      if(yGreater >= inHeight) {
		yGreater = inHeight - 1;
		if(yLesser >= inHeight) yLesser = inHeight - 1;
	      }
	      int yLesserIndex = yLesser * inWidth;
	      int yGreaterIndex = yGreater * inWidth;
	      double yGreaterFactor = inXpos[1] - ((double) yLesser);
	      double yLesserFactor = 1.0 - yGreaterFactor;

	      double ll =
		CNUTypes.getArrayValueAsDouble(inarray,
					       yLesserIndex + xLesserIndex,
					       intype);
	      double lg =
		CNUTypes.getArrayValueAsDouble(inarray,
					       yLesserIndex + xGreaterIndex,
					       intype);
	      double gl =
		CNUTypes.getArrayValueAsDouble(inarray,
					       yGreaterIndex + xLesserIndex,
					       intype);
	      double gg =
		CNUTypes.getArrayValueAsDouble(inarray,
					       yGreaterIndex + xGreaterIndex,
					       intype);
	      double outvalue =
		yLesserFactor  * ((xLesserFactor * ll) + (xGreaterFactor * lg)) +
		yGreaterFactor * ((xLesserFactor * gl) + (xGreaterFactor * gg));
	      CNUTypes.setArrayValue(outvalue, outarray, outIndex, outtype);
	    }
	  }
	}
      }
      else {
	// average rgb values and output default rgb color model
	if((ioutbuffer == null) || (ioutbuffer.length < outLength))
	  ioutbuffer = new int[outLength];
	// ioutbuffer stored as rgbdefault model
	produceColorModel = ColorModel.getRGBdefault();

	for(out.y = 0; out.y < outHeight;
	    out.y++, inYpos[0] += incY[0], inYpos[1] += incY[1] ) {
	  inXpos[0] = inYpos[0]; inXpos[1] = inYpos[1];
	  for(out.x = 0; out.x < outWidth;
	      out.x++, outIndex++, inXpos[0] += incX[0], inXpos[1] += incX[1] ) {
	    if((inXpos[0] < 0) || (inXpos[1] < 0) ||
	       (inXpos[0] >= inWidth) || (inXpos[1] >= inHeight)) {
	      ioutbuffer[outIndex] = 0;
	    }
	    else {
	      int xLesserIndex = (int) inXpos[0];
	      int xGreaterIndex = xLesserIndex + 1;
	      if( xGreaterIndex >= inWidth ) {
		xGreaterIndex = inWidth - 1;
		if( xLesserIndex >= inWidth ) xLesserIndex = inWidth - 1;
	      }
	      double xGreaterFactor = inXpos[0] - ((double) xLesserIndex);
	      double xLesserFactor = 1.0 - xGreaterFactor;
	      int yLesser = (int) inXpos[1];
	      int yGreater = yLesser + 1;
	      if(yGreater >= inHeight) {
		yGreater = inHeight - 1;
		if(yLesser >= inHeight) yLesser = inHeight - 1;
	      }
	      int yLesserIndex = yLesser * inWidth;
	      int yGreaterIndex = yGreater * inWidth;
	      double yGreaterFactor = inXpos[1] - ((double) yLesser);
	      double yLesserFactor = 1.0 - yGreaterFactor;

	      int color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yLesserIndex + xLesserIndex,
					    intype);
	      double factor = yLesserFactor * xLesserFactor;
	      double ad = factor * ( (double) sourceColorModel.getAlpha(color) );
	      double rd = factor * ( (double) sourceColorModel.getRed(color) );
	      double gd = factor * ( (double) sourceColorModel.getGreen(color) );
	      double bd = factor * ( (double) sourceColorModel.getBlue(color) );
	      
	      color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yLesserIndex + xGreaterIndex,
					    intype);
	      factor = yLesserFactor * xGreaterFactor;
	      ad += factor * ( (double) sourceColorModel.getAlpha(color) );
	      rd += factor * ( (double) sourceColorModel.getRed(color) );
	      gd += factor * ( (double) sourceColorModel.getGreen(color) );
	      bd += factor * ( (double) sourceColorModel.getBlue(color) );

	      color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yGreaterIndex + xLesserIndex,
					    intype);
	      
	      factor = yGreaterFactor * xLesserFactor;
	      ad += factor * ( (double) sourceColorModel.getAlpha(color) );
	      rd += factor * ( (double) sourceColorModel.getRed(color) );
	      gd += factor * ( (double) sourceColorModel.getGreen(color) );
	      bd += factor * ( (double) sourceColorModel.getBlue(color) );

	      color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yGreaterIndex + xGreaterIndex,
					    intype);

	      factor = yGreaterFactor * xGreaterFactor;
	      ad += factor * ( (double) sourceColorModel.getAlpha(color) );
	      rd += factor * ( (double) sourceColorModel.getRed(color) );
	      gd += factor * ( (double) sourceColorModel.getGreen(color) );
	      bd += factor * ( (double) sourceColorModel.getBlue(color) );

	      // recombine the colors into as the default rgb color model
	      int a = ((int) Math.round(ad)) & 255;
	      int r = ((int) Math.round(rd)) & 255;
	      int g = ((int) Math.round(gd)) & 255;
	      int b = ((int) Math.round(bd)) & 255;
	      ioutbuffer[outIndex] = (a << 24) | (r << 16) | (g << 8) | b;
	    }
	  }
	}
      }
    }
  }
  /**
   * Fills (creates if needed) an output integer buffer with
   * interpolated and alpha weighted average of surrounding pixels.
   *
   */
  public void averageAlphaWeightedFillBuffer(boolean rgbonly) {
    synchronized (parameterLock) {
      int outLength = outWidth * outHeight;

      // calculate double precision locations and increments
      double[] outTmp = new double[2];
      outTmp[0] = 0; outTmp[1] = 0;
      double[] inYpos = invertMap(outTmp);
      outTmp[0] = 1;
      double[] incX = invertMap(outTmp);
      incX[0] = incX[0] - inYpos[0]; incX[1] = incX[1] - inYpos[1];
      outTmp[0] = 0; outTmp[1] = 1;
      double[] incY = invertMap(outTmp);
      incY[0] = incY[0] - inYpos[0]; incY[1] = incY[1] - inYpos[1];
      double[] inXpos = new double[2];
      // integer locations
      Point out = new Point(0, 0);
      int outIndex = 0;

      Object inarray;
      int intype;
      if(bbuffer != null) {
	inarray = bbuffer;
	intype = CNUTypes.UNSIGNED_BYTE;
      }
      else if(ibuffer != null) {
	inarray = ibuffer;
	intype = CNUTypes.INTEGER;
      }
      else {
	boutbuffer = null;
	ioutbuffer = null;
	return;
      }

      if((! rgbonly) && (sourceColorModel instanceof IndexColorModel)) {
	// averaging index value and output input color model
	produceColorModel = sourceColorModel;
	int outtype;
	Object outarray;
	if(bbuffer != null) {
	  if((boutbuffer == null) || (boutbuffer.length < outLength))
	    boutbuffer = new byte[outLength];
	  outarray = boutbuffer;
	  outtype = CNUTypes.UNSIGNED_BYTE;
	}
	else {
	  if((ioutbuffer == null) || (ioutbuffer.length < outLength))
	    ioutbuffer = new int[outLength];
	  outarray = ioutbuffer;
	  outtype = CNUTypes.INTEGER;
	}

	int transparentPixel =
	  ((IndexColorModel) sourceColorModel).getTransparentPixel();

	for( out.y = 0; out.y < outHeight; out.y++,
	       inYpos[0] += incY[0], inYpos[1] += incY[1] ) {
	  inXpos[0] = inYpos[0]; inXpos[1] = inYpos[1];
	  for( out.x = 0; out.x < outWidth; out.x++, outIndex++,
		 inXpos[0] += incX[0], inXpos[1] += incX[1] ) {
	    if((inXpos[0] < 0) || (inXpos[1] < 0) ||
	       (inXpos[0] >= inWidth) || (inXpos[1] >= inHeight)) {
	      CNUTypes.setArrayValue(0, outarray, outIndex, outtype);
	    }
	    else {
	      int xLesserIndex = (int) inXpos[0];
	      int xGreaterIndex = xLesserIndex + 1;
	      if( xGreaterIndex >= inWidth ) {
		xGreaterIndex = inWidth - 1;
		if( xLesserIndex >= inWidth ) xLesserIndex = inWidth - 1;
	      }
	      double xGreaterFactor = inXpos[0] - ((double) xLesserIndex);
	      double xLesserFactor = 1.0 - xGreaterFactor;
	      int yLesser = (int) inXpos[1];
	      int yGreater = yLesser + 1;
	      if(yGreater >= inHeight) {
		yGreater = inHeight - 1;
		if(yLesser >= inHeight) yLesser = inHeight - 1;
	      }
	      int yLesserIndex = yLesser * inWidth;
	      int yGreaterIndex = yGreater * inWidth;
	      double yGreaterFactor = inXpos[1] - ((double) yLesser);
	      double yLesserFactor = 1.0 - yGreaterFactor;

	      double factor = yLesserFactor * xLesserFactor;
	      int color = CNUTypes.getArrayValueAsInt(inarray,
						      yLesserIndex + xLesserIndex,
						      intype);
	      double alpha;
	      if(color == transparentPixel) alpha = 0.0;
	      else alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      double total_factor = factor;

	      double newcolor = factor * (double) color;

	      factor = yLesserFactor * xGreaterFactor;
	      color = CNUTypes.getArrayValueAsInt(inarray,
						  yLesserIndex + xGreaterIndex,
						  intype);
	      if(color == transparentPixel) alpha = 0.0;
	      else alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      total_factor += factor;
	      newcolor += factor * (double) color;

	      factor = yGreaterFactor * xLesserFactor;
	      color = CNUTypes.getArrayValueAsInt(inarray,
						  yGreaterIndex + xLesserIndex,
						  intype);
	      if(color == transparentPixel) alpha = 0.0;
	      else alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      total_factor += factor;
	      newcolor += factor * (double) color;

	      factor = yGreaterFactor * xGreaterFactor;
	      color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yGreaterIndex + xGreaterIndex,
					    intype);
	      if(color == transparentPixel) alpha = 0.0;
	      else alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      total_factor += factor;

	      newcolor += factor * (double) color;

	      if(total_factor > 1e-16) newcolor /= total_factor;

	      CNUTypes.setArrayValue(newcolor, outarray, outIndex, outtype);
	    }
	  }
	}
      }
      else {
	// average rgb values and output default rgb color model
	produceColorModel = ColorModel.getRGBdefault();
	boutbuffer = null;
	if((ioutbuffer == null) || (ioutbuffer.length < outLength))
	  ioutbuffer = new int[outLength];

	for( out.y = 0; out.y < outHeight; out.y++,
	       inYpos[0] += incY[0], inYpos[1] += incY[1] ) {
	  inXpos[0] = inYpos[0]; inXpos[1] = inYpos[1];
	  for( out.x = 0; out.x < outWidth; out.x++, outIndex++,
		 inXpos[0] += incX[0], inXpos[1] += incX[1] ) {
	    if((inXpos[0] < 0) || (inXpos[1] < 0) ||
	       (inXpos[0] >= inWidth) || (inXpos[1] >= inHeight)) {
	      ioutbuffer[outIndex] = 0;
	    }
	    else {
	      int xLesserIndex = (int) inXpos[0];
	      int xGreaterIndex = xLesserIndex + 1;
	      if( xGreaterIndex >= inWidth ) {
		xGreaterIndex = inWidth - 1;
		if( xLesserIndex >= inWidth ) xLesserIndex = inWidth - 1;
	      }
	      double xGreaterFactor = inXpos[0] - ((double) xLesserIndex);
	      double xLesserFactor = 1.0 - xGreaterFactor;
	      int yLesser = (int) inXpos[1];
	      int yGreater = yLesser + 1;
	      if(yGreater >= inHeight) {
		yGreater = inHeight - 1;
		if(yLesser >= inHeight) yLesser = inHeight - 1;
	      }
	      int yLesserIndex = yLesser * inWidth;
	      int yGreaterIndex = yGreater * inWidth;
	      double yGreaterFactor = inXpos[1] - ((double) yLesser);
	      double yLesserFactor = 1.0 - yGreaterFactor;

	      int color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yLesserIndex + xLesserIndex,
					    intype);
	      
	      double factor = yLesserFactor * xLesserFactor;
	      double alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      double total_factor = factor;
	      
	      double ad = factor * alpha;
	      double rd = factor * ( (double) sourceColorModel.getRed(color) );
	      double gd = factor * ( (double) sourceColorModel.getGreen(color) );
	      double bd = factor * ( (double) sourceColorModel.getBlue(color) );

	      color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yLesserIndex + xGreaterIndex,
					    intype);

	      factor = yLesserFactor * xGreaterFactor;
	      alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      total_factor += factor;

	      ad += factor * alpha;
	      rd += factor * ( (double) sourceColorModel.getRed(color) );
	      gd += factor * ( (double) sourceColorModel.getGreen(color) );
	      bd += factor * ( (double) sourceColorModel.getBlue(color) );

	      color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yGreaterIndex + xLesserIndex,
					    intype);
	      factor = yGreaterFactor * xLesserFactor;
	      alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      total_factor += factor;

	      ad += factor * alpha;
	      rd += factor * ( (double) sourceColorModel.getRed(color) );
	      gd += factor * ( (double) sourceColorModel.getGreen(color) );
	      bd += factor * ( (double) sourceColorModel.getBlue(color) );

	      color =
		CNUTypes.getArrayValueAsInt(inarray,
					    yGreaterIndex + xGreaterIndex,
					    intype);

	      factor = yGreaterFactor * xGreaterFactor;
	      alpha = (double) sourceColorModel.getAlpha(color);
	      factor *= alpha/255.0;
	      total_factor += factor;

	      ad += factor * alpha;
	      rd += factor * ( (double) sourceColorModel.getRed(color) );
	      gd += factor * ( (double) sourceColorModel.getGreen(color) );
	      bd += factor * ( (double) sourceColorModel.getBlue(color) );

	      if(total_factor > 1e-16) total_factor = 1.0/total_factor;
	      else total_factor = 1.0;

	      // recombine the colors into as the default rgb color model
	      int a = ((int) Math.round(ad * total_factor)) & 255;
	      int r = ((int) Math.round(rd * total_factor)) & 255;
	      int g = ((int) Math.round(gd * total_factor)) & 255;
	      int b = ((int) Math.round(bd * total_factor)) & 255;
	      ioutbuffer[outIndex] = (a << 24) | (r << 16) | (g << 8) | b;
	    }
	  }
	}
      }
    }
  }
  /**
   * Completes one frame of an image - called by image producer.
   *
   * @param	status of image
   */
  public void imageComplete(int status) {
    if(status == ImageConsumer.STATICIMAGEDONE ||
       status == ImageConsumer.SINGLEFRAMEDONE) {
      ImageProducer saveIP = null;
      synchronized (parameterLock) {
        if(status == ImageConsumer.SINGLEFRAMEDONE)
	  hints &= ~ImageConsumer.SINGLEFRAME; // remove single frame hint
        else {
	  hints |= ImageConsumer.SINGLEFRAME; // add single frame hint
	  synchronized (IP_consumerLock) {
	    if(ip != null) {
	      saveIP = ip;
	      ip = null; // no more startproduction calls when buffers are full
	    }
	  }
        }
	switch(sampleType) {
	default:
	case REPLICATE:
	  fillBuffer();
	  break;
	case INTERPOLATE:
	  averageFillBuffer(true);
	  break;
	case ALPHA_WEIGHTED_INTERPOLATE:
	  averageAlphaWeightedFillBuffer(true);
	  break;
	case INDEX_INTERPOLATE:
	  averageFillBuffer(false);
	  break;
	case ALPHA_WEIGHTED_INDEX_INTERPOLATE:
	  averageAlphaWeightedFillBuffer(false);
	  break;
	}
      }
      if(saveIP != null) saveIP.removeConsumer(this);
    }
    produce();
  }
  /**
   * Sends current frame on to image consumers.
   */
  private synchronized void produce() {
    int status = ImageConsumer.SINGLEFRAMEDONE;
    synchronized (parameterLock) {
      if((hints & SINGLEFRAME) != 0) status = ImageConsumer.STATICIMAGEDONE;
      if((outWidth < 1) || (outHeight < 1)) status = ImageConsumer.IMAGEERROR;
      else if(produceColorModel == null) {
	System.out.println("LinearImageFilter.produce() - missing produce color model");
	status = ImageConsumer.IMAGEERROR;
      }
      else if((boutbuffer == null) && (ioutbuffer == null)) {
	System.out.println("LinearImageFilter.produce() - buffers not set");
	status = ImageConsumer.IMAGEERROR;
      }
      synchronized (IP_consumerLock) {
        if(consumer != null)
          if(! consumers.contains(consumer)) consumers.addElement(consumer);
        for(int i=consumers.size()-1; i>=0; i--) {
          consumer = consumers.elementAt(i);
          if(status != ImageConsumer.IMAGEERROR) {
	    consumer.setDimensions(outWidth, outHeight);
	    consumer.setProperties(properties);
	    consumer.setColorModel(produceColorModel);
	    consumer.setHints(hints);
	    if(boutbuffer != null) {
	      consumer.setPixels(0, 0, outWidth, outHeight, produceColorModel,
				 boutbuffer, 0, outWidth);
	    }
	    else if(ioutbuffer != null) {
	      consumer.setPixels(0, 0, outWidth, outHeight, produceColorModel,
				 ioutbuffer, 0, outWidth);
	    }
	    else status = ImageConsumer.IMAGEERROR;
	  }
	  consumer.imageComplete(status);
        }
        if(status == ImageConsumer.STATICIMAGEDONE) {
          consumers.removeAllElements();
          consumer = null;
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
    ImageProducer saveIP = null;
    boolean errorFlag = false;
    synchronized (IP_consumerLock) {
      if(! consumers.contains(ic)) consumers.addElement(ic);
      saveIP = this.ip;
    }
    if(saveIP != null) saveIP.startProduction(this);
    else {
      synchronized (parameterLock) {
        if(ioutbuffer != null || boutbuffer != null) produce();
	else errorFlag = true;
      }
    }
    if(errorFlag) ic.imageComplete(ImageConsumer.IMAGEERROR);
  }
  /**
   * Generates a string representation for the given filter sample type.
   *
   * @param sampleType sample type
   * @return string name for sample type
   */
  public static String sampleTypeToString(int sampleType) {
    for(int i=0; i<SAMPLING_TYPES.length; i++) {
      if(SAMPLING_TYPES[i] == sampleType) return SAMPLING_NAMES[i];
    }
    return "UNKNOWN_SAMPLE_TYPE";
  }
  /**
   * Gets the filter sample data type from a string representation.
   *
   * @param typeString	string representation of a CNU data type
   * @return  sample type or <code>UNKNOWN_SAMPLE_TYPE</code> if invalid string
   */
  public static final int sampleTypeValueOf( String typeString ) {
    if(typeString == null) return UNKNOWN_SAMPLE_TYPE;
    // this allows for stuff like "cnu.filter.FilterSampling.REPLICATE"
    int index = typeString.lastIndexOf(".");
    if(index > 0) typeString = typeString.substring(index);

    for(int i=0; i<SAMPLING_TYPES.length; i++) {
      if(SAMPLING_NAMES[i].equalsIgnoreCase(typeString))
	return SAMPLING_TYPES[i];
    }
    return UNKNOWN_SAMPLE_TYPE;
  }
}
