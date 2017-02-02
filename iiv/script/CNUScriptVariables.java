package iiv.script;
import iiv.util.*;
import java.util.*;

/**
 * Class for storing script variables.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class CNUScriptVariables implements Cloneable {
  public static CNUScriptVariables defaultParentVariables =
    new CNUScriptVariables(null);
  private Hashtable<String,Object> variables;
  private CNUScriptVariables parentVariables = null;
  /**
   * Constructs a new instance of CNUScriptVariables with default parent
   * variables.
   *
   */
  public CNUScriptVariables() {
    this.parentVariables = defaultParentVariables;
    variables = new Hashtable<String,Object>(23, 1.0f);
  }
  /**
   * Constructs a new instance of CNUScriptVariables
   *
   * @param parentVariables	parent variables or <code>null</code> if
   *				if no parents
   */
  public CNUScriptVariables(CNUScriptVariables parentVariables) {
    this.parentVariables = parentVariables;
    variables = new Hashtable<String,Object>(23, 1.0f);
  }
  /**
   * Constructs a new instance of CNUScriptVariables
   *
   * @param parentVariables	parent variables or <code>null</code> if
   *				if no parents
   * @param initialSize	initial number of variables to allocate for
   */
  public CNUScriptVariables(CNUScriptVariables parentVariables,
    int initialSize) {
    this.parentVariables = parentVariables;
    variables = new Hashtable<String,Object>(initialSize, 1.0f);
  }
  /**
   * Creates a copy of this variable list.  Does not clone objects in list
   * or parent variable list.
   *
   * @return	copy of list
   */
  public Object clone() {
    synchronized (variables) {
      CNUScriptVariables clonedVariables =
        new CNUScriptVariables(parentVariables);
      Enumeration e = variables.keys();
      while(e.hasMoreElements()) {
        String key = (String) e.nextElement();
        clonedVariables.setVariable(key, variables.get(key));
      }
      return clonedVariables;
    }
  }
  /**
   * Adds or sets a local variable.
   *
   * @param name	name of variable
   * @param value	value for variable
   */
  public void setVariable(String name, Object value) {
    synchronized (variables) {
      variables.put(name, value);
    }
  }
  /**
   * Removes a variable from the local list.
   *
   * @return	the value of the variable or <code>null</code> if the
   *		variable wasn't set
   */
  public Object removeVariable(String name) {
    synchronized (variables) {
      return variables.remove(name);
    }
  }
  /**
   * Removes a variable from the local and all parent list.
   *
   * @return	the value of the local variable or the value
   *		of the lowest parent variable or
   *		<code>null</code> if the variable wasn't set
   */
  public Object globalRemoveVariable(String name) {
    Object lobj = removeVariable(name);
    Object gobj = null;
    if(parentVariables != null)
      gobj = parentVariables.globalRemoveVariable(name);
    if(lobj == null) lobj = gobj;
    return lobj;
  }
  /**
   * Copies a variable and value to the parent list.
   *
   * @param name	name of variable
   */
  public void upVariable(String name) {
    if(parentVariables != null) {
      Object obj = get(name);
      if(obj != null) parentVariables.setVariable(name, obj);
    }
  }
  /**
   * Copies a variable and value to all parent lists.
   *
   * @param name	name of variable
   */
  public void globalizeVariable(String name) {
    if(parentVariables != null) {
      Object obj = get(name);
      if(obj != null) {
	parentVariables.setVariable(name, obj);
        parentVariables.globalizeVariable(name);
      }
    }
  }
  /**
   * Gets a variable value.
   *
   * @param name	name of variable to get
   * @return		variable for name
   */
  public Object get(String name) {
    synchronized (variables) {
      Object obj = variables.get(name);
      if((obj == null) && (parentVariables != null))
        obj = parentVariables.get(name);
      return obj;
    }
  }
  /**
   * Gets a list containing the named variable value from this
   * and each parents.
   *
   * @param name	name of variable to get from this and all parents
   * @param valueList	vector to append values to if <code>null</code>
   *			creates a new vector
   * @return		valueList or newly created non-null vector
   *			appended with the named variable values
   *			from this and each parent.  
   */
  public Vector<Object> getTreeValues(String name, Vector<Object> valueList) {
    if(valueList == null) valueList = new Vector<Object>();
    synchronized (variables) {
      valueList.addElement(variables.get(name));
    }
    if(parentVariables != null) 
      parentVariables.getTreeValues(name, valueList);
    return valueList;
  }
  /**
   * Gets the x origin from the special variable X_ORIGIN as an
   * accumulation of local and all parent X_ORIGIN variables.
   *
   * @return	accumulated x origin or 0 if not set or set wrong.
   */
  public int getXOrigin() {
    int xOrigin = 0;
    synchronized (variables) {
      Object obj = variables.get("X_ORIGIN");
      if(obj instanceof Number) xOrigin += ((Number) obj).intValue();
    }
    if(parentVariables != null) xOrigin += parentVariables.getXOrigin();
    return xOrigin;
  }
  /**
   * Gets the y origin from the special variable Y_ORIGIN as an
   * accumulation of local and all parent Y_ORIGIN variables.
   *
   * @return	accumulated Y origin or 0 if not set or set wrong.
   */
  public int getYOrigin() {
    int yOrigin = 0;
    synchronized (variables) {
      Object obj = variables.get("Y_ORIGIN");
      if(obj instanceof Number) yOrigin += ((Number) obj).intValue();
    }
    if(parentVariables != null) yOrigin += parentVariables.getYOrigin();
    return yOrigin;
  }
  /**
   * Gets a variable value, variable array value or variable field value.
   *
   * @param name	name of variable to get which may be indexed to
   *			an array or a field value of the variable
   * @return		variable for name
   */
  public Object getVariable(String name) {
    Object obj = null;
    if(name == null) return null;
    name = name.trim();
    if("".equals(name)) return null;
    // check for array index
    int startIndex = name.indexOf("[");
    if(startIndex >= 0) {
      if(startIndex == 0) return null;
      if(! name.endsWith("]")) return null;
      String sindex = name.substring(startIndex+1, name.length() - 1);
      name = name.substring(0, startIndex);
      obj = get(name);
      if( obj == null ) return null;
      if("*".equals(sindex)) {
	// wild card to show all array values
	String s = "";
	int l = CNUDisplayScript.getArrayLength(obj);
	for(int arrayIndex = 0; arrayIndex < l; arrayIndex++) {
          if((arrayIndex != 0) && ((arrayIndex % 16) == 0)) s += "\\\n";
	  s += CNUDisplayScript.getArrayValue(obj, arrayIndex) + " ";
	}
	return s;
      }
      else {
        int arrayIndex = Integer.parseInt(sindex);
        if(arrayIndex < 0) return null;
        return CNUDisplayScript.getArrayValue(obj, arrayIndex);
      }
    }
    // check for object fields
    startIndex = name.indexOf(".");
    if(startIndex >= 0) {
      if((startIndex == 0) || (startIndex == (name.length()-1)) ) return null;
      String fieldName = name.substring(startIndex + 1);
      name = name.substring(0, startIndex);
      obj = get(name);
      if( obj == null ) return null;
      else return CNUDisplayScript.getObjectFieldValue(obj, fieldName);
    }
    return get(name);
  }
  /**
   * Substitutes for variables in a parameters list.
   *
   * @param	params		parameter list to perform substitions on
   * @param	ss		where to write messages and errors
   */
  public void substituteVariables(Vector<Object> params, ShowStatus ss) {
    if(params == null) return;
    if(variables.size() == 0 || params.size() == 0) return;
    for(int i = params.size() - 1; i >= 0; i--)
      params.setElementAt(substituteVariables(params.elementAt(i), ss, 0), i);
  }
  /**
   * Substitutes for variables in a single string.
   *
   * @param	obj		object that may be a string to perform
   *				substitions on
   * @param	ss		where to write messages and errors
   * @return			substituted object if whole input string
   *				represented a variable or String with
   *				with parts substituted for or original object
   */
  public Object substituteVariables(Object obj, ShowStatus ss, int depth) {
    if(! (obj instanceof String)) return obj;
    if(depth > 10) {
      if(ss != null) ss.showStatus("max variable depth reached");
      return obj;
    }
    String s = (String) obj;
    int startIndex = s.indexOf("${");
    if(startIndex >= 0) {
      int endIndex = s.indexOf('}', startIndex+1);
      if(endIndex > startIndex) {
	String variableName = s.substring(startIndex + 2, endIndex);
	Object newObj = getVariable(variableName);
	if(newObj == null) {
	  if(ss != null)
	    ss.showStatus("undefined variable \"" + variableName + "\"");
	  return obj;
	}
	else if(startIndex == 0 && endIndex == (s.length() - 1)) {
	  // replacing whole string
	  if(newObj != null)
	    return substituteVariables(newObj, ss, depth++);
	}
	else {
	  // replacing partial string
	  if(newObj instanceof String) {
	    String pre = "";
	    if(startIndex > 0) pre = s.substring(0, startIndex);
	    String post = "";
	    if(endIndex < s.length() - 1) post = s.substring(endIndex+1);
	    newObj = new String(pre + newObj + post);
	    return substituteVariables(newObj, ss, depth++);
	  }
	  else {
	    if(ss != null) ss.showStatus("invalid variable = \"" +
		 variableName + "\" for substitution inside a string");
	    return obj;
	  }
	}
      } // end if endIndex
    } // end if startIndex
    return obj;
  }
  /**
   * Creates a string representation of this list.
   */
  public String toString() {
    return toString(0);
  }
  /**
   * Creates a string representation of this list.
   */
  public String toString(int level) {
    String label = "";
    String string = "";
    if(level > 0) label = "parent" + level + " ";
    synchronized (variables) {
      Enumeration e = variables.keys();
      while(e.hasMoreElements()) {
        Object key = e.nextElement();
        string += label + key + "=" + variables.get(key) + '\n';
      }
    }
    level++;
    if(parentVariables != null) string += parentVariables.toString(level);
    return string;
  }
}
