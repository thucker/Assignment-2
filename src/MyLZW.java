import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class MyLZW {
	private static final int MAXWIDTH = 16;   
	private static final int INITIALWIDTH = 9;       
    private static final int ASCII = 256;        
    private static final int RESETFLAG = 257;    

   
    private static final double COMPRESSION_RATIO_THRESHOLD = 1.1; 
    
    public static void main(String[] args) { 
        if (args.length != 3 && args[0].equals("+")) {
          System.out.println("+/- mode <file1> <outputFile>");
          System.exit(1);
        } else if (args[0].equals("-")) {
          compress(new String(args[1]), new File(args[2]), new File(args[3]));
        } else if (args[0].equals("+")) {
          expand(new File(args[1]), new File(args[2]));
        } else {
          System.out.println("Make sure to enter - or + for compress and decompress");
          System.exit(1);
        }
    }
    
    public static void compress(String modeSelect, File inFile, File outFile) {
    	int W = INITIALWIDTH;
    	int L = (int) Math.pow(2, W);
      try {
        BinaryStdIn binaryIn = new BinaryStdIn(new FileInputStream(inFile));
        BinaryStdOut binaryOut = new BinaryStdOut(new PrintStream(new FileOutputStream(outFile)));
        int bitsUncompressed = 0;
        int bitsCompressed = 0;
        double startingCompressionRatio = 1;
        double curCompressionRatio =1;
        double ratioOfRatios = 1;
        
        boolean monitor = false;
          
        switch (modeSelect){ 
        case "n":
        	binaryOut.write(0, 2);
        	break;
        case "r":
                binaryOut.write(1,2);
                break;
        case "m":
        	binaryOut.write(2,2);
        	break;
        }
             
        String input = binaryIn.readString();
       
        TST<Integer> symbol = new TST<Integer>();
        for (int i = 0; i < ASCII; i++)
        	symbol.put("" + (char) i, i);
        int freeCode = RESETFLAG+1; 


        while (input.length() > 0) {
        	String longestPrefix = symbol.longestPrefixOf(input);
        	int code = symbol.get(longestPrefix); 
        
                binaryOut.write(code, W);
        	bitsCompressed += W; 
        	
        	int t = longestPrefix.length();
        	bitsUncompressed += t * 8;
        	
        	if(!monitor)
        		startingCompressionRatio = bitsUncompressed / bitsCompressed;
        	else {
        		curCompressionRatio = (double) bitsUncompressed / bitsCompressed;
        		ratioOfRatios = startingCompressionRatio/curCompressionRatio;
        	}
        	
        	if (freeCode < L){ 
        		if (t < input.length()){
        			symbol.put(input.substring(0, t+1), freeCode++); 
        		}
        	}	else if ( W < MAXWIDTH){ 
        		W++;
        		L=(int) Math.pow(2, W);
        		if (t < input.length())
        			symbol.put(input.substring(0, t+1), freeCode++);
        	} else {                    
        		switch (modeSelect) {
        		case "m":
        			monitor = true;
        			if ( ratioOfRatios < COMPRESSION_RATIO_THRESHOLD){
        				break; 
        			} else {
        				monitor = false;  
        			}
        			
        		case "r":
                    symbol = new TST<Integer>();
        			W = INITIALWIDTH;
        			L = (int) Math.pow(2, W);
        			for (int i = 0 ; i < ASCII; i++)
        				symbol.put("" + (char) i, i);
        			freeCode = RESETFLAG+1;
        			binaryOut.write(RESETFLAG, W);
        		case "n":
        			break;
        		}
        	}
        	input = input.substring(t);
        }
        binaryOut.write(ASCII, W);
        binaryOut.close();
      } catch (FileNotFoundException ex) {
        System.err.println(ex.getMessage());
      }
    } 


    public static void expand(File inFile, File outFile) {
    	int R = ASCII;
    	int W = INITIALWIDTH;
    	int L = (int) Math.pow(2, W);
        
        int bitsUncompressed = 0;
        int bitsCompressed = 0;
        double startingCompressionRatio = 1;
        double curCompressionRatio =1;
        double ratioOfRatios = 1;
        boolean monitor = false;
        
      try {
        BinaryStdIn binaryIn = new BinaryStdIn(new FileInputStream(inFile));
        BinaryStdOut binaryOut = new BinaryStdOut(new PrintStream(new FileOutputStream(outFile)));

        String[] symbol = new String[(int)Math.pow(2,MAXWIDTH+1)];
        int i;
        for (i = 0; i < R; i++)
        	symbol[i] = "" + (char) i;
        symbol[i++] = "";
        int flag = binaryIn.readInt(2);
        int codeword = binaryIn.readInt(W);
        String val = symbol[codeword];
        int t = val.length();
        bitsUncompressed += t * 8;
        
        i++;

        while (true) {           
            binaryOut.write(val);
            codeword = binaryIn.readInt(W);            
            
            bitsCompressed += W;
            t = val.length();
            bitsUncompressed += t * 8;
            
            if (codeword == R) break;
            String s = symbol[codeword];
            if (i == codeword) s = val + val.charAt(0);  
            
            if (i == (L-1) && W < MAXWIDTH) {
                W++;
                s = symbol[codeword];
                L = (int) Math.pow(2, W);
            }
            
            if (flag == 1 && (W == MAXWIDTH) && (i == (L-1))) {
                W = INITIALWIDTH;
                L = (int) Math.pow(2, W);
                symbol = new String[(int)Math.pow(2,MAXWIDTH+1)];
                for (i = 0; i < R; i++)
                	symbol[i] = "" + (char) i;
                symbol[i++] = ""; 
                codeword = binaryIn.readInt(W);
                val = symbol[codeword];
                i--;
            }
            
            if (flag == 2 && (W == MAXWIDTH) && (i == (L-1))) {
                monitor = true; 
            }
            if(!monitor)
        		startingCompressionRatio = bitsUncompressed / bitsCompressed;
            else if (flag == 2){
        		curCompressionRatio = (double) bitsUncompressed / bitsCompressed;
        		ratioOfRatios = startingCompressionRatio/curCompressionRatio;                 
                if ( ratioOfRatios > 1.1 ){
                	monitor = false;
                	W = INITIALWIDTH;
                	L = (int) Math.pow(2, W);
                	symbol = new String[(int)Math.pow(2,MAXWIDTH+1)];
                    for (i = 0; i < R; i++)
                    	symbol[i] = "" + (char) i;
                    symbol[i++] = "";
                    codeword = binaryIn.readInt(W);
                    val = symbol[codeword];
                    i--;                      
        		}          
            }
          if (i < L) symbol[i++] = val + s.charAt(0);
          val = s;        
        }
        binaryIn.close();
        binaryOut.close();
      } catch (FileNotFoundException ex) {
        System.err.println(ex.getMessage());
      } 
    }
}