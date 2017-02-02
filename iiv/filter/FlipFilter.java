package iiv.filter;
import java.awt.*;
import java.awt.image.*;
import java.util.*;
/**
 * FlipFilter sits between an ImageProducer and ImageConsumer swapping
 * pixels horizontally or vertically.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		java.awt.image.ImageFilter
 * @see		java.awt.image.ImageProducer
 * @see		java.awt.image.ImageConsumer
 * @since	iiV1.0
 */
public class FlipFilter extends ImageFilter
  implements ImageProducer, Mapping2D {
  private Vector<ImageConsumer> consumers = new Vector<ImageConsumer>();
  private ImageProducer ip = null;
  private int lwidth, lheight;
  private boolean flipH, flipV;

  /**
   * Sets the flip flags.
   *
   * @param flipV	<code>true</code> to flip pixels vertically
   * @param flipH	<code>true</code> to flip pixels horizontally
   */
  public synchronized void setFlip(boolean flipV, boolean flipH) {
      this.flipV = flipV;
      this.flipH = flipH;
  }
  /**
   * Maps a precise prefilter location to a precise filtered location.
   *
   * @param in	in pixel location
   * @param out	written over with out pixel location
   */
  public synchronized void map(double[] in, double[] out) {
    if(flipH) out[0] = lwidth - 1 - in[0];
    else out[0] = in[0];
    if(flipV) out[1] = lheight - 1 - in[1];
    else out[1] = in[1];
  }
  /**
   * Maps a prefilter point to the nearest filtered point.
   *
   * @param inPt	prefilter point
   * @param outPt	written over with nearest filtered point
   */
  public synchronized void map(Point inPt, Point outPt) {
    if(flipH) outPt.x = lwidth - 1 - inPt.x;
    else outPt.x = inPt.x;
    if(flipV) outPt.y = lheight - 1 - inPt.y;
    else outPt.y = inPt.y;
  }
  /**
   * Inverts mapping - map a precise filtered location to the precise
   * prefilter location.
   *
   * @param in	filtered location
   * @param out	written over by prefilter location
   */
  public synchronized void invertMap(double[] in, double[] out) {
    map(in, out);
  }
  /**
   * Inverts mapping - map a filtered point location to the nearest
   * prefilter point location
   *
   * @param inPt	filtered location
   * @param outPt	written over by prefilter location
   */
  public synchronized void invertMap(Point inPt, Point outPt) {
    map(inPt, outPt);
  }
  /**
   * Sets the image producer.
   *
   * @param ip	image producer
   */
  public synchronized void setImageProducer(ImageProducer ip) {
     this.ip = ip;
  }
  /**
   * Sets the image dimensions - called by the image producer.
   *
   * @param w	image width
   * @param h	image height
   */
  public synchronized void setDimensions(int w, int h) {
    lwidth = w;
    lheight = h;
    if(consumer != null) super.setDimensions(w, h);
    else {
      for(int i=consumers.size()-1; i>=0; i--) {
        consumer = consumers.elementAt(i);
        super.setDimensions(w, h);
      }
      consumer = null;
    }
  }
  /**
   * Sets the image properties - called by the image producer.
   *
   * @param in_props	image properties
   */
  public synchronized void setProperties(Hashtable<?,?> in_props) {
    Hashtable<Object, Object> props;
    if(in_props == null) props = new Hashtable<Object,Object>();
    else props = new Hashtable<Object,Object>(in_props);
    boolean v = false;
    boolean h = false;
    Object bv = props.get("flipV");
    Object bh = props.get("flipH");
    if (bv != null) v = ((Boolean)bv).booleanValue();
    if (bh != null) h = ((Boolean)bh).booleanValue();
    v ^= flipV;
    h ^= flipH;
    props.put("flipV", new Boolean(v));
    props.put("flipH", new Boolean(h));
    if(consumer != null) consumer.setProperties(props);
    else {
      for(int i=consumers.size()-1; i>=0; i--) {
        consumer = consumers.elementAt(i);
        consumer.setProperties(props);
      }
      consumer = null;
    }
  }
  /**
   * Sets the image hints - called by the image producer.
   *
   * @param h	hints
   */
  public synchronized void setHints(int h) {
    if(consumer != null) super.setHints(h);
    else {
      for(int i=consumers.size()-1; i>=0; i--) {
        consumer = consumers.elementAt(i);
        super.setHints(h);
      }
      consumer = null;
    }
  }
  /**
   * Called by the image producer to set the color model.
   *
   * @param model	color model that producer plans to mostly use
   */
  public synchronized void setColorModel(ColorModel model) {
    if(consumer != null) super.setColorModel(model);
    else {
      for(int i=consumers.size()-1; i>=0; i--) {
        consumer = consumers.elementAt(i);
        super.setColorModel(model);
      }
      consumer = null;
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
  public synchronized void setPixels(int srcX, int srcY, int srcW, int srcH,
        ColorModel model, byte pixels[], int srcOff, int srcScan) {
        byte[] tempBuff = new byte[srcW * srcH];

    int sy = srcOff;
    int outY = 0;
    int outYInc = srcW;
    if(flipV) { outY = (srcH - 1) * srcW; outYInc = -outYInc; }
    int dxInc = 1;
    if(flipH) {
      dxInc = -1;
      outY += (srcW - 1);
    }
    for(int yIn = 0; yIn < srcH; yIn++, sy += srcScan, outY += outYInc) {
      int s = sy;
      int d = outY;
      for(int xIn = 0; xIn < srcW; xIn++, s++, d += dxInc)
	tempBuff[d] = pixels[s];
    }
    if (flipH) srcX = lwidth - (srcX+srcW);
    if (flipV) srcY = lheight - (srcY+srcH);

    if(consumer != null)
      super.setPixels(srcX, srcY, srcW, srcH, model, tempBuff, 0, srcW);
    else {
      for(int i=consumers.size()-1; i>=0; i--) {
        consumer = consumers.elementAt(i);
        super.setPixels(srcX, srcY, srcW, srcH, model, tempBuff, 0, srcW);
      }
      consumer = null;
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
  public synchronized void setPixels(int srcX, int srcY, int srcW, int srcH,
          ColorModel model, int pixels[], int srcOff, int srcScan) {
    int[] tempBuff = new int[srcW * srcH];
    int sy = srcOff;
    int outY = 0;
    int outYInc = srcW;
    if(flipV) { outY = (srcH - 1) * srcW; outYInc = -outYInc; }
    int dxInc = 1;
    if(flipH) {
      dxInc = -1;
      outY += (srcW - 1);
    }
    for(int yIn = 0; yIn < srcH; yIn++, sy += srcScan,
	outY += outYInc) {
      int s = sy;
      int d = outY;
      for(int xIn = 0; xIn < srcW; xIn++, s++, d += dxInc)
         tempBuff[d] = pixels[s];
    }
    if (flipH) srcX = lwidth - (srcX+srcW);
    if (flipV) srcY = lheight - (srcY+srcH);

    if(consumer != null)
      super.setPixels(srcX, srcY, srcW, srcH, model, tempBuff, 0, srcW);
    else {
      for(int i=consumers.size()-1; i>=0; i--) {
        consumer = consumers.elementAt(i);
        super.setPixels(srcX, srcY, srcW, srcH, model, tempBuff, 0, srcW);
      }
      consumer = null;
    }
  }
  /**
   * Completes one frame of an image - called by image producer.
   *
   * @param status	status of image completion from producer
   */
  public synchronized void imageComplete(int status) {
    if(consumer != null)
      super.imageComplete(status);
    else {
      for(int i=consumers.size()-1; i>=0; i--) {
        consumer = consumers.elementAt(i);
        super.imageComplete(status);
      }
      if(status == STATICIMAGEDONE) {
        // remove all image consumers
	consumers.removeAllElements();
      }
      consumer = null;
    }
  }
  /**
   * Registers an image consumer with this producer.
   *
   * @param ic	image consumer
   */
  public synchronized void addConsumer(ImageConsumer ic) {
    if(! consumers.contains(ic)) consumers.addElement(ic);
  }
  /**
   * Determines if an image consumer is registered.
   *
   * @param ic	image consumer
   * @return	<code>true</code> if image consumer is registered
   */
  public synchronized boolean isConsumer(ImageConsumer ic) {
    return consumers.contains(ic);
  }
  /**
   * Removes a registered image consumer.
   *
   * @param ic	image consumer to remove
   */
  public synchronized void removeConsumer(ImageConsumer ic) {
    consumers.removeElement(ic);
  }
  /**
   * Requests pixel data be resent in left to right, top to bottom order.
   *
   * @param ic	image consumer making the request
   */
  public void requestTopDownLeftRightResend(ImageConsumer ic) {
    startProduction(ic);
  }
  /**
   * Triggers the delivery of image data.
   *
   * @param ic	image consumer wanting data to be delivered
   */
  public synchronized void startProduction(ImageConsumer ic) {
    if(ip == null) ic.imageComplete(ImageConsumer.IMAGEERROR);
    if(! consumers.contains(ic)) consumers.addElement(ic);
    ip.startProduction(this);
  }
}
