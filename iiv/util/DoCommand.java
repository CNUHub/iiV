package iiv.util;
import iiv.io.CNUFileObject;
import java.lang.reflect.*;
/**
 * DoCommand holds a single undo or redo command.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.display.CNUDisplay
 * @since	iiV1.0
 */
public class DoCommand {
  Object doLock = new Object();
  Object obj = null;
  Method meth = null;
  String methodName = null;
  Class params[] = null;
  Object args[] = null;
  String presentationName = null;

  public final static Class[] noClasses = new Class[0];
  public final static Object[] noParams = new Object[0];

  /**
   * Constructs a new instance of DoCommand with a method.
   *
   * @param obj		object to apply method to
   * @param meth	method to do
   * @param args	arguments for method
   */
  public DoCommand(Object obj, Method meth, Object args[]) {
    this.obj = obj;
    this.meth = meth;
    this.args = args;
  }
  /**
   * Constructs a new instance of DoCommand with a method.
   *
   * @param obj		object to apply method to
   * @param meth	method to do
   * @param args	arguments for method
   * @param presentationName	name for presenting on undo/redo buttons
   */
  public DoCommand(Object obj, Method meth, Object args[],
		   String presentationName) {
    this.obj = obj;
    this.meth = meth;
    this.args = args;
    this.presentationName = presentationName;
  }
  /**
   * Constructs a new instance of DoCommand with a method name.
   *
   * @param obj		object to apply method to
   * @param methodName	name of method to do
   * @param args	arguments for method
   */
  public DoCommand(Object obj, String methodName, Object args[]) {
    this.obj = obj;
    this.methodName = methodName;
    this.args = args;
  }
  /**
   * Constructs a new instance of DoCommand with a method name.
   *
   * @param obj		object to apply method to
   * @param methodName	name of method to do
   * @param args	arguments for method
   * @param presentationName	name for presenting on undo/redo buttons
   */
  public DoCommand(Object obj, String methodName, Object args[],
		   String presentationName) {
    this.obj = obj;
    this.methodName = methodName;
    this.args = args;
    this.presentationName = presentationName;
  }
  /**
   * Constructs a new instance of DoCommand with a method name and parameters.
   *
   * @param obj		object to apply method to
   * @param methodName	name of method to do
   * @param params	parameters for method to determine arguments from
   * @param args	arguments for method
   */
  public DoCommand(Object obj, String methodName, Class params[], Object args[]) {
    this.obj = obj;
    this.methodName = methodName;
    this.args = args;
    this.params = params;
  }
  /**
   * Constructs a new instance of DoCommand with a method name and parameters.
   *
   * @param obj		object to apply method to
   * @param methodName	name of method to do
   * @param params	parameters for method to determine arguments from
   * @param args	arguments for method
   * @param presentationName	name for presenting on undo/redo buttons
   */
    public DoCommand(Object obj, String methodName, Class params[],
		     Object args[], String presentationName) {
    this.obj = obj;
    this.methodName = methodName;
    this.args = args;
    this.params = params;
    this.presentationName = presentationName;
  }
  /**
   * Search for objects that represent a given filename object.
   *
   * @param sameAsFileObj	object associated with a file to compare to
   * @return			object associated with the same file or
   *				<code>null</code> if non found
   */
  public Object getFileObject(Object sameAsFileObj) {
    synchronized (doLock) {
      Object testobj = obj;
      if(testobj instanceof CNUFileObject) {
	testobj = ((CNUFileObject) testobj).getFileObject(sameAsFileObj);
        if( testobj != null ) return testobj;
      }
      if(args != null) {
        for(int i = 0; i < args.length; i++) {
	  testobj = args[i];
	  if(testobj instanceof CNUFileObject) {
	    testobj = ((CNUFileObject) testobj).getFileObject(sameAsFileObj);
	    if( testobj != null ) return testobj;
	  }
        }
      }
    }
    return null;
  }
  /**
   * Gets a string representing the command.
   *
   * @return	the string representation
   */
  public String toString() {
    String str = super.toString();
    synchronized (doLock) {
      if(methodName != null) str += "\nMethod name =" + methodName;
      if(meth != null) str += "\nmethod = " + meth;
      if(obj != null) str += "\nObject = " + obj;
      if(params != null) {
        for( int i=0; i<params.length; i++)
	  str += "\nparam[" + i + "] = " + params[i];
      }
      if(args != null) {
        for( int i=0; i<args.length; i++)
	  str += "\nargs[" + i + "] = " + args[i];
      }
    }
    return str;
  }
  /**
   * Invokes the command.
   *
   * @exception java.lang.reflect.InvocationTargetException
   *		Error occurred while invoking the command
   * @exception java.lang.IllegalAccessException
   *		Tried to invoke a non-public method
   * @exception java.lang.NoSuchMethodException
   *		Can't find the method in the class
   */
  public Object invoke()
    throws InvocationTargetException, IllegalAccessException,
      NoSuchMethodException
  {
    synchronized (doLock) {
      if(meth == null) {
        if((methodName != null) && (obj != null)) {
	  if((args != null) && (params == null)) params = guessClasses(args);
	  getMethod();
        }
      }
      if(meth == null) return null;
      return meth.invoke(obj, args);
    }
  }
  /**
   * Gets the method.
   *
   * @return	the method
   * @exception java.lang.NoSuchMethodException
   *		Can't find the method in the class
   */
  public Method getMethod() throws NoSuchMethodException {
    synchronized (doLock) {
      if(meth == null) meth = getMethod(obj, methodName, params);
      return meth;
    }
  }
  /**
   * Builds a list of classes based on a list of objects.
   *
   * @param args	object list
   * @return		class list
   */
  public static Class[] guessClasses( Object[] args ) {
    Class params[] = null;
    if(args != null) {
      params = new Class[args.length];
      for(int i=0; i<args.length; i++) {
	if(args[i] != null) params[i] = args[i].getClass();
	else params[i] = null;
      }
    }
    return params;
  }
  /**
   * Gets a presentation name that could be used for a menu item.
   *
   * @return		presentation name
   */
  public String getPresentationName() {
      if(presentationName != null) return presentationName;
      if(methodName != null) return methodName;
      if(meth != null) return meth.getName();
      else return "unknown";
  }

  /**
   * Tries to invoke a method for a given object.
   *
   * @param obj object to invoke method on
   * @param methodName name of method
   * @param paramClasses array of parameter classes
   * @param params array of paramters
   * @return value returned from function or <code>null</code> if failed
   */
  public static Object invokeObjectMethod(Object obj, String methodName, Class[] paramClasses, Object[] params) {
    if(obj == null || methodName == null) return null;
    if(paramClasses == null) paramClasses = noClasses;
    if(params == null) params = noParams;
    try {
      Method method = getMethod(obj, methodName, paramClasses);
      return method.invoke(obj, params);
    } catch (NoSuchMethodException nsme) { // ignore
    } catch (SecurityException se) { // ignore
    } catch (IllegalAccessException iae) { // ignore
    } catch (IllegalArgumentException iarge) { // ignore
    } catch (InvocationTargetException ite) { // ignore
      //    } catch (ClassCastException cce) { // ignore
    }
    return null;
  }
  /**
   * A more complete search for object methods.
   *
   * @param object object to find method of
   * @param methodName name of method
   * @param paramClasses array of parameter classes
   * @return	the method
   * @exception java.lang.NoSuchMethodException
   *		Can't find the method in the class
   */
  public static Method getMethod(Object obj, String methodName, Class[] paramClasses) throws NoSuchMethodException {
    return getMethod(obj.getClass(), methodName, paramClasses);
  }
  /**
   * A more complete search for class methods.
   *
   * @param class class to find method of
   * @param methodName name of method
   * @param paramClasses array of parameter classes
   * @return	the method
   * @exception java.lang.NoSuchMethodException
   *		Can't find the method in the class
   */
  public static Method getMethod(Class<?> objectClass, String methodName, Class[] paramClasses) throws NoSuchMethodException {
    try {
      return objectClass.getMethod(methodName, paramClasses);
    } catch (NoSuchMethodException nsme) {
      Method[] methods = objectClass.getMethods();
      for(int i=0; i<methods.length; i++) {
	Class[] paramTypes = methods[i].getParameterTypes();
	if(methodName.equals(methods[i].getName())) {
	  if(testParameters(paramClasses, paramTypes)) return methods[i];
	}
      }
      throw nsme;
    }
  }
  /**
   * Tests a list of parameters classes to see if they are valid for passing to a method
   * based on the list of method parameters classes.
   *
   * @param testParams	parameters to test
   * @param methodParams	method parameters to test against
   * @return		new array of objects that may be used as parameters for
   *			the parameter types or <code>null</code>
   */
  public static boolean testParameters(Class[] testParams, Class<?>[] methodParams) {
    if(testParams.length != methodParams.length) return false;
    // check if params are appropriate
    for(int p=0; p < testParams.length; p++) {
      if(methodParams[p].isAssignableFrom(testParams[p]));
      else if(methodParams[p].isPrimitive()) {
	// Primitives aren't really classes.  Invoking methods with reflection requires
	// wrapping a primitive in a wrapper class.  So, testParams often lists the wrapper class
	// while the method parameter lists depict primitives with special primitive TYPE classes
	// (primitive TYPE classes differentiate from parameters of the actual wrapper class).
	if(methodParams[p] == Boolean.TYPE) {
	  if(testParams[p] != Boolean.class) return false;
	}
	else if(methodParams[p] == Byte.TYPE) {
	  if(testParams[p] != Byte.class) return false;
	}
	else if(methodParams[p] == Short.TYPE) {
	  if(testParams[p] != Short.class) return false;
	}
	else if(methodParams[p] == Integer.TYPE) {
	  if(testParams[p] != Integer.class) return false;
	}
	else if(methodParams[p] == Long.TYPE) {
	  if(testParams[p] != Long.class) return false;
	}
	else if(methodParams[p] == Float.TYPE) {
	  if(testParams[p] != Float.class) return false;
	}
	else if(methodParams[p] == Double.TYPE) {
	  if(testParams[p] != Double.class) return false;
	}
	else if(methodParams[p] == Character.TYPE) {
	  if(testParams[p] != Character.class) return false;
	}
      }
      else return false;
    }
    return true;
  }
}
