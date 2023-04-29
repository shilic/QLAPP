package netTest;

import java.io.*;

public class MyTool {
    public static void main(String[] args) {

    }
    public static byte[] int2Bytes(int a){ //int转4位byte
        byte[] ans=new byte[4];
        for(int i=0;i<4;i++)
            ans[i]=(byte)(a>>(i*8));//截断 int 的低 8 位为一个字节 byte，并存储起来
        return ans;
    } //int2Bytes
    public static int bytes2Int(byte[] a){ //4位byte转int
        int ans=0;
        for(int i=0;i<4;i++){
            ans<<=8;//左移 8 位
            ans|=a[3-i];//保存 byte 值到 ans 的最低 8 位上
            //intPrint(ans);
        }
        return ans;
    }
    public static short Byte2Unsigned(byte by) {  //将java有符号byte转换成无符号byte，用short模拟
        return (short) (  (short)by&0xff );
    }
    public static int combineByte(byte a, byte b){  //把两个字节组合在一起,例如  0xAF 0xFE 得到0xAFFE
        //return (a*256)+Byte2Unsigned(b);
        int re = (a <<8)|b ;
        //System.out.printf("int = %x\n",re);
        return re;
    }
    public static int combineByte(byte[] b){  //把4个字节组合在一起int,例如  0x18 0xFE 0x01 0x1b 得到 0x18fe011b
        return (b[0] & 0xff) << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | b[3] & 0xff;
    }
    public static byte[] getBooleanArray(byte b) { //将byte转换为一个长度为8的byte数组，数组每个值代表bit
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }
    public static byte[] getBooleanArray(byte[] b){ ////将int  byte转换为一个长度为32的byte数组，数组每个值代表bit
        int length = b.length*8; //存在问题
        int t = combineByte(b);
        byte[] re =new byte[length]; //例如传入一个4位字节数组，那么长度就是32个bit
        for (int i = length-1; i >= 0; i--) {
            re[i] = (byte)(t & 0x00000001);  //把最低位赋值给re[i]
            t = (t >>> 1);  //整体右移1位bit
        }
        return re;
    }
    public static void printFrame(byte[] frame){  //传入字节数组帧，向控制台输出
        for(byte i:frame){
            System.out.printf("%x|",Byte2Unsigned(i));
        }
        System.out.print("\n");
    }
    public static void saveErrorLog(Exception e){  //保存错误日志
        Date d = new Date();
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        File F = new File("error.log");
        PrintStream printer = null;
        try {
            printer = new PrintStream(new FileOutputStream(F,true));
            printer.println(d.getStrDate()); //写入日期
            printer.println(writer.toString()); //写入异常
        } catch (Exception ex) {
            ex.printStackTrace();
            saveErrorLog(ex);
        }
    }
    public static String hex2Str(byte[] data){ //传入一个字节数组，输出16进制字符串
        StringBuilder hexString = new StringBuilder();
        for (byte datum : data) {
            hexString.append(String.format("%02X", datum)).append("|"); // 将byte转换为长度为2的16进制字符串
        }
        return hexString.toString();
    }
}  //class MyTool
