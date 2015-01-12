//package edu.rutgers.winlab;
//
//import java.io.*;
//import java.util.*;
//
//public class Read
//{
//    public static void main(String args[])
//        throws Exception
//    {
//       // for (int i=0;i <args.length; i++) {
//            doit("C:\\Users\\Saumya\\AppData\\Local\\Google\\Picasa2\\db3\\catdata_name.pmp");
//       // }
//    }
//
//    private final static void doit(String p)
//        throws Exception
//    {
//        DataInputStream din = new DataInputStream
//            (new BufferedInputStream
//             (new FileInputStream(p)));
//        dump(din, p);
//        din.close();
//    }
//
//    private final static void dump(DataInputStream din, String path)
//        throws Exception
//    {
//
//        // header
//        long magic = readUnsignedInt(din);
//        if (magic != 0x3fcccccd) {
//            throw new IOException("Failed magic1 "+Long.toString(magic,16));
//        }
//
//        int type = readUnsignedShort(din);
//        System.out.println(path+":type="+Integer.toString(type, 16));
//        if ((magic=readUnsignedShort(din)) != 0x1332) {
//            throw new IOException("Failed magic2 "+Long.toString(magic,16));
//        }
//        if ((magic=readUnsignedInt(din)) != 0x2) {
//            throw new IOException("Failed magic3 "+Long.toString(magic,16));
//        }
//        if ((magic=readUnsignedShort(din)) != type) {
//            throw new IOException("Failed repeat type "+
//                                  Long.toString(magic,16));
//        }
//        if ((magic=readUnsignedShort(din)) != 0x1332) {
//            throw new IOException("Failed magic4 "+Long.toString(magic,16));
//        }
//
//        long v = readUnsignedInt(din);
//        System.out.println("nentries: "+v);
//
//        // records.
//        if (type == 0) {
//            dumpStringField(din,v);
//        }
//        else if (type == 0x1) {
//            dump4byteField(din,v);
//        }
//        else if (type == 0x2) {
//            dumpDateField(din,v);
//        }
//        else if (type == 0x3) {
//            dumpByteField(din, v);
//        }
//        else if (type == 0x4) {
//            dump8byteField(din, v);
//        }
//        else if (type == 0x5) {
//            dump2byteField(din,v);
//        }
//        else if (type == 0x6) {
//            dumpStringField(din,v);
//        }
//        else if (type == 0x7) {
//            dump4byteField(din,v);
//        }
//        else {
//            throw new IOException("Unknown type: "+Integer.toString(type,16));
//        }
//    }
//
//    private final static void dumpStringField(DataInputStream din, long ne)
//        throws IOException
//    {
//        for (long i=0; i<ne; i++) {
//            String v = getString(din);
//            System.out.println("["+i+"] "+v);
//        }
//    }
//
//    private final static void dumpByteField(DataInputStream din, long ne)
//        throws IOException
//    {
//        for (long i=0; i<ne; i++) {
//            int v = din.readUnsignedByte();
//            System.out.println("["+i+"] "+v);
//        }
//    }
//
//    private final static void dump2byteField(DataInputStream din, long ne)
//        throws IOException
//    {
//        for (long idx=0; idx<ne; idx++) {
//            int v = readUnsignedShort(din);
//            System.out.println("["+idx+"] "+v);
//        }
//    }
//
//    private final static void dump4byteField(DataInputStream din, long ne)
//        throws IOException
//    {
//        for (long idx=0; idx<ne; idx++) {
//            long v = readUnsignedInt(din);
//            System.out.println("["+idx+"] "+v);
//        }
//    }
// 
//    private final static void dump8byteField(DataInputStream din, long ne)
//        throws IOException
//    {
//        int[] bytes = new int[8];
//        for (long idx=0;idx<ne; idx++) {
//            for (int i=0; i<8; i++) {
//                bytes[i] = din.readUnsignedByte();
//            }
//            System.out.print("["+idx+"] ");
//            for (int i=7; i>=0; i--) {
//                String x = Integer.toString(bytes[i],16);
//                if (x.length() == 1) {
//                    System.out.print("0");
//                }
//                System.out.print(x);
//            }
//            System.out.println();
//        }
//    }
//
//    private final static void dumpDateField(DataInputStream din, long ne)
//        throws IOException
//    {
//        int[] bytes = new int[8];
//        for (long idx=0;idx<ne; idx++) {
//            long ld = 0;
//            for (int i=0; i<8; i++) {
//                bytes[i] = din.readUnsignedByte();
//                long tmp = bytes[i];
//                tmp <<= (8*i);
//                ld += tmp;
//            }
//            System.out.print("["+idx+"] ");
//            for (int i=7; i>=0; i--) {
//                String x = Integer.toString(bytes[i],16);
//                if (x.length() == 1) {
//                    //System.out.print("0");
//                }
//                //System.out.print(x);
//            }
//            //System.out.print(" ");
//            double d = Double.longBitsToDouble(ld);
//            //System.out.print(d);
//            //System.out.print(" ");
//
//            // days past unix epoch.
//            d -= 25569d;
//            long ut = Math.round(d*86400l*1000l);
//            System.out.println(new Date(ut));
//        }
//    }
//
//    private final static String getString(DataInputStream din)
//        throws IOException
//    {
//        StringBuffer sb = new StringBuffer();
//        int c;
//        while((c = din.read()) != 0) {
//            sb.append((char)c);
//        }
//        return sb.toString();
//    }
//
//    private final static int readUnsignedShort(DataInputStream din)
//        throws IOException
//    {
//        int ch1 = din.read();
//        int ch2 = din.read();
//        if ((ch1 | ch2) < 0)
//            throw new EOFException();
//        return ((ch2<<8) + ch1<<0);
//    }
//
//    private final static long readUnsignedInt(DataInputStream din)
//        throws IOException
//    {
//        int ch1 = din.read();
//        int ch2 = din.read();
//        int ch3 = din.read();
//        int ch4 = din.read();
//        if ((ch1 | ch2 | ch3 | ch4) < 0)
//            throw new EOFException();
//
//        long ret = 
//            (((long)ch4)<<24) +
//            (((long)ch3)<<16) +
//            (((long)ch2)<<8) +
//            (((long)ch1)<<0);
//        return ret;
//    }
//}