package iiv.filter;
import java.awt.image.*;
/**
 * FilterSampling defines routines to allow handling filter sample types
 * for a component.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.DisplayComponent
 * @since	iiV1.132
 */
public interface FilterSampling {
  public final static int UNKNOWN_SAMPLE_TYPE = 0;
  public final static int REPLICATE = 1; // java.awt.Image.SCALE_REPLICATE;
  public final static int INTERPOLATE = 2; // java.awt.Image.SCALE_AREA_AVERAGING;
  public final static int ALPHA_WEIGHTED_INTERPOLATE = 3;
  public final static int INDEX_INTERPOLATE = 4;
  public final static int ALPHA_WEIGHTED_INDEX_INTERPOLATE = 5;
  public final static int[] SAMPLING_TYPES = {
    REPLICATE,
    INTERPOLATE,
    ALPHA_WEIGHTED_INTERPOLATE,
    INDEX_INTERPOLATE,
    ALPHA_WEIGHTED_INDEX_INTERPOLATE
  };
  public final static String[] SAMPLING_NAMES = {
    "REPLICATE",
    "INTERPOLATE",
    "ALPHA_WEIGHTED_INTERPOLATE",
    "INDEX_INTERPOLATE",
    "ALPHA_WEIGHTED_INDEX_INTERPOLATE"
  };
  /**
   * Sets the filter sampling type.
   *
   * @param filterSampleType filter sample type
   */
  public void setFilterSampleType( int filterSampleType );
  /**
   * Gets the filter sampling type.
   *
   * @return filter sample type
   */
  public int getFilterSampleType();
}
