package iiv.display;
import iiv.script.*;
import java.awt.datatransfer.*;
import java.awt.Image;

/**
 * iiVTransferable is a rapper class for transfering iiV
 * components to the clipboard.
 * @author      Joel T. Lee
 * @version %I%, %G%
 * @see         DisplayComponent
 * @since       iiV1.5.5
 */
public class iiVTransferable implements Transferable, ClipboardOwner
{
    private final static Object staticLock = new Object();
    private static DataFlavor iiVStringFlavor = DataFlavor.stringFlavor;
    private static DataFlavor iiVScriptFlavor =
	new DataFlavor(String.class, "iiV DisplayComponent");
    private static DataFlavor iiVImageFlavor = null;
    private String iivscript;
    private Image iivimage;
    private String iivstring;
    private DataFlavor[] supportedFlavors;

    public iiVTransferable(Image image, String script, String string)
    {
	iivscript = script;
	iivimage = image;
	iivstring = string;
	if((image != null) && (script != null) && (string != null))
	  supportedFlavors = new DataFlavor[] {
	      getIIVImageFlavor(), iiVStringFlavor, iiVScriptFlavor };
	else if((image != null) && (script != null))
	  supportedFlavors = new DataFlavor[] { getIIVImageFlavor(),
						iiVScriptFlavor };
	else if((image != null) && (string != null))
	  supportedFlavors = new DataFlavor[] { getIIVImageFlavor(),
						iiVStringFlavor };
	else if((script != null) && (string != null))
	  supportedFlavors = new DataFlavor[] {iiVStringFlavor, iiVScriptFlavor};
	else if(image != null) supportedFlavors = new DataFlavor[] {
	    getIIVImageFlavor()};
	else if(script != null) supportedFlavors = new DataFlavor[] {iiVScriptFlavor};
	else if(string != null) supportedFlavors = new DataFlavor[] {iiVStringFlavor};
    }

    public DataFlavor getIIVImageFlavor() {
	if(iiVImageFlavor == null) {
	    synchronized (staticLock) {
	     if(iiVImageFlavor == null) 
	       iiVImageFlavor = (DataFlavor)
		 CNUDisplayScript.getStaticValue("imageFlavor", DataFlavor.class);
	     if(iiVImageFlavor == null) 
	       iiVImageFlavor = new DataFlavor(Image.class, "iiV Image");
	   }
	}
	return iiVImageFlavor;
    }
    public DataFlavor[] getTransferDataFlavors() {
	return supportedFlavors;
    }
    public boolean isDataFlavorSupported(DataFlavor dataFlavor) {
	for(int i=0; i<supportedFlavors.length; i++)
	    if(supportedFlavors[i].equals(dataFlavor)) return true;
	return false;
    }
    public Object getTransferData(DataFlavor dataFlavor)
      throws UnsupportedFlavorException {
	if(dataFlavor != null) {
	    if(dataFlavor.equals(iiVScriptFlavor) && iivscript != null) {
		return iivscript;
	    }
	    if(dataFlavor.equals(iiVImageFlavor) && iivimage != null) {
		return iivimage;
	    }
	    if(dataFlavor.equals(iiVStringFlavor) && iivstring != null) {
		return iivstring;
	    }
	}
	throw new UnsupportedFlavorException(dataFlavor);
    }
    public void lostOwnership(Clipboard clipboard,
			      Transferable transferable) {
    }
}
