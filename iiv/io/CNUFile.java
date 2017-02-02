package iiv.io;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.net.*;
import java.util.zip.*;
import javax.imageio.ImageIO;
import Acme.JPM.Decoders.*;


/**
*  Class to support standard file ativities with local and URL files
*  the class cannot be reset after construction so there is no reason
*  to clone or synchronize
*/
public class CNUFile {
  private static URL urlContext = null; // used only during construction
  private URL ufile = null;  // cannot be reset after construction
  private File file = null;  // cannot be reset after construction
  private boolean gzip = false; // cannot be reset after construction
/** Create a new instance of CNUFile */
  public CNUFile( String filename ) {
    if( filename.endsWith(".gz") ) gzip = true;
    try {
      if(urlContext != null) ufile = new URL(urlContext, filename);
      else ufile = new URL(filename);
      file = new File(ufile.getFile());
    } catch (MalformedURLException e) {
      ufile = null;
      file = new File(filename);
    }
  }
/** Set the static urlContext */
  public static void setUrlContext(URL context) {
    urlContext = context;
  }
/** Get the static urlContext */
  public static URL getUrlContext() {
    return urlContext;
  }
/** Check if url path */
  public boolean isURL() {
    return (ufile != null);
  }
/** Check if this is a directory */
  public boolean isDirectory() {
    if( ufile != null ) return false;  // don't consider urls directories
    else if( file != null ) return file.isDirectory();
    else return false;
  }
/** Check if this file exists */
  public boolean exists() throws IOException {
    if(ufile != null) {
      // test for existance by opening a connection
      throw new IOException("Check for url existance not implemented");
    }
    else if( (ufile == null) && (file != null) ) return file.exists();
    else throw new IOException("Tried to check for existance of null file");
  }
/** Returns the parent directory name for a regular file or
    the protocol, host, port and parent for a URL */
  public String getParent() {
    if( (ufile != null) && (file != null) ) {
      String parent =  ufile.getProtocol() + "://" + ufile.getHost();
      if( ufile.getPort() > 0 ) parent += ":" + ufile.getPort();
      parent += File.separator + file.getParent();
      return parent;
    }
    else if( file != null ) return file.getParent();
    else return null;
  }
/** Returns the base name for this file */
  public String getName() {
    if(file == null) return null;
    else return file.getName();
  }
/** Returns the path name for this file.  If it is a url this returns the
*   URL name with no protocal, host, port, or other info */
  public String getPath() {
    if(file == null) return null;
    else return file.getPath();
  }
/**
*   Returns a string value - should be same as creation filename
*/
  public String toString() {
    String str = "";
    if(ufile != null) str = ufile.toString();
    else if (file != null) str = file.toString();
    return str;
  }
/**
 * Replaces \'s with \\.
*/
  public static String quoteSlashes(String str) {
    if(str == null) return null;
    // replace all \'s with \\
    char slash = '\\';
    int offset = str.indexOf(slash);
    while( offset > -1 ) {
      // only if not already doubled
	//      if(str.indexOf(slash, offset+1) != offset+1)
	str = str.substring(0, offset) + slash +
	  str.substring(offset, str.length());
      offset += 2; // past slashes
      if( offset >= str.length() ) break;
      offset = str.indexOf(slash, offset); // search remainder of string
    }
    return str;
  }
/** Check if object represents the same file */
  public boolean sameFile( Object fileObject ) {
    if( fileObject instanceof String ) {
      // convert the string to a File or URL for testing
      String filename = (String) fileObject;
      try {
        fileObject = new URL(filename);
      } catch (MalformedURLException e) {
        fileObject = new File(filename);
      }
    }
    // now check standard file objects
    if( fileObject instanceof URL ) {
      if(ufile == null)return false;
      else return ufile.sameFile((URL) fileObject);
    }
    else if( fileObject instanceof File ) {
      if(ufile != null)return false;
      return ((File) fileObject).equals(file);
    }
    else if( fileObject instanceof CNUFile ) {
      if(ufile != null) return ((CNUFile) fileObject).sameFile( ufile );
      else return ((CNUFile) fileObject).sameFile( file );
    }
    else return false;
  }
/** Get an output writer for this file */
  public PrintWriter getPrintWriter() throws IOException {
    if(file == null) return null;
    return new PrintWriter(getWriter());
  }
/** Get an output writer for this file */
  public Writer getWriter() throws IOException {
    if(file == null) return null;
    return new OutputStreamWriter(getOutputStream());
  }
/** Get an output stream for this file */
  public OutputStream getOutputStream() throws IOException {
    OutputStream outS = null;
    if(file != null) outS = new FileOutputStream(file);
    if(gzip) outS = new GZIPOutputStream(outS);
    return outS;
  }
/** Get an input stream for this file */
  public InputStream getInputStream() throws IOException {
    InputStream inS = null;
    if(ufile != null) inS = ufile.openStream();
    else if(file != null) inS = new FileInputStream(file);
    if(gzip) inS = new GZIPInputStream(inS);
    return inS;
  }
/** Get an input reader for this file */
  public Reader getReader() throws IOException {
    Reader rd = null;
    if(gzip || (ufile != null)) rd = new InputStreamReader( getInputStream() );
    else if(file != null) rd = new FileReader(file);
    return rd;
  }
/** Get a sleep input reader for this file */
  public Reader getSleepReader() throws IOException {
    Reader rd = null;
    if(gzip || (ufile != null))
      rd = new SleepInputStreamReader( getInputStream() );
    else if(file != null) rd = new SleepFileReader(file);
    return rd;
  }
/** Get an input streamTokenizer for this file */
  public StreamTokenizer getStreamTokenizer() throws IOException {
    return new StreamTokenizer( getReader() );
  }
/** Get an image of type supported by Toolkit or javax.imageio.ImageIO from this file */
  public Image getImage() {
    Image image = null;
    try {
      if(ufile != null) image = ImageIO.read(ufile);
      else if(file != null) image = ImageIO.read(file);
    } catch (IOException ioe) {
      image = null;
    }
    if(image == null) image = getPpmImage(); // toolkit attempts prior to this caused it not to be called
    if(image == null) {
      if(ufile != null) image = Toolkit.getDefaultToolkit().getImage(ufile);
      else if(file != null) image = Toolkit.getDefaultToolkit().getImage(file.getPath());
    }
    return image;
  }
/** Get a PPM image from this file */
  private Image getPpmImage() {
    try {
      InputStream ip = getInputStream();
      PpmDecoder pd = new PpmDecoder(ip);
      return Toolkit.getDefaultToolkit().createImage(pd);
    } catch (IOException e) {
      return null;
    }
  }
}
