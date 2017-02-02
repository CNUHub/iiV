package iiv.io;
import java.io.*;
/**
 * Class extends FileReader avoiding blocking by sleeping until ready.
 *
 * @author	Joel T. Lee
 * @version %I%, %G%
 * @see		iiv.CNUViewer
 * @since	iiV1.1
 */
public class SleepFileReader extends FileReader implements SleepReader {
  /**
   * Constructs a new instance of SleepBufferedReader.
   *
   * @param file	file to read from
   * @exception FileNotFoundException thrown on error opening file
   */
  public SleepFileReader(String file) throws FileNotFoundException {
    super(file);
  }
  /**
   * Constructs a new instance of SleepBufferedReader.
   *
   * @param file	file to read from
   * @exception FileNotFoundException thrown on error opening file
   */
  public SleepFileReader(File file) throws FileNotFoundException {
    super(file);
  }
  /**
   * Constructs a new instance of SleepBufferedReader.
   *
   * @param fd	file descriptor to read from
   * @exception FileNotFoundException thrown on error opening file
   */
  public SleepFileReader(FileDescriptor fd) throws FileNotFoundException {
    super(fd);
  }
  /**
   * Reads a single character.
   *
   * @return	byte read
   * @exception IOException thrown on error reading
   */
  public int read() throws IOException {
    try {
      while( ! ready() ) Thread.sleep(100);
    } catch (InterruptedException ie) {
    }
    return super.read();
  }
  /**
   * Reads characters into a portion of an array.
   *
   * @param cbuf	buffer to read into
   * @param off		offset into buffer to start reading into
   * @param len		number of characters to read
   * @return	number of bytes read
   * @exception IOException thrown on error reading
   */
  public int read(char cbuf[], int off, int len) throws IOException {
    try {
      while( ! ready() ) Thread.sleep(100);
    } catch (InterruptedException ie) {
    }
    return super.read(cbuf, off, len);
  }
}
