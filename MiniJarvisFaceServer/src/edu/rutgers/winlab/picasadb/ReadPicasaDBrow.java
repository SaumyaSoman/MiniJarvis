package edu.rutgers.winlab.picasadb;

import java.io.*;
import java.util.*;


public class ReadPicasaDBrow
{
    public static void main(String args[])
        throws Exception
    {
//		final String fullPath 	= (new File(args[0])).getCanonicalPath();
//		final String table		= args[1];
//		final Long recordNo		= Long.valueOf(args[2]);
		
		final String fullPath 	= "C:\\Users\\Saumya\\AppData\\Local\\Google\\Picasa2\\db3\\";
		final String table		= "imagedata_tags.pmp";
		final Long recordNo		= Long.valueOf("10000");
		
		System.out.println("Directory: " + fullPath);
		System.out.println("Table:     " + table);
		System.out.println("Record:    " + recordNo);
		
		FilenameFilter filter = new FilenameFilter() { 
            public boolean accept(File dir, String filename)
            { 
            	return filename.startsWith(table+"_") && filename.endsWith(".pmp");
            }
        };
        
		File[] files = new File(fullPath).listFiles(filter);
		
        for (int i=0;i < files.length; i++) {
			String filename 	= files[i].getName();
			String fieldname	= filename.replace(table+"_", "").replace(".pmp", "");
			System.out.print(fieldname+" ");
			doit(fullPath, filename, recordNo);
        }
    }

    private final static void doit(String fullPath, String filename, Long recordNo)
        throws Exception
    {
        DataInputStream din = new DataInputStream
            (new BufferedInputStream
             (new FileInputStream(fullPath + File.separator + filename)));
        dump(din, recordNo);
        din.close();
    }

    private final static void dump(DataInputStream din, Long recordNo)
        throws Exception
    {

        // header
        long magic = readUnsignedInt(din);
        if (magic != 0x3fcccccd) {
            throw new IOException("Failed magic1 "+Long.toString(magic,16));
        }

        int type = readUnsignedShort(din);
        System.out.print("(type="+Integer.toString(type, 16)+"): ");
        if ((magic=readUnsignedShort(din)) != 0x1332) {
            throw new IOException("Failed magic2 "+Long.toString(magic,16));
        }
        if ((magic=readUnsignedInt(din)) != 0x2) {
            throw new IOException("Failed magic3 "+Long.toString(magic,16));
        }
        if ((magic=readUnsignedShort(din)) != type) {
            throw new IOException("Failed repeat type "+
                                  Long.toString(magic,16));
        }
        if ((magic=readUnsignedShort(din)) != 0x1332) {
            throw new IOException("Failed magic4 "+Long.toString(magic,16));
        }

        long v = readUnsignedInt(din);
        //System.out.println("nentries: "+v);
		if (v<=recordNo) {
			System.out.println("[N/A]");
			return;
		}
		
        // records.
        if (type == 0) {
            dumpStringField(din,recordNo);
        }
        else if (type == 0x1) {
            dump4byteField(din,recordNo);
        }
        else if (type == 0x2) {
            dumpDateField(din,recordNo);
        }
        else if (type == 0x3) {
            dumpByteField(din,recordNo);
        }
        else if (type == 0x4) {
            dump8byteField(din,recordNo);
        }
        else if (type == 0x5) {
            dump2byteField(din,recordNo);
        }
        else if (type == 0x6) {
            dumpStringField(din,recordNo);
        }
        else if (type == 0x7) {
            dump4byteField(din,recordNo);
        }
        else {
            throw new IOException("Unknown type: "+Integer.toString(type,16));
        }
    }

    private final static void dumpStringField(DataInputStream din, long recordNo)
        throws IOException
    {
        String v = "";
        long i=0;
        for (i=0; i<=recordNo; i++) {
            v = getString(din);
        }
		System.out.println(v);
    }

    private final static void skipBytes(DataInputStream din, long recordNo, int bytes)
        throws IOException
    {
    	long pos = ((recordNo - 0) * bytes);
    	
    	while (pos > Integer.MAX_VALUE) {
			din.skipBytes(Integer.MAX_VALUE);
			pos = pos - Integer.MAX_VALUE;
    	}
    	int reminder = new Long(pos).intValue(); 
		din.skipBytes(reminder);
    }

    private final static void dumpByteField(DataInputStream din, long recordNo)
        throws IOException
    {
		skipBytes(din, recordNo, 1);
		int v = din.readUnsignedByte();
		System.out.println(v);
    }

    private final static void dump2byteField(DataInputStream din, long recordNo)
        throws IOException
    {
		skipBytes(din, recordNo, 2);
		int v = readUnsignedShort(din);
		System.out.println(v);
    }

    private final static void dump4byteField(DataInputStream din, long recordNo)
        throws IOException
    {
		skipBytes(din, recordNo, 4);
		long v = readUnsignedInt(din);
		System.out.println(v);
    }
 
    private final static void dump8byteField(DataInputStream din, long recordNo)
        throws IOException
    {
		skipBytes(din, recordNo, 8);

        int[] bytes = new int[8];

		for (int i=0; i<8; i++) {
			bytes[i] = din.readUnsignedByte();
		}

		for (int i=7; i>=0; i--) {
			String x = Integer.toString(bytes[i],16);
			if (x.length() == 1) {
				System.out.print("0");
			}
			System.out.print(x);
		}
		System.out.println();
    }

    private final static void dumpDateField(DataInputStream din, long recordNo)
        throws IOException
    {
		skipBytes(din, recordNo, 8);

        int[] bytes = new int[8];

		long ld = 0;
		for (int i=0; i<8; i++) {
			bytes[i] = din.readUnsignedByte();
			long tmp = bytes[i];
			tmp <<= (8*i);
			ld += tmp;
		}

		for (int i=7; i>=0; i--) {
			String x = Integer.toString(bytes[i],16);
			if (x.length() == 1) {
				//System.out.print("0");
			}
			//System.out.print(x);
		}
		//System.out.print(" ");
		double d = Double.longBitsToDouble(ld);
		//System.out.print(d);
		//System.out.print(" ");

		// days past unix epoch.
		d -= 25569d;
		long ut = Math.round(d*86400l*1000l);
		System.out.println(new Date(ut));
    }

    private final static String getString(DataInputStream din)
        throws IOException
    {
        StringBuffer sb = new StringBuffer();
        int c;
        while((c = din.read()) != 0) {
            sb.append((char)c);
        }
        return sb.toString();
    }

    private final static int readUnsignedShort(DataInputStream din)
        throws IOException
    {
        int ch1 = din.read();
        int ch2 = din.read();
        if ((ch1 | ch2) < 0)
            throw new EOFException();
        return ((ch2<<8) + ch1<<0);
    }

    private final static long readUnsignedInt(DataInputStream din)
        throws IOException
    {
        int ch1 = din.read();
        int ch2 = din.read();
        int ch3 = din.read();
        int ch4 = din.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();

        long ret = 
            (((long)ch4)<<24) +
            (((long)ch3)<<16) +
            (((long)ch2)<<8) +
            (((long)ch1)<<0);
        return ret;
    }
}
