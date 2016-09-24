package com.wulff.lecturesight.viscaoverip.protocol;


public class ByteUtils {

  public static String byteArrayToHex(byte[] ba, int len) {
    if (len < 1) {
      len = ba.length;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < len; i++) {
      sb.append(byteToHex(ba[i]));
      if (i + 1 != len) {
        sb.append(" ");
      }
    }
    sb.append("] ");
    return sb.toString();
  }

  public static byte[] s2b(short val){
      byte[] out = new byte[4];

      byte hi = (byte) ((val  >> 8) & 0x00FF);
      byte lo = (byte) (val & 0x00FF);
      out[0] = high(hi);
      out[1] = low(hi);
      out[2] = high(lo);
      out[3] = low(lo);
      return out;
  }

  public static String byteToHex(byte b) {
    return String.format("%02X", b);
  }

  public static byte low(byte b) {
    byte out = (byte)(b & (byte)0x0F);
    return out;
  }

  public static byte high(byte b) {
    byte out = (byte)(b >> 4);
    out &= (byte)0x0F;
    return out;
  }

  public static byte compose(byte hi, byte lo) {
    byte b = hi;
    b <<= 4;
    b += lo;
    return b;
  }

  public static byte compose(int hi, int lo) {
    return compose((byte)hi, (byte)lo);
  }

  public static byte[] i2b(int[] a) {
    byte[] b= new byte[a.length];
    for (int i=0; i < a.length; i++) {
      b[i] = (byte)a[i];
    }
    return b;
  }

  public static byte[] trimArray(byte[] a, int len) {
    byte[] out = new byte[len];
    System.arraycopy(a, 0, out, 0, len);
    return a;
  }

  public static void main(String[] args) {

  System.out.println(byteArrayToHex(s2b((short)2375),-1));
  }
}
