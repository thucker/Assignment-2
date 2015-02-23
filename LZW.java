/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *
 *************************************************************************/


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class LZW {
    private static final int R = 256;        // number of input chars
    private static final int L = 4096;       // number of codewords = 2^W
    private static final int W = 12;         // codeword width

    public static void compress(File inFile, File outFile) { 
      try {
        BinaryStdIn binaryIn = new BinaryStdIn(new FileInputStream(inFile));
        BinaryStdOut binaryOut = new BinaryStdOut(new PrintStream(new FileOutputStream(outFile)));
        String input = binaryIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        int code = R+1;  // R is codeword for EOF

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            binaryOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            input = input.substring(t);            // Scan past s in input.
        }
        binaryOut.write(R, W);
        binaryOut.close();
      } catch (FileNotFoundException ex) {
        System.err.println(ex.getMessage());
      }
    } 


    public static void expand(File inFile, File outFile) {
      try {
        BinaryStdIn binaryIn = new BinaryStdIn(new FileInputStream(inFile));
        BinaryStdOut binaryOut = new BinaryStdOut(new PrintStream(new FileOutputStream(outFile)));

        String[] st = new String[L];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = binaryIn.readInt(W);
        String val = st[codeword];

        while (true) {
            binaryOut.write(val);
            codeword = binaryIn.readInt(W);
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            if (i < L) st[i++] = val + s.charAt(0);
            val = s;
        }
        binaryIn.close();
        binaryOut.close();
      } catch (FileNotFoundException ex) {
        System.err.println(ex.getMessage());
      } 
    }



    public static void main(String[] args) {
   // parse arguments
      if (args.length != 3) {
        System.out.println("usage: java LZW -/+ <input_file> <output_file>");
        System.exit(1);
      } else if (args[0].equals("-")) {
        compress(new File(args[1]), new File(args[2]));
      } else if (args[0].equals("+")) {
        expand(new File(args[1]), new File(args[2]));
      } else {
        System.out.println("Enter - to compress or + to decomopress");
        System.exit(1);
      }
    }

}