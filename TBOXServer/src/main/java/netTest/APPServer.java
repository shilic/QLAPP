package netTest;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static netTest.MyTool.*;
import static netTest.MyTool.hex2Str;

public class APPServer {
    public static void main(String[] args){
        server4TBox();
    }  //main
    public static void server4TBox(){
        Date date = new Date();
        int port = 2000;//设置端口号
        ServerSocket ss;
        Socket tBoxSocket;
        InputStream is;
        try {
            File F1 = new File("Console.log"); //创建文件用于储存网络数据（准备弃用改用数据库）
            FileOutputStream fos = new FileOutputStream(F1, true);  //以字节方式输出文件，不带格式
            ss = new ServerSocket(port); //用端口2000创建服务器socket连接
            while (true){
                String message1 = "\n等待客户端连接，监听端口号" + port + "......";
                System.out.print("\n"+date.getStrDate()+message1);
                fos.write("\n".getBytes(StandardCharsets.UTF_8));
                fos.write(date.getStrDate().getBytes(StandardCharsets.UTF_8));  //写入日期
                fos.write(message1.getBytes(StandardCharsets.UTF_8));  //写入日志
                tBoxSocket = ss.accept();  //创建客户端监听接口
                String message2 = "\nConnection from " + tBoxSocket.getInetAddress().getHostAddress();
                System.out.print(message2);//表示套接字连接成功
                fos.write(message2.getBytes(StandardCharsets.UTF_8));  //写入日志
                is = tBoxSocket.getInputStream();//获取输入流 //从网络上获取数据，输入到程序
                ByteArrayOutputStream bas = new ByteArrayOutputStream();  //用于输出到控制台
                byte[] buffer = new byte[1024];
                int len;
                ///把网络输入流的数据往buffer存，如果存满了buffer则执行下边的循环。
                while ((len=(is.read(buffer)))!=-1) {  //is.read(buffer)把网络上获取到的数据读取到 字节数组buffer中
                    //storage(bas,tBoxSocket,fos,buffer,len);
                    storage2(bas, tBoxSocket,fos,buffer,len);
                }  //写入while
                //为防止有数据还在缓存里，所以在一次TCP连接结束时才组合数据。
                String message3 = "\n连接断开\n";
                System.out.print(message3);
                fos.write(message3.getBytes(StandardCharsets.UTF_8));
                bas.close();
                //fos.close();  //关闭文件
            }  //while外层
        } catch (Exception e) { //捕获异常
            e.printStackTrace();
            saveErrorLog(e);  //保存错误日志

        }
//        finally {//关闭资源
//            is.close(); //关闭网络输入流，
//            tBoxSocket.close();  //关闭Socket监听接口
//            ss.close();  //关闭ServerSocket连接
//        }
    }  //server4TBox
    static void storage(ByteArrayOutputStream bas, Socket tBoxSocket, FileOutputStream fos, byte[] buffer, int len){
        bas.write(buffer,0,len);  //将buffer中的数据循环写入字节数组输出流，每次2048
        byte[] co = bas.toByteArray();
        int count = 0;  //用于记录一次TCP连接，发送了多少帧报文
        for (int i =0;i<co.length;) {   //遍历收到的数据寻找2323
            //在这里写解析
            //如果前一个字节为23同时后一个也是23则表示是GB32960的数据,如果一次TCP连接传了多个数据，依旧会储存到一起
            if((co[i]==0x23)&&(co[i+1]==0x23)){  //读取到2323开头表示这里有一帧报文，执行下边的语句
                count = count+1;  //每循环到这里+1，表示多了一帧报文
                int start = i;  //用于记录报文开始点
                short length= (short) (MyTool.combineByte(co[i+22],co[i+23]) + 25);  //length表示总长度
                //用于计算本帧报文的长度,22位和23位表示长度，使用方法计算其长度,最后加24表示前边的数据,再加一个校验位。
                int end = start +length;  //end表示校验位所在的位置 //用于记录报文结束点
                i = end;  //使循环控制位移动到帧末尾
                byte[] frame = new byte[length];  //根据长度创建一个新的数组用于保存单帧。
                //一次字节数组输出流中，不仅仅只有一帧报文，有可能有多帧，把co数组中的数据提取一帧到新的数组中，然后赋值到对象中
                //把co数组从起始点start开始，写length长度的数据到新的数组frame中
                System.arraycopy(co,start,frame,0,length);
                GB32960 fra = new GB32960(frame);  //创建一个GB32960对象用于处理报文数据
                System.out.println("checkSum正确吗？"+ fra.checkSumB);
                System.out.printf("这是本次TCP连接的第%d帧报文,数据如下:\n",count);
                printFrame(frame);  //打印第count帧数据
                if(fra.head.cmd==0x02||fra.head.cmd==0x03){
                    if(!fra.checkSumB){
                        //answer(fra,frame,tBoxSocket);
                        break;
                    }
                }else if(fra.head.cmd==0x01){  //01
                    answer(fra,frame, tBoxSocket,fos);
                    if(!fra.checkSumB){
                        break;
                    }
                }else if(fra.head.cmd==0x04){   //04
                    answer(fra,frame, tBoxSocket,fos);
                    if(!fra.checkSumB){
                        break;
                    }
                }  //此处报数组下标越界错误，调用了GB32960里的
                //break;
            }  //if 2323
        }  //for
        //printFrame(co);
        System.out.printf("本次输出流写了%d字节数据.\n",bas.size());
        bas.reset();  //清空本次输出流
        //写入文件
        try {
            fos.write(buffer, 0, len);  //把字节数组写入输出流，输出到文件流fos  //原始代码
            fos.write(new byte[]{0x0a,0x0a});  //把换行写入文件中
        } catch (IOException e) {
            e.printStackTrace();
            saveErrorLog(e);  //保存错误日志
        }
        //System.out.printf("本次写入结束,写入了%d字节数据\n",len);
    }  //storage
    static void answer(GB32960 fra, byte[] frame, Socket tBoxSocket, FileOutputStream fos){
        byte[] answer = fra.answerByte(frame);  //传入帧计算应答包
        System.out.print("应答成功\n");
        //printFrame(answer);  //打印该帧的应答包
        OutputStream answerFrame;
        try {
            fos.write("应答包如下:\n".getBytes(StandardCharsets.UTF_8));
            fos.write(hex2Str(answer).getBytes(StandardCharsets.UTF_8));  //将应答包写入文件
            fos.write("\n".getBytes(StandardCharsets.UTF_8));
            answerFrame = tBoxSocket.getOutputStream();   //输出到网络
            answerFrame.write(answer);  //输出到网络
        } catch (IOException e) {
            e.printStackTrace();
            saveErrorLog(e);  //保存错误日志
        }
    }
    static void storage2(ByteArrayOutputStream bas, Socket tBoxSocket, FileOutputStream fos, byte[] buffer, int len){
        bas.write(buffer,0,len);  //将buffer中的数据循环写入字节数组输出流，每次2048
        byte[] frame = bas.toByteArray();
        if((frame[0]==0x23)&&(frame[1]==0x23)){  //读取到2323开头表示这里有一帧报文，执行下边的语句
            short length= (short) (MyTool.combineByte(frame[22],frame[23]) + 25);  //length表示总长度
            if(frame.length==length){  //如果计算长度和实际长度不一致，退出
                GB32960 fra = new GB32960(frame);  //创建一个GB32960对象用于处理报文数据
                System.out.println("里程="+fra.data.vehicle.mileage);
                switch (fra.head.cmd) {
                    case 0x01: case 0x04:
                        String message6 = "\n车辆"+(fra.head.cmd==0x01?"请求登入":"请求登出")+".............\n";
                        System.out.print(message6);
                        try {
                            fos.write(message6.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                            saveErrorLog(e);
                        }
                        answer(fra, frame, tBoxSocket,fos);  //默认checkSumB正确与否都应答
                        break;
                    case 0x02: case 0x03:
                        String message7 = "\n车辆"+(fra.head.cmd==0x02?"实时上传中":"补发数据")+"..............\n";
                        System.out.print(message7);
                        try {
                            fos.write(message7.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                            saveErrorLog(e);
                        }
                        if(!fra.checkSumB){  //如果checkSumB错误则应答
                            answer(fra, frame, tBoxSocket,fos);
                        }
                        break;
                    default:
                        String message8 = "\n-----------------其余报文，本程序不作处理----------------\n";
                        System.out.print(message8);
                        try {
                            fos.write(message8.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                            saveErrorLog(e);
                        }
                        break;
                }  //switch (fra.head.cmd)
                String message9 = " checkSum= "+(fra.checkSumB?"true":"false")+"   报文接收成功\n";
                System.out.print(message9);
                //printFrame(frame);  //打印第count帧数据
                try {
                    fos.write(message9.getBytes(StandardCharsets.UTF_8));
                    fos.write(hex2Str(frame).getBytes(StandardCharsets.UTF_8));  //把报文写进文件
                    fos.write("\n".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                    saveErrorLog(e);
                }
            }   //计算长度，报文长度正确
            else {  //报文长度错误
                String message5 ="\n----------------------警告--报文长度错误--警告------------------\n";
                System.out.print(message5);
                //printFrame(frame);
                try {
                    fos.write(message5.getBytes(StandardCharsets.UTF_8));
                    fos.write(hex2Str(frame).getBytes(StandardCharsets.UTF_8));  //把报文写进文件
                    fos.write("\n".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                    saveErrorLog(e);  //保存错误日志
                }
            }
        }  //if 2323
        else { //报文没2323开头
            String message4 ="\n---------------------------没找到2323开头的报文--------------------\n";
            System.out.print(message4);
            //printFrame(frame);
            try {
                fos.write(message4.getBytes(StandardCharsets.UTF_8));
                fos.write(hex2Str(frame).getBytes(StandardCharsets.UTF_8)); //把报文写进文件
                fos.write("\n".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                saveErrorLog(e);  //保存错误日志
            }
        }
        String message10 = "本次输出流写了"+bas.size()+"字节数据.\n";
        System.out.print(message10);
        try {
            fos.write(message10.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            saveErrorLog(e);  //保存错误日志
        }
        bas.reset();  //清空本次输出流
    }   //storage2
}   //class APPServer