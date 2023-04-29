package netTest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 */
public class Date {  //获取日期
    Calendar now = Calendar.getInstance(); //使用 Calendar 获取当前日期和时间
    java.util.Date d = new java.util.Date(); //使用date方法获取日期
    public Calendar getDate() throws ParseException {//获取当前时间 //static?
        //获取时间 1. Calendar
        //System.out.println(now.getTime());  //Mon Apr 17 17:11:37 CST 2023
	    //System.out.println(d); //输出原始格式的日期  //Mon Apr 17 17:11:37 CST 2023
        return now;
    }
    public String getStrDate() throws ParseException {  //返回字符串格式的日期 "yyyy-MM-dd HH:mm:ss"
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //定义一个日期输出的格式
        String dateNowStr = sdf.format(d); //把日期按照新的格式转换成字符串 //如果变量未使用将会浪费内存
        //String str = dateNowStr; //要跟上面sdf定义的格式一样 ，年月日时分秒 "2012-1-13 17:26:33"
        //System.out.println("格式化后的日期：" + dateNowStr);
        //java.util.Date today = sdf.parse(dateNowStr); //字符串转换成日期
        //System.out.println("字符串转成日期：" + today);
        return dateNowStr;
    }   //返回字符串格式的日期 "yyyy-MM-dd HH:mm:ss"
    public int getYear(){
        //System.out.println("年: " + now.get(Calendar.YEAR));
        return now.get(Calendar.YEAR);
    }
    public byte getMonth(){
        //System.out.println("月: " + (now.get(Calendar.MONTH) + 1) + "");
        return (byte)(now.get(Calendar.MONTH) + 1) ;
    }
    public byte getDay(){
        //System.out.println("日: " + now.get(Calendar.DAY_OF_MONTH));
        return (byte)now.get(Calendar.DAY_OF_MONTH);
    }
    public byte getHour(){
        //System.out.println("时: " + now.get(Calendar.HOUR_OF_DAY));
        return (byte)now.get(Calendar.HOUR_OF_DAY);
    }
    public byte getMin(){
        //System.out.println("分: " + now.get(Calendar.MINUTE));
        return (byte)now.get(Calendar.MINUTE);
    }
    public byte getSec(){
        //System.out.println("秒: " + now.get(Calendar.SECOND));
        //System.out.println("当前时间毫秒数：" + now.getTimeInMillis());
        return (byte)now.get(Calendar.SECOND);
    }
    public byte[] getByteDate(){   //获取字节数组格式的日期
        byte[] byteDate = new byte[6];  //用6位的字节数组储存  年月日时分秒  的数据。
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
