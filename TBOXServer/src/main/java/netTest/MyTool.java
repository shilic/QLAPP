package netTest;

import java.io.*;

public class MyTool {
    public static void main(String[] args) {

    }
    public static byte[] int2Bytes(int a){ //intת4λbyte
        byte[] ans=new byte[4];
        for(int i=0;i<4;i++)
            ans[i]=(byte)(a>>(i*8));//�ض� int �ĵ� 8 λΪһ���ֽ� byte�����洢����
        return ans;
    } //int2Bytes
    public static int bytes2Int(byte[] a){ //4λbyteתint
        int ans=0;
        for(int i=0;i<4;i++){
            ans<<=8;//���� 8 λ
            ans|=a[3-i];//���� byte ֵ�� ans ����� 8 λ��
            //intPrint(ans);
        }
        return ans;
    }
    public static short Byte2Unsigned(byte by) {  //��java�з���byteת�����޷���byte����shortģ��
        return (short) (  (short)by&0xff );
    }
    public static int combineByte(byte a, byte b){  //�������ֽ������һ��,����  0xAF 0xFE �õ�0xAFFE
        //return (a*256)+Byte2Unsigned(b);
        int re = (a <<8)|b ;
        //System.out.printf("int = %x\n",re);
        return re;
    }
    public static int combineByte(byte[] b){  //��4���ֽ������һ��int,����  0x18 0xFE 0x01 0x1b �õ� 0x18fe011b
        return (b[0] & 0xff) << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | b[3] & 0xff;
    }
    public static byte[] getBooleanArray(byte b) { //��byteת��Ϊһ������Ϊ8��byte���飬����ÿ��ֵ����bit
        byte[] array = new byte[8];
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte)(b & 1);
            b = (byte) (b >> 1);
        }
        return array;
    }
    public static byte[] getBooleanArray(byte[] b){ ////��int  byteת��Ϊһ������Ϊ32��byte���飬����ÿ��ֵ����bit
        int length = b.length*8; //��������
        int t = combineByte(b);
        byte[] re =new byte[length]; //���紫��һ��4λ�ֽ����飬��ô���Ⱦ���32��bit
        for (int i = length-1; i >= 0; i--) {
            re[i] = (byte)(t & 0x00000001);  //�����λ��ֵ��re[i]
            t = (t >>> 1);  //��������1λbit
        }
        return re;
    }
    public static void printFrame(byte[] frame){  //�����ֽ�����֡�������̨���
        for(byte i:frame){
            System.out.printf("%x|",Byte2Unsigned(i));
        }
        System.out.print("\n");
    }
    public static void saveErrorLog(Exception e){  //���������־
        Date d = new Date();
        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        File F = new File("error.log");
        PrintStream printer = null;
        try {
            printer = new PrintStream(new FileOutputStream(F,true));
            printer.println(d.getStrDate()); //д������
            printer.println(writer.toString()); //д���쳣
        } catch (Exception ex) {
            ex.printStackTrace();
            saveErrorLog(ex);
        }
    }
    public static String hex2Str(byte[] data){ //����һ���ֽ����飬���16�����ַ���
        StringBuilder hexString = new StringBuilder();
        for (byte datum : data) {
            hexString.append(String.format("%02X", datum)).append("|"); // ��byteת��Ϊ����Ϊ2��16�����ַ���
        }
        return hexString.toString();
    }
}  //class MyTool
