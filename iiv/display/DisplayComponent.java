package iiv.display;
import iiv.filter.*;
import java.awt.*;
import java.awt.image.*;
import iiv.data.*;
import iiv.util.*;
import iiv.io.*;
import iiv.dialog.*;
import iiv.script.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

/**
 * DisplayComponent defines standard routines specific to a components
 * displayed by iiV.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @since	iiV1.0
 */
public abstract class DisplayComponent extends JComponent
implements ShowStatus, CNUFileObject,
  Zoomable, Rotatable, FilterSampling, Flippable, Croppable,
  LocationMapping, ShowPointImage {

  private Object stateParameterLock = new Object();
  private boolean valid = false;
  private boolean filtersValid = false;
  private ColorModel cm = null;
  private boolean grabColorModel = false;
  private boolean colorFilterNeeded = false;
  private double zoomV = 1;
  private double zoomH = 1;
  private double rotation=0;
  private int filterSampleType = FilterSampling.REPLICATE;
  private LinearImageFilter linearFilter = null;
  private boolean flipV = false;
  private boolean flipH = false;
  private FlipFilter flipFilter = null;
  private Font font = null;
  private Rectangle cropBox = null;
  private boolean filterChanged = false;

  private Dimension preferredSize = new Dimension(0, 0);

  private int[][] trackingCrosshair = null;
  private Color trackingCrosshairColor = Color.red;
  private boolean trackingCrosshairDashed = false;

  private int[] fixedCrosshairIndices = null;
  private int[][] fixedCrosshair = null;
    //  private int fixedCrosshairType = ShowPointImage.NO_CROSSHAIR;
  private Color fixedCrosshairColor = Color.red;
  private boolean fixedCrosshairDashed = false;

  private Object imageLock = new Object();
  private ImageProducer ip = null;
  private Dimension ipSize = new Dimension(0, 0);
  private Image image = null;
  private int[] grabbedPixels = null;
  private boolean pixelsGrabbed = false;
  private Rectangle displayImageBounds = new Rectangle();
  private ShowStatus showStatusParent = null;
  /**
   * Constructs a new instance of DisplayComponent with all default settings.
   */
  public DisplayComponent() {
    initColorModel(DisplayComponentDefaults.getDefaultColorModel());
    if(DisplayComponentDefaults.getDefaultZoomState()) {
      zoomV = DisplayComponentDefaults.getDefaultZoomV();
      zoomH = DisplayComponentDefaults.getDefaultZoomH();
    }
    if(DisplayComponentDefaults.getDefaultRotateState())
      rotation = DisplayComponentDefaults.getDefaultRotation();
    filterSampleType = DisplayComponentDefaults.getDefaultFilterSampleType();
    flipV = DisplayComponentDefaults.getDefaultFlipV();
    flipH = DisplayComponentDefaults.getDefaultFlipH();
    font = DisplayComponentDefaults.getDefaultFont();
    setForeground(DisplayComponentDefaults.getDefaultForeground());
    if(DisplayComponentDefaults.getDefaultCropState())
      initCrop(DisplayComponentDefaults.getDefaultCrop());
  }
  /**
   * Throws exception to disallow cloning.
   *
   * @exception CloneNotSupportedException thrown to disallow cloning
   */
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException("DisplayComponent not directly cloneable");
  }
  /**
   * Validates this component.
   */
  public void validate() {
    if(! isValid()) {
      super.validate();
      // moved validateFilters and updateSize calls to doLayout
      // because this routine was getting bypassed with jdk1.4
      doLayout();
    }
  }
  /**
   * Checks if the image filters are valid.
   *
   * @return	<code>true</code> if filters are valid
   */
  public boolean isValidFilters() {
    synchronized (stateParameterLock) {
      return filtersValid;
    }
  }
  /**
   * Validates filters.
   */
  public void validateFilters() {
    synchronized (stateParameterLock) {
      if(! filtersValid) updateFilters();
      filtersValid = true;
    }
  }
  /**
   * Invalidate filters.
   */
  public void invalidateFilters() {
    synchronized (stateParameterLock) {
      filtersValid = false;
    }
    invalidate();
  }
  /**
   * Sets the display image.
   *
   * @param image	image this component displays
   */
  public void setImage( Image image ) {
    synchronized (imageLock) {
      this.image = image;
      ip = null;
      pixelsGrabbed = false;
      if(image != null) setImageProducer(image.getSource());
    }
  }
  /**
   * Gets the display image.
   *
   * @return	image this component displays which may be <code>null</code>
   */
  public Image getImage() {
    synchronized (imageLock) {
      return(image);
    }
  }
  /**
   * Sets the image producer.
   */
  public void setImageProducer( ImageProducer ip ) {
    synchronized (imageLock) {
      this.ip = ip;
      updateIpSize();
    }
  }
  /**
   * Gets the image producer.
   *
   * @return	the image producer which may be <code>null</code>
   */
  public ImageProducer getImageProducer() {
    synchronized (imageLock) {
      return ip;
    }
  }
  /**
   * Gets the show status parent.
   *
   * return the show status parent which may be <code>null</code>
   */
  public ShowStatus getShowStatusParent() {
    if(showStatusParent == null) {
      if(showStatusParent == null) {
        Component p = getParent();
        if(p != null) {
	  synchronized (p.getTreeLock()) {
            while( p != null) {
              if(p instanceof ShowStatus) break;
              p = p.getParent();
            }
            showStatusParent = (ShowStatus) p;
          }
        }
      }
    }
    return showStatusParent;
  }
  /**
   * Shows status and error messages.
   *
   * @param s	message to show
   */
  public void showStatus(String s) {
    ShowStatus ss = getShowStatusParent();
    if(ss != null) ss.showStatus(s);
    else System.out.println(s);
  }
  /** 
   * Shows throwable objects.
   *
   * @param t	throwable object to show trace of
   */
  public void showStatus(Throwable t) {
    ShowStatus ss = getShowStatusParent();
    if(ss != null) ss.showStatus(t);
    else t.printStackTrace();
  }
  /**
   * Sets filter changed flag.
   */
  public void setFilterChanged() {
    filterChanged = true;
  }
  /**
   * Sets the font for displaying labels.
   *
   * @param font	the font
   */
  public void setFont(Font font) {
    synchronized (stateParameterLock) {
      if(font != null) {
	if(! font.equals(this.font) ) {
          this.font = font;
	  super.setFont(font);
	  invalidate();
	}
      }
    }
  }
  /**
   * Gets the font for display labels.
   *
   * @return the font
   */
  public Font getFont() {
    synchronized (stateParameterLock) {
      if(font == null) {
	font = super.getFont();
	if(font == null) {
          font = DisplayComponentDefaults.getDefaultFont();
	  if(font == null) {
	    Component parent = getParent();
	    if(parent != null) font = parent.getFont();
	    if(font == null) {
	      font = new Font("TimesRoman", Font.PLAIN, 14);
	      DisplayComponentDefaults.setDefaultFont(font);
	    }
	  }
        }
      }
      return(font);
    }
  }
  /**
   * Sets independent horizontal and vertical zoom factors.
   *
   * @param zoomV		vertical zoom
   * @param zoomH		horizontal zoom
   */
  public void setZoom(double zoomV, double zoomH) {
    if((zoomV < 1e-3) || (zoomV > 1e3) || (Math.abs(zoomV - 1) < 1e-3))
      zoomV = 1;
    if((zoomH < 1e-3) || (zoomH > 1e3) || (Math.abs(zoomH - 1) < 1e-3))
      zoomH = 1;
    synchronized (stateParameterLock) {
      if((this.zoomH != zoomH) || (this.zoomV != zoomV)) {
	invalidateFilters();
        this.zoomH = zoomH; this.zoomV = zoomV;
      }
    }
  }
  /**
   * Gets the vertical zoom value.
   *
   * @return	vertical zoom
   */
  public double getZoomV() {
    synchronized (stateParameterLock) {
      return(zoomV);
    }
  }
  /**
   * Gets the horizontal zoom value.
   *
   * @return	horizontal zoom
   */
  public double getZoomH() {
    synchronized (stateParameterLock) {
      return(zoomH);
    }
  }
  /**
   * Sets independent horizontal and vertical zoom factors and filter sample type.
   *
   * @param zoomV		vertical zoom
   * @param zoomH		horizontal zoom
   * @param filterSampleType filter sample type
   */
  public void setZoom(double zoomV, double zoomH, int filterSampleType) {
    setZoom(zoomV, zoomH);
    setFilterSampleType(filterSampleType);
  }
  /**
   * Sets the filter sampling type.
   *
   * @param filterSampleType filter sample type
   */
  public void setFilterSampleType( int filterSampleType ) {
    synchronized (stateParameterLock) {
      if(this.filterSampleType != filterSampleType) {
        this.filterSampleType = filterSampleType;
	invalidateFilters();
      }
    }
  }
  /**
   * Gets the filter sampling type.
   *
   * @return filter sample type
   */
  public int getFilterSampleType() {
    synchronized (stateParameterLock) {
      return(filterSampleType);
    }
  }
  /**
   * Sets the rotation angle (degrees).
   *
   * @param rotation		rotation in degrees
   */
  public void setRotation(double rotation) {
    synchronized (stateParameterLock) {
      if(this.rotation != rotation) {
        this.rotation = rotation;
	invalidateFilters();
      }
    }
  }
  /**
   * Gets the rotation angle.
   *
   * @return	rotation angle in degrees
   */
  public double getRotation() {
    synchronized (stateParameterLock) {
      return(rotation);
    }
  }
  /**
   * Sets both flip options.
   *
   * @param flipV	<code>true</code> to flip image vertically
   * @param flipH	<code>true</code> to flip image horizontally
   */
  public void setFlips(boolean flipV, boolean flipH) {
    synchronized (stateParameterLock) {
      if((this.flipV != flipV) || (this.flipH != flipH)) {
        this.flipV = flipV;
        this.flipH = flipH;
	invalidateFilters();
      }
    }
  }
  /**
   * Gets the vertical flip option.
   *
   * @return	<code>true</code> if image is flipped vertically
   */
  public boolean getFlipV() {
    synchronized (stateParameterLock) {
      return flipV;
    }
  }
  /**
   * Gets the horizontal flip option.
   *
   * @return	<code>true</code> if image is flipped horizontally
   */
  public boolean getFlipH() {
    synchronized (stateParameterLock) {
      return flipH;
    }
  }
  /**
   * Initialize the crop box.
   *
   * @param cropBox	initial crop box
   */
  final private void initCrop(Rectangle cropBox) {
    synchronized (stateParameterLock) {
      // setting null cropBox removes cropping
      if(cropBox == null) {
	if(this.cropBox != null) {
	  this.cropBox = null;
	  invalidateFilters();
	}
      } else if(! cropBox.equals(this.cropBox)) {
        this.cropBox = cropBox.getBounds();
	invalidateFilters();
      }
    }
  }
  /**
   * Crops the input image by a given box.
   *
   * @param cropBox	new crop box
   */
  public void setCrop(Rectangle cropBox) {
    initCrop(cropBox);
  }
  /**
   * Gets the current crop box.
   *
   * @return	the crop box
   */
  public Rectangle getCrop() {
    synchronized (stateParameterLock) {
      if(cropBox == null) return null;
      else return cropBox.getBounds();
    }
  }
  /**
   * Restricts a box to fit on the image producer.
   *
   * @param cropBox	box to restrict
   * @return		box that intersects image producer
   */
  public Rectangle restrictCropBox(Rectangle cropBox) {
    if((ipSize == null) || (cropBox == null)) return cropBox;
    return cropBox.intersection(new Rectangle(ipSize));
  }
  /**
   * Sets a new color model and changed status to true.
   *
   * @param cm	new color model
   */
  public void setColorModel(ColorModel cm) {
    synchronized (stateParameterLock) {
      if(cm != this.cm) {
	initColorModel(cm);
	colorFilterNeeded = true;
	invalidateFilters();
      }
    }
  }
  /**
   * Sets the initial color model and changed status to false.
   *
   * @param cm	initial color model
   */
  final protected void initColorModel(ColorModel cm) {
    synchronized (stateParameterLock) {
      this.cm = cm;
      colorFilterNeeded = false;
    }
  }
  /**
   * Gets the color model.
   *
   * @return	the color model
   */
  public ColorModel getColorModel() {
    synchronized (stateParameterLock) {
      return(cm);
    }
  }
  /**
   * Sets whether the color model will be grabbed from the
   * image producer.  Only grabbed if the current color model
   * is null.  This is a work around to get the color model
   * for iiv.io.CNUStdImageFile but not get it for shapes
   * extending DisplayDraw.
   *
   * @param grabColorModel <code>true</code> to allow grabbing
   */
  public void setGrabColorModelState(boolean grabColorModel) {
    synchronized (stateParameterLock) {
      this.grabColorModel = grabColorModel;
    }
  }
  /**
   * Sets the preferred size of this component.
   *
   * @param d	preferred size
   */
  public void setPreferredSize(Dimension d) {
    preferredSize.setSize(d);
  }
  /**
   * Sets the preferred size of this component.
   *
   * @param width	preferred width
   * @param height	preferred height
   */
  public void setPreferredSize(int width, int height) {
    preferredSize.setSize(width, height);
  }
  /**
   * Gets the preferred size of this component.
   *
   * @return	preferred size
   */
  public Dimension getPreferredSize() {
    validate();
    return(preferredSize.getSize());
  }
  /**
   * Gets the minimum size of this component.
   *
   * @return	the minimum size
   */
  public Dimension getMininumSize() { return getPreferredSize(); }
  /**
   * Gets the maximum size of this component.
   *
   * @return	the maximum size
   */
  public Dimension getMaximumSize() { return getPreferredSize(); }
  /**
   * Sets the location of this image within the graphics area.
   *
   * @param x	x location
   * @param y	y location
   */
  protected void setImageLocation(int x, int y) {
    synchronized (imageLock) {
      displayImageBounds.setLocation(x, y);
    }
  }
  /**
   * Sets the location of this image within the graphics area.
   *
   * @param pt	location
   */
  protected void setImageLocation(Point pt) {
    synchronized (imageLock) {
      displayImageBounds.setLocation(pt);
    }
  }
  /**
   * Gets the location of this image within the graphics area.
   *
   * @return	the location
   */
  public Point getImageLocation() {
    synchronized (imageLock) {
      return(displayImageBounds.getLocation());
    }
  }
  /**
   * Gets the bounds of this image within the graphics area.
   *
   * @return	display image bounds
   */
  public Rectangle getImageBounds() {
    synchronized (imageLock) {
      return displayImageBounds.getBounds();
    }
  }
  /**
    * Sets the size of the image within the graphics area.
    *
    * @param width	width of image
    * @param height	height of image
    */
  private void setImageSize(int width, int height) {
    synchronized (imageLock) {
      displayImageBounds.setSize(width, height);
    }
  }
  /**
   * Sets the size of the image within this component.
   *
   * @param d	dimension of image
   */
  private void setImageSize(Dimension d) {
    synchronized (imageLock) {
      displayImageBounds.setSize(d);
    }
  }
  /**
   * Gets the size of the image within this component.
   *
   * @return	size of image
   */
  public Dimension getImageSize() {
    synchronized (imageLock) {
      return(displayImageBounds.getSize());
    }
  }
  /**
   * Determines if any image filtering is needed.
   *
   * @return	<code>true</code> if filters are needed
   */
  public boolean filtersNeeded() {
    synchronized (stateParameterLock) {
      return
	(grabColorModel && (cm == null)) ||
	(colorFilterNeeded && (this.cm != null)) ||
	(cropBox != null) ||
	flipV || flipH ||
	(zoomV != 1) || (zoomH != 1) ||
	(rotation != 0);
    }
  }
  /**
   * Filters the image producer and creates a new image if needed.
   */
  public void updateFilters() {
    synchronized (imageLock) { synchronized (stateParameterLock) {
      if(ip != null) {
	ImageProducer filtered_ip = ip;
	if(grabColorModel && (cm == null)) {
	  // add filter step to record color model from image producer
	  filtered_ip =
	    new FilteredImageSource(filtered_ip,
				    new ImageFilter() {
				      public void setColorModel(ColorModel model) {
					if((cm == null) && (model != null)) {
					  initColorModel(model);
					}
				      }
				    }
				    );
	}
        if(colorFilterNeeded && (this.cm != null)) {
	  image = null;
	  ColorFilter cf = new ColorFilter(this.cm);
	  filtered_ip = new FilteredImageSource(filtered_ip, cf);
        }
        if(cropBox != null) {
	  // make sure cropBox fits inside image
	  if( (cropBox.x >= 0) &&
	      ((cropBox.x + cropBox.width) <= ipSize.width) &&
	      (cropBox.y >= 0) &&
	      ((cropBox.y + cropBox.height) <= ipSize.height) ) {
	    image = null;
	    CropImageFilter cif = new CropImageFilter(cropBox.x, cropBox.y,
						      cropBox.width,
						      cropBox.height);
	    filtered_ip = new FilteredImageSource(filtered_ip, cif);
	  } else cropBox = null;
        }
        if(flipV || flipH) {
	  image = null;
	  if(flipFilter == null) flipFilter = new FlipFilter();
	  flipFilter.setFlip(flipV, flipH);
	  flipFilter.setImageProducer(filtered_ip);
	  filtered_ip = flipFilter;
        }
	else flipFilter = null;
        if((zoomV != 1) || (zoomH != 1) || (rotation != 0)) {
	  image = null;
	  linearFilter = new LinearImageFilter();
	  linearFilter.setZoom(zoomV, zoomH);
	  linearFilter.setAngle(rotation * Math.PI / 180.0);
	  linearFilter.setFilterSampleType(filterSampleType);
          if(linearFilter.getIsIdentity()) linearFilter = null;
	  else {
	    linearFilter.setImageProducer(filtered_ip);
	    filtered_ip = linearFilter;
	  }
        } else linearFilter = null;

	if(filterChanged) image = null;
        else if(image != null)
	  if(image.getSource() != filtered_ip) image = null;
        if(image == null) {
          fixedCrosshair = null; // may no longer be valid
	  pixelsGrabbed = false; // need to regrab pixels
	  image = Toolkit.getDefaultToolkit().createImage(filtered_ip);
	  filterChanged = false;
        }
      }
    } }
  }
  /**
   * Uses MediaTracker to wait for an image to be fully loaded.
   *
   * @param image	image to wait on.
   */
  public boolean track(Image image) {
    final MediaTracker tracker = new MediaTracker(this);
    tracker.addImage(image, 0);
    Thread trackerThread = new Thread() {
      public void run() {
	try {
	  tracker.waitForID(0);
	} catch (InterruptedException ie) {
	  System.out.println(
	    "DisplayComponent.track returning on interrupedException");
	}
      }
    };
    trackerThread.setDaemon(true);
    trackerThread.start();
    try {
      // limit tracking to 5 seconds
      trackerThread.join(5000);
    } catch (InterruptedException ie) {} // ignore
    if(trackerThread.isAlive()) trackerThread.interrupt();
    if(tracker.isErrorAny()) return false;
    return tracker.checkAll();
  }
  /**
   * Calculates the size of the original image producer.
   */
  public void updateIpSize() {
    synchronized (imageLock) {
      if(ip == null) ipSize.setSize(0, 0);
      else {
        Image tmpImage = null;
        if( image != null ) if(image.getSource() == ip) tmpImage = image;
        if(tmpImage == null)
	  tmpImage = Toolkit.getDefaultToolkit().createImage(ip);
        if(image == null) {
	  image = tmpImage;
	  pixelsGrabbed = false; // need to regrab pixels
	}
        ipSize.setSize(tmpImage.getWidth(null), tmpImage.getHeight(null));
        if((ipSize.height < 1) || (ipSize.width < 1)) {
          // Can't go on until sizes are complete
	  if(! track(tmpImage))
  showStatus("DisplayComponent.updateIpSize() - error tracking ip image");
	  ipSize.setSize(tmpImage.getWidth(null), tmpImage.getHeight(null));
          if((ipSize.height < 1) || (ipSize.width < 1))
	    showStatus("DisplayComponent.updateIpSize() - IP image size error");
        }
      }
    }
    // Note - unknown problem occurred when updateIpSize() was not called every
    // time updateSize() was.  ColorBars wouldn't display. Only occurred when
    // ip == image.getImageProducer().  Fixed by setting image to tmpImage
    // created in updateIpSize().  Maybe image was not being created for some
    // reason.  When it happens paint gets called repeatedly. -- also fixed by
    // calling track in updateImage but that is slow -- reusing tmpImage works
    // best
  }
  /**
   * Calculates this components preferred size.
   */
  public void updateSize() {
    synchronized (imageLock) {
      if((ipSize.width < 1) || (ipSize.height < 1)) updateIpSize();
      if(image == null) setImageSize(0, 0);
      else if(image.getSource() == ip) setImageSize(ipSize);
      else {
        setImageSize(image.getWidth(null), image.getHeight(null));
        if((displayImageBounds.height < 1) || (displayImageBounds.width < 1)) {
	  // Can't go on until sizes are complete
          if(! track(image)) showStatus("DisplayComponent.updateSize() - error tracking image");
          setImageSize(image.getWidth(null), image.getHeight(null));
          if((displayImageBounds.height < 1) ||
	    (displayImageBounds.width < 1))
	    showStatus("DisplayComponent.updateSize() - image size error");
        }
      }
      setPreferredSize(getImageSize());
    }
  }
  /**
   * Updates filters and total size of this component.
   */
  public void doLayout() {
    validateFilters();
    updateSize();
    super.doLayout();
  }
  /**
   * Paints this component on the graphics context.
   *
   * @param g graphics context to paint on
   */
  public void paint(Graphics g) {
    synchronized (imageLock) {
      if((displayImageBounds.height < 1) || (displayImageBounds.width < 1)) {
	showStatus("DisplayComponent.paint() - image size error calling updateSize()");
	updateSize();
      }
      drawTrackingCrosshair(null, null);  // erase any previous tracking crosshair
      if((displayImageBounds.height > 0) && (displayImageBounds.width > 0) && (image != null)) {
	try {
          g.drawImage(image, displayImageBounds.x,
		      displayImageBounds.y, this);
        } catch (java.lang.NullPointerException npe) {
	  System.err.println("java.iiV.DisplayComponent->paint(g) caught NullPointerException from Graphics.drawImage(...)");
	  System.err.println("java.iiV.DisplayComponent->paint(g) error previously found when image had transparent colors with a specific X11 enviroment");
	  System.err.println("java.iiV.DisplayComponent->paint(g) temp fix--edit color model enabling alphas with one transparent alpha>0(i.e.1) or one opaque alpha<255(i.e.254) to force alpha usage");
//	  throw npe;
	}
      }
      if(fixedCrosshair != null) {
        Shape shape = g.getClip();  // save previous clipping
	// clipping needed because crop not built into crosshair
        if((displayImageBounds.height > 0) && (displayImageBounds.width > 0))
	  g.setClip(displayImageBounds.x, displayImageBounds.y,
		    displayImageBounds.width, displayImageBounds.height);
	g.setColor(fixedCrosshairColor);
	if(fixedCrosshairDashed) drawDashedLines(fixedCrosshair, g);
	else drawLines(fixedCrosshair, g);
	g.setClip(shape);  // restore previous clipping
      }
      else if(fixedCrosshairIndices != null)
	setCrosshair(fixedCrosshairIndices, fixedCrosshairColor);
    }
  }
  /**
   * Calculates point location in the initial image
   * from a component relative location
   * correcting for image location and filters (flip, zoom and crop)
   *
   * @param pt	location in component
   * @return	location in original image with no filtering
   */
  public Point trueLocation(Point pt) {
    if(pt == null) return null;
    Point fixedPt = new Point(pt);
    synchronized (imageLock) { synchronized (stateParameterLock) {
      // correct for image location within component
      fixedPt.translate(-displayImageBounds.x, -displayImageBounds.y);
      // correct for linear filter
      if(linearFilter != null) linearFilter.invertMap(fixedPt, fixedPt);
      // correct for flips
      if(flipFilter != null) flipFilter.invertMap(fixedPt, fixedPt);
      // correct for crop
      if(cropBox != null) fixedPt.translate(cropBox.x, cropBox.y);

      // make sure the point fits in the input image ipSize
      // if no ip assume point can be anywhere over component
      Dimension size = ipSize;
      if(ip == null) size = getSize();
      if(fixedPt.x < 0) fixedPt.x = 0;
      else if(fixedPt.x >= size.width) fixedPt.x = size.width - 1;
      if(fixedPt.y < 0) fixedPt.y = 0;
      else if(fixedPt.y >= size.height) fixedPt.y = size.height - 1;
    } }
    return fixedPt;
  }
  /**
   * Converts an original image point to a component display location
   * correcting for image location and filters (flip, zoom and crop)
   *
   * @param pt	location in original image with no filtering
   * @return	location in component
   */
  public Point displayLocation(Point pt) {
    Point displayPt = new Point(pt);
    synchronized (imageLock) { synchronized (stateParameterLock) {
      // make sure the input point fits over the input image ipSize
      // if no ip assume point can be anywhere over component
      Dimension size = ipSize;
      if(ip == null) size = getSize();
      if(displayPt.x < 0) displayPt.x = 0;
      else if(displayPt.x >= size.width) displayPt.x = size.width - 1;
      if(displayPt.y < 0) displayPt.y = 0;
      else if(displayPt.y >= size.height) displayPt.y = size.height - 1;

      // correct for crop
      if(cropBox != null) displayPt.translate(-cropBox.x, -cropBox.y);

      // correct for flips
      if(flipFilter != null) flipFilter.map(displayPt, displayPt);
      // correct for rotation
      if(linearFilter != null) linearFilter.map(displayPt, displayPt);
      // correct for image location within component
      displayPt.translate(displayImageBounds.x, displayImageBounds.y);
    } }
    return displayPt;
  }
  /**
   * Gets the coordinate mapping - subclasses must override this and
   * implement CoordinateMappable.
   *
   * @return	coordinate map
   */
  public CoordinateMap getCoordinateMap() { return null; }
  /**
   * Gets the coordinate resolution - overridden in subclasses if available.
   *
   * @return coordinate resolutions
   */
  public XYZDouble getCoordinateResolutions() { return null; }
  /**
   * Gets the indices corresponding to the original raw data this display
   * component is created from based on a point relative to the component.
   * This default implementation transforms the point with trueLocation and
   * calls getIndicesFromNonfilteredPoint.
   *
   * @param pt	point 	location relative to component
   * @return	indices indices to original raw data which may have
   *			any number of dimensions or <code>null</code>
   *			if invalid point
   */
  public int[] getIndices(Point pt) {
    if(pt == null) return null;
    synchronized (imageLock) {
      if(image == null) {
	  // assumes component not using image uses whole component
	  Dimension size = getSize();
	  if(pt.x < 0 || pt.x > size.width || pt.y < 0 || pt.y > size.height)
	      return null;
      }
      else {
	  // ignore points outside of image
	  if(! displayImageBounds.contains(pt) ) return null;
	  pt = trueLocation(pt);
      }
    }
    return getIndicesFromNonfilteredPoint(pt);
  }
  /**
   * Gets the indices corresponding to the original raw data this display
   * component is created from based on a point relative to the non-filtered
   * (no flip, rotation, zoom, or offset) image.  This default implementation
   * returns the original point stored in a 2-dimensional int array.
   *
   * @param pt	point 	location relative to non-filtered image
   * @return	indices indices to original raw data which may have
   *			any number of dimensions or <code>null</code>
   *			if invalid point
   */
  public int[] getIndicesFromNonfilteredPoint(Point pt) {
    if(pt == null) return null;
    return new int[] {pt.x, pt.y};
   }
 /**
   * Gets the point relative to the component given the indices
   * corresponding to the original raw data this display component
   * is created from.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	location relative to the component.  Indices that map
   *		off the image are forced onto the image.
   */
  public Point getPoint(int[] indices) {
    Point pt = getNonfilteredPoint(indices);
    return displayLocation(pt);
  }
  /**
   * Gets the point relative to the non-filtered
   * (no flip, rotation, zoom, or offset) image given the indices
   * corresponding to the original raw data this display component
   * is created from.  This default implementation returns the
   * indices stored in a Point object.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	location relative to non-filtered image (negative value
   *		for x or y indicates the indice doesn't map to that dimension)
   */
  public Point getNonfilteredPoint(int[] indices) {
    if(indices == null) return new Point(-1, -1);
    if(indices.length < 1) return new Point(-1, -1);
    if(indices.length == 1)  return new Point(indices[0], -1);
    return new Point(indices[0], indices[1]);
  }
  /**
   * Determines if the given indices relative to the raw data are located
   * on the display image before cropping.
   *
   * @param	indices indices to original raw data which may have
   *		any number of dimensions
   * @return	<code>true</code> if indices are located on the image this component
   *		represents.
   */
  public boolean onImage(int[] indices) {
    if(indices == null) return false;
    if(indices.length != 2) return false;
    if(indices[0] < 0 || indices[1] < 0) return false;
    synchronized (imageLock) {
      if(indices[0] >=  ipSize.width || indices[1] >= ipSize.height) return false;
    }
    return true;
  }
  /**
   * Gets the data value at the given indice to the data represented
   * by this display component.
   *
   * @param indices	indices to data
   * @return		value to display
   */
  public double getValue(int[] indices) { return 0; }
  /**
   * Gets the data value represented by this display component
   * at the given component relative point.
   *
   * @param pt		point to get value at
   * @return		value at point
   */
  public double getValue(Point pt) {
    return getValue(getIndicesFromNonfilteredPoint(trueLocation(pt)));
  }
  /**
   * Get the RGB value of a point over the image.
   *
   * @param pt location to get RGB value of.
   * @return pixel value in default RGB color model.
   */
  public int getImageRGB(Point pt) {
    int pixel = 0;
    synchronized (imageLock) {
      if(displayImageBounds.contains(pt) ) {
	int w = displayImageBounds.width;
	int h = displayImageBounds.height;
	if(! pixelsGrabbed) {
	  if(image != null) {
	    grabbedPixels = new int[w * h];
	    PixelGrabber pixelGrabber = new PixelGrabber(image,
							 0, 0, w, h,
							 grabbedPixels,
							 0, w);
	    try {
	      pixelsGrabbed = pixelGrabber.grabPixels();
	    } catch (InterruptedException ie) {
	      System.out.println(
	      "DisplayComponent.getImageRGB returning on interrupedException");
	    }
	  }
	}
	if(pixelsGrabbed) {
	  int index = (pt.y - displayImageBounds.y) * w
	    + pt.x - displayImageBounds.x;
	  pixel = grabbedPixels[index];
	}
      }
    }
    /*
    ColorModel rgb = ColorModel.getRGBdefault();
    System.out.println("DisplayComponent.getImageRGB() returning pixel="
		       + pixel +
		       "rgba=" + rgb.getRed(pixel) + rgb.getGreen(pixel) +
		       rgb.getBlue(pixel) + rgb.getAlpha(pixel));
    */
    return pixel;
  }
  /**
   * Gets the quantification factor at this indice.
   *
   * @return		quantification factor
   *			(this default implementation returns
   *			 value from protected method getFactor())
   */
  public double getFactor() { return 1.0d; }
  /**
   * Displays the value of the data associated with this component
   * at the given indice in the given show point dialog.
   *
   * @param indices	the indices into the data
   * @param spd		the display to show the point data in
   */
  public void showPoint(int[] indices, ShowPointDisplay spd) {
    if(indices != null  && spd != null) {
      XYZDouble xyzpt = null;
      String mapName = null;
      CoordinateMap coorM = getCoordinateMap();
      if(coorM != null) {
        xyzpt = coorM.toSpace(new XYZDouble(indices, 0),
			      getCoordinateResolutions());
        mapName = coorM.getName();
      }
      spd.showPoint(indices, getValue(indices), getFactor(),
		    getName(), xyzpt, mapName);
    }
  }
  /**
   * Erases the old tracking crosshair a draws a new one on this component with
   * exclusive-or for fast drawing an erasing.
   *
   * @param indices		indices the crosshair points to
   * @param crosshairColor	color of crosshair
   */
  public void drawTrackingCrosshair(int[] indices, Color crosshairColor) {
    if((indices == null) && (trackingCrosshair == null)) return;
    synchronized(imageLock) {
      Graphics g = getGraphics();
      if(g == null) return;
      Shape shape = g.getClip();  // save previous clipping
      if((displayImageBounds.height > 0) && (displayImageBounds.width > 0))
	g.setClip(displayImageBounds.x, displayImageBounds.y,
		  displayImageBounds.width, displayImageBounds.height);
      // erase old
      if(trackingCrosshair != null) {
        g.setXORMode(trackingCrosshairColor);
        if(trackingCrosshairDashed) drawDashedLines(trackingCrosshair, g);
	else drawLines(trackingCrosshair, g);
      }
      // draw new
      if(indices != null) {
        trackingCrosshair = bldCrosshair(indices);
	trackingCrosshairDashed = ! onImage(indices);
	if(trackingCrosshair != null) {
          if(crosshairColor != null) trackingCrosshairColor = crosshairColor;
          g.setXORMode(trackingCrosshairColor);
	  if(trackingCrosshairDashed) drawDashedLines(trackingCrosshair, g);
          else drawLines(trackingCrosshair, g);
	}
      }
      else trackingCrosshair = null;
      g.setClip(shape);  // restore previous clipping
    }
  }
  /**
   * Erases the old crosshair a draws a new one on this component.
   *
   * @param indices		indices the crosshair points to
   * @param crosshairColor	color of crosshair
   */
  public void setCrosshair(int indices[], Color crosshairColor) {
    int[][] oldCrosshair = null;
    int[][] newCrosshair = null;
    synchronized(imageLock) {
      oldCrosshair = fixedCrosshair;
      if(indices != null) {
        if(crosshairColor != null) fixedCrosshairColor = crosshairColor;
	// create copy of indices
	fixedCrosshairIndices = indices.clone();
	// now calculate new crosshair to display
        fixedCrosshair = bldCrosshair(fixedCrosshairIndices);
	newCrosshair = fixedCrosshair;
	// change to dashed lines if not on image
	fixedCrosshairDashed = ! onImage(fixedCrosshairIndices);
      }
      else {
        fixedCrosshairIndices = null;
        fixedCrosshair = null;
      }
    }
    // repaint outside of sync
    if(oldCrosshair != null) repaintLineAreas(oldCrosshair);
    if(newCrosshair != null) repaintLineAreas(newCrosshair);
  }
  /**
   * Gets a copy of the currently displayed crosshair indices.
   *
   * @return	indices to currently displayed crosshair
   */
   public int[] getCrosshairIndices() {
     int[] indices = null;
     synchronized (imageLock) {
       if(fixedCrosshairIndices != null) {
         indices = fixedCrosshairIndices.clone();
       }
     }
     return indices;
   }
  /**
   * Gets the currently displayed crosshair color
   *
   * @return	color of currently displayed crosshair
   */
   public Color getCrosshairColor() { return fixedCrosshairColor; }
  /**
   * Builds an array of line endpoints for drawing a cross hair at the given
   * indices on this image.
   *
   * @param indices		the indices into the data
   * @return			array of line endpoints
   */
  public int[][] bldCrosshair(int[] indices) {
    int [][] crosshair = null;
    if(indices != null) {
      Point pt = getNonfilteredPoint(indices);
      if(pt.x >= 0) {
        Point px1 = displayLocation(new Point(pt.x, 0));
        Point px2 = displayLocation(new Point(pt.x, ipSize.height - 1));
	if(pt.y >= 0) {
	  Point px11 = displayLocation(new Point(pt.x, pt.y - 1));
	  Point px21 = displayLocation(new Point(pt.x, pt.y + 1));
          Point py1 = displayLocation(new Point(0, pt.y));
          Point py11 = displayLocation(new Point(pt.x - 1, pt.y));
          Point py21 = displayLocation(new Point(pt.x + 1, pt.y));
          Point py2 = displayLocation(new Point(ipSize.width - 1, pt.y));
	  crosshair = new int[][] {
	    { px11.x, px11.y, px1.x, px1.y }, {px21.x, px21.y, px2.x, px2.y},
	    { py11.x, py11.y, py1.x, py1.y  }, {py21.x, py21.y, py2.x, py2.y}
	  };
	}
        else crosshair = new int[][] { { px1.x, px1.y, px2.x, px2.y } };
      }
      else if(pt.y >= 0) {
        Point py1 = displayLocation(new Point(0, pt.y));
        Point py2 = displayLocation(new Point(ipSize.width - 1, pt.y));
	crosshair = new int[][] { { py1.x, py1.y, py2.x, py2.y } };
      }
    }
    return crosshair;
  }
  /**
   * Draws a set of lines.
   *
   * @param ar  array containing list of lines.
   */
  public static void drawLines(int[][] ar, Graphics g) {
    if(ar == null) return;
    for(int i=0; i<ar.length; i++) {
      int[] linepts = ar[i];
      g.drawLine(linepts[0], linepts[1], linepts[2], linepts[3]);
    }
  }
  /**
   * Draws a set of dashed lines.
   *
   * @param ar  array containing list of lines.
   */
  public static void drawDashedLines(int[][] ar, Graphics g) {
    int dashLength = 8;
    int dashSpace = 2;
    if(ar == null) return;
    for(int i=0; i<ar.length; i++) {
      int[] linepts = ar[i];
      DisplayShape.drawDashedLine(g, linepts[0], linepts[1], linepts[2], linepts[3],
				  dashLength, dashSpace, 0);
    }
  }
  /**
   * Repaints areas associated with a set of lines to hopefully
   * be more efficient then repainting whole image.
   *
   * @param ar  array containing list of lines
   */
  public void repaintLineAreas(int[][] ar) {
    for(int i=0; i<ar.length; i++) {
      int[] linepts = ar[i];
      int x = linepts[0];
      int w = linepts[2] - linepts[0];
      if(w < 0) { x = linepts[2]; w = -w; };
      w = w + 1;
      int y = linepts[1];
      int h = linepts[3] - linepts[1];
      if(h < 0) { y = linepts[3]; h = -h; }
      h = h + 1;
      repaint(x, y, w, h);
    }
  }
  /**
   * Gets the name string for showpoint - needs to be over ridden.
   *
   * @return	name for this component
   */
  public String getName() { return null; }
  /**
   * Creates a string representing this component.
   *
   * @return	string representing this component
   */
  public String toString() {
    return super.toString() + "\n" + getName();
  }
  /**
   * Creates a script to recreate the settings on an existing object.
   *
   * @param scriptedObjects scripted objects list this object already
   *                        exists in.
   * @return	script to recreate this component
   */
  public String postObjectToScript(CNUScriptObjects scriptedObjects) {
    StringBuffer sb = new StringBuffer();
    String objectVariableName = scriptedObjects.get(this);
    // font
    Font font = getFont();
    sb.append(objectVariableName).append(".setFont(\n");

    sb.append("new java.awt.Font(\"").append(font.getName()).append("\",\n");
    sb.append("iiv.script.CNUDisplayScript.fontStyleValueOf(\"");
    sb.append(CNUDisplayScript.fontStyleToString(font)).append("\")");
    sb.append(", ");
    sb.append(font.getSize()).append(")\n");

    sb.append(");\n");
    // text Color
    Color c = getForeground();
    // 05/10/2016 - jtl - null check to fix script setForeground("") command not found
    if(c != null) {
      sb.append(objectVariableName).append(".setForeground(");
      sb.append("newColorObject(\"").append(CNUDialog.colorToString(c)).append("\")");
      sb.append(");\n");
    }
    // color model
    ColorModel cm = getColorModel();
    if(cm instanceof IndexColorModel) {
      if(cm instanceof CNUColorModel) sb.append(((CNUColorModel) cm).toScript(scriptedObjects));
      else sb.append(CNUColorModel.toScript((IndexColorModel) cm, scriptedObjects));
      sb.append(objectVariableName).append(".setColorModel(script_rtn);\n");
    }
    // filter sample type
    sb.append(objectVariableName).append(".setFilterSampleType(iiv.filter.FilterSampling.");
    sb.append(LinearImageFilter.sampleTypeToString(getFilterSampleType())).append(");\n");
    // zoom
    sb.append(objectVariableName).append(".setZoom(");
    sb.append(getZoomV()).append(", ").append(getZoomH()).append(");\n");
    // rotation
    sb.append(objectVariableName).append(".setRotation(").append(getRotation()).append(");\n");
    // flips
    sb.append(objectVariableName).append(".setFlips(");
    sb.append(getFlipV()).append(", ").append(getFlipH()).append(");\n");
    // crop
    Rectangle crop = getCrop();
    if(crop == null) sb.append(objectVariableName).append(".setCrop(null);\n");
    else {
      sb.append(objectVariableName).append(".setCrop(");
      sb.append("new java.awt.Rectangle(").append(crop.x).append(", ").append(crop.y).append(", ");
      sb.append(crop.width).append(", ").append(crop.height).append(")");
      sb.append(");\n");
    }
    // crosshair
    int[] crosshairIndices = fixedCrosshairIndices;
    if(crosshairIndices != null) {
      String colorVariableName = objectVariableName + "_crosshairColor";
      sb.append(colorVariableName);
      sb.append(" = newColorObject(\"").append(CNUDialog.colorToString(fixedCrosshairColor)).append("\");\n");
      
      String arrayVariableName = objectVariableName + "_crosshairArray";
      sb.append(arrayVariableName).append(" = new int[] {");
      for(int i = 0; i < crosshairIndices.length; i++) {
	if(i != 0) sb.append(", ");
	sb.append(crosshairIndices[i]);
      }
      sb.append("};\n");

      sb.append(objectVariableName);
      sb.append(".setCrosshair(").append(arrayVariableName).append(", ");
      sb.append(colorVariableName).append(");\n");
      sb.append("unset(\"").append(arrayVariableName).append("\");\n");
      sb.append("unset(\"").append(colorVariableName).append("\");\n");
    }
    return sb.toString();
  }
  /**
   * If color map or coordinate map represents the same file return it
   *  - must be overridden in components associated with files.
   *
   * @param sameFileAsObj	object associated with a file to compare to
   * @return			object associated with the same file or
   *				<code>null</code> if non found
   */
  public Object getFileObject(Object sameFileAsObj) {
    ColorModel cm = getColorModel();
    if(cm instanceof CNUFileObject)
      return ((CNUFileObject) cm).getFileObject(sameFileAsObj);

    CoordinateMap coorM = getCoordinateMap();
    if(coorM instanceof CNUFileObject)
      return ((CNUFileObject) coorM).getFileObject(sameFileAsObj);

    return null;
  }
} // end DisplayComponent class
