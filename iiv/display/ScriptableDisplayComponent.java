package iiv.display;
import iiv.script.*;
import iiv.io.*;
import java.awt.image.*;
import java.io.*;

public class ScriptableDisplayComponent extends DisplayComponent implements iiVScriptable {
  private static final long serialVersionUID = 2177759651711396200L;
  public String toScript(CNUScriptObjects scriptedObjects) {
    String className = getClass().getName();
    StringBuffer sb = new StringBuffer();
    sb.append("// -- start ").append(className).append(" script\n");
    if(scriptedObjects == null) scriptedObjects = new CNUScriptObjects();
    String variableName = scriptedObjects.get(this);
    if(variableName == null) {
      try {
	variableName = scriptedObjects.addObject(this, "scriptabledisplaycomponent");
	ImageProducer ip = getImageProducer();
	if(ip != null) {
	  CNUIntImage cnuIntImage = new CNUIntImage(ip);
	  cnuIntImage.consume();
	  sb.append(cnuIntImage.toScript(scriptedObjects));
	  sb.append(variableName).append(" = new ").append(className).append("();\n");
	  sb.append(variableName).append(".setImageProducer(");
	  sb.append(scriptedObjects.get(cnuIntImage)).append(");\n");
	  sb.append(postObjectToScript(scriptedObjects));
	}
      } catch (IOException ioe) {
	  System.out.println("ScriptableDisplayComponent.toScript caught IOException");
	  ioe.printStackTrace();
      }
    }
    if(variableName != null) sb.append("script_rtn=").append(variableName).append(";\n");
    else sb.append("script_rtn=null;\n");
    sb.append("// -- end ").append(className).append(" script\n");
    return sb.toString();
  }
}


