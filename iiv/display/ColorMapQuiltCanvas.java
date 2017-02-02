package iiv.display;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
/**
 * Canvas to display a color map quilt.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		SingleComponentCanvas
 * @see		ColorMapCanvas
 * @since	iiV1.0
 */
public class ColorMapQuiltCanvas extends SingleComponentCanvas {
  private static final long serialVersionUID = -5913923477589547996L;
  private DisplayColorMapQuilt dcmq = null;
  // list of components
  private Object boxesLock = new Object();
  private Rectangle box1 = null;
  private Rectangle box2 = null;
  /**
   * Constructs a new instance of ColorMapQuiltCanvas.
   *
   * @param cm	color model to display on the canvas
   */
  public ColorMapQuiltCanvas(ColorModel cm) {
    super(new DisplayColorMapQuilt(cm));
    dcmq = (DisplayColorMapQuilt) getDisplayComponent();
    dcmq.setZoom(1, 1);
    dcmq.setRotation(0);
    dcmq.setFlips(false, false);
    dcmq.setCrop(null);
    dcmq.setFont(getFont());
    dcmq.setForeground(getForeground());
    dcmq.validate();
    dcmq.setSize(dcmq.getPreferredSize());
    setPreferredSize(dcmq.getSize());
  }
/** Add an boxed element at index location */
  public void setBox1(int index) {
    Rectangle area = null;
    Point orig = null;
    synchronized (boxesLock) {
      area = box1;
      box1 = dcmq.getIndexBorder(index);
      if(area != null) area.add(box1);
      else area = box1.getBounds();
      orig = dcmq.getImageLocation();
    }
    area.grow(1,1);
    repaint(area.x + orig.x, area.y + orig.y, area.width, area.height);
  }
/** Add an boxed element at index location */
  public void setBox2(int index) {
    Rectangle area = null;
    Point orig = null;
    synchronized (boxesLock) {
      area = box2;
      box2 = dcmq.getIndexBorder(index);
      if(area != null) area.add(box2);
      else area = box2.getBounds();
      orig = dcmq.getImageLocation();
    }
    area.grow(1,1);
    repaint(area.x + orig.x, area.y + orig.y, area.width, area.height);
  }
/** Paints the image to the screen */
  public void paint(Graphics g) {
    super.paint(g);
    synchronized (boxesLock) {
      if((box1 != null) || (box2 != null)) {
	Point orig = dcmq.getImageLocation();
	g.translate(orig.x, orig.y);
	g.setColor(Color.black);
	if(box1 != null) g.drawRect(box1.x, box1.y, box1.width, box1.height);
	
	if(box2 != null) g.drawRect(box2.x, box2.y, box2.width, box2.height);
	if(box1 != null) {
	  g.setColor(Color.red);
	  g.drawRect(box1.x+1, box1.y+1, box1.width-2, box1.height-2);
	}
	if(box2 != null) {
	  g.setColor(Color.blue);
	  g.drawRect(box2.x+1, box2.y+1, box2.width-2, box2.height-2);
	}
	g.translate(-orig.x, -orig.y);
      }
    }
  }
}
/**  Draw a box with xor */
/*
  private Object boxLock = new Object();
  private Graphics drawBoxG = null;
  public void xorDrawBox(Rectangle box) {
    synchronized (boxLock) {
      if(drawBoxG == null) drawBoxG = getGraphics();
      drawBoxG.setXORMode(Color.red);
      drawBoxG.drawRect(box.x, box.y, box.width, box.height);
    }
  }
      if((lkup != null) && (dcmq != null)) {
        if(indexBbox != null)  lkup.xorDrawBox(indexBbox);
        indexBbox = dcmq.getIndexBorder(index);
	Point pt = dcmq.getImageLocation();
	indexBbox.translate(pt.x, pt.y);
        lkup.xorDrawBox(indexBbox);
      }
*/
