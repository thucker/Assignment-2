/*************************************************************************
 *  Compilation:  javac BinaryStdOut.java
 *  Execution:    java BinaryStdOut
 *
 *  Write binary data to PrintStream, either one 1-bit boolean,
 *  one 8-bit char, one 32-bit int, one 64-bit double, one 32-bit float,
 *  or one 64-bit long at a time.
 *
 *  The bytes written are not aligned.
 *
 *************************************************************************/


import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.PrintStream;

/**
 *  <i>Binary output</i>. This class provides methods for converting
 *  primtive type variables (<tt>boolean</tt>, <tt>byte</tt>, <tt>char</tt>,
 *  <tt>int</tt>, <tt>long</tt>, <tt>float</tt>, and <tt>double</tt>)
 *  to sequences of bits and writing them to a PrintStream.
 *  Uses big-endian (most-significant byte first).
 *  <p>
 *  The client must <tt>flush()</tt> the output stream when finished writing bits.
 *  <p>
 *  The client should not intermixing calls to <tt>BinaryStdOut</tt> with calls
 *  to <tt>StdOut</tt> or <tt>System.out</tt>; otherwise unexpected behavior 
 *  will result.
 */
public final class BinaryStdOut {
    private final BufferedOutputStream out;

    private int buffer;     // 8-bit buffer of bits to write out
    private int N;          // number of bits remaining in buffer

    public BinaryStdOut(PrintStream outputStream) {
      out = new BufferedOutputStream(outputStream);
    }

   /**
     * Write the specified bit to a PrintStream.
     */
    private void writeBit(boolean bit) {
        // add bit to buffer
        buffer <<= 1;
        if (bit) buffer |= 1;

        // if buffer is full (8 bits), write out as a single byte
        N++;
        if (N == 8) clearBuffer();
    } 

   /**
     * Write the 8-bit byte to a PrintStream.
     */
    private void writeByte(int x) {
        assert x >= 0 && x < 256;

        // optimized if byte-aligned
        if (N == 0) {
            try { out.write(x); }
            catch (IOException e) { e.printStackTrace(); }
            return;
        }

        // otherwise write one bit at a time
        for (int i = 0; i < 8; i++) {
            boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

    // write out any remaining bits in buffer to a PrintStream, padding with 0s
    private void clearBuffer() {
        if (N == 0) return;
        if (N > 0) buffer <<= (8 - N);
        try { out.write(buffer); }
        catch (IOException e) { e.printStackTrace(); }
        N = 0;
        buffer = 0;
    }

   /**
     * Flush PrintStream, padding 0s if number of bits written so far
     * is not a multiple of 8.
     */
    public void flush() {
        clearBuffer();
        try { out.flush(); }
        catch (IOException e) { e.printStackTrace(); }
    }

   /**
     * Flush and close PrintStream. Once PrintStream is closed, you can no
     * longer write bits to it.
     */
    public void close() {
        flush();
        try { out.close(); }
        catch (IOException e) { e.printStackTrace(); }
    }


   /**
     * Write the specified bit to PrintStream.
     * @param x the <tt>boolean</tt> to write.
     */
    public void write(boolean x) {
        writeBit(x);
    } 

   /**
     * Write the 8-bit byte to PrintStream.
     * @param x the <tt>byte</tt> to write.
     */
    public void write(byte x) {
        writeByte(x & 0xff);
    }

   /**
     * Write the 32-bit int to PrintStream.
     * @param x the <tt>int</tt> to write.
     */
    public void write(int x) {
        writeByte((x >>> 24) & 0xff);
        writeByte((x >>> 16) & 0xff);
        writeByte((x >>>  8) & 0xff);
        writeByte((x >>>  0) & 0xff);
    }

   /**
     * Write the r-bit int to PrintStream.
     * @param x the <tt>int</tt> to write.
     * @param r the number of relevant bits in the char.
     * @throws RuntimeException if <tt>r</tt> is not between 1 and 32.
     * @throws RuntimeException if <tt>x</tt> is not between 0 and 2<sup>r</sup> - 1.
     */
    public void write(int x, int r) {
        if (r == 32) write(x);
        if (r < 1 || r > 32)        throw new RuntimeException("Illegal value for r = " + r);
        if (x < 0 || x >= (1 << r)) throw new RuntimeException("Illegal " + r + "-bit char = " + x);
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }





   /**
     * Write the 64-bit double to PrintStream.
     * @param x the <tt>double</tt> to write.
     */
    public void write(double x) {
        write(Double.doubleToRawLongBits(x));
    }

   /**
     * Write the 64-bit long to PrintStream.
     * @param x the <tt>long</tt> to write.
     */
    public void write(long x) {
        writeByte((int) ((x >>> 56) & 0xff));
        writeByte((int) ((x >>> 48) & 0xff));
        writeByte((int) ((x >>> 40) & 0xff));
        writeByte((int) ((x >>> 32) & 0xff));
        writeByte((int) ((x >>> 24) & 0xff));
        writeByte((int) ((x >>> 16) & 0xff));
        writeByte((int) ((x >>>  8) & 0xff));
        writeByte((int) ((x >>>  0) & 0xff));
    }

   /**
     * Write the 32-bit float to PrintStream.
     * @param x the <tt>float</tt> to write.
     */
    public void write(float x) {
        write(Float.floatToRawIntBits(x));
    }

   /**
     * Write the 16-bit int to PrintStream.
     * @param x the <tt>short</tt> to write.
     */
    public void write(short x) {
        writeByte((x >>>  8) & 0xff);
        writeByte((x >>>  0) & 0xff);
    }

   /**
     * Write the 8-bit char to PrintStream.
     * @param x the <tt>char</tt> to write.
     * @throws RuntimeException if <tt>x</tt> is not betwen 0 and 255.
     */
    public void write(char x) {
        if (x < 0 || x >= 256) throw new RuntimeException("Illegal 8-bit char = " + x);
        writeByte(x);
    }

   /**
     * Write the r-bit char to PrintStream.
     * @param x the <tt>char</tt> to write.
     * @param r the number of relevant bits in the char.
     * @throws RuntimeException if <tt>r</tt> is not between 1 and 16.
     * @throws RuntimeException if <tt>x</tt> is not between 0 and 2<sup>r</sup> - 1.
     */
    public void write(char x, int r) {
        if (r == 8) write(x);
        if (r < 1 || r > 16)        throw new RuntimeException("Illegal value for r = " + r);
        if (x < 0 || x >= (1 << r)) throw new RuntimeException("Illegal " + r + "-bit char = " + x);
        for (int i = 0; i < r; i++) {
            boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
            writeBit(bit);
        }
    }

   /**
     * Write the string of 8-bit characters to PrintStream.
     * @param s the <tt>String</tt> to write.
     * @throws RuntimeException if any character in the string is not
     * between 0 and 255.
     */
    public void write(String s) {
        for (int i = 0; i < s.length(); i++)
            write(s.charAt(i));
    }

   /**
     * Write the String of r-bit characters to PrintStream.
     * @param s the <tt>String</tt> to write.
     * @param r the number of relevants bits in each character.
     * @throws RuntimeException if r is not between 1 and 16.
     * @throws RuntimeException if any character in the string is not
     * between 0 and 2<sup>r</sup> - 1.
     */
    public void write(String s, int r) {
        for (int i = 0; i < s.length(); i++)
            write(s.charAt(i), r);
    }
}