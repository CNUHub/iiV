package iiv.display;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
/**
 * Canvas to display a color map.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		SingleComponentCanvas
 * @see		ColorMapQuiltCanvas
 * @since	iiV1.0
 */
public class ColorMapCanvas extends SingleComponentCanvas {
  private static final long serialVersionUID = 2563198796210078515L;
  /**
   * Construct a new instance of ColorMapCanvas.
   *
   * @param cm	color model to display on the canvas
   */
  public ColorMapCanvas( ColorModel cm ) {
    super(new DisplayColorMap(cm));
    DisplayColorMap dcm = (DisplayColorMap) getDisplayComponent();
    dcm.setZoom(1, 1);
    dcm.setRotation(0);
    dcm.setFlips(false, false);
    dcm.setCrop(null);
    dcm.setFont(getFont());
    dcm.setForeground(getForeground());
    dcm.validate();
    dcm.setSize(dcm.getPreferredSize());
    setPreferredSize(dcm.getSize());
  }
}
