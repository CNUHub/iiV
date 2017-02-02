package iiv.data;
/**
 * CNUConversionTypes defines types used by
 * CNUDataConversions and ConvertDataInputStream.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.io.CNUDataConversions
 * @see		iiv.io.ConvertDataInputStream
 * @since	iiV1.0
 */
public interface CNUConversionTypes {
  public int NO_CONVERSION = 0;
  public int SWAP_BYTES = 1;
  public int SWAP_SHORTS = 2;
  public int REVERSE_BYTES = 3;
  public int VAX_TO_SUN = 4;
  public int RGB24_TO_INT = 5;
  public int BGR24_TO_INT = 6;
  public String CONVERSION_NAMES[] = {
    "NO_CONVERSION", "SWAP_BYTES", "SWAP_SHORTS",
    "REVERSE_BYTES", "VAX_TO_SUN", "RGB24_TO_INT", "BGR24_TO_INT"
  };
}
