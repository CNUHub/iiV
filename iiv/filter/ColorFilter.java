package iiv.filter;
import java.awt.*;
import java.awt.image.*;
/**
 * ColorFilter sits between an ImageProducer and ImageConsumer replacing
 * the color model of the data.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		java.awt.image.ImageFilter
 * @see		java.awt.image.ImageProducer
 * @see		java.awt.image.ImageConsumer
 * @since	iiV1.0
 */
public class ColorFilter extends ImageFilter {
  ColorModel newModel;
  /**
   * Creates a new instance of ColorFilter.
   *
   * @param newModel	new color model
   */
  public ColorFilter(ColorModel newModel) {
    this.newModel = newModel;
  }
  /**
   * Called by the image producer to set the color model.
   *
   * @param model	color model that producer plans to mostly use
   */
  public void setColorModel(ColorModel model) {
    if( ((newModel instanceof IndexColorModel) &&
	 (model instanceof IndexColorModel)) ||
	((newModel instanceof DirectColorModel) &&
	 (newModel != model)) )
      super.setColorModel(newModel);
    else // leave color model alone
      super.setColorModel(model);
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
    // only change model if using the same type
    if( ((newModel instanceof IndexColorModel) &&
	 (model instanceof IndexColorModel)) )
      super.setPixels(srcX, srcY, srcW, srcH, newModel,
			  pixels, srcOff, srcScan);
    else if(newModel instanceof DirectColorModel &&
	    (newModel != model) ) {
      int redMask = ((DirectColorModel) newModel).getRedMask();
      int redShift = 0;
      while( (redMask & 1) == 0 ) { redMask = redMask >> 1; redShift++; }

      int blueMask = ((DirectColorModel) newModel).getBlueMask();
      int blueShift = 0;
      while( (blueMask & 1) == 0 ) { blueMask = blueMask >> 1; blueShift++; }

      int greenMask = ((DirectColorModel) newModel).getGreenMask();
      int greenShift = 0;
      while( (greenMask & 1) == 0 )
      { greenMask = greenMask >> 1; greenShift++; }

      int alphaMask = ((DirectColorModel) newModel).getAlphaMask();
      int alphaShift = 0;
      while( (alphaMask & 1) == 0 )
      { alphaMask = alphaMask >> 1; alphaShift++; }

      int[] tempBuff = new int[srcW * srcH];
      int sy = srcOff;
      int d = 0;
      for (int y = 0; y < srcH; y++, sy += srcScan) {
	int s = sy;
	for (int x = 0; x < srcW; x++, s++, d++) {
	  int color = model.getRed(pixels[s]);
	  tempBuff[d] = (color & redMask) << redShift;
	  color = model.getBlue(pixels[s]);
	  tempBuff[d] |= (color & blueMask) << blueShift;
	  color = model.getGreen(pixels[s]);
	  tempBuff[d] |= (color & greenMask) << greenShift;
	  color = model.getAlpha(pixels[s]);
	  tempBuff[d] |= (color & alphaMask) << alphaShift;
	  tempBuff[d] = pixels[s];
        }
      }
      super.setPixels(srcX, srcY, srcW, srcH, model, tempBuff, 0, srcW);
    }
    else // leave color model alone
      super.setPixels(srcX, srcY, srcW, srcH, model, pixels, srcOff, srcScan);
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
    if( ((newModel instanceof IndexColorModel) &&
	 (model instanceof IndexColorModel)) )
	super.setPixels(srcX, srcY, srcW, srcH, newModel,
			  pixels, srcOff, srcScan);
    else // leave color model alone
      super.setPixels(srcX, srcY, srcW, srcH, model, pixels, srcOff, srcScan);
  }
}
