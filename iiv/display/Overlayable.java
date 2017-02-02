package iiv.display;
import java.awt.*;
/**
 * Overlayable defines methods to allow creating overlays
 * for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUDisplay
 * @see		DisplayComponent
 * @since	iiV1.132
 */
public interface Overlayable {
  /**
   * Creates a component based on different data valid for
   * overlaying on top of this component.  Returns <code>null</code>
   * if the data isn't valid for overlaying.
   * Duplicates information needed to create the component
   * showing related data at the same location relative
   * to the top left corner as this component.
   * Information such as color model and scaling are not duplicated
   * but instead set to creation defaults that may be
   * preset to provide see through.
   *
   * @param obj		data object to base new overlay component on
   * @return		component that was created for overlaying or
   *			<code>null</code> if invalid object
   */
  public Component createOverlay(Object obj);
}
