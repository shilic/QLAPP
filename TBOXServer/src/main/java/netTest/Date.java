package netTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 */
public class Date {  //��ȡ����
    Calendar now = Calendar.getInstance(); //ʹ�� Calendar ��ȡ��ǰ���ں�ʱ��
    java.util.Date d = new java.util.Date(); //ʹ��date������ȡ����
    public Calendar getDate() throws ParseException {//��ȡ��ǰʱ�� //static?
        //��ȡʱ�� 1. Calendar
        //System.out.println(now.getTime());  //Mon Apr 17 17:11:37 CST 2023
	    //System.out.println(d); //���ԭʼ��ʽ������  //Mon Apr 17 17:11:37 CST 2023
        return now;
    }
    public String getStrDate() throws ParseException {  //�����ַ�����ʽ������ "yyyy-MM-dd HH:mm:ss"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //����һ����������ĸ�ʽ
        String dateNowStr = sdf.format(d); //�����ڰ����µĸ�ʽת�����ַ��� //�������δʹ�ý����˷��ڴ�
        //String str = dateNowStr; //Ҫ������sdf����ĸ�ʽһ�� ��������ʱ���� "2012-1-13 17:26:33"
        //System.out.println("��ʽ��������ڣ�" + dateNowStr);
        //java.util.Date today = sdf.parse(dateNowStr); //�ַ���ת��������
        //System.out.println("�ַ���ת�����ڣ�" + today);
        return dateNowStr;
    }   //�����ַ�����ʽ������ "yyyy-MM-dd HH:mm:ss"
    public int getYear(){
        //System.out.println("��: " + now.get(Calendar.YEAR));
        return now.get(Calendar.YEAR);
    }
    public byte getMonth(){
        //System.out.println("��: " + (now.get(Calendar.MONTH) + 1) + "");
        return (byte)(now.get(Calendar.MONTH) + 1) ;
    }
    public byte getDay(){
        //System.out.println("��: " + now.get(Calendar.DAY_OF_MONTH));
        return (byte)now.get(Calendar.DAY_OF_MONTH);
    }
    public byte getHour(){
        //System.out.println("ʱ: " + now.get(Calendar.HOUR_OF_DAY));
        return (byte)now.get(Calendar.HOUR_OF_DAY);
    }
    public byte getMin(){
        //System.out.println("��: " + now.get(Calendar.MINUTE));
        return (byte)now.get(Calendar.MINUTE);
    }
    public byte getSec(){
        //System.out.println("��: " + now.get(Calendar.SECOND));
        //System.out.println("��ǰʱ���������" + now.getTimeInMillis());
        return (byte)now.get(Calendar.SECOND);
    }
    public byte[] getByteDate(){   //��ȡ�ֽ������ʽ������
        byte[] byteDate = new byte[6];  //��6λ���ֽ����鴢��  ������ʱ����  �����ݡ�
        byteDate[0] = (byte) ( now.get(Calendar.YEAR)-2000 ) ;
        byteDate[1] = (byte) ( now.get(Calendar.MONTH) + 1 ) ;
        byteDate[2] = (byte) now.get(Calendar.DAY_OF_MONTH) ;
        byteDate[3] = (byte) now.get(Calendar.HOUR_OF_DAY) ;
        byteDate[4] = (byte) now.get(Calendar.MINUTE) ;
        byteDate[5] = (byte) now.get(Calendar.SECOND) ;
        return byteDate;
    }

//    public static void main(String[] args){
//        Date d = new Date();
//        byte[] date = d.getByteDate();
//        for(byte i:date){
//            System.out.println(i);
//        }
//    }

}  //class
