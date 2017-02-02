package iiv.filter;
import iiv.data.*;
import java.awt.*;
import java.awt.image.*;

/**
 * ShapeColorFilter sits between an ImageProducer and ImageConsumer replacing
 * the color model of the data.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		java.awt.image.ImageFilter
 * @see		java.awt.image.ImageProducer
 * @see		java.awt.image.ImageConsumer
 * @since	iiV1.0
 */
public class ShapeColorFilter extends ImageFilter {
  private Color foregroundColor = Color.black;
  private int rgbReplaceColor;
  private ColorModel iModel = null;
  /**
   * Creates a new instance of ShapeColorFilter.
   *
   * @param foregroundColor	new foreground color
   * @param oldForegroundColor	old foreground color to replace
   */
  public ShapeColorFilter(Color foregroundColor, Color oldForegroundColor) {
    if(foregroundColor != null) this.foregroundColor = foregroundColor;
    if(oldForegroundColor != null)
      rgbReplaceColor = oldForegroundColor.getRGB();
    else rgbReplaceColor = this.foregroundColor.getRGB();
    byte[] reds = new byte[256];
    byte[] greens = new byte[256];
    byte[] blues = new byte[256];
    byte[] alphas = new byte[256];
    byte red = (byte) this.foregroundColor.getRed();
    byte green = (byte) this.foregroundColor.getGreen();
    byte blue = (byte) this.foregroundColor.getBlue(); 
    for(int i=0; i<256; i++) {
      reds[i] = red; greens[i] = green; blues[i] = blue;
      alphas[i] = (byte) i; // index 0 transparent, 255 solid
    }
    iModel = new IndexColorModel(8, 256, reds, greens, blues, alphas);
  }
  /**
   * Called by the image producer to set the color model.
   *
   * @param model	color model that producer plans to mostly use
   */
  public void setColorModel(ColorModel model) {
    super.setColorModel(iModel);
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
    byte transparentPixelValue = 0;
    byte textPixelValue = (byte) 255;
    byte[] tempBuff = new byte[srcW * srcH];
    int sy = srcOff;
    int d = 0;
    for (int y = 0; y < srcH; y++, sy += srcScan) {
      int s = sy;
      for (int x = 0; x < srcW; x++, s++, d++) {
	if(model.getRGB(pixels[s]) == rgbReplaceColor)
	  tempBuff[d] = textPixelValue;
	else tempBuff[d] = transparentPixelValue;
      }
    }
    super.setPixels(srcX, srcY, srcW, srcH, iModel, tempBuff, 0, srcW);
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
    int rgbForegroundColor = Color.black.getRGB(); //foregroundColor.getRGB();
    byte transparentPixelValue = 0;
    byte textPixelValue = (byte) 255;
    byte[] tempBuff = new byte[srcW * srcH];
    int sy = srcOff;
    int d = 0;
    for (int y = 0; y < srcH; y++, sy += srcScan) {
      int s = sy;
      for (int x = 0; x < srcW; x++, s++, d++) {
	// byte converted to positive int prior to lookup because
	// some AWT color models implementations fail to handle negative bytes
	int rgb = model.getRGB(CNUTypes.UnsignedByteToInt(pixels[s]));
	if(rgb == rgbReplaceColor)
	  tempBuff[d] = textPixelValue;
	else tempBuff[d] = transparentPixelValue;
      }
    }
    super.setPixels(srcX, srcY, srcW, srcH, iModel, tempBuff, 0, srcW);
  }
}
