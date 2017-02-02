package iiv.script;
import iiv.*;
import iiv.display.*;
import iiv.util.*;
import iiv.io.*;
import iiv.data.*;
import iiv.filter.*;
import iiv.dialog.*;
import bsh.*;
import bsh.util.*;
import java.lang.reflect.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import java.awt.image.*;
import javax.swing.*;

/**
 * Class to handle parsing iiV scripts.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see bsh.Interpreter
 * @since	iiV1.17
 */
public class iiVBshScript implements CNUFileObject, Runnable {
  /** Header put at the beginning of all script files */
  public final static String HEADER = "// iiVBshScript";
  /** Beginning of version line */
  public final static String VERSION_LEAD = "// Version ";
  /**
   * The current script version number - should be convertible to a double
   * precision number for comparison reasons.
   * Note - script version should always be less then CNUViewer version number
   * and script version should only increase when new commands are added
   * not just when CNUViewer is updated
   */
  public final static String VERSION_NUMBER = "1.183";
  /** Combined complete version line */
  public final static String VERSION = VERSION_LEAD + VERSION_NUMBER;
  /** List of packages to search for classes in */
  public static String[] iiVPackages = {
    "iiv", "iiv.io", "iiv.script", "iiv.data", "iiv.dialog",
    "iiv.display", "iiv.filter", "iiv.gui", "iiv.util" };
  /** Represents a null object */
  public final static Object NULLOBJ = new Object() {
    public String toString() {return "null";};
  };
  /** Represents an end object */
  public final static Object ENDOBJ =  new Object() {
    public String toString() {return "end";};
  };
  private CNUFile cnufile = null;
  private Reader inReader;
  private CNUViewer cnuv = null;
  private CNUDisplay cnud = null;
  private CNUScriptVariables variables = null;
  /**
   * Constructs a new instance of iiVBshScript
   *
   * @param filename	file to read script from
   * @param cnuv	CNUViewer to apply script to
   * @exception	IOException	thrown on error reading file
   */
  public iiVBshScript(String filename, CNUViewer cnuv)
    throws IOException {
    this.cnuv = cnuv;
    this.cnud = cnuv.getCNUDisplay();
    cnufile = new CNUFile(filename);
    if( ! isiiVBshScript(cnufile.getReader()) )
	throw new IOException("File does not contain an iiV beanshell script");
  }
  /**
   * Constructs a new instance of iiVBshScript.
   *
   * @param filename	file to read script from
   * @exception	IOException	thrown on error reading file
   */
  public iiVBshScript(String filename) throws IOException {
    cnufile = new CNUFile(filename);
    if( ! isiiVBshScript(cnufile.getReader()) )
	throw new IOException("File does not contain an iiV beanshell script");
  }
  /**
   * Constructs a new instance of iiVBshScript to read from a InputStream
   *
   * @param in		input stream to parse script commands from
   * @param cnuv	CNUViewer to apply script to
   */
  public iiVBshScript(InputStream in, CNUViewer cnuv) {
    this.cnuv = cnuv;
    this.cnud = cnuv.getCNUDisplay();
    this.inReader = new SleepInputStreamReader(in);
  }
  /**
   * Constructs a new instance of iiVBshScript to read from a Reader
   *
   * @param in		reader to parse script commands from
   * @param cnuv	CNUViewer to apply script to
   */
  public iiVBshScript(Reader in, CNUViewer cnuv) {
    this.cnuv = cnuv;
    this.cnud = cnuv.getCNUDisplay();
    this.inReader = in;
  }
  /**
   * Sets the viewer to apply parsed commands to.
   *
   * @param cnuv	non-null viewer
   */
  public void setCNUViewer(CNUViewer cnuv) {
    if(cnuv != null) {
      this.cnuv = cnuv;
      this.cnud = cnuv.getCNUDisplay();
    }
  }
  /**
   * Sets the list of script variables with initial values that
   * may be added to and modified by the script.
   *
   * @param variables list of script variables which may be <code>null</code>
   */
  public void setVariables(CNUScriptVariables variables) {
    this.variables = variables;
  }
  /**
   * Gets the list of script variables.
   *
   * @return list of script variables which may be <code>null</code>
   */
  public CNUScriptVariables getVariables() { return variables; }
  /**
   * Finds out if a object represents the same file as that being parsed.
   *
   * @param fileObject	object to compare file names with
   * @return		<code>true</code> if fileObject represents the same file
   */
  public boolean sameFile( Object fileObject ) {
    if(cnufile == null) return false;
    else if( fileObject instanceof iiVBshScript )
      return ( (iiVBshScript) fileObject ).sameFile(cnufile);
    else return cnufile.sameFile(fileObject);
  }
  /**
   * Gets this object if it represents the same file.
   * @param sameFileAsObj	object to compare file names with
   * @return		this object or <code>null</code>
   */
  public Object getFileObject(Object sameFileAsObj) {
    if(sameFile( sameFileAsObj )) return this;
    else return null;
  }
  /**
   * Implements Runnable interface so parsing may be done in a thread.
   */
  public void run() {
    if( (cnufile == null) && (inReader == null) ) return;
    try {
      Object rd = inReader;
      // use sleep readers for threaded processes
      // otherwise reads may block freezing source thread also
      if(rd == null) rd = cnufile.getSleepReader();
      else if(! (rd instanceof Reader) ) ; // ignore invalid object
      else if(rd instanceof PipedReader) ; // don't sleep read pipes
      else if(! (rd instanceof SleepReader) ) {
	rd = new SleepBufferedReader((Reader) rd);
      }
      while (rd instanceof Reader) {
	rd = readComponentsFromScript((Reader) rd, cnud, cnuv, variables,
				      (ShowStatus) cnuv);
      }
    } catch (IOException e) {
      cnuv.showStatus(e);
    }
  }
  /**
   * Adds components to the display based on this script file.
   */
  public synchronized void display() {

    if( (cnud == null) || (cnuv == null) ||
	 ((cnufile == null) && (inReader == null)) ) return;
    try {
      Object rd = inReader;
      if(rd == null) rd = cnufile.getReader();
      while (rd instanceof Reader) {
        rd = readComponentsFromScript((Reader) rd, cnud, cnuv, variables,
				      (ShowStatus) cnuv);
      }
    } catch (IOException e) {
      cnuv.showStatus(e);
    }
  }
  /**
   * Gets the header script.
   *
   * @return	the header script
   */
  public static String getHeaderScript() {
    return HEADER + "\n" + VERSION + "\n";
  }
  /**
   * Checks if an input reader represents a script.
   *
   * @param reader	input to check
   * @return		<code>true</code> if input contained the correct header
   */
  public static boolean isiiVBshScript( Reader reader ) {
    if(reader != null)  try {
      BufferedReader buffreader = new BufferedReader(reader);
      // sanity check - make sure it at least starts with a comment before
      // trusting readline.  Readline could be wastefull with a binary file.
      buffreader.mark(10);
      char[] cc = new char[2];
      if(buffreader.read(cc) == 2) {
	String  scc = new String(cc);
	if("//".equals(scc) || "#!".equals(scc)) {
	  buffreader.reset();
	  // header may start any of the first
	  // 10 lines if the others are #! or // comments
	  for(int i=0; i<10; i++) {
	    String line = buffreader.readLine();
	    if(line == null) break;
	    if(HEADER.equals(line.trim())) {
	      String version = buffreader.readLine();
	      if(version == null) break;
	      if(version.startsWith(VERSION_LEAD)) {
	        version = version.substring(VERSION_LEAD.length()).trim();
		// search for any white spaces inside of version
		for(int index=0; index < version.length(); index++) {
		  if(Character.isWhitespace(version.charAt(index))) {
		    version = version.substring(0, index);
		    break;
		  }
		}
		// version number should be less then this version number
                if((Double.valueOf(VERSION_NUMBER)).doubleValue() <
		   (Double.valueOf(version)).doubleValue())
		  System.out.println("*Warning* iiV script version=" + version +
				     " is greater than current reader version=" + VERSION_NUMBER
				     + ". Some errors may occur.");
		return true;
              }
	    }
	    else if(line.startsWith("#!") || line.startsWith("//")) ; // ignore
	    else break; // unknown line so not a proper script
	  } // end for(int i=0; i<10; i++)
	}
      } // end if(buffreader.read(cc) == 2)
    } catch (IOException e1) {
      // treat IOException as proof of not a script
    }
    return false;
  }
  /**
   * Adds components to the display based on an input script.
   *
   * @param script	input to parse script from
   * @param cnud	place to display objects in
   * @param cnuv	controller associated with display
   * @param ss		where to write status and error messages
   */
  public static final Object readComponentsFromScript(String script,
						      CNUDisplay cnud,
						      CNUViewer cnuv,
						      CNUScriptVariables variables,
						      final ShowStatus ss)
    throws IOException {
    if(script == null) return null;
    Object status = null;
    boolean hideStatus = false;
    if(ss instanceof Component) hideStatus = ! ((Component) ss).isVisible();
    try {
      OutputStream os = new OutputStream() {
	  ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(1024);
	  public void flush() {
	    String str;
	    synchronized(byteArrayStream) {
	      str = byteArrayStream.toString();
	      byteArrayStream.reset();
	    }
	    ss.showStatus(str);
	  }
	  public void write(byte[] b) throws IOException {
	    synchronized(byteArrayStream) { byteArrayStream.write(b); }
	    if(byteArrayStream.size() > 256) flush();
	  }
	  public void write(byte[] b, int off, int len) {
	    synchronized(byteArrayStream) { byteArrayStream.write(b, off, len); }
	    if(byteArrayStream.size() > 256) flush();
	  }
	  public void write(int b) {
	    synchronized(byteArrayStream) { byteArrayStream.write(b); }
	    if(byteArrayStream.size() > 256) flush();
	  }
	  public void close()
	    throws IOException {
	    flush();
	  }
	};
      PrintStream ps = new PrintStream(os, true);
      Interpreter ibsh = new bsh.Interpreter();
      ibsh.setOut(ps); ibsh.setErr(ps);
      initializeBeanShelliiVValues(ibsh, cnud, cnuv);
      status = ibsh.eval(script);
      if(status == null) status = ibsh.get("script_rtn");
      if(hideStatus) ((Component) ss).setVisible(false);
/*    } catch (IOException e) {
      if(ss != null) ss.showStatus(e);
      else throw e;
*/
    } catch (EvalError ee) {
      if(ss != null) ss.showStatus(ee);
      else ee.printStackTrace();
    }
    return status;
  }
  /**
   * Adds components to the display based on an input script.
   *
   * @param reader	input to parse script from
   * @param cnud	place to display objects in
   * @param cnuv	controller associated with display
   * @param ss		where to write status and error messages
   */
  public static final Object readComponentsFromScript( Reader reader,
	CNUDisplay cnud, CNUViewer cnuv,
	CNUScriptVariables variables, final ShowStatus ss)
    throws IOException {
    if(reader == null) return null;
    Object status = null;
    boolean hideStatus = false;
    if(ss instanceof Component) hideStatus = ! ((Component) ss).isVisible();
    try {
      OutputStream os = new OutputStream() {
	  ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(1024);
	  public void flush() {
	    String str;
	    synchronized(byteArrayStream) {
	      str = byteArrayStream.toString();
	      byteArrayStream.reset();
	    }
	    ss.showStatus(str);
	  }
	  public void write(byte[] b) throws IOException {
	    synchronized(byteArrayStream) { byteArrayStream.write(b); }
	    if(byteArrayStream.size() > 256) flush();
	  }
	  public void write(byte[] b, int off, int len) {
	    synchronized(byteArrayStream) { byteArrayStream.write(b, off, len); }
	    if(byteArrayStream.size() > 256) flush();
	  }
	  public void write(int b) {
	    synchronized(byteArrayStream) { byteArrayStream.write(b); }
	    if(byteArrayStream.size() > 256) flush();
	  }
	  public void close()
	    throws IOException {
	    flush();
	  }
	};
      PrintStream ps = new PrintStream(os, true);
      Interpreter ibsh = new bsh.Interpreter();
      ibsh.setOut(ps); ibsh.setErr(ps);
      //      Interpreter ibsh = new bsh.Interpreter(reader, ps, ps, false);
      initializeBeanShelliiVValues(ibsh, cnud, cnuv);
//      ibsh.run();
      status = ibsh.eval(reader);
      if(status == null) status = ibsh.get("script_rtn");
      if(hideStatus) ((Component) ss).setVisible(false);
      /*    } catch (IOException e) {
      if(ss != null) ss.showStatus(e);
      else throw e; */
    } catch (EvalError ee) {
      if(ss != null) ss.showStatus(ee);
      else ee.printStackTrace();
    }
    return status;
  }
  /**
   * Adds components to the display based on an input script.
   *
   * @param jConsole	console bsh.Interpreter will read from
   * @param cnud	place to display objects in
   * @param cnuv	controller associated with display
   */
  public static final void readComponentsFromScript( ConsoleInterface jConsole,
						     CNUDisplay cnud,
						     CNUViewer cnuv)
    throws IOException {
    if(jConsole == null) throw new IOException("null jConsole");
    Interpreter ibsh = new bsh.Interpreter(jConsole);
    initializeBeanShelliiVValues(ibsh, cnud, cnuv);
    ibsh.run();
  }
  /**
   * Initializes a BeanShell Interpreter with standard iiV variables and commands.
   *
   * @param ibsh	interpreter to initialize
   * @param cnud	place to display objects in
   * @param cnuv	controller associated with display
   */
  public static final void initializeBeanShelliiVValues(Interpreter ibsh,
							CNUDisplay cnud,
							CNUViewer cnuv) {
      try {
	if(cnuv != null) ibsh.set("CNUVIEWER", cnuv);
	if(cnud != null) ibsh.set("CNUDISPLAY", cnud);
	// initiate beanshell to invoke iiV commands
	ibsh.eval("invoke(String name, Object[] args) { Object rtn=iiv.script.iiVBshScript.invoke(this.interpreter, this.callstack, name, args); if(rtn instanceof java.lang.Exception) error(rtn); return rtn;};");
      } catch (EvalError ee) {
	if(cnuv != null) cnuv.showStatus(ee);
	else ee.printStackTrace();
      }
  }
  /**
   * Creates a safe string representation for echoing to a status window by
   * quoting white spaces and returns ("\n");
   *
   * @param s	string to create safe value of
   * @return	safe string
   */
  public static final String safeString(String s) {
    StringBuffer sb = new StringBuffer(s);
    boolean stringChanged = false;
    boolean quotesNeeded = false;
    for(int i = sb.length() - 1; i >= 0; i--) {
      char c = sb.charAt(i);
      if(Character.isWhitespace(c)) {
	quotesNeeded = true;
	if(c == '\n') {
	  sb.setCharAt(i, '\\');
	  sb.insert(i+1, 'n');
	  stringChanged = true;
	}
      }
    }
    if(quotesNeeded) {
      if(sb.charAt(0) != '"') {
	sb.insert(0, '"');
	stringChanged = true;
      }
      int last = sb.length() - 1;
      if(sb.charAt(last) != '\"') {
	sb.insert(last+1, '"'); 
	stringChanged = true;
      }
    }
    if(stringChanged) return sb.toString();
    else return s;    
  }
  /**
   * Invokes special iiV script commands.
   *
   * @param command	name of command to invoke
   * @param args	arguments to pass to method
   */
  public static Object invoke(Interpreter interpreter,
			      CallStack callstack,
			      String command,
			      Object[] args) throws InterruptedException {
   // Give a chance for iiVBshScript thread to be interrupted
    if(Thread.currentThread().isInterrupted())
      throw new InterruptedException();
    CNUViewer cnuv=null;
    CNUDisplay cnud=null;
    if(interpreter != null) {
      try {
	cnuv = (CNUViewer) interpreter.get("CNUVIEWER");
	cnud = (CNUDisplay) interpreter.get("CNUDISPLAY");
      } catch (EvalError ee) {}
    }

    // set up to make further processing simpler
    int nps = 0;
    int ncnt = 0;
    int scnt = 0;
    String firstName = null;
    if(args != null) {
      args = Primitive.unwrap(args);
      nps = args.length;
      // count the number of initial numbers
      for(; ncnt < nps; ncnt++)
	if( ! (args[ncnt] instanceof Number) ) break;
      if(ncnt > nps) ncnt = nps;
      // count the number of initial strings
      if(ncnt == 0) {
	for(; scnt < nps; scnt++)
	  if( ! (args[scnt] instanceof String) ) break;
	if(scnt > nps) scnt = nps;
      }
      if(scnt > 0) firstName = (String) args[0];
    }
    //for(int i=0; i<args.length; i++) cnuv.showStatus("args["+i+"]="+args[i]+" "+args[i].getClass());
    //cnuv.showStatus("nps=" + nps + " ncnt=" + ncnt + " scnt=" + scnt);
    // parse and invoke commands
    if( "autoShowStatus".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getStatusWindow().setAutoShow(
					     ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for autoShowStatus");
      }
      else return
	     new LineParseException("invalid parameters for autoShowStatus");
    }
    else if( "backgroundColor".equals( command ) ) {
      Color c = getColor(args);
      if(c != null) cnud.setDisplayBackground(c);
      else return new LineParseException("invalid backgroundColor");
    }
    else if( "color".equals( command ) ) {
      if(nps == 1 && scnt == 1) {
	if("apply".equals(firstName)) cnud.apply(cnud.COLOR_MODEL);
	else if("current".equals(firstName))
	  cnuv.setDefaultColorModel(cnud.getCurrentColorModel());
	else cnuv.setDefaultColorModel(
				       CNUColorModel.readLookupFile(firstName, cnuv) );
      }
      else if(nps == 1 && (args[0] instanceof ColorModel))
	  cnuv.setDefaultColorModel((ColorModel) args[0]);
      else return new LineParseException(
					 "invalid color lookup model specification");
    }
    else if( "colorMap".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	if( "horizontal".equals(firstName) )
	  cnud.addDisplayColorMap(cnuv.getDefaultColorModel(),
				  DisplayColorMap.HORIZONTAL);
	else if( "vertical".equals(firstName) )
	  cnud.addDisplayColorMap(cnuv.getDefaultColorModel(),
				  DisplayColorMap.VERTICAL);
	else if( "quilt".equals(firstName) )
	  cnud.addDisplayColorMapQuilt(cnuv.getDefaultColorModel());
	else return new LineParseException("invalid colorMap specification");
      }
      else return new LineParseException("invalid colorMap specification");
    }
    else if( "columns".equals(command) ) {
      if(nps == 1 && ncnt == 1)
	cnuv.setNumberOfColumns(((Number) args[0]).intValue());
      else return new LineParseException("invalid column value");
    }
    else if( "copy".equals(command) ) {
      if(nps == 0) cnud.copySelectedToClipboard();
      else if(nps == 1 && scnt == 1) {
	if( "script".equals(firstName) )
	  cnud.copySelectedToClipboard(CNUDisplay.SCRIPT_ONLY_COPY_MODE);
	else if( "image".equals(firstName) )
	  cnud.copySelectedToClipboard(CNUDisplay.IMAGE_ONLY_COPY_MODE);
	else if( "text".equals(firstName) )
	  cnud.copySelectedToClipboard(CNUDisplay.TEXT_ONLY_COPY_MODE);
	else return new LineParseException("invalid copy specification");
      }
      else return new LineParseException("invalid copy option");
    }
    else if( "crop".equals(command) ) {
      if(nps == 4 && ncnt == 4) { 
	// set both the default and dialog to current
	int xbeg = ((Number) args[0]).intValue();
	int ybeg = ((Number) args[1]).intValue();
	int xend = ((Number) args[2]).intValue();
	int yend = ((Number) args[3]).intValue();
	int  width = xend - xbeg + 1;
	if(width <= 0) {
	  width = xbeg - xend + 1; xbeg = xend;
	}
	int height = yend - ybeg + 1;
	if(height <= 0) {
	  height = ybeg - yend + 1; ybeg = yend;
	}
	Rectangle cropBox =
	  new Rectangle( xbeg, ybeg, width, height);
	cnuv.setCropBox(cropBox, true);
      }
      else if(nps == 1 && scnt == 1) {
	if("off".equals(firstName)) cnuv.setCropBox(-1, -1, -1, -1, false);
	else if("apply".equals(firstName)) cnud.apply(cnud.CROP);
	else if("current".equals(firstName)) {
	  // set both the default and dialog to current
	  Rectangle cropBox = cnud.getCurrentCropBox();
	  cnuv.setCropBox(cropBox, cnuv.getCropBoxState());
	}
	else if("undo".equals(firstName)) cnud.uncrop();
	else return new LineParseException("invalid crop option");
      }
      else return new LineParseException("invalid crop range");
    }
    else if("display".equals(command)) {
      Object obj = null;
      if(nps == 0) obj = cnuv.getViewObject();
      else if(nps >= 1 && scnt >= 1) {
	Vector<Object> params = new Vector<Object>(args.length);
	for(int i=1; i<args.length; i++) params.add(args[i]);
	obj = CNUDisplayScript.getObject(firstName, params, (ShowStatus) cnuv);
      }
      else if(nps == 1 && scnt == 0 && ncnt == 0) obj = args[0];
      else if(nps == 3 && scnt == 0 && ncnt == 0) {
	if( (args[0] instanceof Component) &&
	    (args[1] instanceof Number) &&
	    (args[2] instanceof Number) ) {
	  cnuv.displayComponent((Component) args[0],
	     new Point(
		((Number) args[1]).intValue() + getXOrigin(interpreter),
		((Number) args[2]).intValue() + getYOrigin(interpreter)
		) );
	  return args[0];
	}
      }
      if(obj != null) {
	cnuv.displayImageData(obj);
	return obj;
      }
      return new LineParseException("invalid display specification");
    }
    else if( "displayClass".equals( command ) ) {
      Object obj = null;
      if(nps >= 1 && scnt >= 1) {
	Vector<Object> params = new Vector<Object>(args.length);
	for(int i=1; i<args.length; i++) params.add(args[i]);
	obj = CNUDisplayScript.getObject(firstName, params, (ShowStatus) cnuv);
      }
      if(obj instanceof Component) {
	cnud.addAndRepaint((Component) obj);
	return obj;
      }
      else return new LineParseException(
					 "invalid display class specification");
    }
    else if( "displayCursor".equals(command) ) {
      if(nps == 0) return new Boolean(cnud.getShowInsertCursor());
      else if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnud.setShowInsertCursor(
				   ((Boolean) args[0]).booleanValue());
	else
	  return new LineParseException("invalid parameter for displayCursor");
      }
      else return new LineParseException("invalid parameters for displayCursor");
    }
    else if( "displayImageData".equals( command ) ) {
      if(nps == 1 && scnt == 0 && ncnt == 0) {
	cnuv.displayImageData(args[0]);
	return args[0];
      }
      else return
	     new LineParseException("invalid display image data specification");
    }
    else if( "displayShowPointLinesFreeze".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setMainShowPointDisplayLinesFreezeState(
				      ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for displayShowPointLinesFreeze");
      }
      else return
	     new LineParseException("invalid parameters for showPointLinesFreeze");
    }
    else if( "displayShowPointLinesRecord".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setMainShowPointDisplayLinesRecordState(
			  ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for displayShowPointLinesRecord");
      }
      else return
	     new LineParseException("invalid parameters for showPointLinesRecord");
    }
    else if( "echo".equals(command) ) {
      if(nps == 0) { if(cnuv != null) cnuv.showStatus(""); }
      else {
	if(cnuv != null) {
	  String s = args[0].toString();
	  for(int i = 1; i < args.length; i++)
	    s += " " + args[i].toString();
	  cnuv.showStatus(s);
	}
	return args[0]; // allows echo $_ to remain
      }
    }
    else if( "file".equals( command ) ) {
      if(nps == 1 && scnt == 1)
	cnuv.displayImageData(cnuv.getDataOrImage(firstName));
      else return new LineParseException("invalid file name");
    }
    else if( "fileType".equals( command ) ) {
      if(nps == 1 && scnt == 1) {
	// add and select the file type
	if( ! cnuv.addFileClass(firstName))
	  return new LineParseException("invalid file type");
      }
      else if(nps == 1 && ncnt == 0) {
	// add and select the file type
	Object obj = args[0];
	if(obj instanceof Class) {
	  if( ! cnuv.addFileClass((Class) obj))
	    return new LineParseException("invalid file type class");
	}
      }
      else return new LineParseException("invalid file type");
    }
    else if( "filterSampling".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	int sampleType = LinearImageFilter.sampleTypeValueOf(firstName);
	if(sampleType == FilterSampling.UNKNOWN_SAMPLE_TYPE)
	  return new LineParseException("invalid filterSampling");
	else DisplayComponentDefaults.setDefaultFilterSampleType(sampleType);
      }
      else return new LineParseException("invalid filterSampling");
    }
    else if( "flip".equals( command ) ) {
      if(scnt == 0) return new LineParseException("invalid flip specificaton");
      else for(; scnt > 0; scnt--) {
	if("vertical".equals(firstName)) cnuv.setDefaultFlipV(true);
	else if("verticalOff".equals(firstName)) cnuv.setDefaultFlipV(false);
	else if("horizontal".equals(firstName)) cnuv.setDefaultFlipH(true);
	else if("horizontalOff".equals(firstName)) cnuv.setDefaultFlipH(false);
	else if("off".equals(firstName)) {
	  cnuv.setDefaultFlipV(false); cnuv.setDefaultFlipH(false);
	}
	else if("apply".equals(firstName)) cnud.apply(cnud.FLIPS);
	else {
	  return new LineParseException("invalid flip specification = " + firstName);
	}
      }
    }
    else if( "font".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	if("off".equals(firstName)) {
	  cnud.setFont(null); // fall back to default
	  DisplayComponentDefaults.setDefaultFont(null);
	}
	else if("apply".equals(firstName)) cnud.updateFont();
	else return new LineParseException("invalid font");
      }
      else if(nps == 3 && scnt == 2 && args[2] instanceof Number) {
	String fontStyleName = (String) args[1];
	int fontStyle = CNUDisplayScript.fontStyleValueOf(fontStyleName);
	int points = ((Number) args[2]).intValue();
	Font font = new Font(firstName, fontStyle, points);
	if(font != null) {
	  DisplayComponentDefaults.setDefaultFont(font);
	  cnud.setFont(font);
	}
	else return new LineParseException("font not found");
      }
      else return new LineParseException("invalid font");
    }
    else if( ("foregroundColor".equals(command)) ||
	     ("textColor".equals(command)) ) {
      Color c = getColor(args);
      if(c != null) cnud.setForeground(c);
      else if(nps == 1 && scnt == 1) {
	// fall back to default
	if("off".equals(firstName)) cnud.setForeground(null);
	else if("apply".equals(firstName))
	  cnud.updateForegroundColor(cnud.getForeground());
	else return new LineParseException("invalid textColor");
      }
      else return new LineParseException("invalid textColor");
    }
    else if( "grid".equals(command) ) {
      if(nps == 2 && ncnt == 1) {
	int units =
	  CNUDisplay.stringToDisplayUnitValue((String) args[1]);
	if(units != CNUDisplay.BAD_UNITS)
	  cnud.setGridSpacing(((Number) args[0]).doubleValue(), units);
	else return new LineParseException("invalid grid units");
      }
      else if(nps == 1 && scnt == 1) {
	if("on".equals(firstName)) cnud.setGridState(true);
	else if("off".equals(firstName)) cnud.setGridState(false);
	else return new LineParseException("invalid grid setting=" + firstName);
      }
      else return new LineParseException("invalid grid spacing");
    }
    else if( "gridColor".equals(command) ) {
      Color c = getColor(args);
      if(c != null) cnud.setGridColor(c);
      else return new LineParseException("invalid gridColor");
    }
    else if( "gridOffsets".equals(command) ) {
      if(nps == 3 && ncnt == 2) {
	int units =
	  CNUDisplay.stringToDisplayUnitValue((String) args[2]);
	if(units != CNUDisplay.BAD_UNITS)
	  cnud.setGridOffset(((Number) args[0]).doubleValue(),
			     ((Number) args[1]).doubleValue(),
			     units);
	else return new LineParseException("invalid grid offsets units");
      }
      else return new LineParseException("invalid grid offsets");
    }
    else if( "hide".equals( command ) ) {
      if(nps == 1 && ncnt == 0) {
	Object obj = args[0];
	if(scnt == 1) {
	  if("menubar".equalsIgnoreCase(firstName)) {
	    cnuv.setMenuBarVisible(false); return "";
	  }
	  else if("tools".equalsIgnoreCase(firstName) ||
		  "toolpanel".equalsIgnoreCase(firstName)) {
	    cnuv.hideToolPanel(); return "";
	  }
	  else obj = cnuv.getNamedDialog(firstName);
	}
	if(obj instanceof Component) {
	  ((Component) obj).setVisible(false);
	  return obj;
	}
	else return new LineParseException("invalid hide parameter");
      }
      else return new LineParseException("invalid hide command");
    }
    else if( "incrementSlices".equals(command) ) {
      if(nps == 1 && ncnt == 1)
	cnud.incrementSlices( ((Number) args[0]).intValue() );
      else return new LineParseException("invalid slice increment");
    }
    else if( "iRange".equals(command) ) {
      if(nps == 2 && ncnt == 2) {
	cnuv.getDisplayComponentDefaults().setDefaultIRange(((Number)args[0]).intValue(),
	    ((Number) args[1]).intValue(), -1);
	cnuv.setIDimLimitMode(true);
      }
      else if(nps == 1 && scnt == 1) {
	if("off".equals(firstName)) cnuv.setIDimLimitMode(false);
	else return new LineParseException("invalid i range");
      }
      else return new LineParseException("invalid i range");
    }
    else if( "help".equals(command)) {
      if(nps == 1 && scnt == 1) {
	String str = helpString(firstName);
	cnuv.showStatus(str);
	return str;
      }
      else if(args.length == 0) {
	String str = helpString();
	cnuv.showStatus(str);
	return str;
      }
      else return new LineParseException("invalid help parameters");
    }
    else if( "justification".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	// fall back to default
	if("left".equals(firstName))
	  cnud.setJustification(DisplayText.LEFT);
	else if("centered".equals(firstName))
	  cnud.setJustification(DisplayText.CENTERED);
	else if("right".equals(firstName))
	  cnud.setJustification(DisplayText.RIGHT);
	else if("apply".equals(firstName))
	  cnud.updateJustification();
	else
	  return new LineParseException("invalid justification=" + firstName);
      }
      else return new LineParseException("invalid justification=");
    }
    else if( "move".equals(command) ) {
      if(nps == 2 && ncnt == 2)
	cnud.translateSelections(
				 new Point(((Number) args[0]).intValue(),
					   ((Number) args[1]).intValue()) );
      else return new LineParseException("invalid move amount");
    }
    else if( "newColorObject".equals(command) ) {
      Color c = getColor(args);
      if(c != null) return c;
      else return new LineParseException("invalid color");
    }
    else if( "newFileObject".equals( command ) ) {
      Object obj = null;
      if(nps >= 2 && scnt >= 1) {
	Object arg1 = args[1];
	Class objectClass = null;
	if(arg1 instanceof Class) objectClass = (Class) arg1;
	else if(arg1 instanceof ClassIdentifier)
	  objectClass = ((ClassIdentifier) arg1).getTargetClass();
	else if(arg1 instanceof String) {
	  try {
	    objectClass = CNUDisplayScript.findClass((String) arg1);
	  } catch (ClassNotFoundException cnfe) {
	    cnuv.showStatus("Class not found");
	    return cnfe;
	  }
	}
	obj = cnuv.getFileObject(firstName); // check already read file
	if(obj != null) {
	  if(objectClass.isInstance(obj)) return obj;
	  else if(obj instanceof SingleImg) {
	    obj = ((SingleImg) obj).getData();
	    if(objectClass.isInstance(obj)) return obj;
	  }
	  obj = null;
	}
	if(obj == null) {
	  Vector<Object> params = new Vector<Object>(args.length);
	  for(int i=2; i<args.length; i++) params.add(args[i]);
	  obj = CNUDisplayScript.getObject(objectClass, params, (ShowStatus) cnuv);
	}
      }
      if(obj != null) return obj;
      else return new LineParseException("error getting file class");
    }
    else if( "numberFormat".equals(command) ) {
      if((nps == 1) && (args[0] instanceof NumberFormat)) {
	DisplayNumberFormat.setDefaultNumberFormat(
						   (NumberFormat) args[0]);
	return args[0];
      }
      else if((nps == 7) && (ncnt == 4) &&
	      (args[4] instanceof Boolean) &&
	      (args[5] instanceof Number) &&
	      (args[6] instanceof String)) {
	int maxIntDigits = ((Number) args[0]).intValue();
	int minIntDigits = ((Number) args[1]).intValue();
	int maxFracDigits =  ((Number) args[2]).intValue();
	int minFracDigits =  ((Number) args[3]).intValue();
	boolean commas = ((Boolean) args[4]).booleanValue();
	int exponentMultiples = ((Number) args[5]).intValue();
	String expSymbol = (String) args[6];
	DisplayNumberFormat dnf =
	  new DisplayNumberFormat(exponentMultiples, expSymbol);
	dnf.setMaximumIntegerDigits(maxIntDigits);
	dnf.setMinimumIntegerDigits(minIntDigits);
	dnf.setMaximumFractionDigits(maxFracDigits);
	dnf.setMinimumFractionDigits(minFracDigits);
	dnf.setGroupingUsed(commas);
	DisplayNumberFormat.setDefaultNumberFormat(dnf);
	return dnf;
      }
      else return new LineParseException("invalid numberFormat paramaters");
    }
    else if( "paper".equals(command) ) {
      if(nps == 3 && ncnt == 2 && (args[2] instanceof String)) {
	int units =
	  CNUDisplay.stringToDisplayUnitValue((String) args[2]);
	if(units != CNUDisplay.BAD_UNITS)
	  cnud.setPaperSize(((Number) args[0]).doubleValue(),
			    ((Number) args[1]).doubleValue(),
			    units);
	else return new LineParseException("invalid paper units");
      }
      else if(nps == 1 && scnt == 1) {
	if("on".equals(firstName)) cnud.setPaperState(true);
	else if("off".equals(firstName)) cnud.setPaperState(false);
	else
	  return new LineParseException("invalid paper setting=" + firstName);
      }
      else return new LineParseException("invalid paper setting");
    }
    else if( "paperColor".equals(command) ) {
      Color c = getColor(args);
      if(c != null) cnud.setPaperColor(c);
      else return new LineParseException("invalid paperColor");
    }
    else if( "paperOffsets".equals(command) ) {
      if(nps == 3 && ncnt == 2 && (args[2] instanceof String)) {
	int units =
	  CNUDisplay.stringToDisplayUnitValue((String) args[2]);
	if(units != CNUDisplay.BAD_UNITS)
	  cnud.setPaperOffset(((Number) args[0]).doubleValue(),
			      ((Number) args[1]).doubleValue(),
			      units);
	else return new LineParseException("invalid paper offsets units");
      }
      else return new LineParseException("invalid paper offsets");
    }
    else if( "paste".equals(command) ) {
      if(nps == 0) cnud.pasteClipboardToDisplay();
      else if(nps == 1 && scnt == 1) {
        if( "script".equals(firstName) ) cnud.pasteScript();
        else if( "image".equals(firstName) ) cnud.pasteImage();
        else if( "text".equals(firstName) ) cnud.pasteText();
        else return new LineParseException("invalid paste specification");
      }
      else return new LineParseException("invalid paste option");
    }
    else if( "position".equals(command) ) {
      if(nps == 2 && ncnt == 2)
	cnud.resetLayout(
			 ((Number) args[0]).intValue() + getXOrigin(interpreter),
			 ((Number) args[1]).intValue() + getYOrigin(interpreter),
			 false);
      else return new LineParseException("invalid position");
    }
    else if( "readanew".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	if("on".equals(firstName)) cnuv.setReadAnewState(true);
	else if("off".equals(firstName)) cnuv.setReadAnewState(false);
	else return new LineParseException("invalid readanew option");
      }
      else return new LineParseException("invalid readanew option");
    }
    else if( "readdisplays".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	if("on".equals(firstName)) cnuv.setReadDisplaysState(true);
	else if("off".equals(firstName)) cnuv.setReadDisplaysState(false);
	else return new LineParseException("invalid readdisplays option");
      }
      else return new LineParseException("invalid readdisplays option");
    }
    else if( "rotate".equals(command) ) {
      if(nps == 1 && ncnt == 1) {
	double rotation = ((Number) args[0]).doubleValue();
	rotation %= 360;
	if(rotation < 0) rotation += 360;
	if(rotation > 180) rotation -= 360;
	cnuv.setDefaultRotation(rotation);
      }
      else if(nps == 1 && scnt == 1) {
	if("apply".equals(firstName)) cnud.apply(CNUDisplay.ROTATION);
	else if("on".equals(firstName)) cnuv.setDefaultRotationState(true);
	else if("off".equals(firstName)) cnuv.setDefaultRotationState(false);
	else return new LineParseException("invalid rotation");
      }
      else return new LineParseException("invalid rotation");
    }
    else if( "save".equals(command) ) {
      if(nps == 2 && scnt == 2) {
	String fileType = firstName;
	String fileName = (String) args[1];
	Dialog savedialog = cnuv.getSaveDialog();
	if(savedialog == null)
	  return new LineParseException("save function not available");
	String methodName;
	Vector<Object> methodparams = new Vector<Object>();
	if("settings".equals(fileType)) {
	  methodName = "saveViewer";
	  methodparams.addElement(cnuv);
	  methodparams.addElement(fileName);
	}
	else {
	  methodName = "saveDisplay";
	  methodparams.addElement(cnud);
	  methodparams.addElement(fileName);
	  methodparams.addElement(fileType);
	  methodparams.addElement(NULLOBJ);
	}
	try {
	  return CNUDisplayScript.callObjectMethod(savedialog, methodName, methodparams, cnuv);
	} catch (InvocationTargetException ite) {
	  return ite;
	} catch (IllegalAccessException iae) {
	  return iae;
	}
      }
      else return new LineParseException("invalid save format or file");
    }
    else if( "saveStatus".equals( command ) ) {
      if(nps == 1 && scnt == 1) cnuv.saveStatus(firstName);
      else return new LineParseException("invalid save status file name");
    }
    else if( "scale".equals(command) ||
	     "translation".equals(command) ||
	     "scaleToRange".equals(command) ||
	     "min".equals(command) ||
	     "minValue".equals(command) ||
	     "max".equals(command) ||
	     "maxValue".equals(command) ||
	     "quant".equals(command) ) {
      Vector<Object> params = new Vector<Object>();
      for(int i=0; i<args.length; i++) params.addElement(args[i]);
      CNUScale scaleToSet =
	CNUDisplayScript.readScaleParams(command, params, null, cnuv, cnud);
      if(scaleToSet != null) {
	// scaling not actually set until EOL or ;
	CNUScale.setDefaultScale(scaleToSet);
	cnuv.setScaleMode(CNUViewer.LAST_SCALING);
      }
    }
    else if( "screenResCorrection".equals( command ) ) {
      if(nps == 1 && ncnt == 1) {
	cnud.setScreenResCorrection(
				    ((Number) args[0]).doubleValue());
      }
      else return new LineParseException("invalid screen resolution correction");
    }
    else if( "select".equals( command ) ) {
      // determine which components to select
      int start = -1;
      int stop = -1;
      if(nps == 1 && ncnt == 1)
	start = stop = ((Number) args[0]).intValue();
      else if(nps == 2 && ncnt == 2) {
	start = ((Number) args[0]).intValue();
	stop = ((Number) args[1]).intValue();
      }
      else if(nps == 1 && scnt == 1) {
	if( "all".equals(firstName) ) { cnud.selectAll(); return ""; }
	else if( "top".equals(firstName) ) {
	  cnud.selectTopComponents(); return "";
	}
	else if( "bottom".equals(firstName) ) {
	  cnud.selectBottomComponents(); return "";
	}
	else if( "first".equals(firstName) ) start = stop = 0;
	else if( "last".equals(firstName) )
	  start = stop = cnud.getDisplayComponentCount() - 1;
	else if( "additions".equals(firstName) ) {
	  cnud.setSelectAdditions(true); return "";
	}
      }
      else if((nps == 1) && (scnt == 0) && (ncnt == 0)) {
	Object obj = args[0];
	if(cnud.isImmediateParentOf(obj)) {
	  cnud.addSelection( (Component) obj );
	  return obj;
	}
      }
      // now select the components
      if( (start >= 0) && (start <= stop) &&
	  (stop < cnud.getDisplayComponentCount()) ) {
	for( ; start <= stop; start++)
	  cnud.addSelection(cnud.getDisplayComponent(start));
	return "";
      }
      return new LineParseException("invalid select range");
    }
    else if( "selectShowPointLines".equals( command ) ) {
      ShowPointDialogInterface spd =
	(ShowPointDialogInterface) cnuv.getShowPointDialog();
      // determine which components to select
      int start = -1;
      int stop = -1;
      if(nps == 1 && ncnt == 1)
	start = stop = ((Number) args[0]).intValue();
      else if(nps == 2 && ncnt == 2) {
	start = ((Number) args[0]).intValue();
	stop = ((Number) args[1]).intValue();
      }
      else if(nps == 1 && scnt == 1) {
	if( "all".equals(firstName) ) { spd.selectAllLines(); return ""; }
	else if( "first".equals(firstName) ) start = stop = 0;
	else if( "last".equals(firstName) )
	  start = stop = spd.getLineCount() - 1;
	else if( "additions".equals(firstName) ) {
	  spd.setSelectAdditions(true); return "";
	}
      }
      else if((nps == 1) && (scnt == 0) && (ncnt == 0)) {
	Object obj = args[0];
	if(obj instanceof ShowPointDisplayLine) {
	  if(spd.containsShowPointLine( (ShowPointDisplayLine) obj )) {
	    spd.addSelection( (ShowPointDisplayLine) obj );
	    return obj;
	  }
	}
      }
      // now select the components
      if(spd.addSelections(start, stop) > 0) return "";
      return new LineParseException("invalid selectShowPointDisplayLines range");
    }
    else if( "setCrosshairColor".equals( command ) ) {
      Color c = getColor(args);
      if(c != null) cnuv.getShowPointController().setCrosshairColor(c);
      else return new LineParseException("invalid crosshair color");
    }
    else if( "show".equals( command ) ) {
      if(nps == 1 && ncnt == 0) {
	Object obj = args[0];
	if(scnt == 1) {
	  if("menubar".equalsIgnoreCase(firstName)) {
	    cnuv.setMenuBarVisible(true); return "";
	  }
	  else if("tools".equalsIgnoreCase(firstName) ||
		  "toolpanel".equalsIgnoreCase(firstName)) {
	    cnuv.showToolPanel(); return "";
	  }
	  else obj = cnuv.getNamedDialog(firstName);
	}
	if(obj instanceof Component) {
	  ((Component) obj).setVisible(true);
	  return obj;
	}
	else return new LineParseException("invalid show parameter");
      }
      else return new LineParseException("invalid show command");
    }
    else if( "showPoint".equals( command ) ) {
      if(ncnt != 0) {
	if(nps == ncnt) {
	  int[] point = new int[ncnt];
	  for(int i=0; i<ncnt; i++)
	    point[i] = ((Number)args[i]).intValue();
	  cnuv.getShowPointController().setCrosshairs(null, point);
	  return "";
	}
      }
      return new LineParseException("invalid showPoint parameters");
    }
    else if( "showPointCrosshair".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setCrosshairState(
	    ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for showPointCrosshair");
      }
      else return
	     new LineParseException("invalid parameters for showPointCrosshair");
    }
    else if( "showPointLinesCrosshair".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setLinesCrosshairState(
	       ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for showPointLinesCrosshair");
      }
      else return
	     new LineParseException("invalid parameters for showPointLinesCrosshair");
    }
    else if( "showPointLinesFreeze".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setLinesFreeze(
	    ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for showPointLinesFreeze");
      }
      else return
	     new LineParseException("invalid parameters for showPointLinesFreeze");
    }
    else if( "showPointLinesRecord".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setLinesRecord(
	    ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for showPointLinesRecord");
      }
      else return
	     new LineParseException("invalid parameters for showPointLinesRecord");
    }
    else if( "showPointLinesTrack".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setLinesTrackingState(
	    ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for showPointLinesTrack");
      }
      else return
	     new LineParseException("invalid parameters for showPointLinesTrack");
    }
    else if( "showPointMapTracking".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setMapTracking(
	    ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for showPointMapTracking");
      }
      else return
	     new LineParseException("invalid parameters for showPointMapTracking");
    }
    else if( "showPointRecord".equals( command ) ) {
      if(nps == 1) {
	if(args[0] instanceof Boolean)
	  cnuv.getShowPointController().setRecordState(
	    ((Boolean) args[0]).booleanValue());
	else return
	       new LineParseException("invalid parameter for showPointRecord");
      }
      else return
	     new LineParseException("invalid parameters for showPointRecord");
    }
    else if( "slices".equals(command) ) {
      if(nps == 2 && ncnt == 2)
	cnuv.setSlices( ((Number) args[0]).intValue(),
			((Number) args[1]).intValue(), -1, true);
      else if(nps == 1 && scnt == 1) {
	if("off".equals(firstName)) cnuv.setSliceLimitMode(false);
	else return new LineParseException("invalid slice range");
      }
      else return new LineParseException("invalid slice range");
    }
    else if( "snap".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	if("top".equals(firstName)) cnud.apply(CNUDisplay.SNAP_TOP);
	else if("bottom".equals(firstName)) cnud.apply(CNUDisplay.SNAP_BOTTOM);
	else if("left".equals(firstName)) cnud.apply(CNUDisplay.SNAP_LEFT);
	else if("right".equals(firstName)) cnud.apply(CNUDisplay.SNAP_RIGHT);
	else return new LineParseException("invalid snap");
      }
      else return new LineParseException("invalid snap");
    }
    else if( "spatialMap".equals( command ) ) {
      if(nps == 1 && scnt == 1) {
	if("apply".equals(firstName)) cnud.updateCoordinateMap();
	else if("current".equals(firstName))
	  LinearCoordinateMap.setDefaultCoordinateMap(
						      cnud.getCoordinateMap());
	else if("off".equals(firstName))
	  LinearCoordinateMap.setDefaultCoordinateMap(null);
	else LinearCoordinateMap.setDefaultCoordinateMap(
	  LinearCoordinateMap.readCoordinateMap(firstName,cnuv,(ShowStatus) cnuv));
      }
      else if(nps == 1 && (args[0] instanceof CoordinateMap))
	LinearCoordinateMap.setDefaultCoordinateMap((CoordinateMap)
						    args[0]);
      else if(nps == 1 && (args[0] == null))
	LinearCoordinateMap.setDefaultCoordinateMap(null);
      else return new LineParseException("invalid spatial map specification");
    }
    else if( "text".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	DisplayText dt = new DisplayText(firstName);
	dt.setJustification(cnud.getJustification());
	cnud.addAndRepaint(dt);
	return dt;
      }
      else return new LineParseException("invalid text");
    }
    else if( "transparentColor".equals( command ) ) {
      if(nps == 1 && ncnt == 1)
	cnuv.setDefaultTransparentColor(((Number) args[0]).intValue());
      else if(nps == 1 && scnt == 1) {
	if("off".equals(firstName)) cnuv.setDefaultTransparentColor(-1);
	else if("apply".equals(firstName)) cnud.apply(cnud.TRANSPARENT_COLOR);
	else return new LineParseException("invalid transparentColor");
      }
      else return new LineParseException("invalid transparentColor");
    }
    else if( "unselect".equals( command ) ) {
      if(nps == 1 && scnt == 1) {
	if("all".equals(firstName)) cnud.clearAllSelections();
	else if("additions".equals(firstName)) cnud.setSelectAdditions(false);
	else return new LineParseException("invalid unselect specification");
      }
      else return new LineParseException("invalid unselect specification");
    }
    else if( "unselectShowPointLines".equals( command ) ) {
      ShowPointDialogInterface spd =
	(ShowPointDialogInterface) cnuv.getShowPointDialog();
      if(nps == 1 && scnt == 1) {
	if("all".equals(firstName)) spd.clearAllSelections();
	else if("additions".equals(firstName)) spd.setSelectAdditions(false);
	else return
	       new LineParseException("invalid unselectShowPointLines specification");
      }
      else return new LineParseException("invalid unselect specification");
    }
    else if( "view".equals(command) ) {
      if(nps == 1 && scnt == 1) {
	if("coronal".equals(firstName))
	  cnuv.setSliceViewMode(CNUDimensions.CORONAL);
	else if("sagittal".equals(firstName))
	  cnuv.setSliceViewMode(CNUDimensions.SAGITTAL);
	else if("transverse".equals(firstName))
	  cnuv.setSliceViewMode(CNUDimensions.TRANSVERSE);
	else return new LineParseException("invalid view option");
      }
      else return new LineParseException("invalid view option");
    }
    else if( "zoom".equals(command) ) {
      if(nps == 1 && ncnt == 1)
	cnuv.setDefaultZoom( ((Number) args[0]).doubleValue()  );
      else if(nps == 2 && ncnt == 2)
	cnuv.setDefaultZoom( ((Number) args[0]).doubleValue(),
			     ((Number) args[1]).doubleValue() );
      else if(nps == 1 && scnt == 1) {
	if("apply".equals(firstName)) cnud.apply(CNUDisplay.ZOOM);
	else if("on".equals(firstName)) cnuv.setDefaultZoomState(true);
	else if("off".equals(firstName)) cnuv.setDefaultZoomState(false);
	else {
	  int sampleType = LinearImageFilter.sampleTypeValueOf(firstName);
	  if(sampleType == FilterSampling.UNKNOWN_SAMPLE_TYPE)
	    return new LineParseException("invalid zoom");
	  else
	    DisplayComponentDefaults.setDefaultFilterSampleType(sampleType);
	}
      }
      else return new LineParseException("invalid zoom");
    }
    // remaining no parameter commands
    else if(nps == 0) {
      if("exit".equals(command)) cnuv.destroy();
      else if("quit".equals(command)) cnuv.destroy();
      else if( "addCrosshairs".equals( command ) )
	cnuv.getShowPointController().addCrosshairTrackers();
      else if( "addShowPointLines".equals( command ) )
	cnuv.getShowPointController().addShowPointLines();
      else if( "addUnrelatedShowPointLine".equals( command ) )
	cnuv.getShowPointController().addShowPointLine(null, null, cnud);
      else if("attachTools".equals(command)) cnuv.attach();
      else if("back".equals(command)) cnud.selectedToBack();
      else if("clearCrosshairs".equals(command) ||
	      "deleteCrosshairs".equals(command))
	cnuv.getShowPointController().deleteCrosshairs();
      else if("clearDisplay".equals(command)) cnud.removeAll(); // clear was same as beanshell command
      else if("clearStatus".equals(command)) cnuv.clearStatus();
      else if("clearUndos".equals(command)) cnuv.getUndoRedo().clearUndos();
      else if("delete".equals(command)) cnud.removeSelections();
      else if("detachTools".equals(command)) cnuv.detach();
      else if("disableStatusEdit".equals(command)) {
	StatusWindow sw = cnuv.getStatusWindow();
	if(sw != null) sw.disableEdit();
      }
      else if("disableUndos".equals(command))
	cnuv.getUndoRedo().setEnabled(false);
      else if( "displayAddShowPointLines".equals( command ) )
	cnuv.getShowPointController().addShowPointLinesToDisplay();
      else if("enableStatusEdit".equals(command)) {
	StatusWindow sw = cnuv.getStatusWindow();
	if(sw != null) sw.enableEdit();
      }
      else if("enableUndos".equals(command))
	cnuv.getUndoRedo().setEnabled(true);
      else if( "freezeCrosshairs".equals( command ) )
	cnuv.getShowPointController().removeCrosshairTrackers();
      else if("front".equals(command)) cnud.selectedToFront();
      else if("garbageCollect".equals(command)) System.gc();
      else if("group".equals(command)) cnud.groupSelectedComponents();
      else if("groupOverlapping".equals(command)) cnud.groupOverlappingComponents();
      else if("hideShowMemory".equals(command))
	cnuv.getShowMemoryDialog().setVisible(false);
      else if("hideShowPoint".equals(command))
	cnuv.getShowPointDialog().setVisible(false);
      else if("hideStatus".equals(command)) cnuv.hideStatus();
      else if("hideTools".equals(command)) cnuv.hideToolPanel();
      else if("labelsApply".equals(command)) cnud.apply(cnud.LABELS);
      else if("labelsOff".equals(command))
	cnuv.setSliceAndOrientationLabels(false, false);
      else if("numberFormatApply".equals(command))
	cnud.apply(cnud.NUMBER_FORMAT);
      else if("orientationLabelsOff".equals(command) ||
	      "orientationLabelOff".equals(command))
	cnuv.setSliceAndOrientationLabels(cnuv.getSliceLabelOn(), false);
      else if("orientationLabelsOn".equals(command) ||
	      "orientationLabelOn".equals(command))
	cnuv.setSliceAndOrientationLabels(cnuv.getSliceLabelOn(), true);
      else if("overlay".equals(command))
	cnud.overlay(cnuv.getViewObject(), false);
      else if("overlayAndGroup".equals(command))
	cnud.overlay(cnuv.getViewObject(), true);
      else if("redo".equals(command)) cnuv.getUndoRedo().redo();
      else if("relayout".equals(command)) cnud.relayout();
      else if( "removeShowPointLines".equals( command ) )
	((ShowPointDialogInterface) cnuv.getShowPointDialog()).removeShowPointLines();
      else if("showShowMemory".equals(command))
	cnuv.getShowMemoryDialog().setVisible(true);
      else if("showShowPoint".equals(command))
	cnuv.getShowPointDialog().setVisible(true);
      else if("showStatus".equals(command)) cnuv.showStatus();
      else if("showTools".equals(command)) cnuv.showToolPanel();
      else if("sliceLabelOff".equals(command) ||
	      "sliceLabelsOff".equals(command) )
	cnuv.setSliceAndOrientationLabels(false,
					  cnuv.getOrientationLabelsOn());
      else if("sliceLabelsOn".equals(command) ||
	      "sliceLabelOn".equals(command) )
	cnuv.setSliceAndOrientationLabels(true,
					  cnuv.getOrientationLabelsOn());
      else if( "startSliceTracking".equals( command ) )
	cnuv.getShowPointController().addSliceTrackers();
      else if( "stopSliceTracking".equals( command ) )
	cnuv.getShowPointController().removeSliceTrackers();
      else if("undo".equals(command)) cnuv.getUndoRedo().undo();
      else if("ungroup".equals(command)) cnud.unGroup();
      else return new LineParseException("invalid command: " + command);
    }
    else return new LineParseException("invalid command: " + command);
    return null;
  }
  /**
   * Parses a single line of script.
   *
   * @param command	first word from line
   * @param params	remaining parameters from the line
   * @param variables	variables
   * @param cnud	object for commands to display to
   * @param cnuv	controller for commands to interface with
   * @param ss		where print messages
  public static final Object parseSingleLine(String command,
					     Vector params,
					     CNUScriptVariables variables,
					     CNUDisplay cnud, CNUViewer cnuv,
					     ShowStatus ss) {
      // set up to make further processing simpler
      int nps = params.size();
      // count the number of initial numbers
      int ncnt = 0;
      for(; ncnt < nps; ncnt++)
	if( ! (params.elementAt(ncnt) instanceof Number) ) break;
      if(ncnt > nps) ncnt = nps;
      // count the number of initial strings
      int scnt = 0;
      if(ncnt == 0) {
        for(; scnt < nps; scnt++)
	  if( ! (params.elementAt(scnt) instanceof String) ) break;
        if(scnt > nps) scnt = nps;
      }
      String firstName = null;
      if(scnt > 0) firstName = (String) params.firstElement();

      else if( "set".equals(command) ) {
	if(nps == 0) {
	  if(ss != null) ss.showStatus(variables.toString());
	}
	else if(nps == 1 && scnt == 1) {
          // sets status to variable
	  return variables.get((String) params.firstElement());
	}
	else if(nps == 2 && scnt >= 1) {
	  variables.setVariable((String) params.firstElement(),
				 args[1]);
	  return args[1]; // sets status to the same
	}
	else return new LineParseException("invalid parameters to set");
      }
      else if( "unset".equals(command) ) {
	if(nps == 1 && scnt == 1) {
	  return variables.removeVariable((String) params.firstElement());
	}
	else return new LineParseException("invalid parameters to unset");
      }

      else if( "+".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  if( (params.firstElement() instanceof Integer) &&
	      (args[1] instanceof Integer) )
	    return new Integer(((Number) params.firstElement()).intValue() +
			       ((Number) args[1]).intValue());
	  else
	    return new Double(((Number) params.firstElement()).doubleValue() +
			      ((Number) args[1]).doubleValue());
	}
	else return new LineParseException("invalid parameters for addition");
      }
      else if( "-".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  if( (params.firstElement() instanceof Integer) &&
	      (args[1] instanceof Integer) )
	    return new Integer(((Number) params.firstElement()).intValue() -
			       ((Number) args[1]).intValue());
	  else
	    return new Double(((Number) params.firstElement()).doubleValue() -
			      ((Number) args[1]).doubleValue());
	}
	else if(nps == 1 && ncnt == 1) {
	  if(params.firstElement() instanceof Integer)
	    return new Integer(-((Number) params.firstElement()).intValue());
	  else
	    return new Double(-((Number) params.firstElement()).doubleValue());
	}
	else return
	 new LineParseException("invalid parameters for subtraction or negation");
      }
      else if( "*".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  if( (params.firstElement() instanceof Integer) &&
	      (args[1] instanceof Integer) )
	    return new Integer(((Number) params.firstElement()).intValue() *
			       ((Number) args[1]).intValue());
	  else
	    return new Double(((Number) params.firstElement()).doubleValue() *
			      ((Number) args[1]).doubleValue());
	}
	else return
	  new LineParseException("invalid parameters for multiplication");
      }
      else if( "/".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  return new Double(((Number) params.firstElement()).doubleValue() /
			    ((Number) args[1]).doubleValue());
	}
	else return
	  new LineParseException("invalid parameters for division");
      }
      else if( "classField".equals(command) ||
	       "staticValue".equals( command) ) {
	Object value = null;
	if(nps == 1 && scnt == 1) value = getStaticValue(firstName, null);
	else if(nps == 2 && scnt == 2) {
          try {
	    Class objectClass = findClass(firstName);
	    value = getStaticValue((String) args[1], objectClass);
	  } catch (ClassNotFoundException cfe) {
	  }
	}
	if(value != null) return value;
	else return new LineParseException("invalid classField parameter");
      }
      else if( "classMethod".equals( command ) ) {
	boolean success = false;
	if(nps >= 2 && scnt >= 2) {
	  params.removeElementAt(0);
	  String methodName = (String) args[0];
	  params.removeElementAt(0);
	  try {
	    return callStaticClassMethod(firstName, methodName, params, cnuv);
	  } catch (ClassNotFoundException cfe) {
	    return cfe; 
	  } catch (InvocationTargetException ite) {
	    return ite; 
	  } catch (IllegalAccessException iae) {
	    return iae; 
	  }
	}
	return new LineParseException("invalid class method specification");
      }

      else if( "new".equals( command ) || "newClassObject".equals( command )) {
	Object obj = null;
	if(nps >= 1 && scnt >= 1) {
	  params.removeElementAt(0);
	  if(firstName.endsWith("[]"))
	    obj = getArray(firstName.substring(0, firstName.length() - 2),
			   params, ss);
	  else obj = getObject(firstName, params, ss);
	}
	if(obj != null) return obj;
	else return new LineParseException("error getting class object");
      }


      else if( "objectMethod".equals( command ) ) {
	boolean success = false;
	if(nps >= 2) {
	  Object obj = args[0];
	  params.removeElementAt(0);
	  if(args[0] instanceof String) {
	    String methodName = (String) args[0];
	    params.removeElementAt(0);
            try {
	      return callObjectMethod(obj, methodName, params, cnuv);
	    } catch (InvocationTargetException ite) {
	      return ite;
	    }  catch (IllegalAccessException iae) {
	      return iae;
	    }
	  }
	}
	return new LineParseException("invalid class method specification");
      }

      else if( "sleep".equals(command) ) {
	if(nps == 1 && ncnt == 1) try {
	  Thread.sleep( ((Number)args[0]).longValue() );
	} catch (InterruptedException ie) {
	  if(ss != null) ss.showStatus("sleep interrupted");
	  return ie;
	}
	else return new LineParseException("invalid sleep time");
      }

    return "";
  }
   */

  /**
   * Parses a single line via a thread.
   *
   * @param command	first word from line
   * @param params	remaining parameters from the line
   * @param variables	variables
   * @param cnud	object for commands to display to
   * @param cnuv	controller for commands to interface with
   * @param ss		where print messages
  public static Thread threadParseSingleLine(final String command,
				      final Vector params,
				      final CNUScriptVariables variables,
				      final CNUDisplay cnud,
				      final CNUViewer cnuv,
				      final ShowStatus ss) {
    final Vector lparams = (Vector) params.clone();
    final CNUScriptVariables lvariables = (CNUScriptVariables) variables.clone();
     Thread parseThread = new Thread() {
       public void run() {
	 parseSingleLine(command, lparams, lvariables,
			 cnud, cnuv, ss);
       }
     };
     parseThread.setDaemon(true);
     parseThread.start();
     return parseThread;
  }
   */
  /**
   * Parses scale values on a single parameter line.
   *
   * @param params	parameters representing a scale object
   * @return		a scale object or <code>null</code>
  public static final CNUScale readScaleParams(String currentName,
					       Vector params,
					       CNUScale scaleToSet,
					       CNUViewer cnuv,
					       CNUDisplay cnud) {
    // allow for multiple scale inputs before setting at EOL or ;
    try {
      while(currentName != null) {
        if( "scale".equals(currentName) ) {
	  if( (args[0] instanceof Double) ) {
            if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
            scaleToSet.setScaleFactor(
		((Number) args[0]).doubleValue());
	    params.removeElementAt(0);
	  }
	  else if(args[0] instanceof String) {
	    currentName = (String) args[0];
	    params.removeElementAt(0);
	    if("default".equals(currentName) || "off".equals(currentName))
	      cnuv.setScaleMode(CNUViewer.DEFAULT_SCALING);
	    else if("positive".equals(currentName))
	      cnuv.setScaleMode(CNUViewer.POSITIVE_SCALING);
	    else if("negative".equals(currentName))
	      cnuv.setScaleMode(CNUViewer.NEGATIVE_SCALING);
	    else if("current".equals(currentName)) {
	      scaleToSet = cnud.getScale().ScaleDuplicate();
	    }
	    else if("toRange".equals(currentName)) {
	      if(scaleToSet == null) scaleToSet = CNUScale.getDefaultScale();
	      scaleToSet.setToFitDataInRange(cnud.getType());
	    }
	    else if("apply".equals(currentName)) {
	      // scaling not normally set until EOL or ;
	      if(scaleToSet != null)
	        CNUScale.setDefaultScale(scaleToSet);
	      cnud.updateScale();
	    }
	  }
	  else if(args[0] instanceof CNUScale) {
	    scaleToSet = (CNUScale) args[0];
	  }
	  else {
	    cnuv.showStatus("invalid scale");
	    return null;
	  }
        }
	else if( "translation".equals(currentName) ) {
	  if( args[0] instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setTranslation(((Number) args[0]).doubleValue());
	    params.removeElementAt(0);
	  }
	  else {
	    return null;
	  }
	}
        else if("scaleToRange".equals(currentName)) {
	  if(scaleToSet == null)
	    scaleToSet = CNUScale.getDefaultScale();
	  if(scaleToSet != null)
	    scaleToSet.setToFitDataInRange(cnud.getType());
        }
        else if( "min".equals(currentName) ) {
	  if( args[0] instanceof Double ) {
            if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    if( args[1] instanceof Double ) {
	      scaleToSet.setThreshMin(((Number) args[0]).doubleValue(),
 				      ((Number) args[1]).doubleValue());
	      params.removeElementAt(0);
	    }
	    else scaleToSet.setThreshMin(((Number) args[0]).doubleValue(),
				         scaleToSet.getThreshMinValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale minimum");
	    return null;
	  }
        }
        else if( "minValue".equals(currentName) ) {
	  if( args[0] instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setThreshMin(scaleToSet.getThreshMin(),
				    ((Number) args[0]).doubleValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale minimum value");
	    return null;
	  }
        }
        else if( "max".equals(currentName) ) {
	  if( args[0] instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    if( args[1] instanceof Double ) {
	      scaleToSet.setThreshMax(((Number) args[0]).doubleValue(),
				    ((Number) args[1]).doubleValue());
	      params.removeElementAt(0);
	    }
	    else scaleToSet.setThreshMax(((Number) args[0]).doubleValue(),
				         scaleToSet.getThreshMaxValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale maximum");
	    return null;
	  }
        }
        else if( "maxValue".equals(currentName) ) {
	  if( args[0] instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setThreshMax(scaleToSet.getThreshMax(),
				    ((Number) args[0]).doubleValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale maximum value");
	    return null;
	  }
        }
        else if( "quant".equals(currentName) ) {
	  if( args[0] instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setQuantification(((Number) args[0]).doubleValue());
	    scaleToSet.setQuantificationState(true); // otherwise ignored
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale quantification");
	    return null;
	  }
        }
	currentName = null;
	if(params.size() > 0)
	  if(args[0] instanceof String) {
	    currentName = (String) args[0];
	    params.removeElementAt(0);
	  }
      } // end while
    } catch (IndexOutOfBoundsException ioobe) {
      cnuv.showStatus("invalid scale");
      scaleToSet = null;
    }
    return scaleToSet;
  }
   */
  /** Array of help strings */
  public final static String ci[][] = {
    {"addCrosshairs"},
    {"addShowPointLines"},
    {"addUnrelatedShowPointLine"},
    {"attachTools"},
    {"autoShowStatus", "boolean"},
    {"back"},
    {"backgroundColor", "Color_object"},
    {"backgroundColor", "\"colorName_string\""},
    {"backgroundColor", "red_integer", "green_integer", "blue_integer"},
    {"clearDisplay"},
    {"clearCrosshairs"},
    {"clearStatus"},
    {"clearUndos"},
    {"color", "\"apply\""}, {"color", "ColorModel_object"},
    {"color", "\"current\""}, {"color", "\"file_name_string\""},
    {"colorMap", "\"horizontal\""}, {"colorMap", "\"quilt\""}, {"colorMap", "\"vertical\""},
    {"columns", "integer"},
    {"copy"},
    {"copy", "\"script\""}, {"copy", "\"image\""}, {"copy", "\"text\""},
    {"crop", "x_beg_integer", "y_beg_integer", "x_end_integer", "y_end_integer"},
    {"crop", "\"apply\""}, {"crop", "\"current\""}, {"crop", "\"off\""},
    {"crop", "\"undo\""},
    {"delete"},
    {"deleteCrosshairs"},
    {"detachTools"},
    {"disableStatusEdit"},
    {"disableUndos"},
    {"display"},
    {"display", "Component_object", "x_location_integer", "y_location_integer"},
    {"display", "\"class_name\"", "[param1]", "[param2]", "[...]"},
    {"display", "displayable_object"},
    {"displayAddShowPointLines"},
    {"displayClass", "\"class_name\"", "[param1]", "[param2]", "[...]"},
    {"displayCursor"},
    {"displayCursor", "boolean"},
    {"displayImageData", "displayable_object"},
    {"displayShowPointLinesFreeze", "boolean"},
    {"displayShowPointLinesRecord", "boolean"},
    {"echo", "[param1]", "[param2]", "[...]"},
    {"enableStatusEdit"},
    {"enableUndos"},
    {"exit"},
    {"file", "\"file_name_string\""},
    {"fileType", "classToSelect"},
    {"fileType", "\"classNameToSelect_string\""},
    {"fileType", "\"raw\""},
    {"fileType", "\"standard\""},
    {"filterSampling", "\"alpha_weighted_interpolate\""},
    {"filterSampling", "\"interpolate\""}, {"filterSampling", "\"replicate\""},
    {"flip", "\"apply\""},
    {"flip", "\"horizontal\""}, {"flip", "\"horizontalOff\""},
    {"flip", "off"},
    {"flip", "\"vertical\""}, {"flip", "\"verticalOff\""},
    {"font", "\"apply\""},
    {"font", "\"type_string\"", "\"style_string\"", "points_integer"},
    {"font", "\"off\""},
    {"foregroundColor", "Color_object"},
    {"foregroundColor", "\"apply\""}, {"foregroundColor", "\"colorName_string\""},
    {"foregroundColor", "\"off\""},
    {"foregroundColor", "red_integer", "green_integer", "blue_integer"},
    {"freezeCrosshairs"},
    {"front"},
    {"garbageCollect"},
    {"group"},
    {"groupOverlapping"},
    {"grid", "\"on\""}, {"grid", "\"off\""},
    {"grid", "spacing_float", "\"units_name\""},
    {"gridColor", "Color_object"}, {"gridColor", "\"colorName_string\""},
    {"gridColor", "red_integer", "green_integer", "blue_integer"},
    {"gridOffsets", "x_float", "y_float", "units_name"},
    {"help"}, {"help", "\"command\""}, {"help", "\"commandBegins*\""},
    {"hide", "component_object"},
    {"hide", "menubar"}, {"hide", "\"tools\""}, {"hide", "\"window_name_string\""},
    {"hideShowMemory"}, {"hideShowPoint"}, {"hideStatus"}, {"hideTools"},
    {"incrementSlices", "amount_integer"},
    {"iRange", "first_integer", "last_integer"},
    {"iRange", "\"off\""},
    {"justification", "\"apply\""},
    {"justification", "\"centered\""}, {"justfication", "\"left\""},
    {"justification", "\"right\""},
    {"labelsApply"}, {"labelsOff"},
    {"move", "x_integer", "y_integer"},
    {"newColorObject",  "\"colorName_string\""},
    {"newColorObject", "red_integer", "green_integer", "blue_integer"},
    {"newFileObject", "\"filename_string\"",
     "\"className_string\"", "[param1]", "[param2]", "[...]"},
    {"numberFormat", "max_int_digits_integer", "min_int_digits_integer",
     "max_fraction_digits_integer", "min_fraction_digits_integer",
     "commas_boolean",
     "exponent_multiples_integer", "\"exponent_symbol_string\""},
    {"numberFormat", "NumberFormat_object"},
    {"numberFormatApply"},
    {"paper", "\"on\""}, {"paper", "\"off\""},
    {"paper", "width_float", "height_float", "\"units_name\""},
    {"paperColor", "Color_object"}, {"paperColor", "\"colorName_string\""},
    {"paperColor", "red_integer", "green_integer", "blue_integer"},
    {"paperOffsets", "x_float", "y_float", "\"units_name\""},
    {"paste"},
    {"paste", "\"image\""},
    {"paste", "\"script\""},
    {"paste", "\"text\""},
    {"position", "x_integer", "y_integer"},
    {"readanew", "\"off\""}, {"readanew", "\"on\""},
    {"readdisplays", "\"off\""}, {"readdisplays", "\"on\""},
    {"rotate", "\"apply\""}, {"rotate", "\"off\""}, {"rotate", "\"on\""},
    {"rotate", "value_float"},
    {"save", "\"gif\"", "\"filename\""}, {"save", "\"ppm\"", "\"filename\""},
    {"save", "\"script\"", "\"filename\""},
    {"save", "\"settings\"", "\"filename\""},
    {"saveStatus", "\"filename\""},
    {"scale", "\"apply\""}, {"scale", "\"current\""}, {"scale", "CNUScale_object"},
    {"scale", "\"default\""}, {"scale", "\"negative\""}, {"scale", "\"positive\""},
    {"scale", "\"toRange\""}, {"scale", "value_float"},	{"scaleToRange"},
    {"screenResCorrection", "factor_number"},
    {"select", "\"additions\""}, {"select", "\"all\""}, {"select", "\"bottom\""},
    {"select", "displayed_object"},
    {"select", "\"first\""},
    {"select", "first_integer", "last_integer"}, {"select", "\"last\""},
    {"select", "\"top\""},
    {"selectShowPointLines", "\"additions\""}, {"selectShowPointLines", "\"all\""},
    {"selectShowPointLines", "displayed_ShowPointDisplayLine_object"},
    {"selectShowPointLines", "\"first\""}, {"selectShowPointLines", "\"last\""},
    {"selectShowPointLines", "first_integer", "last_integer"},
    {"setCrosshairColor", "Color_object"},
    {"setCrosshairColor", "\"colorName_string\""},
    {"setCrosshairColor", "red_integer", "green_integer", "blue_integer"},
    {"show", "\"menubar\""}, {"show", "\"tools\""}, {"show", "\"window_name_string\""},
    {"showPoint", "x_integer", "[y_integer]", "[z_integer]", "[i_integer]"},
    {"showPointCrosshair", "boolean"},
    {"showPointLinesCrosshair", "boolean"},
    {"showPointLinesFreeze", "boolean"},
    {"showPointLinesRecord", "boolean"},
    {"showPointLinesTrack", "boolean"},
    {"showPointMapTracking", "boolean"},
    {"showPointRecord", "boolean"},
    {"slices", "first_integer", "last_integer"}, {"slices", "\"off\""},
    {"snap", "\"top\""}, {"snap", "\"bottom\""},
    {"snap", "\"left\""}, {"snap", "\"right\""},
    {"spatialMap", "\"apply\""}, {"spatialMap", "\"current\""},
    {"spatialMap", "CoordinateMap_object"}, {"spatialMap", "\"filename\""},
    {"spatialMap", "\"off\""},
    {"text", "\"text_string\""},
    {"translation", "value_float"},
    {"transparentColor", "\"apply\""},
    {"transparentColor", "index_byte"}, {"transparentColor", "\"off\""},
    {"unselect", "\"additions\""}, {"unselect", "\"all\""},
    {"unselectShowPointLines", "\"additions\""}, {"unselectShowPointLines", "\"all\""},
    {"view", "\"coronal\""}, {"view", "\"sagittal\""}, {"view", "\"transverse\""},




    {"orientationLabelsOff"}, {"orientationLabelsOn"},
    {"overlay"}, {"overlayAndGroup"},
    {"redo"},
    {"relayout"},
    {"removeShowPointLines"},
    {"showShowMemory"}, {"showShowPoint"}, {"showStatus"}, {"showTools"},
    {"sliceLabelOff"}, {"sliceLabelOn"},
    {"startSliceTracking"},
    {"stopSliceTracking"},
    {"undo"},
    {"ungroup"},
    {"zoom", "\"alpha_weighted_interpolate\""},
    {"zoom", "\"apply\""},
    {"zoom", "\"interpolate\""}, {"zoom", "\"off\""}, {"zoom", "\"on\""},
    {"zoom", "\"replicate\""},
    {"zoom", "value_float"},
    {"zoom", "vertical_float", "horizontal_float"}
  };
  /*
	{"?"}, {"?", "command"}, {"?", "commandBegins*"},
	{"+", "number", "number"},
	{"-", "number"},
	{"-", "number", "number"},
	{"*", "number", "number"},
	{"/", "number", "number"},
	{"classMethod", "class_name", "static_method_name",
	  "[param1]", "[param2]", "[...]"},
	{"classField", "className.fieldName_string"},
	{"classField", "className_string", "fieldName_string"},

	{"end"},

	{"max", "thresh_float", "thresh_value_float"},
	{"max", "thresh_float"},
	{"maxValue", "thresh_value_float"},
	{"min", "thresh_float", "thresh_value_float"},
	{"min", "thresh_float"},
	{"minValue", "thresh_value_float"},
	{"new", "className_string", "[param1]", "[param2]", "[...]"},
	{"new", "Object[]", "[object1]", "[object2]", "[...]"},
	{"new", "String[]", "[string1]", "[string2]", "[...]"},
	{"new", "primativeName_string[]",
	  "[primative1]", "[primative2]", "[...]"},
	{"objectMethod", "object", "methodName_string",
	  "[param1]", "[param2]", "[...]"},
	{"quant", "quantification_factor_float"},
	{"quit"},

	{"set"}, {"set", "variable_name"}, {"set", "variable_name", "value"},
	{"sleep", "milliseconds_integer"},
	{"unset", "variable_name"},

  };
  */
  /**
   * Creates a help string for all commands.
   *
   * @return	help string
   */
  public static final String helpString() {
    String s="iiV Beanshell Commands:\n";
    for(int i = 0; i < ci.length; i++) {
      s += "\t";
      int j = 0;
      s += ci[i][j] + "(";
      j++;
      if(j < ci[i].length) s += " " + ci[i][j];
      for(j++; j < ci[i].length; j++) s += ", " + ci[i][j];
      s += " );\n";
    }
    return s;
  }
  /**
   * Creates a help string for a single command.
   *
   * @param command	command to look up
   * @return		help string for the command
   */
  public static final String helpString(String command) {
    String s = "";
    String startsWith = null;
    if(command == null) return "no help found";
    if(command.length() != 1) {
      int index = command.indexOf('*');
      if(index == (command.length() - 1)) {
	startsWith = command.substring(0, command.length() - 1);
      }
    }
    for(int i = 0; i < ci.length; i++) {
      if(command.equals(ci[i][0])) {
	int j = 0;
	s += ci[i][j] + "(";
	j++;
	if(j < ci[i].length) s += " " + ci[i][j];
	for(j++; j < ci[i].length; j++) s += ", " + ci[i][j];
	s += " );\n";
      }
      else if(startsWith != null) {
        if((ci[i][0]).startsWith(startsWith)) {
	  int j = 0;
	  s += ci[i][j] + "(";
	  j++;
	  if(j < ci[i].length) s += " " + ci[i][j];
	  for(j++; j < ci[i].length; j++) s += ", " + ci[i][j];
	  s += " );\n";
        }
      }
    }
    if("".equals(s)) return "no help found";
    return s;
  }
  /**
   * Parses color values.
   *
   * @param args	arguments representing a color
   * @return		a color or <code>null</code>
   */
  public static final Color getColor(Object[] args) {
    if(args.length == 3)
      if( (args[0] instanceof Number) &&
          (args[1] instanceof Number) &&
	  (args[2] instanceof Number) ) 
        return new Color(((Number) args[0]).intValue(),
		         ((Number) args[1]).intValue(),
		         ((Number) args[2]).intValue());
      else return null;
    else if(args.length == 1) {
      if(args[0] instanceof String)
        return CNUDialog.stringToColor((String) args[0]);
      else if(args[0] instanceof Color)
	return (Color) args[0];
      else return null;
    }
    else return null;
  }

  /**
   * Gets the x origin from the special variable X_ORIGIN.
   *
   * @return	accumulated x origin or 0 if not set or set wrong.
   */
  public static int getXOrigin(Interpreter interpreter) {
    int xOrigin = 0;
    try {
      Object obj = interpreter.get("Y_ORIGIN");
      if(obj instanceof Number) xOrigin += ((Number) obj).intValue();
    } catch (EvalError ee) {}
    return xOrigin;
  }
  /**
   * Gets the y origin from the special variable Y_ORIGIN.
   *
   * @return	accumulated Y origin or 0 if not set or set wrong.
   */
  public static int getYOrigin(Interpreter interpreter) {
    int yOrigin = 0;
    try {
      Object obj = interpreter.get("Y_ORIGIN");
      if(obj instanceof Number) yOrigin += ((Number) obj).intValue();
    } catch (EvalError ee) {}
    return yOrigin;
  }
  /**
   * Returns a script line for a specific font.
   *
   * @param font	font to create script for
   * @return		script to recreate font as default
   */
  public static String fontToScript(Font font) {
    if(font == null) return "";
    String script = "font(\"" + font.getName() + "\", \"";
    script += CNUDisplayScript.fontStyleToString(font);
    script += "\", " + font.getSize() + ");\n";
    return script;
  }
  /**
   * Returns a script line for a CNUScale.
   *
   * @param sc	scale
   * @return	script to recreate scale as the default
   */
  public static String scaleToScript(CNUScale sc) {
    String script = "";
    if(sc == null) script += "null";
    else {
      script = "scale(" + sc.getScaleFactor();
      if(! sc.identity()) {
	if(sc.getTranslation() != 0.0)
	  script += ", \"translation\", " + sc.getTranslation();
        if(sc.getThreshMinState())
	  script += ", \"min\", " + sc.getThreshMin() + ", " + sc.getThreshMinValue();
        if(sc.getThreshMaxState())
	  script += ", \"max\", " + sc.getThreshMax() + ", " + sc.getThreshMaxValue();
      }
      if(sc.getQuantificationState())
	script += ", \"quant\", " + sc.getQuantification();
      script += ")";
    } 
    return script + ";\n";
  }
  /**
   * Returns a script line for a cropbox.
   *
   * @param cropBox	crop box
   * @return		script to recreate crop box as the default
   */
  public static String cropBoxToScript(Rectangle cropBox) {
    String script;
    if(cropBox == null) script = "crop(\"off\");\n";
    else script = "crop(" + cropBox.x + ", " + cropBox.y + ", " +
	   (cropBox.x + cropBox.width - 1) + ", " +
	   (cropBox.y + cropBox.height - 1) + ");\n";
    return script;
  }
  /**
   * Returns a script line for a filter sampling type.
   *
   * @param filterSampleType	filter sample type
   * @return			script to recreate sample type as the default
   */
  public static String filterSampleTypeToScript(int filterSampleType) {
    return "filterSampling(\"" +
      LinearImageFilter.sampleTypeToString(filterSampleType) + "\");\n";
  }
  /**
   * Returns a script line for a rotation.
   *
   * @param rotation	angle in degrees
   * @return		script to recreate rotation as default value
   */
  public static String rotationToScript(double rotation) {
    return "rotate(" + rotation % 360.0 + ");\n";
  }
}
