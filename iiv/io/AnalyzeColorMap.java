package iiv.io;
import iiv.display.*;
import iiv.dialog.*;
import java.io.*;
import java.net.*;
import java.awt.image.*;
/**
 * AnalzyeColorMap extends CNUColorModel and allows reading and writing
 * <b>ANALYZE</b> style colormap files.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUColorModel
 * @since	iiV1.0
 */
public class AnalyzeColorMap extends CNUColorModel {
  /**
   * Constructs a new instance of AnalyzeColorMap.
   *
   * @param filename	the file to read colors from
   * @exception ColorMapException	thrown on error reading file
   */
  public AnalyzeColorMap(String filename)
       throws IOException, ColorMapException {
    super(ReadAnalyzeColorMap(filename));
    setCNUFile(filename);
    setSaved(true);
  }
  /**
   * Reads an ANALYZE color map.
   *
   * @param filename	the file to read colors from
   * @return		a byte array of colors sized 3*256
   */
  public static byte[] ReadAnalyzeColorMap(String filename)
       throws IOException, ColorMapException
  {    
    if( ! filename.endsWith(".lkup") ) filename = filename + ".lkup";

    CNUFile cnuf = new CNUFile(filename);
    StreamTokenizer tokenizer = cnuf.getStreamTokenizer();

    byte[] cmap = new byte[256*3];
    for(int i=0; i<3*256; i++){
      switch (tokenizer.nextToken()) {
      case StreamTokenizer.TT_NUMBER:
	int tmp;
	tmp=(int)tokenizer.nval;
	cmap[i]=(byte)tmp;
	break;
      case StreamTokenizer.TT_EOF:
	throw new ColorMapException
	  ("File = " + filename + " - premature end of file");
      case StreamTokenizer.TT_EOL:
	throw new ColorMapException
	  ("File = " + filename + " - invalid end of line found");
      case StreamTokenizer.TT_WORD:
	throw new ColorMapException("File=" +
		      filename + " - found invalid word=" +
		      tokenizer.sval);
      default:
	throw new ColorMapException
	  ("File = " + filename + " - unknown parsing error");
      }
    }
    return(cmap);
  }
  /**
   * Writes an ANALYZE colormap file.
   *
   * @param filename	the file to write to
   * @param cd		Determines how to handle write overs if file
   *			exists.
   *			If cd is <code>null</code>, the file is written
   *			over with no warning,
   *			else if cd <code>instanceof ContinueDialog</code>, the
   *			dialog is used to query the user before writting over,
   *			else no write over is performed.
   * @param icm		IndexColorModel to write to file
   * @exception	IOException	thrown if error writing to cnufile
   * @see			ContinueDialog
   */
  public static void WriteAnalyzeColorMap( String filename, Object cd,
			            IndexColorModel icm)
	throws IOException, ColorMapException {
    if(filename != null) {
      filename = filename.trim();
      if("".equals(filename)) filename = null;
    }
    if(filename == null)
      throw new IOException("WriteColorMap error - null file name");
    if( ! filename.endsWith(".lkup") ) filename = filename + ".lkup";
    CNUFile cnufile = new CNUFile(filename);
    if( cnufile.exists() ) {
      if(cd == null) throw
	new IOException("WriteColorMap - attempt to write over existing file "
	+ filename);
      if(cd instanceof ContinueDialog) {
        ((ContinueDialog)cd).beep();
        ((ContinueDialog)cd).setVisible(true);
        if( ! ((ContinueDialog)cd).getContinueFlag() ) throw
	  new IOException("WriteColorMap - did not write file " + filename);
      }
    }
    PrintWriter pw = null;
    try {
      pw = cnufile.getPrintWriter();
      if(icm.getMapSize() != 256)
	throw new ColorMapException("WriteColorMap - invalid Analyze color map size");
      for(int i = 0; i < 256; i++)
      pw.println(icm.getRed(i) + " " + icm.getGreen(i) + " " + icm.getBlue(i));
    } finally {
      if(pw != null) pw.close();
    }
  }
}
