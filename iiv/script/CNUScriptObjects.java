package iiv.script;
import iiv.util.*;
import java.util.*;

/**
 * Class for storing scripted objects.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 */
public class CNUScriptObjects implements Cloneable {
  private Hashtable<Object,String> objects;
  private int variableNameHighestInt;
  public static String standardVariableBaseName="_vnum";
  /**
   * Constructs a new instance of CNUScriptObjects
   *
   */
  public CNUScriptObjects() {
    objects = new Hashtable<Object,String>(23, 1.0f);
    variableNameHighestInt = 0;
  }
  /**
   * Constructs a new instance of CNUScriptObjects
   *
   * @param initialSize	initial number of objects to allocate for
   */
  public CNUScriptObjects(int initialSize) {
    objects = new Hashtable<Object,String>(initialSize, 1.0f);
    variableNameHighestInt = 0;
  }
  /**
   * Creates a copy of this variable list.  Does not clone objects in list.
   *
   * @return	copy of list
   */
  public Object clone() {
    synchronized (objects) {
      CNUScriptObjects clonedObjects = new CNUScriptObjects();
      Enumeration e = objects.keys();
      while(e.hasMoreElements()) {
        Object key = e.nextElement();
        clonedObjects.objects.put(key, objects.get(key));
      }
      clonedObjects.variableNameHighestInt = variableNameHighestInt;
      return clonedObjects;
    }
  }
  /**
   * Adds an object returning a new variable name or its
   * current variable name.
   *
   * @param obj	object to add
   * @param rootname	root name for variable
   * @return variable name of either added object or existing
   */
  public String addObject(Object obj, String rootname) {
    synchronized (objects) {
	String variableName = get(obj);
	if(variableName == null) {
	    variableName = getUniqueName(rootname);
	    objects.put(obj, variableName);
	}
	return variableName;
    }
  }
  /**
   * Gets a unique name based on a root name.
   * @param rootname name to base unique name on.
   * @return unique name
   */
  private String getUniqueName(String rootname) {
    synchronized (objects) {
	return rootname + standardVariableBaseName +
	    (variableNameHighestInt++);
    }
  }
  /**
   * Removes a object from the local list.
   *
   * @param  obj object to remove
   * @return	the name of the variable or <code>null</code> if the
   *		object isn't assigned a variable
   */
  public String removeObject(Object obj) {
    synchronized (objects) {
      return objects.remove(obj);
    }
  }
  /**
   * Gets a object name.
   *
   * @param obj	object to get name variable name of
   * @return		variable name for object
   */
  public String get(Object obj) {
    synchronized (objects) {
      return objects.get(obj);
    }
  }

  /**
   * Creates a script to unset all scripted variables.
   *
   * @return	unset script
   */
  public String buildUnsetAllScript() {
    String className = getClass().getName();
    StringBuffer sb=new StringBuffer();
    sb.append("// -- start ").append(className).append(" unset all variables script\n");
    synchronized (objects) {
      Enumeration e = objects.keys();
      while(e.hasMoreElements()) {
        Object key = e.nextElement();
	sb.append("unset(\"");
	sb.append(removeObject(key));
	sb.append("\");\n");
      }
    }
    sb.append("// -- end ").append(className).append(" unset all variables script\n");
    return sb.toString();
  }
  /**
   * Creates a string representation of this list.
   */
  public String toString() {
    String string = "";
    synchronized (objects) {
      Enumeration e = objects.keys();
      while(e.hasMoreElements()) {
        Object key = e.nextElement();
        string += key + "=" + objects.get(key) + '\n';
      }
    }
    return string;
  }
}
