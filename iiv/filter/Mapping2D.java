package iiv.filter;
import java.awt.*;
/**
 * Interface for classes that performs 2D mapping.
 */
public interface Mapping2D {
  /**
   * Maps an integer point location to a transformed location.
   *
   * @param inPt	in pixel location
   * @param outPt	written over with out pixel location
   */
  public void map(Point inPt, Point outPt);
  /**
   * Maps an integer point location to an inverse transformed location.
   *
   * @param inPt	in pixel location
   * @param outPt	written over with out pixel location
   */
  public void invertMap(Point inPt, Point outPt);
  /**
   * Maps a double precision point location to a transformed location.
   *
   * @param in	in pixel location
   * @param out	written over with out pixel location
   */
  public void map(double[] in, double[] out);
  /**
   * Maps a double precision point location to an inverse transformed location.
   *
   * @param in	in pixel location
   * @param out	written over with out pixel location
   */
  public void invertMap(double[] in, double[] out);
}