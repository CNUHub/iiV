package iiv.gui;
import javax.swing.DefaultBoundedRangeModel;
/**
 * Modifies the javax.swing.DefaultBoundedRangeModel to auto extend its size when setting a value outsides its current bounds.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @see		javax.swing.BoundedRangeModel
 * @since	iiV1.1186
 */
public class AutoExtendBoundedRangeModel extends DefaultBoundedRangeModel {
  public AutoExtendBoundedRangeModel() {super();}
  public AutoExtendBoundedRangeModel(int value, int extent, int min, int max) {
    super(value, extent, min, max);
  }
  public void setValue(int n) {
    if(n > getMaximum()) setMaximum(n);
    if(n < getMinimum()) setMinimum(n);
    super.setValue(n);
  }
}
