package iiv.io;
import java.io.*;
import iiv.data.*;
import java.net.*;
/**
 * Class that handles ECAT files as image data.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		CNUImgFile
 */
public class CNUEcatImgFile extends CNUImgFile {
  CNUEcatHeader ecatHeader = new CNUEcatHeader();
  /**
   * Constructs an instance of CNUEcatImgFile.
   *
   * @param filename	name of ECAT file with or without .img extension
   * @exception IOException thrown on errors reading from the file.
   */
  public CNUEcatImgFile(String filename) throws IOException {
    filename = buildEcatFileName(filename);
    setFileName(filename);
  }
  /**
   * Constructs an instance of CNUEcatImgFile with an extra ignored parameter.
   *
   * @param filename	name of ECAT file with or without .img extension
   * @param ignored	ignored
   * @exception IOException thrown on errors reading from the file.
   */
  public CNUEcatImgFile(String filename, Object ignored) throws IOException {
    this(filename);
  }
  /**
   * Overrides getDimensions to read data if not read.
   *
   * @return	the dimensions of the image data
   */
  public synchronized CNUDimensions getDimensions(){
    // dimensions not set until data read
    CNUDimensions dims = super.getDimensions();
    if(dims != null) return dims;
    try {
      readData();
      return(super.getDimensions());
    } catch (IOException ioe) {
      System.out.println(ioe);
    }
    return null;
  }
  /**
   * Overides readData to call ReadImg.
   *
   * @exception IOException thrown on errors reading from the file.
   */
  public void readData() throws IOException {
    InputStream inS = null;
    try {
      CNUFile cnufile = getCNUFile();
      if(cnufile != null) inS = cnufile.getInputStream();
      ReadImg(inS);
    } finally {
      if(inS != null)inS.close();
    }
  }
  /**
   * Builds an ECAT file name
   * (ie filename with .img, .scn, .atn, or .nrm extension).
   * Also re-attaches .gz for compressed files
   *
   * @param filename	filename with or without appropriate extension
   * @return		filename with appropriate extension
   */
  static public String buildEcatFileName(String filename) {
    if(filename != null) {
      String extension = "";
      if( filename.endsWith(".gz") ) {
	filename =
	  filename.substring(0, filename.lastIndexOf('.'));
	extension = ".gz";
      }
      if( ! ( filename.endsWith(".img") || filename.endsWith(".scn") 
	     || filename.endsWith(".atn") || filename.endsWith(".nrm") ) ) {
	if( filename.endsWith(".log") )
	  filename = filename.substring(0, filename.lastIndexOf('.'));
        filename = filename + ".img";
      }
      filename += extension;
    }
    return filename;
  }
  /**
   * Creates a string representation of this object.
   */
  public String toString() {
    return super.toString() + "\n" + ecatHeader.toString();
  }
  /**
   * Reads a standard sized block of bytes from an input stream.
   *
   * @param inS		input stream
   * @param block	array to read into.  If <code>null</code> an
   *			new array will be created of size
   *			<code>offset + CNUEcatHeader.MatBLKSIZE</code>.
   * @param offset	offset into array to start read into if < 0 set to 0
   * @param count	number of bytes to read if < 0 set to
   *			<code>block.length - offset</code>
   * @exception IOException thrown on errors reading from the file.
   *
   */
  public static byte[] readBlock(InputStream inS, byte[] block,
			  int offset, int count)
       throws IOException
  {
    if(offset < 0) offset = 0;
    if(block == null) {
      if(count < 0) count = CNUEcatHeader.MatBLKSIZE;
      block = new byte[offset + count];
    }
    else if(count < 0) count = block.length - offset;

    int bytes_read = offset;
    int end_bytes_read = Math.min(offset + count, block.length);
    int nbytes=0;
    while( (nbytes != -1) && bytes_read < end_bytes_read){
      nbytes = inS.read(block, bytes_read,
			end_bytes_read - bytes_read);
      if(nbytes > 0)bytes_read += nbytes;
    }
    if(bytes_read != end_bytes_read)
      throw new IOException("wrong number of bytes read");
    return block;
  }
  /**
   * Reads the data from the file ignoring dims.
   *
   * @param inS	input stream
   * @exception IOException thrown on errors reading from the file.
   */
  public void ReadImg(InputStream inS)
       throws IOException
  {
    int nextReadBlk = 1;  // read blocks are 1 relative
    // first read the main header saving it in ecatHeader
    ecatHeader.setMainHdr(readBlock(inS, null, 0, CNUEcatHeader.MatBLKSIZE));
    nextReadBlk++;
    // get the number of frames and planes
    int num_frames = ecatHeader.getIntValue("num_frames");
    int num_planes = ecatHeader.getIntValue("num_planes");
    if(num_frames < 1)
      throw new IOException("ecat header number of frames not set");
    if(num_planes < 1)
      throw new IOException("ecat header number of planes not set");
    // Create frame, subhdr and data blocks
    byte[] frameMatrixBlock = new byte[CNUEcatHeader.MatBLKSIZE];
    byte[] subHdrBlock = new byte[CNUEcatHeader.MatBLKSIZE];
    byte[] dataBlock = new byte[CNUEcatHeader.MatBLKSIZE];
    // Create arrays to hold per plane matrix info
    int[] frame = new int[num_planes];
    int[] plane = new int[num_planes];
    int[] startBlock = new int[num_planes];
    int[] endBlock = new int[num_planes];
    int[] matStat = new int[num_planes];
    // initialize data info
    CNUDimensions dims = null;
    Object dataArray = null;
    int ecatType = CNUEcatHeader.VAX_I2;
    int ecatWordBytes = CNUEcatHeader.bytesPerWord(ecatType);
    int outOffset = 0;
    int endPlaneOffset = 0;
    int zIncrement = 0;
    int convertCount = 0;
      // need to track quantification factors
    float[] quantification = new float[num_frames * num_planes];
    int quantIndex = 0;
    float maxQuantification = 1;
    float minQuantification = 1;
    int nxtframematblk = CNUEcatHeader.MatFirstDirBlk;
    for(int i = 0; i < num_frames; i++) {
      // read the frame matrix directory assumes it is continuous
      boolean need_more_planes = true;
      int matrixZ = 0;
      int dataZ = 0;
      while( dataZ < num_planes ) {
        if(nextReadBlk == nxtframematblk) {
          // read matrix block
          frameMatrixBlock = readBlock(inS, frameMatrixBlock, 0, -1);
          nextReadBlk++;
          // interpet 4 word block header info
          int frameMatrixOffset = 0;
          int nfree =
	    CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				      CNUEcatHeader.VAX_I4);
          frameMatrixOffset += 4;
          nxtframematblk =
 	    CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				      CNUEcatHeader.VAX_I4);
          frameMatrixOffset += 4;
          int prevblk =
	    CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				      CNUEcatHeader.VAX_I4);
          frameMatrixOffset += 4;
          int nused =
	    CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				    CNUEcatHeader.VAX_I4);
          frameMatrixOffset += 4;
          // interpet plane info from matrix block
          while( (frameMatrixOffset < CNUEcatHeader.MatBLKSIZE) &&
	         ( matrixZ < num_planes ) ) {
	    int matnum =
	      CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				        CNUEcatHeader.VAX_I4);
	    frameMatrixOffset += 4;
	    frame[matrixZ] = matnum & 0x1ff;
	    if( frame[matrixZ] != (i + 1) )
	      throw new IOException(
	        "Frame out of order:  frame=" + frame[matrixZ] +
		" (i+1) = " + (i+1));
	    plane[matrixZ] = ( (matnum >> 16) & 0xff ) |
		( (matnum >> 1) & 0x300 );
	    if( plane[matrixZ] != (matrixZ + 1) )
	      throw new IOException(
	        "Plane out of order:  plane=" + plane[matrixZ] +
		" (matrixZ+1) = " + (matrixZ+1));
	    startBlock[matrixZ] =
	      CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				        CNUEcatHeader.VAX_I4);
	    frameMatrixOffset += 4;
	    endBlock[matrixZ] =
	      CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				        CNUEcatHeader.VAX_I4);
	    frameMatrixOffset += 4;
	    matStat[matrixZ] =
	      CNUEcatHeader.getIntValue(frameMatrixBlock, frameMatrixOffset,
				      CNUEcatHeader.VAX_I4);
	    frameMatrixOffset += 4;
            matrixZ++;
          } // end while( (frameMatrixOffset < CNUEcatHeader.MatBLKSIZE) ...)
        } // end if(nextReadBlk == nxtframematblk)
        else if(nextReadBlk == startBlock[dataZ]) {
	  // read sub header block for this plane
	  subHdrBlock =
	    readBlock(inS, subHdrBlock, 0, CNUEcatHeader.MatBLKSIZE);
	  nextReadBlk++;
	  // keep the quantification factor
	  switch(ecatHeader.getFileType()) {
	  case CNUEcatHeader.IMAGE_FILE:
	    quantification[quantIndex] =
	      CNUEcatHeader.getFloatValue("quant_scale",
	        ecatHeader.getSubHdrInfo(), subHdrBlock);
	    break;
	  default:
	    quantification[quantIndex] =
	      CNUEcatHeader.getFloatValue("scale_factor",
	        ecatHeader.getSubHdrInfo(), subHdrBlock);
	    break;
	  }
	  // Satoshi tends to send zeroed planes to
	  // float.MAX_VALUE/2=1.7014118E38 which screws up the
	  // new quantification factor
	  // guard against outrageous quantification values
	  if( Math.abs(quantification[quantIndex]) >= Float.MAX_VALUE/4 )
		quantification[quantIndex] = 0.0f;
	  // get the data type for this plane
	  ecatType = ecatHeader.getIntValue("data_type",
	               ecatHeader.getSubHdrInfo(), subHdrBlock);
	  ecatWordBytes = CNUEcatHeader.bytesPerWord(ecatType);
	  // save the first subhdr in ecatHeader
	  if(ecatHeader.getSubHdr() == null) {
	    ecatHeader.setSubHdr(subHdrBlock); // first frame header
	    subHdrBlock = null; // don't reuse this byte array
	    dims = ecatHeader.getDimensions();
	    this.initDataArray(dims);  // initialize the data array
	    dataArray = getDataArray();
	    setDimensions(dims);
	    zIncrement = dims.xdim() * dims.ydim();
	    endPlaneOffset = outOffset + zIncrement;
	    // initialize max and min quantification factors
	    maxQuantification = quantification[quantIndex];
	    minQuantification = quantification[quantIndex];
	  } else {
	    // keep running max and min quantification factors
	    maxQuantification =
	      Math.max(maxQuantification, quantification[quantIndex]);
	    minQuantification =
	      Math.min(minQuantification, quantification[quantIndex]);
          }
	  // increment index for next time we store a quantification factor
	  quantIndex++;
	  // now read the actual data and transfer it to the dataArray
	  while(nextReadBlk <= endBlock[dataZ]) {
	    dataBlock = readBlock(inS, dataBlock, 0, CNUEcatHeader.MatBLKSIZE);
	    nextReadBlk++;
            convertCount = dataBlock.length/ecatWordBytes;
	    convertCount = Math.min(convertCount, endPlaneOffset-outOffset);
	    outOffset = CNUEcatHeader.copyConvertBlock
		  (dataBlock, 0, ecatType,
		  dataArray, outOffset, dims.getType(),
		  convertCount);
	  }
	  dataZ++;
	  endPlaneOffset = outOffset + zIncrement;
        } // end if(nextReadBlk == startBlock[dataZ])
        else {
	  throw new IOException(
	     "Matrix blocks out of order: nextReadBlk = " + nextReadBlk +
		 "\nFrame = " + (i+1) +" nxtframematblk=" + nxtframematblk +
		 "\ndataZ=" + dataZ + " startBlock[dataZ]=" + startBlock[dataZ]);
        }
      } // end while( dataZ < num_planes )
    } // end for(i = 0; i < num_frames ...

    // set a single quantification factor for all data
    float newQuantification = maxQuantification;
    if(Math.abs(newQuantification) < Math.abs(minQuantification))
      newQuantification = minQuantification;
    // don't like negative quantifications
    if(newQuantification < 0.0f) newQuantification = -newQuantification;
    // don't like zero quantification values
    if(newQuantification == 0.0f) newQuantification = 1.0f;
    setFactor(newQuantification);
    if( maxQuantification != minQuantification ) {
    // adjust all planes to the single factor
      quantIndex = 0;
      int planeLength = dims.xdim() * dims.ydim();
      int planeOffset = 0;
      CNUScale sc = new CNUScale(1.0);
      for(int i = 0; i < num_frames; i++) {
        for(int z = 0; z < num_planes; z++, quantIndex++,
		planeOffset += planeLength) {
	  sc.setScaleFactor( quantification[quantIndex]/newQuantification );
	  CNUTypes.copyArray( dataArray, planeOffset, dims.getType(), 1,
		  dataArray, planeOffset, dims.getType(), planeLength, sc);
        }
      }
    }
  } // end ReadImg
  /**
   * Reads and prints an ECAT header as a standalone java program.
   *
   * @param args	array of arguments from the command line
   */
  static public void main(String[] args) throws IOException {
    try {
      CNUEcatImgFile img = new CNUEcatImgFile(args[0]);
      img.readData();
      System.out.println(img.toString());
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    System.exit(0);
  }
} // end CNUEcatImgFile
