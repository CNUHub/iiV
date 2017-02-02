package iiv.display;
import iiv.*;
import iiv.filter.*;
import iiv.data.*;
import iiv.gui.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * DisplayComponentDefaults defines default values used when
 * creating display components.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUViewer
 * @see		DisplayComponent
 * @since	iiV 1.132
 */
public class DisplayComponentDefaults {
  static private Object defaultLock = new Object();
  static private Vector<Object[]> colorModelListeners = new Vector<Object[]>();
  static private ColorModel defaultColorModel = CNUColorModel.getGreyColorModel();
  static private int defaultFilterSampleType = FilterSampling.REPLICATE;
  static private ButtonModel zoomStateButtonModel = null;
  static private double defaultZoomV = 1;
  static private double defaultZoomH = 1;
  static private ButtonModel rotateStateButtonModel = null;
  static private double defaultRotation = 0;
  static private ButtonModel flipVStateButtonModel = null;
  static private ButtonModel flipHStateButtonModel = null;
  static private Font defaultFont = null;
  static private Color defaultForeground = null;

  static public final String limitIDimTipText =
    "<html><font size=2>When checked the number of slices for newly displayed images is<p> limited by the i dimension range set in the Limits/Crop dialog</font>";
  static private ButtonModel iDimLimitButtonModel = null;
  static private DefaultBoundedRangeModel firstIDimModel =
      new DefaultBoundedRangeModel(0, 0, 0, 127);
  static private DefaultBoundedRangeModel lastIDimModel =
      new DefaultBoundedRangeModel(127, 0, 0, 127);
  static private ButtonModel limitSlicesButtonModel = null;

  static public final String limitSlicesTipText =
    "<html><font size=2>When checked the number of slices for newly displayed images is<p> limited by the slice range set in the Limits/Crop dialog</font>";
  static private ButtonModel sliceLimitButtonModel = null;
  static private DefaultBoundedRangeModel firstFrameModel =
      new DefaultBoundedRangeModel(0, 0, 0, 127);
  static private DefaultBoundedRangeModel lastFrameModel =
      new DefaultBoundedRangeModel(127, 0, 0, 127);

  static private ButtonModel cropStateButtonModel = null;
  static private Rectangle defaultCropBox = null;
  static private DefaultBoundedRangeModel xBegModel =
      new DefaultBoundedRangeModel(0, 0, 0, 127);
  static private DefaultBoundedRangeModel xEndModel =
      new DefaultBoundedRangeModel(127, 0, 0, 127);
  static private DefaultBoundedRangeModel yBegModel =
      new DefaultBoundedRangeModel(0, 0, 0, 127);
  static private DefaultBoundedRangeModel yEndModel =
      new DefaultBoundedRangeModel(127, 0, 0, 127);

  static public String[] sliceViewChoices = { "Transverse",
					      "Coronal", "Sagittal" };
  static private DefaultComboBoxModel sliceViewChoiceModel =
      new DefaultComboBoxModel(sliceViewChoices);
  static public String sliceViewChoiceTipText =
    "Pull to select the view mode for newly displayed images";

  static public String[] scaleChoices = { "Default Scale",
			    "Positive Scale",
			    "Negative Scale",
			    "Last Scale" };
  static private DefaultComboBoxModel scaleModeChoiceModel =
      new DefaultComboBoxModel(scaleChoices);
  static public String scaleChoiceTipText =
    "Pull to select the scale option for newly displayed images";
  static private AutoExtendBoundedRangeModel xGotoLocationModel = new AutoExtendBoundedRangeModel(0, 0, 0, 127);
  static private AutoExtendBoundedRangeModel yGotoLocationModel = new AutoExtendBoundedRangeModel(0, 0, 0, 127);
  static private AutoExtendBoundedRangeModel zGotoLocationModel = new AutoExtendBoundedRangeModel(0, 0, 0, 127);
  /**
   * Disallows creation of instances of this class.
   */
  public DisplayComponentDefaults() {}
  /**
   * Retrieve the slice view choice model.
   *
   * @return slice view choice model
   */
  public static DefaultComboBoxModel getSliceViewChoiceModel() {
      return sliceViewChoiceModel;
  }
  /**
   * Sets the slice view mode.
   *
   * @param viewMode	CNUDimensions.TRANSVERSE, CNUDimensions.CORONAL or
   *			CNUDimensions.SAGITTAL
   */
  public static void setSliceViewMode( final int viewMode ) {
    int index = 0;
    switch(viewMode) {
    case CNUDimensions.TRANSVERSE:
    default:
	break;
    case CNUDimensions.CORONAL:
	index = 1;
	break;
    case CNUDimensions.SAGITTAL:
	index = 2;
	break;
    }
    getSliceViewChoiceModel().setSelectedItem(sliceViewChoices[index]);
  }
  /**
   * Gets the slice view mode.
   *
   * @return	CNUDimensions.TRANSVERSE, CNUDimensions.CORONAL or
   *		CNUDimensions.SAGITTAL
   */
  public static int getSliceViewMode() {
      DefaultComboBoxModel dcbm = getSliceViewChoiceModel();
      switch(dcbm.getIndexOf(dcbm.getSelectedItem())) {
      case 0:
      default:
	return CNUDimensions.TRANSVERSE;
      case 1:
	return CNUDimensions.CORONAL;
      case 2:
	return CNUDimensions.SAGITTAL;
      }
  }
  /**
   * Sets the scaling option.
   *
   * @param scaleOption one of DEFAULT_SCALING, LAST_SCALING,
   *			POSITIVE_SCALING or NEGATIVE_SCALING
   */
  static public void setScaleMode(final int scaleOption) {
      int index = 0;
      switch (scaleOption) {
      default:
      case CNUViewer.DEFAULT_SCALING:
	index = 0;
        break;
      case CNUViewer.POSITIVE_SCALING:
	index = 1;
        break;
      case CNUViewer.NEGATIVE_SCALING:
	index = 2;
        break;
      case CNUViewer.LAST_SCALING:
	index = 3;
        break;
      }
      getScaleModeChoiceModel().setSelectedItem(scaleChoices[index]);
  }
  /**
   * Gets the current scaling option.
   *
   * @return	 one of DEFAULT_SCALING, LAST_SCALING,
   *		POSITIVE_SCALING or NEGATIVE_SCALING
   */
  static public int getScaleMode() {
      DefaultComboBoxModel dcbm = getScaleModeChoiceModel();
    switch(dcbm.getIndexOf(dcbm.getSelectedItem())) {
    default:
    case 0:
      return CNUViewer.DEFAULT_SCALING;
    case 1:
      return CNUViewer.POSITIVE_SCALING;
    case 2:
      return CNUViewer.NEGATIVE_SCALING;
    case 3:
      return CNUViewer.LAST_SCALING;
    }
  }
  /**
   * Retrieve the scale mode choice model.
   *
   * @return scale mode choice model
   */
  public static DefaultComboBoxModel getScaleModeChoiceModel() {
      return scaleModeChoiceModel;
  }
  /**
   *  Adds an object whose setColorModel method will be called
   *  when the default color model changes.
   *
   *  @param obj object to add to list
   */
  public static void addDefaultColorModelListener(Object obj) {
    if(obj == null) return;
    try {
      Method method = obj.getClass().getMethod("setColorModel",
					       new Class[] {ColorModel.class});
      synchronized(colorModelListeners) {
	colorModelListeners.addElement(new Object[] {obj, method});
      }
    } catch (NoSuchMethodException nsme) {
	System.out.println("in DisplayComponentDefaults.addToDefaultColorModelListener Object="
			   + obj + " method setColorModel not found");
    }
  }
  /**
   * Sets the default color model.
   *
   * @param cm	non <code>null</code> color model to set as default
   */
  public static void setDefaultColorModel(ColorModel cm) {
    if(cm != null) {
      if(! cm.equals(defaultColorModel) ) {
	defaultColorModel = cm;
	Object[] params = new Object[] {cm};
	Object[] objectAndMethods = null;
	synchronized(colorModelListeners) {
	  objectAndMethods = new Object[colorModelListeners.size()];
	  colorModelListeners.copyInto(objectAndMethods);
	}
	for(int i=0; i<objectAndMethods.length; i++) {
	  Object obj[] = (Object[]) objectAndMethods[i];
	  try {
	    ((Method) obj[1]).invoke(obj[0], params);
	  } catch (SecurityException se) { // ignore
	  } catch (IllegalAccessException iae) { // ignore
	  } catch (IllegalArgumentException iarge) { // ignore
	  } catch (InvocationTargetException ite) { // ignore
	  } catch (ClassCastException cce) { // ignore
	  }
	}
      }
    }
  }
  /**
   * Gets the default color model.
   *
   * @return	the default color model
   */
  public static ColorModel getDefaultColorModel() {
    return defaultColorModel;
  }
  /**
   * Sets the transparent color index on the current default color model.
   * Ignored if default color model not an IndexColorModel.
   *
   * @param trans	transparent color index
   */
  public static void setDefaultTransparentColor( int trans ) {
    ColorModel cm = getDefaultColorModel();
    if(cm instanceof IndexColorModel) {
      setDefaultColorModel(
        CNUColorModel.getTransparentColorModel((IndexColorModel)cm, trans));
    }
  }
  /**
   * Gets the default transparent color.
   *
   * @return	index to transparent color or -1 if not set
   */
  public static int getDefaultTransparentColor() {
    ColorModel cm = getDefaultColorModel();
    if(cm instanceof IndexColorModel)
      return ((IndexColorModel) cm).getTransparentPixel();
    else return -1;
  }
  /**
   * Sets the default filter sample type.
   *
   * @param fst	filter sample type
   */
  public static void setDefaultFilterSampleType(int fst) {
    defaultFilterSampleType = fst;
  }
  /**
   * Gets the default filter sample type.
   *
   * @return default filter sample type
   */
  public static int getDefaultFilterSampleType() {
    return defaultFilterSampleType;
  }
 /**
  * Get the zoom state button model.
  *
  * @return zoom state button model.
  */
  public static ButtonModel getZoomStateButtonModel() {
    if(zoomStateButtonModel == null) synchronized (defaultLock) {
      if(zoomStateButtonModel == null) {
	zoomStateButtonModel = new JCheckBox().getModel();
	zoomStateButtonModel.setSelected(false);
      }
    }
    return zoomStateButtonModel;
  }
  /**
   * Sets the default zoom state.
   *
   * @param state	<code>true</code> to zoom by default
   */
  public static void setDefaultZoomState(boolean state) {
    ButtonModel bm = getZoomStateButtonModel();
    if(bm.isSelected() != state) bm.setSelected(state);
  }
  /**
   * Gets the default zoom box state.
   *
   * @return	<code>true</code> to zoom by default
   */
  public static boolean getDefaultZoomState() {
    return getZoomStateButtonModel().isSelected();
  }
  /**
   * Sets the default vertical zoom.
   *
   * @param zoom new default vertical zoom
   */
  public static void setDefaultZoomV(double zoom) {
    if((zoom < 1e-3) || (zoom > 1e3) || (Math.abs(zoom - 1) < 1e-3))
      defaultZoomV = 1;
    else defaultZoomV = zoom;
  }
  /**
   * Gets the default vertical zoom.
   *
   * @return	current default vertical zoom
   */
  public static double getDefaultZoomV() {
    return defaultZoomV;
  }
  /**
   * Sets the default horizontal zoom.
   *
   * @param zoom	new default horizontal zoom
   */
  public static void setDefaultZoomH(double zoom) {
    if((zoom < 1e-3) || (zoom > 1e3) || (Math.abs(zoom - 1) < 1e-3))
      defaultZoomH = 1;
    else defaultZoomH = zoom;
  }
  /**
   * Gets the default horizontal zoom.
   *
   * @return	current default horizontal zoom
   */
  public static double getDefaultZoomH() {
    return defaultZoomH;
  }
 /**
  * Get the rotation state button model.
  *
  * @return rotation state button model.
  */
  public static ButtonModel getRotateStateButtonModel() {
    if(rotateStateButtonModel == null) synchronized (defaultLock) {
      if(rotateStateButtonModel == null) {
	rotateStateButtonModel = new JCheckBox().getModel();
	rotateStateButtonModel.setSelected(false);
      }
    }
    return rotateStateButtonModel;
  }
  /**
   * Sets the default rotation state.
   *
   * @param state	<code>true</code> to rotate by default
   */
  public static void setDefaultRotateState(boolean state) {
    ButtonModel bm = getRotateStateButtonModel();
    if(bm.isSelected() != state) bm.setSelected(state);
  }
  /**
   * Gets the default rotation state.
   *
   * @return	<code>true</code> to rotate by default
   */
  public static boolean getDefaultRotateState() {
    return getRotateStateButtonModel().isSelected();
  }
  /**
   * Sets the default rotation angle.
   *
   * @param angle	new default rotation angle (degrees)
   */
  public static void setDefaultRotation(double angle) {
    defaultRotation = angle;
  }
  /**
   * Gets the default rotation angle.
   *
   * @return	current default rotation angle (degrees)
   */
  public static double getDefaultRotation() {
    return defaultRotation;
  }
 /**
  * Get the vertical flip state button model.
  *
  * @return vertical flip state button model.
  */
  public static ButtonModel getFlipVStateButtonModel() {
    if(flipVStateButtonModel == null) synchronized (defaultLock) {
      if(flipVStateButtonModel == null) {
	flipVStateButtonModel = new JCheckBox().getModel();
	flipVStateButtonModel.setSelected(false);
      }
    }
    return flipVStateButtonModel;
  }
  /**
   * Sets the default vertical flip mode.
   *
   * @param flipV	<code>true</code> to flip vertically by default
   */
  public static void setDefaultFlipV(boolean flipV) {
    ButtonModel bm = getFlipVStateButtonModel();
    if(bm.isSelected() != flipV) bm.setSelected(flipV);
  }
  /**
   * Gets the default vertical flip mode.
   *
   * @return	<code>true</code> if defaults to flip vertical
   */
  public static boolean getDefaultFlipV() {
    return getFlipVStateButtonModel().isSelected();
  }
 /**
  * Get the horizontal flip state button model.
  *
  * @return horizontal flip state button model.
  */
  public static ButtonModel getFlipHStateButtonModel() {
    if(flipHStateButtonModel == null) synchronized (defaultLock) {
      if(flipHStateButtonModel == null) {
	flipHStateButtonModel = new JCheckBox().getModel();
	flipHStateButtonModel.setSelected(false);
      }
    }
    return flipHStateButtonModel;
  }
  /**
   * Sets the default horizontal flip mode.
   *
   * @param flipH	<code>true</code> to flip horizontally by default
   */
  public static void setDefaultFlipH(boolean flipH) {
    ButtonModel bm = getFlipHStateButtonModel();
    if(bm.isSelected() != flipH) bm.setSelected(flipH);
  }
  /**
   * Gets the default horizontal flip mode.
   *
   * @return	<code>true</code> if defaults to flip horizontal
   */
  public static boolean getDefaultFlipH() {
    return getFlipHStateButtonModel().isSelected();
  }
  /**
   * Sets the default font.
   *
   * @param font	new default font
   */
  public static void setDefaultFont(Font font) {
    defaultFont = font;
  }
  /**
   * Gets the default font.
   *
   * @return	current default font
   */
  public static Font getDefaultFont() {
    return defaultFont;
  }
  /**
   * Sets the default foreground color.
   *
   * @param foreground	new default foreground color
   */
  public static void setDefaultForeground(Color foreground) {
    defaultForeground = foreground;
  }
  /**
   * Gets the default foreground color.
   *
   * @return	current default foreground color
   */
  public static Color getDefaultForeground() {
    return defaultForeground;
  }
  /**
   * Sets the i dimension limits.
   *
   * @param firstIDim	first i dimension
   * @param lastIDim	last i dimension
   * @param nIDims	number of i dimensions
   */
  public static void setDefaultIRange(int firstIDim,
				      int lastIDim, int nIDims) {
    if(firstIDim < 0) firstIDim = firstIDimModel.getValue();
    if(lastIDim < 0) lastIDim = lastIDimModel.getValue();

    int max = Math.max(firstIDimModel.getMaximum(), nIDims);
    max = Math.max(max, firstIDim + 1);
    max = Math.max(max, lastIDim + 1);

    firstIDimModel.setMaximum(max);
    lastIDimModel.setMaximum(max);
    firstIDimModel.setValue(firstIDim);
    lastIDimModel.setValue(lastIDim);
  }
  /**
   * Get the first i dimension limit.
   *
   * @return	first i dimension limit
   */
  public static int getDefaultFirstIDimLimit() {
      return firstIDimModel.getValue();
  }
  /**
   * Get the last i dimension limit.
   *
   * @return	last i dimension limit
   */
  public static int getDefaultLastIDimLimit() {
      return lastIDimModel.getValue();
  }
 /**
  * Get the default first idim range bound model.
  *
  * @return first idim begin range bound model
  */
  public static BoundedRangeModel getDefaultFirstIDimRangeModel() {
    return firstIDimModel;
  }
 /**
  * Get the default last idim range bound model.
  *
  * @return last idim begin range bound model
  */
  public static BoundedRangeModel getDefaultLastIDimRangeModel() {
    return lastIDimModel;
  }
  /**
   * Sets the default slice limits.
   *
   * @param firstSlice	first slice
   * @param lastSlice	last slice
   * @param nSlices	number of slices
   */
  public static void setDefaultSlices(int firstSlice,
			       int lastSlice, int nSlices) {
    if(firstSlice < 0) firstSlice = firstFrameModel.getValue();
    if(lastSlice < 0) lastSlice = lastFrameModel.getValue();
    int max = firstFrameModel.getMaximum();
    max = Math.max(max, nSlices);
    max = Math.max(max, firstSlice + 1);
    max = Math.max(max, lastSlice + 1);

    firstFrameModel.setMaximum(max);
    lastFrameModel.setMaximum(max);
    firstFrameModel.setValue(firstSlice);
    lastFrameModel.setValue(lastSlice);
  }
  /**
   * Gets the default first slice number.
   *
   * @return first slice
   */
  public static int getDefaultFirstSliceLimit() {
    return firstFrameModel.getValue();
  }
  /**
   * Gets the default last slice number.
   *
   * @return last slice
   */
  public static int getDefaultLastSliceLimit() {
    return lastFrameModel.getValue();
  }
 /**
  * Get the crop state button model.
  *
  * @return crop state button model.
  */
  public static ButtonModel getCropStateButtonModel() {
    if(cropStateButtonModel == null) synchronized (defaultLock) {
      if(cropStateButtonModel == null) {
	cropStateButtonModel = new JCheckBox().getModel();
	cropStateButtonModel.setSelected(false);
      }
    }
    return cropStateButtonModel;
  }
  /**
   * Sets the default crop box state.
   *
   * @param state	<code>true</code> to crop by default
   */
  public static void setDefaultCropState(boolean state) {
    ButtonModel bm = getCropStateButtonModel();
    if(bm.isSelected() != state) bm.setSelected(state);
  }
  /**
   * Gets the default crop box state.
   *
   * @return	<code>true</code> to crop by default
   */
  public static boolean getDefaultCropState() {
    return getCropStateButtonModel().isSelected();
  }
  /**
   * Sets the default crop box.
   *
   * @param xbeg	x begin
   * @param ybeg	y begin
   * @param xend	x end
   * @param yend	y end
   */
  public static void setDefaultCrop(int xbeg, int ybeg, int xend, int yend) {
    int max = xBegModel.getMaximum();
    if(xbeg > max) max = xbeg;
    if(xend > max) max = xend;
    if(ybeg > max) max = ybeg;
    if(yend > max) max = yend;
    if(max > xBegModel.getMaximum()) {
      xBegModel.setMaximum(max); xEndModel.setMaximum(max);
      yBegModel.setMaximum(max); yEndModel.setMaximum(max);
    }
    if(xbeg >= 0) xBegModel.setValue(xbeg);
    if(xend >= 0) xEndModel.setValue(xend);
    if(ybeg >= 0) yBegModel.setValue(ybeg);
    if(yend >= 0) yEndModel.setValue(yend);
  }
  /**
   * Sets the default crop box.
   *
   * @param cropBox	new default crop box
   */
  public static void setDefaultCrop(Rectangle cropBox) {
    if(cropBox != null) {
      setDefaultCrop(cropBox.x, cropBox.y,
		     cropBox.x + cropBox.width - 1,
		     cropBox.y + cropBox.height - 1);
    }
    else setDefaultCrop(0, 0, xBegModel.getMaximum(), yBegModel.getMaximum());
  }
  /**
   * Gets the default crop box.
   *
   * @return	current default crop box
   */
  public static Rectangle getDefaultCrop() {
    int xbeg = xBegModel.getValue(); int xend = xEndModel.getValue();
    if(xend < xbeg) {int tmp = xend; xend = xbeg; xbeg = tmp; }
    int ybeg = yBegModel.getValue(); int yend = yEndModel.getValue();
    if(yend < ybeg) {int tmp = yend; yend = ybeg; ybeg = tmp; }
    Rectangle box = new Rectangle(xbeg, ybeg, xend - xbeg + 1,
				  yend - ybeg + 1);
    return box;
  }
 /**
  * Get the default crob box x begin range bound model.
  *
  * @return x begin range bound model
  */
  public static BoundedRangeModel getDefaultCropXBeginRangeModel() {
    return xBegModel;
  }
 /**
  * Get the default crob box x end range bound model.
  *
  * @return x end range bound model
  */
  public static BoundedRangeModel getDefaultCropXEndRangeModel() {
    return xEndModel;
  }
 /**
  * Get the default crob box y begin range bound model.
  *
  * @return y begin range bound model
  */
  public static BoundedRangeModel getDefaultCropYBeginRangeModel() {
    return yBegModel;
  }
 /**
  * Get the default crob box y end range bound model.
  *
  * @return y end range bound model
  */
  public static BoundedRangeModel getDefaultCropYEndRangeModel() {
    return yEndModel;
  }
 /**
  * Get the limit slices button model.
  *
  * @return limit slices button model.
  */
  public static ButtonModel getSliceLimitButtonModel() {
    if(sliceLimitButtonModel == null) synchronized (defaultLock) {
      if(sliceLimitButtonModel == null) {
	sliceLimitButtonModel = new JCheckBox().getModel();
	sliceLimitButtonModel.setSelected(false);
      }
    }
    return sliceLimitButtonModel;
  }
  /**
   * Sets the slice limit mode displaying it as a Checkbox component
   *
   * @param mode  <code>true</code> if slices should be limited
   * @see	iiv.dialog.CropDialog
   */
  public static void setSliceLimitMode(final boolean mode) {
    ButtonModel bm = getSliceLimitButtonModel();
    if(bm.isSelected() != mode) bm.setSelected(mode);
  }
  /**
   * Gets the slice limit mode.
   * @return	<code>true</code> if slices should be limited
   * @see	iiv.dialog.CropDialog
   */
  public static boolean getSliceLimitMode() {
    return getSliceLimitButtonModel().isSelected();
  }
 /**
  * Get the default first frame range bound model.
  *
  * @return first frame begin range bound model
  */
  public static BoundedRangeModel getDefaultFirstFrameRangeModel() {
    return firstFrameModel;
  }
 /**
  * Get the default last frame range bound model.
  *
  * @return last frame begin range bound model
  */
  public static BoundedRangeModel getDefaultLastFrameRangeModel() {
    return lastFrameModel;
  }
 /**
  * Get the limit idim button model.
  *
  * @return limit idim button model.
  */
  public static ButtonModel getIDimLimitButtonModel() {
    if(iDimLimitButtonModel == null) synchronized (defaultLock) {
      if(iDimLimitButtonModel == null) {
	  //iDimLimitButtonModel = new DefaultButtonModel(); //didn't work why?
	  iDimLimitButtonModel = new JCheckBox().getModel();
	  iDimLimitButtonModel.setSelected(false);
      }
    }
    return iDimLimitButtonModel;
  }
  /**
   * Sets the i dim limit mode displaying it as a Checkbox component
   *
   * @param mode	<code>true</code> if i range should be limited
   * @see	iiv.dialog.CropDialog
   */
  public static void setIDimLimitMode(final boolean mode) {
    ButtonModel bm = getIDimLimitButtonModel();
    if(bm.isSelected() != mode) bm.setSelected(mode);
  }
  /**
   * Gets the i dim limit mode.
   * @return	<code>true</code> if i dimensions should be limited
   * @see	iiv.dialog.CropDialog
   */
  public static boolean getIDimLimitMode() {
    return getIDimLimitButtonModel().isSelected();
  }
 /**
  * Get the default first idim range bound model.
  *
  * @return first idim range bound model
  */
  public static BoundedRangeModel getDefaultFirstIdimRangeModel() {
    return firstIDimModel;
  }
 /**
  * Get the default last idim range bound model.
  *
  * @return last idim range bound model
  */
  public static BoundedRangeModel getDefaultLastIdimRangeModel() {
    return lastIDimModel;
  }
 /**
  * Get the x goto location range bound model.
  *
  * @return x goto location bound model
  */
  public static AutoExtendBoundedRangeModel getXGotoLocationModel() {
    return xGotoLocationModel;
  }
 /**
  * Get the y goto location range bound model.
  *
  * @return y goto location bound model
  */
  public static AutoExtendBoundedRangeModel getYGotoLocationModel() {
    return yGotoLocationModel;
  }
 /**
  * Get the z goto location range bound model.
  *
  * @return z goto location bound model
  */
  public static AutoExtendBoundedRangeModel getZGotoLocationModel() {
    return zGotoLocationModel;
  }
  /*
   * Retrieve the goto location indices
   *
   * @return goto location indices
   */
  public static int[] getGotoLocationIndices() {
    return new int[] {
      xGotoLocationModel.getValue(),
      yGotoLocationModel.getValue(),
      zGotoLocationModel.getValue()
    };
  }
  /*
   * Set the currently displayed indices
   *
   * @param  new indices
   */
  public static void setGotoLocationIndices(int[] indices) {
    int x = 0; int y = 0; int z = 0;
    if(indices != null) {
      if(indices.length > 0) {
	x = indices[0];
	if(indices.length > 1) {
	  y = indices[1];
	  if(indices.length > 2) z = indices[2];
	}
      }
    }
    if(xGotoLocationModel.getValue() != x) xGotoLocationModel.setValue(x);
    if(yGotoLocationModel.getValue() != y) yGotoLocationModel.setValue(y);
    if(zGotoLocationModel.getValue() != z) zGotoLocationModel.setValue(z);
  }

} // end DisplayComponentDefaults class
