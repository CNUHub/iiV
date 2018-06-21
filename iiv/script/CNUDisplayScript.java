package iiv.script;
import iiv.*;
import iiv.display.*;
import iiv.util.*;
import iiv.io.*;
import iiv.data.*;
import iiv.filter.*;
import iiv.dialog.*;
import java.lang.reflect.*;
import java.lang.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import java.awt.image.*;

/**
 * Class to handle parsing iiV scripts.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class CNUDisplayScript implements CNUFileObject, Runnable {
  /** Header put at the beginning of all script files */
  public final static String HEADER = "# iiVDisplayScript";
  /** Old header put at the beginning of all script files */
  public final static String OLDHEADER = "# CNUDisplayScript";
  /** Beginning of version line */
  public final static String VERSION_LEAD = "# Version ";
  /**
   * The current script version number - should be convertible to a double
   * precision number for comparison reasons.
   * Note - script version should always be less then CNUViewer version number
   * and script version should only increase when new commands are added
   * not just when CNUViewer is updated
   */
  public final static String VERSION_NUMBER = "1.16";
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
   * Constructs a new instance of CNUDisplayScript
   *
   * @param filename	file to read script from
   * @param cnuv	CNUViewer to apply script to
   * @exception	IOException	thrown on error reading file
   */
  public CNUDisplayScript(String filename, CNUViewer cnuv)
    throws IOException {
    this.cnuv = cnuv;
    this.cnud = cnuv.getCNUDisplay();
    cnufile = new CNUFile(filename);
    if( ! isCNUDisplayScript(cnufile.getReader()) )
	throw new IOException("File does not contain an iiV script");
  }
  /**
   * Constructs a new instance of CNUDisplayScript.
   *
   * @param filename	file to read script from
   * @exception	IOException	thrown on error reading file
   */
  public CNUDisplayScript(String filename) throws IOException {
    cnufile = new CNUFile(filename);
    if( ! isCNUDisplayScript(cnufile.getReader()) )
	throw new IOException("File does not contain an iiV script");
  }
  /**
   * Constructs a new instance of CNUDisplayScript to read from a InputStream
   *
   * @param in		input stream to parse script commands from
   * @param cnuv	CNUViewer to apply script to
   */
  public CNUDisplayScript(InputStream in, CNUViewer cnuv) {
    this.cnuv = cnuv;
    this.cnud = cnuv.getCNUDisplay();
    this.inReader = new SleepInputStreamReader(in);
  }
  /**
   * Constructs a new instance of CNUDisplayScript to read from a Reader
   *
   * @param in		reader to parse script commands from
   * @param cnuv	CNUViewer to apply script to
   */
  public CNUDisplayScript(Reader in, CNUViewer cnuv) {
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
    else if( fileObject instanceof CNUDisplayScript )
      return ( (CNUDisplayScript) fileObject ).sameFile(cnufile);
    else return cnufile.sameFile(fileObject);
  }
  /**
   * Gets this object if it represents the same file.
   * @param sameFileAsObj object to compare file names with
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
   * Checks if an input reader represents a valid script.
   *
   * @param reader	input to check
   * @return		<code>true</code> if input contained the correct header
   */
  public static boolean isCNUDisplayScript( Reader reader ) {
    if(reader != null)  try {
      char[] chdr = new char[HEADER.length()];
      // read header line
      if(reader.read(chdr) == HEADER.length()) {
	// file must start with HEADER
	String shdr = new String(chdr);
        if( HEADER.equals(shdr) || OLDHEADER.equals(shdr) ) {
          char[] cversion = new char[VERSION.length() + 10];
          // reader version line
	  if(reader.read(cversion) > VERSION_LEAD.length()) {
	    String version = new String(cversion);
	    // remove leading and trailing white spaces
	    version = version.trim();
	    if(version.length() > 1) {
	      // version must start with VERSION_LEAD
	      if( version.startsWith(VERSION_LEAD) ) {
	        version = version.substring(VERSION_LEAD.length()).trim();
		// search for any white spaces inside of version
		for(int index=0; index < version.length(); index++) {
		  if(Character.isWhitespace(version.charAt(index))) {
		    version = version.substring(0, index);
		    break;
		  }
		}
		// version number must be less then this version number
                if((Double.valueOf(VERSION_NUMBER)).doubleValue() >=
		   (Double.valueOf(version)).doubleValue()) return true;
              }
	    }
	  }
	}
      }
    } catch (IOException e1) {
      // treat IOException as proof of not a script
    }
    return false;
  }
  /**
   * Get a standard set up StreamTokenizer.
   *
   * @param reader for tokenizer
   * @return standard StreamTokenizer
   */
  public static final StreamTokenizer getStandardTokenizer(Reader reader) {
    StreamTokenizer tokens = new StreamTokenizer(reader);
    tokens.wordChars(':', ':');
    tokens.wordChars('_', '_');
    tokens.wordChars('.', '.');
    tokens.wordChars('?', '?');
    tokens.wordChars('{', '{');
    tokens.wordChars('}', '}');
    tokens.wordChars('<', '<');
    tokens.wordChars('>', '>');
    tokens.wordChars('(', '(');
    tokens.wordChars(')', ')');
    tokens.wordChars('$', '$');
    tokens.wordChars('/', '/');
    tokens.wordChars('+', '+');
    tokens.wordChars('-', '-');
    tokens.wordChars('*', '*');
    tokens.wordChars('@', '@');
    tokens.wordChars('[', '[');
    tokens.wordChars(']', ']');
    tokens.wordChars('&', '&');
    tokens.ordinaryChar(';');
    tokens.whitespaceChars('=', '=');
    tokens.whitespaceChars(',', ',');
    tokens.quoteChar('"');
    // script type comments
    tokens.commentChar('#');  // this works?
    // set C++ type comments
    tokens.slashStarComments(true); // this doesn't seem to work
    tokens.slashSlashComments(true); // this doesn't seem to work

    tokens.eolIsSignificant(true);
    tokens.parseNumbers();
    tokens.wordChars('\\', '\\');
    return tokens;
  }
  /**
   * Reads and interpets a script.
   *
   * @param reader	input to parse script from
  public static final Object readScript(Reader reader, CNUViewer cnuv,
					CNUScriptVariables variables,
					ShowStatus ss)
    throws IOException, LineParseException {
    if(reader == null) return null;
    StreamTokenizer tokens = getStandardTokenizer(reader);
    Vector paramLines = null;
    int sublevel = 0;
    int c = StreamTokenizer.TT_EOL; // arbitrary to enter loop
    while(c != StreamTokenizer.TT_EOF) {
      c = tokens.nextToken();
      // skip EOF, EOL or ; left over by getParameters
      if(c == StreamTokenizer.TT_EOF || c == StreamTokenizer.TT_EOL ||
	 c == ';') continue;
      tokens.pushBack(); // let getParameters deal with this token
      int line = tokens.lineno();
      Vector params = null;
      params = getParameters(tokens, params);
      if((params == null) || (params.size() == 0)) continue;  // goto next line
      if( "{".equals(params.firstElement()) ) {
	sublevel++;
	continue;
      }
      else if( "}".equals(params.firstElement()) ) {
	sublevel--;
	if(sublevel < 0)
	  throw new LineParseException("Mismatched parentheses on line=" + line);
	continue;
      }
      params.insertElementAt(new Integer(line), 0); // keep line number
      params.insertElementAt(new Integer(line), 0); // keep sublevel
      if(sublevel > 0) {
	if(paramLines == null) paramLines = new Vector();
	// keeping parameters for possible reprocessing
	paramLines.addElement(params);
	params = null;
      }
    } // end while
    return paramLines;
  }
   */
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
	CNUScriptVariables variables, ShowStatus ss)
    throws IOException {
    if(reader == null) return null;
    Object status = null;
    Vector<Object> params = new Vector<Object>(10, 0);
    if(variables == null) variables = new CNUScriptVariables(null);
    // some standard predefined variables
    if(cnuv != null) variables.setVariable("CNUVIEWER", cnuv);
    if(cnud != null) variables.setVariable("CNUDISPLAY", cnud);
    boolean hideStatus = false;
    if(ss instanceof Component) hideStatus = ! ((Component) ss).isVisible();
    try {
      StreamTokenizer tokens = getStandardTokenizer(reader);
      int c = StreamTokenizer.TT_EOL; // arbitrary to enter loop
      outofwhileloop:
      while(c != StreamTokenizer.TT_EOF && ! Thread.currentThread().isInterrupted()) {
	try {
	  c = tokens.nextToken();
          // skip EOF, EOL or ; left over by getParameters
	  if(c == StreamTokenizer.TT_EOF || c == StreamTokenizer.TT_EOL ||
	     c == ';') continue;
	  tokens.pushBack(); // let getParameters deal with this token
	  int line = tokens.lineno();
          params.removeAllElements();
	  params = getParameters(tokens, params);
	  if(params.size() == 0) continue;  // goto next line
          if(ss != null) {
	    String echoLine = "[" + reader + ":" + line + "]";
	    for(int i=0; i<params.size(); i++) {
	      echoLine += " " + safeString(params.elementAt(i).toString());
	    }
	    ss.showStatus(echoLine + ";");
          }
	  variables.substituteVariables(params, ss);
	  if(params.firstElement() instanceof String) {
	    String currentCmd = (String) params.firstElement();
	    params.removeElementAt(0);
	    if( "end".equals(currentCmd) ) break outofwhileloop;
	    else if( "replaceScript".equals(currentCmd) ) {
	      if(params.elementAt(0) instanceof String)
		return (new CNUFile((String)params.elementAt(0))).getReader();
	      else if(params.elementAt(0) instanceof CNUFile)
		return ((CNUFile)params.elementAt(0)).getReader();
	      else if(params.elementAt(0) instanceof Reader)
		return params.elementAt(0);
	      else if(ss != null)
		ss.showStatus("invalid replaceScript command");
	      else throw new IOException("invalid replaceScript command");
	    }
	    int ampIndex = params.lastIndexOf("&");
	    if((ampIndex > -1) && (ampIndex == (params.size()-1))) {
		params.removeElementAt(ampIndex);
		if(ss != null)
		  ss.showStatus("running command in seperate thread");
		status = threadParseSingleLine(currentCmd, params, variables,
				               cnud, cnuv, ss);
	    }
	    else status =
	      parseSingleLine(currentCmd, params, variables, cnud, cnuv, ss);
	    if(status == ENDOBJ) break outofwhileloop;
	    else if(status == null) status = NULLOBJ;
	    else if(status instanceof Exception) {
	      if(ss != null) ss.showStatus((Exception) status);
	      else throw new IOException(status.toString());
	    }
	    variables.setVariable("status", status);
	  }
	  else if(ss != null)
	    ss.showStatus("line did not start with a command name");
	  else throw new IOException("line did not start with a command name");
        } catch (IllegalArgumentException iae) {
	  if(ss != null) {
	    ss.showStatus("error processing parameters");
            ss.showStatus(iae);
	  }
	  else throw iae;
        }
      } // end while
      if(hideStatus) ((Component) ss).setVisible(false);

      if(Thread.currentThread().isInterrupted()) {
	if(ss != null) ss.showStatus("parsing script interrupted");
	else throw new IOException("parsing script interrupted");
	status = null;
      }

    } catch (IOException e) {
      if(ss != null) ss.showStatus(e);
      else throw e;
      status = null;
    }
    return status;
  }
  /**
   * Retrieves an array value from an array object.
   *
   * @param obj		object that should be an array
   * @param arrayIndex	index to the array
   * @return		object at index or <code>null</code> if invalid array or
   *			index
   */
  public static final Object getArrayValue(Object obj, int arrayIndex) {
    if( obj == null ) return null;
    if(arrayIndex < 0) return null;
    try {
      // check for object or primitive arrays
      // primitive types need to be wrapped as objects
      if(obj instanceof String[]) return ((String[]) obj)[arrayIndex];
      else if(obj instanceof Object[]) return ((Object[]) obj)[arrayIndex];
      else if(obj instanceof boolean[])
	return new Boolean(((boolean[]) obj)[arrayIndex]);
      else if(obj instanceof char[])
	return new Character(((char[]) obj)[arrayIndex]);
      else if(obj instanceof byte[])
	return new Byte(((byte[]) obj)[arrayIndex]);
      else if(obj instanceof short[]) 
	return new Short(((short[]) obj)[arrayIndex]);
      else if(obj instanceof int[]) 
	return new Integer(((int[]) obj)[arrayIndex]);
      else if(obj instanceof long[]) 
	return new Long(((long[]) obj)[arrayIndex]);
      else if(obj instanceof float[]) 
	return new Float(((float[]) obj)[arrayIndex]);
      else if(obj instanceof double[]) 
	return new Double(((double[]) obj)[arrayIndex]);
    } catch (NumberFormatException nfe) {
    } catch (ArrayIndexOutOfBoundsException aioobe) {
    }
    return null;
  }
  /**
   * Creates a safe string representation for echoing to a status window by
   * quoting white spaces and returns ("\n");
   *
   * @param s	string to create safe value of
   * @return	save string
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
   * Parses a single line of script.
   *
   * @param command	first word from line
   * @param params	remaining parameters from the line
   * @param variables	variables
   * @param cnud	object for commands to display to
   * @param cnuv	controller for commands to interface with
   * @param ss		where print messages
   */
  public static final Object parseSingleLine(String command,
					     Vector<Object> params,
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

      if( "echo".equals(command) ) {
	if(nps == 0) { if(ss != null) ss.showStatus(""); }
	else {
	  if(ss != null) {
	    String s = (params.firstElement()).toString();
	    for(int i = 1; i < nps; i++)
	    s += " " + (params.elementAt(i)).toString();
	    ss.showStatus(s);
	  }
	  return params.firstElement(); // allows echo ${status} to keep status
	}
      }
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
				 params.elementAt(1));
	  return params.elementAt(1); // sets status to the same
	}
	else return new LineParseException("invalid parameters to set");
      }
      else if( "unset".equals(command) ) {
	if(nps == 1 && scnt == 1) {
	  return variables.removeVariable((String) params.firstElement());
	}
	else return new LineParseException("invalid parameters to unset");
      }
      else if("?".equals(command) || "help".equals(command)) {
	if(nps == 1 && scnt == 1) cnuv.showStatus(helpString(firstName));
	else if(nps == 0) cnuv.showStatus(helpString());
	else return new LineParseException("invalid help parameters");
      }
      else if( "+".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  if( (params.firstElement() instanceof Integer) &&
	      (params.elementAt(1) instanceof Integer) )
	    return new Integer(((Number) params.firstElement()).intValue() +
			       ((Number) params.elementAt(1)).intValue());
	  else
	    return new Double(((Number) params.firstElement()).doubleValue() +
			      ((Number) params.elementAt(1)).doubleValue());
	}
	else return new LineParseException("invalid parameters for addition");
      }
      else if( "-".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  if( (params.firstElement() instanceof Integer) &&
	      (params.elementAt(1) instanceof Integer) )
	    return new Integer(((Number) params.firstElement()).intValue() -
			       ((Number) params.elementAt(1)).intValue());
	  else
	    return new Double(((Number) params.firstElement()).doubleValue() -
			      ((Number) params.elementAt(1)).doubleValue());
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
	      (params.elementAt(1) instanceof Integer) )
	    return new Integer(((Number) params.firstElement()).intValue() *
			       ((Number) params.elementAt(1)).intValue());
	  else
	    return new Double(((Number) params.firstElement()).doubleValue() *
			      ((Number) params.elementAt(1)).doubleValue());
	}
	else return
	  new LineParseException("invalid parameters for multiplication");
      }
      else if( "/".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  return new Double(((Number) params.firstElement()).doubleValue() /
			    ((Number) params.elementAt(1)).doubleValue());
	}
	else return
	  new LineParseException("invalid parameters for division");
      }
      else if( "autoShowStatus".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getStatusWindow().setAutoShow(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for autoShowStatus");
	}
	else return
	  new LineParseException("invalid parameters for autoShowStatus");
      }
      else if( "backgroundColor".equals( command ) ) {
	Color c = getColor(params);
	if(c != null) cnud.setDisplayBackground(c);
	else return new LineParseException("invalid backgroundColor");
      }
      else if( "classField".equals(command) ||
	       "staticValue".equals( command) ) {
	Object value = null;
	if(nps == 1 && scnt == 1) value = getStaticValue(firstName, null);
	else if(nps == 2 && scnt == 2) {
          try {
	    Class objectClass = findClass(firstName);
	    value = getStaticValue((String) params.elementAt(1), objectClass);
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
	  String methodName = (String) params.firstElement();
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
      else if( "color".equals( command ) ) {
	if(nps == 1 && scnt == 1) {
	  if("apply".equals(firstName)) cnud.apply(cnud.COLOR_MODEL);
	  else if("current".equals(firstName))
	      cnuv.setDefaultColorModel(cnud.getCurrentColorModel());
	  else cnuv.setDefaultColorModel(
	      CNUColorModel.readLookupFile(firstName, cnuv) );
	}
	else if(nps == 1 && (params.firstElement() instanceof ColorModel))
	  cnuv.setDefaultColorModel((ColorModel) params.firstElement());
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
	  cnuv.setNumberOfColumns(((Number) params.firstElement()).intValue());
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
	  int xbeg = ((Number) params.elementAt(0)).intValue();
	  int ybeg = ((Number) params.elementAt(1)).intValue();
	  int xend = ((Number) params.elementAt(2)).intValue();
	  int yend = ((Number) params.elementAt(3)).intValue();
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
	  params.removeElementAt(0);
	  obj = getObject(firstName, params, ss);
	}
	else if(nps == 1 && scnt == 0 && ncnt == 0) obj = params.elementAt(0);
	else if(nps == 3 && scnt == 0 && ncnt == 0) {
	  if( (params.elementAt(0) instanceof Component) &&
	      (params.elementAt(1) instanceof Number) &&
	      (params.elementAt(2) instanceof Number) ) {
	    cnuv.displayComponent((Component) params.elementAt(0),
	      new Point(
	   ((Number) params.elementAt(1)).intValue() + variables.getXOrigin(),
	   ((Number) params.elementAt(2)).intValue() + variables.getYOrigin()
	      ) );
	    return params.elementAt(0);
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
	  params.removeElementAt(0);
	  obj = getObject(firstName, params, ss);
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
	  if(params.firstElement() instanceof Boolean)
	    cnud.setShowInsertCursor(
	      ((Boolean) params.firstElement()).booleanValue());
	  else
	    return new LineParseException("invalid parameter for displayCursor");
	}
        else return new LineParseException("invalid parameters for displayCursor");
      }
      else if( "displayImageData".equals( command ) ) {
	if(nps == 1 && scnt == 0 && ncnt == 0) {
	  cnuv.displayImageData(params.elementAt(0));
	  return params.elementAt(0);
	}
	else return
	  new LineParseException("invalid display image data specification");
      }
      else if( "displayShowPointLinesFreeze".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setMainShowPointDisplayLinesFreezeState(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for displayShowPointLinesFreeze");
	}
	else return
	  new LineParseException("invalid parameters for showPointLinesFreeze");
      }
      else if( "displayShowPointLinesRecord".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setMainShowPointDisplayLinesRecordState(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for displayShowPointLinesRecord");
	}
	else return
	  new LineParseException("invalid parameters for showPointLinesRecord");
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
	  Object obj = params.elementAt(0);
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
	    return new LineParseException(
	      "invalid flip specification = " + firstName);
	  }
	  params.removeElementAt(0);
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
	else if(nps == 3 && scnt == 2) {
	  String fontStyleName = (String) params.elementAt(1);
	  int fontStyle = fontStyleValueOf(fontStyleName);
	  int points = ((Number) params.elementAt(2)).intValue();
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
	Color c = getColor(params);
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
	    CNUDisplay.stringToDisplayUnitValue((String) params.elementAt(1));
	  if(units != CNUDisplay.BAD_UNITS)
	    cnud.setGridSpacing(((Number) params.elementAt(0)).doubleValue(),
				units);
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
	Color c = getColor(params);
	if(c != null) cnud.setGridColor(c);
	else return new LineParseException("invalid gridColor");
      }
      else if( "gridOffsets".equals(command) ) {
	if(nps == 3 && ncnt == 2) {
	  int units =
	    CNUDisplay.stringToDisplayUnitValue((String) params.elementAt(2));
	  if(units != CNUDisplay.BAD_UNITS)
	    cnud.setGridOffset(((Number) params.elementAt(0)).doubleValue(),
			       ((Number) params.elementAt(1)).doubleValue(),
			       units);
	  else return new LineParseException("invalid grid offsets units");
	}
	else return new LineParseException("invalid grid offsets");
      }
      else if( "hide".equals( command ) ) {
	if(nps == 1 && ncnt == 0) {
	  Object obj = params.firstElement();
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
	  cnud.incrementSlices( ((Number) params.elementAt(0)).intValue() );
	else return new LineParseException("invalid slice increment");
      }
      else if( "iRange".equals(command) ) {
	if(nps == 2 && ncnt == 2) {
	  cnuv.getDisplayComponentDefaults().setDefaultIRange(((Number)params.elementAt(0)).intValue(),
		((Number) params.elementAt(1)).intValue(), -1);
	  cnuv.setIDimLimitMode(true);
	}
	else if(nps == 1 && scnt == 1) {
	  if("off".equals(firstName)) cnuv.setIDimLimitMode(false);
	  else return new LineParseException("invalid i range");
	}
	else return new LineParseException("invalid i range");
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
	    new Point(((Number) params.elementAt(0)).intValue(),
		      ((Number) params.elementAt(1)).intValue()) );
	else return new LineParseException("invalid move amount");
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
      else if( "newColorObject".equals(command) ) {
	Color c = getColor(params);
	if(c != null) return c;
	else return new LineParseException("invalid color");
      }
      else if( "newFileObject".equals( command ) ) {
	Object obj = null;
	if(nps >= 2 && scnt >= 2) {
	  params.removeElementAt(0);
	  String className = (String) params.firstElement();
	  params.removeElementAt(0);
	  obj = cnuv.getFileObject(firstName); // check already read file
	  if(obj != null) {
	    if(ss != null)
	      ss.showStatus("found existing object of class type");
	    Class objectClass;
	    try {
	      objectClass = findClass(className);
	    } catch (ClassNotFoundException cnfe) {
	      cnuv.showStatus("existing object found but Class not found");
	      return cnfe;
	    }
	    if(objectClass.isInstance(obj)) return obj;
	    else if(obj instanceof SingleImg) {
	      obj = ((SingleImg) obj).getData();
	      if(objectClass.isInstance(obj)) return obj;
	    }
	    obj = null;
	  }
	  if(obj == null) {
	    cnuv.showStatus("creating newFileObject from file");
	    obj = getObject(className, params, ss);
	  }
	}
	if(obj != null) return obj;
	else return new LineParseException("error getting file class");
      }
      else if( "numberFormat".equals(command) ) {
	if((nps == 1) && (params.firstElement() instanceof NumberFormat)) {
	  DisplayNumberFormat.setDefaultNumberFormat(
	    (NumberFormat) params.firstElement());
	  return params.firstElement();
	}
	else if((nps == 7) && (ncnt == 4) &&
	   (params.elementAt(4) instanceof Boolean) &&
	   (params.elementAt(5) instanceof Number) &&
	   (params.elementAt(6) instanceof String)) {
	  int maxIntDigits = ((Number) params.elementAt(0)).intValue();
	  int minIntDigits = ((Number) params.elementAt(1)).intValue();
	  int maxFracDigits =  ((Number) params.elementAt(2)).intValue();
	  int minFracDigits =  ((Number) params.elementAt(3)).intValue();
	  boolean commas = ((Boolean) params.elementAt(4)).booleanValue();
	  int exponentMultiples = ((Number) params.elementAt(5)).intValue();
	  String expSymbol = (String) params.elementAt(6);
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
      else if( "objectMethod".equals( command ) ) {
	boolean success = false;
	if(nps >= 2) {
	  Object obj = params.firstElement();
	  params.removeElementAt(0);
	  if(params.firstElement() instanceof String) {
	    String methodName = (String) params.firstElement();
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
      else if( "paper".equals(command) ) {
	if(nps == 3 && ncnt == 2) {
	  int units =
	    CNUDisplay.stringToDisplayUnitValue((String) params.elementAt(2));
	  if(units != CNUDisplay.BAD_UNITS)
	    cnud.setPaperSize(((Number) params.elementAt(0)).doubleValue(),
			      ((Number) params.elementAt(1)).doubleValue(),
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
	Color c = getColor(params);
	if(c != null) cnud.setPaperColor(c);
        else return new LineParseException("invalid paperColor");
      }
      else if( "paperOffsets".equals(command) ) {
	if(nps == 3 && ncnt == 2) {
	  int units =
	    CNUDisplay.stringToDisplayUnitValue((String) params.elementAt(2));
	  if(units != CNUDisplay.BAD_UNITS)
	    cnud.setPaperOffset(((Number) params.elementAt(0)).doubleValue(),
			        ((Number) params.elementAt(1)).doubleValue(),
			        units);
	  else return new LineParseException("invalid paper offsets units");
	}
	else return new LineParseException("invalid paper offsets");
      }
      else if( "position".equals(command) ) {
	if(nps == 2 && ncnt == 2)
	  cnud.resetLayout(
	    ((Number) params.elementAt(0)).intValue() + variables.getXOrigin(),
	    ((Number) params.elementAt(1)).intValue() + variables.getYOrigin(),
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
	  double rotation = ((Number) params.elementAt(0)).doubleValue();
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
	  String fileName = (String) params.elementAt(1);
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
	    return callObjectMethod(savedialog, methodName, methodparams, cnuv);
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
        CNUScale scaleToSet =
	  readScaleParams(command, params, null, cnuv, cnud);
  	if(scaleToSet != null) {
          // scaling not actually set until EOL or ;
          CNUScale.setDefaultScale(scaleToSet);
          cnuv.setScaleMode(CNUViewer.LAST_SCALING);
	}
      }
      else if( "screenResCorrection".equals( command ) ) {
	if(nps == 1 && ncnt == 1) {
	  cnud.setScreenResCorrection(
	    ((Number) params.firstElement()).doubleValue());
	}
	else return new LineParseException("invalid screen resolution correction");
      }
      else if( "select".equals( command ) ) {
	// determine which components to select
	int start = -1;
	int stop = -1;
	if(nps == 1 && ncnt == 1)
	  start = stop = ((Number) params.elementAt(0)).intValue();
	else if(nps == 2 && ncnt == 2) {
	  start = ((Number) params.elementAt(0)).intValue();
	  stop = ((Number) params.elementAt(1)).intValue();
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
	  Object obj = params.firstElement();
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
	  start = stop = ((Number) params.elementAt(0)).intValue();
	else if(nps == 2 && ncnt == 2) {
	  start = ((Number) params.elementAt(0)).intValue();
	  stop = ((Number) params.elementAt(1)).intValue();
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
	  Object obj = params.firstElement();
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
	Color c = getColor(params);
	if(c != null) cnuv.getShowPointController().setCrosshairColor(c);
	else return new LineParseException("invalid crosshair color");
      }
      else if( "show".equals( command ) ) {
	if(nps == 1 && ncnt == 0) {
	  Object obj = params.firstElement();
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
	      point[i] = ((Number)params.elementAt(i)).intValue();
	    cnuv.getShowPointController().setCrosshairs(null, point);
	    return "";
	  }
	}
	return new LineParseException("invalid showPoint parameters");
      }
      else if( "showPointCrosshair".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setCrosshairState(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for showPointCrosshair");
	}
	else return
	  new LineParseException("invalid parameters for showPointCrosshair");
      }
      else if( "showPointLinesCrosshair".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setLinesCrosshairState(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for showPointLinesCrosshair");
	}
	else return
	  new LineParseException("invalid parameters for showPointLinesCrosshair");
      }
      else if( "showPointLinesFreeze".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	      cnuv.getShowPointController().setLinesFreeze(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for showPointLinesFreeze");
	}
	else return
	  new LineParseException("invalid parameters for showPointLinesFreeze");
      }
      else if( "showPointLinesRecord".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setLinesRecord(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for showPointLinesRecord");
	}
	else return
	  new LineParseException("invalid parameters for showPointLinesRecord");
      }
      else if( "showPointLinesTrack".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setLinesTrackingState(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for showPointLinesTrack");
	}
	else return
	  new LineParseException("invalid parameters for showPointLinesTrack");
      }
      else if( "showPointMapTracking".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setMapTracking(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for showPointMapTracking");
	}
	else return
	  new LineParseException("invalid parameters for showPointMapTracking");
      }
      else if( "showPointRecord".equals( command ) ) {
	if(nps == 1) {
	  if(params.firstElement() instanceof Boolean)
	    cnuv.getShowPointController().setRecordState(
	      ((Boolean) params.firstElement()).booleanValue());
	  else return
	    new LineParseException("invalid parameter for showPointRecord");
	}
	else return
	  new LineParseException("invalid parameters for showPointRecord");
      }
      else if( "sleep".equals(command) ) {
	if(nps == 1 && ncnt == 1) try {
	  Thread.sleep( ((Number)params.elementAt(0)).longValue() );
	} catch (InterruptedException ie) {
	  if(ss != null) ss.showStatus("sleep interrupted");
	  return ie;
	}
	else return new LineParseException("invalid sleep time");
      }
      else if( "slices".equals(command) ) {
	if(nps == 2 && ncnt == 2)
	   cnuv.setSlices( ((Number) params.elementAt(0)).intValue(),
			   ((Number) params.elementAt(1)).intValue(), -1, true);
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
	    LinearCoordinateMap.readCoordinateMap(firstName,cnuv,ss));
	}
	else if(nps == 1 && (params.firstElement() instanceof CoordinateMap))
	  LinearCoordinateMap.setDefaultCoordinateMap((CoordinateMap)
	    params.firstElement());
	else if(nps == 1 && (params.firstElement() == null))
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
	  cnuv.setDefaultTransparentColor(
	    ((Number) params.elementAt(0)).intValue());
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
	  int viewMode = CNUDimensions.orientationValueOf(firstName);
	  if(viewMode != CNUDimensions.UNKNOWN) cnuv.setSliceViewMode(viewMode);
	  else return new LineParseException("invalid view option");
	}
	else return new LineParseException("invalid view option");
      }
      else if( "zoom".equals(command) ) {
	if(nps == 1 && ncnt == 1)
	  cnuv.setDefaultZoom( ((Number) params.elementAt(0)).doubleValue()  );
	else if(nps == 2 && ncnt == 2)
	  cnuv.setDefaultZoom( ((Number) params.elementAt(0)).doubleValue(),
			       ((Number) params.elementAt(1)).doubleValue() );
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
	else if("clear".equals(command)) cnud.removeAll();
	else if("clearCrosshairs".equals(command) ||
		"deleteCrosshairs".equals(command))
	  cnuv.getShowPointController().deleteCrosshairs();
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
	else if("redo".equals(command)) cnuv.getUndoRedo().redo();
	else if("relayout".equals(command)) cnud.relayout();
        else if( "removeShowPointLines".equals( command ) )
	  ((ShowPointDialogInterface) cnuv.getShowPointDialog()).removeShowPointLines();
	else if("showShowPoint".equals(command))
		cnuv.getShowPointDialog().setVisible(true);
	else if("showShowMemory".equals(command))
		cnuv.getShowMemoryDialog().setVisible(true);
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
	else return new LineParseException("invalid command");
      }
      else return new LineParseException("invalid command name");
    return "";
  }
  /**
   * Parses a single line via a thread.
   *
   * @param command	first word from line
   * @param params	remaining parameters from the line
   * @param variables	variables
   * @param cnud	object for commands to display to
   * @param cnuv	controller for commands to interface with
   * @param ss		where print messages
   */
  public static Thread threadParseSingleLine(final String command,
				      final Vector<Object> params,
				      final CNUScriptVariables variables,
				      final CNUDisplay cnud,
				      final CNUViewer cnuv,
				      final ShowStatus ss) {
    //    final Vector<Object> lparams = (Vector<Object>) params.clone();
    final Vector<Object> lparams = new Vector<Object>(params);
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
  /**
   * Parses color values.
   *
   * @param params	parameters representing a color
   * @return		a color or <code>null</code>
   */
  public static final Color getColor(Vector<Object> params) {
    if(params.size() == 3)
      if( (params.elementAt(0) instanceof Number) &&
          (params.elementAt(1) instanceof Number) &&
	  (params.elementAt(2) instanceof Number) ) 
        return new Color(((Number) params.elementAt(0)).intValue(),
		         ((Number) params.elementAt(1)).intValue(),
		         ((Number) params.elementAt(2)).intValue());
      else return null;
    else if(params.size() == 1) {
      if(params.firstElement() instanceof String)
        return CNUDialog.stringToColor((String) params.firstElement());
      else if(params.firstElement() instanceof Color)
	return (Color) params.firstElement();
      else return null;
    }
    else return null;
  }
  /**
   * Parses scale values on a single parameter line.
   *
   * @param params	parameters representing a scale object
   * @return		a scale object or <code>null</code>
   */
  public static final CNUScale readScaleParams(String currentName,
					       Vector<Object> params,
					       CNUScale scaleToSet,
					       CNUViewer cnuv,
					       CNUDisplay cnud) {
    // allow for multiple scale inputs before setting at EOL or ;
    try {
      while(currentName != null) {
        if( "scale".equals(currentName) ) {
	  if( (params.elementAt(0) instanceof Double) ) {
            if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
            scaleToSet.setScaleFactor(
		((Number) params.elementAt(0)).doubleValue());
	    params.removeElementAt(0);
	  }
	  else if(params.elementAt(0) instanceof String) {
	    currentName = (String) params.elementAt(0);
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
	  else if(params.firstElement() instanceof CNUScale) {
	    scaleToSet = (CNUScale) params.firstElement();
	  }
	  else {
	    cnuv.showStatus("invalid scale");
	    return null;
	  }
        }
	else if( "translation".equals(currentName) ) {
	  if( params.elementAt(0) instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setTranslation(((Number) params.elementAt(0)).doubleValue());
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
	  if( params.elementAt(0) instanceof Double ) {
            if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    if( params.elementAt(1) instanceof Double ) {
	      scaleToSet.setThreshMin(((Number) params.elementAt(0)).doubleValue(),
 				      ((Number) params.elementAt(1)).doubleValue());
	      params.removeElementAt(0);
	    }
	    else scaleToSet.setThreshMin(((Number) params.elementAt(0)).doubleValue(),
				         scaleToSet.getThreshMinValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale minimum");
	    return null;
	  }
        }
        else if( "minValue".equals(currentName) ) {
	  if( params.elementAt(0) instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setThreshMin(scaleToSet.getThreshMin(),
				    ((Number) params.elementAt(0)).doubleValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale minimum value");
	    return null;
	  }
        }
        else if( "max".equals(currentName) ) {
	  if( params.elementAt(0) instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    if( params.elementAt(1) instanceof Double ) {
	      scaleToSet.setThreshMax(((Number) params.elementAt(0)).doubleValue(),
				    ((Number) params.elementAt(1)).doubleValue());
	      params.removeElementAt(0);
	    }
	    else scaleToSet.setThreshMax(((Number) params.elementAt(0)).doubleValue(),
				         scaleToSet.getThreshMaxValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale maximum");
	    return null;
	  }
        }
        else if( "maxValue".equals(currentName) ) {
	  if( params.elementAt(0) instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setThreshMax(scaleToSet.getThreshMax(),
				    ((Number) params.elementAt(0)).doubleValue());
	    params.removeElementAt(0);
	  }
	  else {
	    cnuv.showStatus("invalid scale maximum value");
	    return null;
	  }
        }
        else if( "quant".equals(currentName) ) {
	  if( params.elementAt(0) instanceof Double ) {
	    if(scaleToSet == null) scaleToSet = new CNUScale(1.0);
	    scaleToSet.setQuantification(((Number) params.elementAt(0)).doubleValue());
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
	  if(params.firstElement() instanceof String) {
	    currentName = (String) params.firstElement();
	    params.removeElementAt(0);
	  }
      } // end while
    } catch (IndexOutOfBoundsException ioobe) {
      cnuv.showStatus("invalid scale");
      scaleToSet = null;
    }
    return scaleToSet;
  }
  /** Array of help strings */
  public final static String ci[][] = {
	{"?"}, {"?", "command"}, {"?", "commandBegins*"},
	{"+", "number", "number"},
	{"-", "number"},
	{"-", "number", "number"},
	{"*", "number", "number"},
	{"/", "number", "number"},
	{"addShowPointLines"},
	{"addUnrelatedShowPointLine"},
	{"addCrosshairs"},
	{"attachTools"},
	{"autoShowStatus", "boolean"},
	{"back"},
	{"backgroundColor", "Color_object"},
	{"backgroundColor", "colorName_string"},
	{"backgroundColor", "red_integer", "green_integer", "blue_integer"},
	{"classMethod", "class_name", "static_method_name",
	  "[param1]", "[param2]", "[...]"},
	{"classField", "className.fieldName_string"},
	{"classField", "className_string", "fieldName_string"},
	{"clear"},
	{"clearCrosshairs"},
	{"clearStatus"},
	{"clearUndos"},
	{"color", "apply"}, {"color", "ColorModel_object"},
	{"color", "current"}, {"color", "file_name_string"},
	{"colorMap", "horizontal"}, {"colorMap", "quilt"},
	{"colorMap", "vertical"},
	{"columns", "integer"},
	{"copy"},
	{"copy", "script"},
	{"copy", "image"},
	{"copy", "text"},
	{"crop", "x_beg_integer", "y_beg_integer",
	  "x_end_integer", "y_end_integer"},
	{"crop", "apply"}, {"crop", "current"}, {"crop", "off"},
	{"crop", "undo"},
	{"delete"},
	{"deleteCrosshairs"},
	{"detachTools"},
	{"disableStatusEdit"},
	{"disableUndos"},
	{"display"},
	{"display", "Component_object", "x_location_integer", "y_location_integer"},
	{"display", "class_name", "[param1]", "[param2]", "[...]"},
	{"display", "displayable_object"},
	{"displayAddShowPointLines"},
	{"displayClass", "class_name", "[param1]", "[param2]", "[...]"},
	{"displayCursor"},
	{"displayCursor", "boolean"},
	{"displayImageData", "displayable_object"},
	{"displayShowPointLinesFreeze", "boolean"},
	{"displayShowPointLinesRecord", "boolean"},
	{"echo", "[param1]", "[param2]", "[...]"},
	{"enableUndos"},
	{"enableStatusEdit"},
	{"end"},
	{"exit"},
	{"file", "file_name_string"},
	{"fileType", "classToSelect"},
	{"fileType", "classNameToSelect_string"},
	{"fileType", "raw"},
	{"fileType", "standard"},
	{"filterSampling", "alpha_weighted_interpolate"},
	{"filterSampling", "interpolate"}, {"filterSampling", "replicate"},
	{"flip", "apply"},
	{"flip", "horizontal"}, {"flip", "horizontalOff"},
	{"flip", "off"},
	{"flip", "vertical"}, {"flip", "verticalOff"},
	{"font", "apply"},
	{"font", "type_string", "style_string", "points_integer"},
	{"font", "off"},
	{"foregroundColor", "Color_object"},
	{"foregroundColor", "apply"}, {"foregroundColor", "colorName_string"},
	{"foregroundColor", "off"},
	{"foregroundColor", "red_integer", "green_integer", "blue_integer"},
	{"freezeCrosshairs"},
	{"front"},
	{"garbageCollect"},
	{"grid", "on"}, {"grid", "off"},
	{"grid", "spacing_float", "units_name"},
	{"gridColor", "Color_object"}, {"gridColor", "colorName_string"},
	{"gridColor", "red_integer", "green_integer", "blue_integer"},
	{"gridOffsets", "x_float", "y_float", "units_name"},
	{"group"},
	{"groupOverlapping"},
	{"help"}, {"help", "command"},
	{"help"}, {"help", "commandBegins*"},
	{"hide", "component_object"},
	{"hide", "menubar"}, {"hide", "tools"}, {"hide", "window_name_string"},
	{"hideShowMemory"}, {"hideShowPoint"}, {"hideStatus"}, {"hideTools"},
	{"incrementSlices", "amount_integer"},
	{"iRange", "first_integer", "last_integer"},
	{"iRange", "off"},
	{"justification", "centered"}, {"justfication", "left"},
	{"justification", "right"},
	{"labelsApply"}, {"labelsOff"},
	{"max", "thresh_float", "thresh_value_float"},
	{"max", "thresh_float"},
	{"maxValue", "thresh_value_float"},
	{"min", "thresh_float", "thresh_value_float"},
	{"min", "thresh_float"},
	{"minValue", "thresh_value_float"},
	{"move", "x_integer", "y_integer"},
	{"new", "className_string", "[param1]", "[param2]", "[...]"},
	{"new", "Object[]", "[object1]", "[object2]", "[...]"},
	{"new", "String[]", "[string1]", "[string2]", "[...]"},
	{"new", "primativeName_string[]",
	  "[primative1]", "[primative2]", "[...]"},
	{"newColorObject",  "colorName_string"},
	{"newColorObject", "red_integer", "green_integer", "blue_integer"},
	{"newFileObject", "filename_string",
	  "className_string", "[param1]", "[param2]", "[...]"},
	{"numberFormat", "max_int_digits_integer", "min_int_digits_integer",
	 "max_fraction_digits_integer", "min_fraction_digits_integer",
	 "commas_boolean",
	 "exponent_multiples_integer", "exponent_symbol_string"},
	{"numberFormat", "NumberFormat_object"},
	{"numberFormatApply"},
	{"objectMethod", "object", "methodName_string",
	  "[param1]", "[param2]", "[...]"},
	{"orientationLabelsOff"}, {"orientationLabelsOn"},
	{"overlay"}, {"overlayAndGroup"},
	{"paper", "on"}, {"paper", "off"},
	{"paper", "width_float", "height_float", "units_name"},
	{"paperColor", "Color_object"}, {"paperColor", "colorName_string"},
	{"paperColor", "red_integer", "green_integer", "blue_integer"},
	{"paperOffsets", "x_float", "y_float", "units_name"},
	{"paste"},
	{"paste", "script"},
	{"paste", "image"},
	{"paste", "text"},
	{"position", "x_integer", "y_integer"},
	{"quant", "quantification_factor_float"},
	{"quit"},
	{"readanew", "off"}, {"readanew", "on"}, {"readdisplays", "off"},
	{"readdisplays", "on"},
	{"redo"},
	{"relayout"},
	{"removeShowPointLines"},
	{"replaceScript", "filename"},
	{"replaceScript", "CNUFile_object"},
	{"replaceScript", "Reader_object"},
	{"rotate", "apply"}, {"rotate", "off"}, {"rotate", "on"},
	{"rotate", "value_float"},
	{"save", "gif", "filename"}, {"save", "ppm", "filename"},
	{"save", "script", "filename"},
	{"save", "settings", "filename"},
	{"saveStatus", "filename"},
	{"scale", "apply"}, {"scale", "current"}, {"scale", "CNUScale_object"},
	{"scale", "default"}, {"scale", "negative"}, {"scale", "positive"},
	{"scale", "toRange"}, {"scale", "value_float"},	{"scaleToRange"},
	{"screenResCorrection", "factor_number"},
	{"select", "additions"}, {"select", "all"}, {"select", "bottom"},
	{"select", "displayed_object"},
	{"select", "first"},
	{"select", "first_integer", "last_integer"}, {"select", "last"},
	{"select", "top"},
	{"selectShowPointLines", "additions"}, {"selectShowPointLines", "all"},
	{"selectShowPointLines", "displayed_ShowPointDisplayLine_object"},
	{"selectShowPointLines", "first"}, {"selectShowPointLines", "last"},
	{"selectShowPointLines", "first_integer", "last_integer"},
	{"set"}, {"set", "variable_name"}, {"set", "variable_name", "value"},
	{"setCrosshairColor", "Color_object"},
	{"setCrosshairColor", "colorName_string"},
	{"setCrosshairColor", "red_integer", "green_integer", "blue_integer"},
	{"show", "menubar"}, {"show", "tools"}, {"show", "window_name_string"},
	{"showPoint", "x_integer", "[y_integer]", "[z_integer]", "[i_integer]"},
	{"showPointCrosshair", "boolean"},
	{"showPointLinesCrosshair", "boolean"},
	{"showPointLinesFreeze", "boolean"},
	{"showPointLinesRecord", "boolean"},
	{"showPointLinesTrack", "boolean"},
	{"showPointMapTracking", "boolean"},
	{"showPointRecord", "boolean"},
	{"showShowMemory"}, {"showShowPoint"}, {"showStatus"}, {"showTools"},
	{"sleep", "milliseconds_integer"},
	{"sliceLabelOff"}, {"sliceLabelOn"},
	{"slices", "first_integer", "last_integer"}, {"slices", "off"},
	{"snap", "top"}, {"snap", "bottom"},
	{"snap", "left"}, {"snap", "right"},
	{"spatialMap", "apply"}, {"spatialMap", "current"},
	{"spatialMap", "CoordinateMap_object"}, {"spatialMap", "filename"},
	{"spatialMap", "off"},
	{"startSliceTracking"},
	{"stopSliceTracking"},
	{"text", "text_string"},
	{"translation", "value_float"},
	{"transparentColor", "apply"},
	{"transparentColor", "index_byte"}, {"transparentColor", "off"},
	{"undo"},
	{"ungroup"},
	{"unselect", "additions"}, {"unselect", "all"},
	{"unselectShowPointLines", "additions"}, {"unselectShowPointLines", "all"},
	{"unset", "variable_name"},
	{"view", "coronal"}, {"view", "sagittal"}, {"view", "transverse"},
	{"view", "xy_slice"}, {"view", "xz_slice"}, {"view", "yx_slice"},
	{"view", "yz_slice"}, {"view", "zx_slice"}, {"view", "zy_slice"},
	{"zoom", "alpha_weighted_interpolate"},
	{"zoom", "apply"},
	{"zoom", "interpolate"}, {"zoom", "off"}, {"zoom", "on"},
	{"zoom", "replicate"},
	{"zoom", "value_float"},
	{"zoom", "vertical_float", "horizontal_float"}
  };
  /**
   * Creates a help string for all commands.
   *
   * @return	help string
   */
  public static final String helpString() {
    String s="CNUViewer Commands:\n";
    for(int i = 0; i < ci.length; i++) {
      s += "\t";
      for(int j = 0; j < ci[i].length; j++) s += " " + ci[i][j];
      s += ";\n";
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
	for(int j = 0; j < ci[i].length; j++) s += " " + ci[i][j];
	s += ";\n";
      }
      else if(startsWith != null) {
        if((ci[i][0]).startsWith(startsWith)) {
	  for(int j = 0; j < ci[i].length; j++) s += " " + ci[i][j];
	  s += ";\n";
        }
      }
    }
    if("".equals(s)) return "no help found";
    return s;
  }
  /**
   * Gets number values from a StreamTokenizer into an array.
   *
   * @param tokens	stream tokenizer
   * @param values	array to hold values
   */
  public static final int getNumberTokens(StreamTokenizer tokens,
	 double[] values)
    throws IOException {
     int c = tokens.nextToken();
     int cnt = 0;
     while((c == StreamTokenizer.TT_NUMBER) && (cnt < values.length)) {
       values[cnt] = tokens.nval;
       c = tokens.nextToken();
       // exponents not handled so handle them myself
       if(c == StreamTokenizer.TT_WORD) {
	 if(tokens.sval.length() >= 2) {
	   if( (Character.toLowerCase(tokens.sval.charAt(0)) == 'e') &&
	       ( (Character.isDigit(tokens.sval.charAt(1))) ||
	         (tokens.sval.charAt(1) == '-') ||
		 (tokens.sval.charAt(1) == '+') ) ) {
	     // maybe an exponent
	     try {
	       Double expon = Double.valueOf("1" + tokens.sval);
	       values[cnt] *= expon.doubleValue();
               c = tokens.nextToken();
	     } catch (NumberFormatException e) { } // not exponent
	   }
	 }
       } // end if(c == StreamTokenizer.TT_WORD)
       cnt++;
     } // end while((c == StreamTokenizer.TT_NUMBER) &&
     // push back unused last token
     tokens.pushBack();
     return cnt;
  }
  /**
   * Checks a parameter list for static values.
   *
   * @param params	parameter list
   * @param testClass	class to check for static values in,
   *			may be <code>null</code>
   * @return		input parameter vector with static values substituted for
   */
  public static Vector<Object> checkForStaticValues(Vector<Object> params, Class testClass) {
    if(params == null) return null;
    for(int i = 0; i < params.size(); i++) {
      if(params.elementAt(i) instanceof String) {
	Object obj = getStaticValue((String) params.elementAt(i), testClass);
	if(obj != null) params.setElementAt(obj, i);
      }
    }
    return params;
  }
  /**
   * Searches standard iiv packages for a class.
   *
   * @param s		string that represents a class
   * @return		class found
   * @exception ClassNotFoundException Exception thrown if class not found
   */
  public static Class findClass(String s)
    throws ClassNotFoundException {
    return findClass(s, iiVPackages);
  }
  /**
   * Searches packages for a class.
   *
   * @param s		string that represents a class
   * @param testPackages packages to look for class in	
   *			may be <code>null</code>
   * @return		class found
   * @exception ClassNotFoundException Exception thrown if class not found
   */
  public static Class findClass(String s, String[] testPackages)
    throws ClassNotFoundException {
    if(s == null) throw new ClassNotFoundException("null class name");
    if(testPackages == null) return Class.forName(s);
    StringBuffer sb = new StringBuffer();
    boolean testRawNameLast = true;
    if(s.indexOf('.') > 1) {
      testRawNameLast = false;
      try {
	return Class.forName(s);
      } catch (ClassNotFoundException cnfe) {
	sb.append(cnfe.getMessage());
      }
    }
    for(int i=0; i<testPackages.length; i++) {
      try {
	return Class.forName(testPackages[i] + "." + s);
      } catch (ClassNotFoundException cnfe) {
	sb.append(cnfe.getMessage());
      }
    }
    if(testRawNameLast) {
      try {
	return Class.forName(s);
      } catch (ClassNotFoundException cnfe) {
	sb.append(cnfe.getMessage());
      }
    }
    throw new ClassNotFoundException(sb.toString());
  }
  /**
   * Tries to evaluate a string as an static class value.
   *
   * @param s		string that may represent a static value
   * @param testClass	class to check for static values in,
   *			may be <code>null</code>
   * @return		static value found or <code>null</code>
   */
  public static Object getStaticValue(String s, Class testClass) {
    if(s == null) return null;
    try {
      String className = null;
      int index;
      if((index = s.indexOf('/')) < 0) { // class strings shouldn't contain /
	if((index = s.lastIndexOf('.')) > 0) {
	  className = s.substring(0, index);
	  testClass = findClass(className);
	  s = s.substring(index + 1);
	}
      }
      if(testClass != null) {
	Field f = testClass.getField(s);
	if(f != null) {
	  if(Modifier.isStatic(f.getModifiers())) {
	    return f.get(null);
	  }
	}
      }
    } catch (ClassNotFoundException cnfe) {
    } catch (NoSuchFieldException nsfe) {
    } catch (SecurityException se) {
    } catch (IllegalArgumentException iae) {
    } catch (IllegalAccessException iae1) {
    }
    return null;
  }
  /**
   * Tries to evaluate a string as an object field value.
   *
   * @param obj		object to evaluate field on
   * @param fieldName	name of field to evaluate
   * @return		field value found or <code>null</code>
   */
  public static Object getObjectFieldValue(Object obj, String fieldName) {
    Class<?> testClass = null;
    if(fieldName == null || obj == null) return null;
    try {
      testClass = obj.getClass();
      String subFieldName = null;
      int index = fieldName.indexOf('.');
      if(index >= 0) {
	if((index == 0) || (index == fieldName.length())) return null;
	subFieldName = fieldName.substring(index + 1, fieldName.length());
	fieldName = fieldName.substring(0, index);
      }
      if(fieldName.endsWith("()")) {
	index = fieldName.length() - 2;
	if(index <= 0) return null;
	fieldName = fieldName.substring(0, index);
	// simple method with no parameters
	Method m = testClass.getMethod(fieldName, new Class[0]);
        if(m != null) {
	  obj = m.invoke(obj, new Object[0]);
	  if(subFieldName != null) return getObjectFieldValue(obj, subFieldName);
	  return obj;
        }
      }
      else {
        Field f = testClass.getField(fieldName);
        if(f != null) {
	  obj = f.get(obj);
	  if(subFieldName != null) return getObjectFieldValue(obj, subFieldName);
	  return obj;
        }
      }
    } catch (NoSuchMethodException nsme) {
      System.out.println("method not found");
      Method[] meths = testClass.getClass().getMethods();
      if(meths.length == 0) System.out.println("No methods");
      else for(int j=0; j<meths.length; j++) System.out.println(meths[j]);
    } catch (InvocationTargetException ite) {
    } catch (NoSuchFieldException nsfe) {
      if("length".equals(fieldName)) {
	// possible request for length of an array
	int l = getArrayLength(obj);
	if(l > -1) return new Integer(l);
      }
      System.out.println("field not found - valid fields=");
      Field[] fds = testClass.getFields();
      if(fds.length == 0) System.out.println("No fields");
      else for(int j=0; j<fds.length; j++) System.out.println(fds[j]);
    } catch (SecurityException se) {
    } catch (IllegalArgumentException iae) {
    } catch (IllegalAccessException iae1) {
    }
    return null;
  }
  /**
   * Gets the length of an unknown array.
   *
   * @param obj	object that may be an array of unknown type
   * @return	length of array or <code>-1</code> if not an array
   */
  public static int getArrayLength(Object obj) {
    // possible request for length of an array
    int l = -1;
    if(obj instanceof Object[]) l = ((Object[]) obj).length;
    else if(obj instanceof boolean[]) l = ((boolean[]) obj).length;
    else if(obj instanceof char[]) l = ((char[]) obj).length;
    else if(obj instanceof byte[]) l = ((byte[]) obj).length;
    else if(obj instanceof short[]) l = ((short[]) obj).length;
    else if(obj instanceof int[]) l = ((int[]) obj).length;
    else if(obj instanceof long[]) l = ((long[]) obj).length;
    else if(obj instanceof float[]) l = ((float[]) obj).length;
    else if(obj instanceof double[]) l = ((double[]) obj).length;
    return l;
  }
  /**
   * Parses method parameters from a StreamTokenizer.
   *
   * @param tokens	tokens to parse
   * @param params	vector to return parameters in, may be <code>null</code>
   * @return		vector of parameters
   */
  public static Vector<Object> getParameters(StreamTokenizer tokens, Vector<Object> params)
    throws IOException {
    if(params == null) params = new Vector<Object>(5, 0);
    double[] values = new double[5];
    int c = tokens.nextToken();
    int bracketCount = 0;
    while(c != StreamTokenizer.TT_EOF) {
      if(c == StreamTokenizer.TT_EOL) break; // end loop at uncommented EOL
      else if(c == ';') break; // end loop - equivalent to end of line (EOL)
      else if(c == StreamTokenizer.TT_WORD || c == '"') {
	if("\\".equals(tokens.sval)) {
	  c = tokens.nextToken(); // ignore next EOL
	  if(c != StreamTokenizer.TT_EOL) tokens.pushBack();
	}
	// ignore words surrounded by [] such as at the beginning of lines
	else if(tokens.sval.startsWith("[")) {
	  bracketCount++;
	  if(tokens.sval.endsWith("]")) bracketCount--;
	}
	else if(bracketCount > 0) {
	  // ignore word
	  if(tokens.sval.endsWith("]")) bracketCount--;
	}
	else if("true".equalsIgnoreCase(tokens.sval))
	  params.addElement(new Boolean(true));
	else if("false".equalsIgnoreCase(tokens.sval))
	  params.addElement(new Boolean(false));
	else if("null".equals(tokens.sval)) params.addElement(NULLOBJ);
	else params.addElement(tokens.sval);
      }
      else if(bracketCount > 0) ; // ignore comments words
      else if(c == StreamTokenizer.TT_NUMBER) {
	tokens.pushBack();
	int ncnt = getNumberTokens(tokens, values);
	for(int i = 0; i < ncnt; i++)
	  params.addElement(new Double(values[i]));
      }
      else params.addElement(new Character((char)c));
      c = tokens.nextToken();
    }
    // push back unused last token
    tokens.pushBack();
    return params;
  }
  /**
   * Tests and converts a list of parameters against a list of parameter types.
   *
   * @param params	parameters to test
   * @param paramTypes	parameter types parameters should correspond with
   * @return		new array of objects that may be used as parameters for
   *			the parameter types or <code>null</code>
   */
  public static Object[] testParameters(Vector<Object> params, Class[] paramTypes) {
    if(paramTypes.length != params.size()) return null;
    Object[] paramValues = new Object[paramTypes.length];
    // check if params are appropriate
    for(int p=0; (p < paramTypes.length); p++) {
      paramValues[p] = params.elementAt(p);
      if(paramTypes[p].isInstance(paramValues[p])) ;
      else if(paramTypes[p].isPrimitive()){
        if(paramValues[p] instanceof Boolean) {
	  if(paramTypes[p] == Boolean.TYPE) ; // success
	  else return null;
        }
        else if(paramValues[p] instanceof Character) {
	  // Character match precisely
	  if(paramTypes[p] == Character.TYPE) ; // success
	  else return null;
        }
        else if(paramValues[p] instanceof Byte) {
	  // Byte match precisely, why ?
	  if(paramTypes[p] == Byte.TYPE) ; // success
	  else return null;
        }
        else if(paramValues[p] instanceof Short) {
	  // Short match precisely, why ?
	  if(paramTypes[p] == Short.TYPE) ; // success
	  else return null;
        }
        else if(paramValues[p] instanceof Long) {
	  // Long match precisely, why ?
	  if(paramTypes[p] == Long.TYPE) ; // success
	  else return null;
        }
        else if(paramValues[p] instanceof Number) {
	  // the remaining number types Double, Float or Integer
	  if(paramTypes[p] == Double.TYPE) {
	    paramValues[p] =
	      new Double(((Number)paramValues[p]).doubleValue());
	  }
	  else if(paramTypes[p] == Float.TYPE) {
	    paramValues[p] =
	      new Float(((Number)paramValues[p]).floatValue());
	  }
	  else if(paramTypes[p] == Long.TYPE) {
	    paramValues[p] =
	      new Long(((Number)paramValues[p]).longValue());
          }
	  else if(paramTypes[p] == Integer.TYPE) {
	    paramValues[p] =
	      new Integer(((Number)paramValues[p]).intValue());
	  }
          else if(paramTypes[p] == Short.TYPE) {
	    paramValues[p] =
	      new Short(((Number)paramValues[p]).shortValue());
	  }
	  else if(paramTypes[p] == Byte.TYPE) {
	    paramValues[p] =
	      new Byte(((Number)paramValues[p]).byteValue());
	  }
	  else return null;
        }
        else return null;
      } // end if primitive
      else if(paramValues[p] == NULLOBJ) {
	// non-primitives can be set to null
	paramValues[p] = null;
      }
      else return null;
    }
    return paramValues;
  }
  /**
   * Creates an array of objects or primitive types.
   *
   * @param className	primitive name or class name to build array of
   * @param params	values to put in the array
   * @param ss		where to write status messages and errors
   * @return		new array of type or <code>null</code>
   */
  public static Object getArray(String className, Vector params, ShowStatus ss) {
    int nps = params.size();
    Object array = null;
    if("Object".equals(className)) {
      array = new Object[nps];
      for(int i = 0; i < nps; i++) ((Object[]) array)[i] = params.elementAt(i);
    }
    else if("String".equals(className)) {
      array = new String[nps];
      for(int i = 0; i < nps; i++)
	((String[]) array)[i] = params.elementAt(i).toString();
    }
    else if("boolean".equals(className)) {
      array = new boolean[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(obj instanceof Boolean)
	  ((boolean[]) array)[i] = ((Boolean) obj).booleanValue();
	else return null;
      }
    }
    else if("char".equals(className)) {
      array = new char[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(obj instanceof Character)
	  ((char[]) array)[i] = ((Character) obj).charValue();
	else if(obj instanceof String) {
	  if( ((String) obj).length() == 1)
	    ((char[]) array)[i] = ((String) obj).charAt(0);
	  else return null;
	}
	else return null;
      }
    }
    else if("byte".equals(className)) {
      array = new byte[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(! (obj instanceof Number) ) return null;
	else ((byte[]) array)[i] = ((Number) obj).byteValue();
      }
    }
    else if("short".equals(className)) {
      array = new short[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(! (obj instanceof Number) ) return null;
	else ((short[]) array)[i] = ((Number) obj).shortValue();
      }
    }
    else if("int".equals(className)) {
      array = new int[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(! (obj instanceof Number) ) return null;
	else ((int[]) array)[i] = ((Number) obj).intValue();
      }
    }
    else if("long".equals(className)) {
      array = new long[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(! (obj instanceof Number) ) return null;
	else ((long[]) array)[i] = ((Number) obj).longValue();
      }
    }
    else if("float".equals(className)) {
      array = new float[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(! (obj instanceof Number) ) return null;
	else ((float[]) array)[i] = ((Number) obj).floatValue();
      }
    }
    else if("double".equals(className)) {
      array = new double[nps];
      for(int i = 0; i < nps; i++) {
	Object obj = params.elementAt(i);
	if(! (obj instanceof Number) ) return null;
	else ((double[]) array)[i] = ((Number) obj).doubleValue();
      }
    }
    else {
      try {
	// get the class
	Class objectClass = findClass(className);
	array = new Object[nps];
	for(int i = 0; i < nps; i++) {
	  Object obj = params.elementAt(i);
	  if(! (objectClass.isInstance(obj)) ) return null;
	  else ((Object[]) array)[i] = obj;
	}
      } catch (ClassNotFoundException cfe) {
	if(ss != null) ss.showStatus(cfe);
	return null;
      }
    }
    return array;
  }
  /**
   * Creates an object of the given class name with given parameters.
   *
   * @param className	name of class to create instance of
   * @param params	parameters to instantiat class with
   * @param ss		where to write error messages to
   * @return		new instance of class or <code>null</code>
   */
  public static Object getObject(String className, Vector<Object> params,
				 ShowStatus ss) {
    try {
      // get the class
      Class objectClass = findClass(className);
      // construct and return the object
      return getObject(objectClass, params);
    } catch (ClassNotFoundException cfe) {
      if(ss != null) ss.showStatus(cfe);
    } catch (ObjectConstructionException oce) {
      if(ss != null) ss.showStatus(oce);
    }
    return null;
  }
  /**
   * Creates an object of the given class name with given parameters.
   *
   * @param objectClass	class to create instance of
   * @param params	parameters to instantiat class with
   * @param ss		where to write error messages to
   * @return		new instance of class or <code>null</code>
   */
  public static Object getObject(Class objectClass, Vector<Object> params,
				 ShowStatus ss) {
    try {
      // construct and return the object
      return getObject(objectClass, params);
    } catch (ObjectConstructionException oce) {
      if(ss != null) ss.showStatus(oce);
    }
    return null;
  }
  /**
   * Creates an object of the given class name with given parameters from
   * a string tokenizer.
   *
   * @param className	name of class to create instance of
   * @param tokens	parameters to instantiat class with
   * @param ss		where to write error messages to
   * @return		new instance of class or <code>null</code>
   */
  public static Object getObject(String className, StreamTokenizer tokens,
				 ShowStatus ss) throws IOException {
    // get the constructor parameters
    Vector<Object> params = getParameters(tokens, null);
    // show the class and constructor parameters
    if(ss != null) {
      String s = className;
      for(int i=0; i<params.size(); i++)
	s += " '" + params.elementAt(i) + "'";
      ss.showStatus(s);
    }
    return getObject(className, params, ss);
  }
  /**
   * Creates an object of the given class with given parameters.
   *
   * @param objectClass	class to create instance of
   * @param params	parameters to instantiat class with
   * @return		new instance of class
   * @exception ObjectConstructionException	thrown on error costructing
   */
  public static Object getObject(Class objectClass, Vector<Object> params)
    throws ObjectConstructionException {
    try {
      // check parameters for static values
      params = checkForStaticValues(params, objectClass);
      // look for the first constructor that match the parameters
      // getConstructors finds only constructors declared public
      Constructor[] constructors = objectClass.getConstructors();
      // getDeclaredConstructors not allowed by Netscape java
      // but will find constructors that may be valid
      //      if(constructors.length < 1)
      //	constructors = objectClass.getDeclaredConstructors();
      for(int i=0; i<constructors.length; i++) {
	Class[] paramTypes = constructors[i].getParameterTypes();
	if(paramTypes.length == params.size()) {
	  Object[] paramValues = testParameters(params, paramTypes);
	  if(paramValues != null) {
	    // construct and return a new instance of this class
	    return(constructors[i].newInstance(paramValues));
	  }
	}
      }
      String s = "invalid constructor for " + objectClass +
	"\nproper constructors=";
        for(int i=0; i<constructors.length; i++)
	  s += "\n" + constructors[i].toString();
      throw new ObjectConstructionException(s);
    } catch (InvocationTargetException ite) {
      throw new ObjectConstructionException(
	  ite.toString() + " for class=" + objectClass);
    } catch (InstantiationException ie) {
      throw new ObjectConstructionException(
	  ie.toString() + " for class=" + objectClass);
    } catch (IllegalAccessException iae) {
      throw new ObjectConstructionException(
	  iae.toString() + " for class=" + objectClass);
    }
  }
  /**
   * Calls an object method.
   *
   * @param obj		object to invoke method of
   * @param methodName	name of method to invoke
   * @param params	parameters to method
   * @param ss		where to write error messages
   * @return		object returned from invoking method or <code>null</code>
   * @exception InvocationTargetException thrown on error invoking
   * @exception IllegalAccessException thrown on error invoking
   */
  public static Object callObjectMethod(Object obj, String methodName,
		Vector<Object> params, ShowStatus ss)
	throws InvocationTargetException, IllegalAccessException {
    if(obj == null || methodName == null || params == null) return null;
    Class objectClass = obj.getClass();
    params = checkForStaticValues(params, objectClass);
    // now look for the first method that match the parameters
    Method[] methods = objectClass.getMethods();
    for(int i=0; i<methods.length; i++) {
      Class[] paramTypes = methods[i].getParameterTypes();
      if(methodName.equals(methods[i].getName()) &&
	(paramTypes.length == params.size()) ) {
	Object[] paramValues = testParameters(params, paramTypes);
	if(paramValues != null) {
	  return methods[i].invoke(obj, paramValues);
	}
      }
    }
    if(ss != null) {
	ss.showStatus("invalid method for " + objectClass);
	//	ss.showStatus("\nproper methods=");
	//        for(int i=0; i<methods.length; i++)
	//	  ss.showStatus(methods[i].toString());
    }
    return null;
  }
  /**
   * Calls a static class method for a given class, method, and params.
   *
   * @param className	name of class to invoke static method of
   * @param methodName	name of method to invoke
   * @param params	parameters to method
   * @param ss		where to write error messages
   * @return		object returned from invoking method or <code>null</code>
   * @exception ClassNotFoundException thrown on error finding class
   * @exception InvocationTargetException thrown on error invoking
   * @exception IllegalAccessException thrown on error invoking
   */
  public static Object callStaticClassMethod(String className,
	String methodName,
	Vector<Object> params, ShowStatus ss)
	throws ClassNotFoundException, InvocationTargetException,
	IllegalAccessException {
    Class objectClass = findClass(className);
    params = checkForStaticValues(params, objectClass);
    // now look for the first method that match the parameters
    Method[] methods = objectClass.getMethods();
    for(int i=0; i<methods.length; i++) {
      Class[] paramTypes = methods[i].getParameterTypes();
      if(methodName.equals(methods[i].getName()) &&
	(paramTypes.length == params.size())
	 && Modifier.isStatic(methods[i].getModifiers())) {
	Object[] paramValues = testParameters(params, paramTypes);
	if(paramValues != null) {
	  return methods[i].invoke(null, paramValues);
	}
      }
    }
    if(ss != null) ss.showStatus("invalid method for " + objectClass);
    return new LineParseException("invalid method");
  }
  /**
   * Calls a static method for a given class.
   *
   * @param className	name of class to invoke static method of
   * @param tokens	parameters to method
   * @param ss		where to write error messages
   * @return		<code>true</code> if method called
   * @exception IOException error reading from tokenizer
   */
  public static boolean callStaticClassMethod(String className,
					      StreamTokenizer tokens,
					      ShowStatus ss)
    throws IOException {
    // get the method name
    int c = tokens.nextToken();
    if(c != StreamTokenizer.TT_WORD) {
      if(ss != null) ss.showStatus("bad class method name");
      // push back unused last token
      tokens.pushBack();
      return false;
    }
    String methodName = tokens.sval;
    // get the method parameters
    Vector<Object> params = getParameters(tokens, null);
    try {
      callStaticClassMethod(className, methodName, params, ss);
      return true;
    } catch (ClassNotFoundException cfe) {
      if(ss != null) ss.showStatus(cfe); 
    } catch (InvocationTargetException ite) {
      if(ss != null) ss.showStatus(ite); 
    } catch (IllegalAccessException iae) {
      if(ss != null) ss.showStatus(iae); 
    }
    return false;
  }
  /**
   * Returns a string for a font style.
   *
   * @param font	font style
   * @return		string for font style
   */
  public static String fontStyleToString(Font font) {
    if(font.isBold() && font.isItalic()) return "bold-italic";
    else if( font.isBold() ) return "bold";
    else if( font.isItalic() ) return "italic";
    else return "plain";
  }
  /**
   * Returns a style for a font style string.
   *
   * @param fontStyleName	name of font style
   * @return			font style
   */
  public static int fontStyleValueOf(String fontStyleName) {
    int fontStyle = Font.PLAIN;
    if(fontStyleName == null) return fontStyle;
    fontStyleName = fontStyleName.toLowerCase(); // case insensitive
    if(fontStyleName.indexOf("italic", 0) > -1) fontStyle = Font.ITALIC;
    if(fontStyleName.indexOf("bold", 0) > -1) fontStyle |= Font.BOLD;
    return fontStyle;
  }
  /**
   * Returns a script line for a specific font.
   *
   * @param font	font to create script for
   * @return		script to recreate font as default
   */
  public static String fontToScript(Font font) {
    if(font == null) return "";
    String script = "font " + font.getName() + " ";
    script += fontStyleToString(font);
    script += " " + font.getSize() + ";\n";
    return script;
  }
  /**
   * Returns a script line for a rotation.
   *
   * @param rotation	angle in degrees
   * @return		script to recreate rotation as default value
   */
  public static String rotationToScript(double rotation) {
    return "rotate " + rotation % 360.0 + ";\n";
  }
  /**
   * Returns a script line for a CNUScale.
   *
   * @param sc	scale
   * @return	script to recreate scale as the default
   */
  public static String scaleToScript(CNUScale sc) {
    String script = "";
    if(sc != null) {
      script = "scale " + sc.getScaleFactor();
      if(! sc.identity()) {
	if(sc.getTranslation() != 0.0)
	  script += " translation " + sc.getTranslation();
        if(sc.getThreshMinState())
	  script += " min " + sc.getThreshMin() + " " + sc.getThreshMinValue();
        if(sc.getThreshMaxState())
	  script += " max " + sc.getThreshMax() + " " + sc.getThreshMaxValue();
      }
      if(sc.getQuantificationState())
	script += " quant " + sc.getQuantification();
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
    if(cropBox == null) script = "crop off;\n";
    else script = "crop = " + cropBox.x + " " + cropBox.y + " " +
	   (cropBox.x + cropBox.width - 1) + " " +
	   (cropBox.y + cropBox.height - 1) + ";\n";
    return script;
  }
  /**
   * Returns a script line for a filter sampling type.
   *
   * @param filterSampleType	filter sample type
   * @return			script to recreate sample type as the default
   */
  public static String filterSampleTypeToScript(int filterSampleType) {
    return "filterSampling " +
      LinearImageFilter.sampleTypeToString(filterSampleType) + ";\n";
  }
  /**
   * Returns a script to build a string and store it in a variable
   *
   * @param string string to be built
   * @param scriptedObjects to check if string exists or to add string to
   * @return			script to recreate string
   */
  public static String stringToScript(String string, CNUScriptObjects
				      scriptedObjects) {
      StringBuffer sb = new StringBuffer();
      if(scriptedObjects == null) {
	  sb.append("set stringtmp "); sb.append(string); sb.append("\n;");
      }
      else {
	  String variableName = scriptedObjects.get(string);
	  if(variableName != null) {
	      sb.append("set "); sb.append(variableName); sb.append(";\n");
	  }
	  else {
	      variableName = scriptedObjects.addObject(string, "string");;
	      sb.append("set "); sb.append(variableName);
	      sb.append(" "); sb.append(string);
	      sb.append(";\n");
	      
	  }
      }
      return sb.toString();
  }
}
