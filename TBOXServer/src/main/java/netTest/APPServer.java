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
        int port = 2000;//���ö˿ں�
        ServerSocket ss;
        Socket tBoxSocket;
        InputStream is;
        try {
            File F1 = new File("Console.log"); //�����ļ����ڴ����������ݣ�׼�����ø������ݿ⣩
            FileOutputStream fos = new FileOutputStream(F1, true);  //���ֽڷ�ʽ����ļ���������ʽ
            ss = new ServerSocket(port); //�ö˿�2000����������socket����
            while (true){
                String message1 = "\n�ȴ��ͻ������ӣ������˿ں�" + port + "......";
                System.out.print("\n"+date.getStrDate()+message1);
                fos.write("\n".getBytes(StandardCharsets.UTF_8));
                fos.write(date.getStrDate().getBytes(StandardCharsets.UTF_8));  //д������
                fos.write(message1.getBytes(StandardCharsets.UTF_8));  //д����־
                tBoxSocket = ss.accept();  //�����ͻ��˼����ӿ�
                String message2 = "\nConnection from " + tBoxSocket.getInetAddress().getHostAddress();
                System.out.print(message2);//��ʾ�׽������ӳɹ�
                fos.write(message2.getBytes(StandardCharsets.UTF_8));  //д����־
                is = tBoxSocket.getInputStream();//��ȡ������ //�������ϻ�ȡ���ݣ����뵽����
                ByteArrayOutputStream bas = new ByteArrayOutputStream();  //�������������̨
                byte[] buffer = new byte[1024];
                int len;
                ///��������������������buffer�棬���������buffer��ִ���±ߵ�ѭ����
                while ((len=(is.read(buffer)))!=-1) {  //is.read(buffer)�������ϻ�ȡ�������ݶ�ȡ�� �ֽ�����buffer��
                    //storage(bas,tBoxSocket,fos,buffer,len);
                    storage2(bas, tBoxSocket,fos,buffer,len);
                }  //д��while
                //Ϊ��ֹ�����ݻ��ڻ����������һ��TCP���ӽ���ʱ��������ݡ�
                String message3 = "\n���ӶϿ�\n";
                System.out.print(message3);
                fos.write(message3.getBytes(StandardCharsets.UTF_8));
                bas.close();
                //fos.close();  //�ر��ļ�
            }  //while���
        } catch (Exception e) { //�����쳣
            e.printStackTrace();
            saveErrorLog(e);  //���������־

        }
//        finally {//�ر���Դ
//            is.close(); //�ر�������������
//            tBoxSocket.close();  //�ر�Socket�����ӿ�
//            ss.close();  //�ر�ServerSocket����
//        }
    }  //server4TBox
    static void storage(ByteArrayOutputStream bas, Socket tBoxSocket, FileOutputStream fos, byte[] buffer, int len){
        bas.write(buffer,0,len);  //��buffer�е�����ѭ��д���ֽ������������ÿ��2048
        byte[] co = bas.toByteArray();
        int count = 0;  //���ڼ�¼һ��TCP���ӣ������˶���֡����
        for (int i =0;i<co.length;) {   //�����յ�������Ѱ��2323
            //������д����
            //���ǰһ���ֽ�Ϊ23ͬʱ��һ��Ҳ��23���ʾ��GB32960������,���һ��TCP���Ӵ��˶�����ݣ����ɻᴢ�浽һ��
            if((co[i]==0x23)&&(co[i+1]==0x23)){  //��ȡ��2323��ͷ��ʾ������һ֡���ģ�ִ���±ߵ����
                count = count+1;  //ÿѭ��������+1����ʾ����һ֡����
                int start = i;  //���ڼ�¼���Ŀ�ʼ��
                short length= (short) (MyTool.combineByte(co[i+22],co[i+23]) + 25);  //length��ʾ�ܳ���
                //���ڼ��㱾֡���ĵĳ���,22λ��23λ��ʾ���ȣ�ʹ�÷��������䳤��,����24��ʾǰ�ߵ�����,�ټ�һ��У��λ��
                int end = start +length;  //end��ʾУ��λ���ڵ�λ�� //���ڼ�¼���Ľ�����
                i = end;  //ʹѭ������λ�ƶ���֡ĩβ
                byte[] frame = new byte[length];  //���ݳ��ȴ���һ���µ��������ڱ��浥֡��
                //һ���ֽ�����������У�������ֻ��һ֡���ģ��п����ж�֡����co�����е�������ȡһ֡���µ������У�Ȼ��ֵ��������
                //��co�������ʼ��start��ʼ��дlength���ȵ����ݵ��µ�����frame��
                System.arraycopy(co,start,frame,0,length);
                GB32960 fra = new GB32960(frame);  //����һ��GB32960�������ڴ���������
                System.out.println("checkSum��ȷ��"+ fra.checkSumB);
                System.out.printf("���Ǳ���TCP���ӵĵ�%d֡����,��������:\n",count);
                printFrame(frame);  //��ӡ��count֡����
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
                }  //�˴��������±�Խ����󣬵�����GB32960���
                //break;
            }  //if 2323
        }  //for
        //printFrame(co);
        System.out.printf("���������д��%d�ֽ�����.\n",bas.size());
        bas.reset();  //��ձ��������
        //д���ļ�
        try {
            fos.write(buffer, 0, len);  //���ֽ�����д���������������ļ���fos  //ԭʼ����
            fos.write(new byte[]{0x0a,0x0a});  //�ѻ���д���ļ���
        } catch (IOException e) {
            e.printStackTrace();
            saveErrorLog(e);  //���������־
        }
        //System.out.printf("����д�����,д����%d�ֽ�����\n",len);
    }  //storage
    static void answer(GB32960 fra, byte[] frame, Socket tBoxSocket, FileOutputStream fos){
        byte[] answer = fra.answerByte(frame);  //����֡����Ӧ���
        System.out.print("Ӧ��ɹ�\n");
        //printFrame(answer);  //��ӡ��֡��Ӧ���
        OutputStream answerFrame;
        try {
            fos.write("Ӧ�������:\n".getBytes(StandardCharsets.UTF_8));
            fos.write(hex2Str(answer).getBytes(StandardCharsets.UTF_8));  //��Ӧ���д���ļ�
            fos.write("\n".getBytes(StandardCharsets.UTF_8));
            answerFrame = tBoxSocket.getOutputStream();   //���������
            answerFrame.write(answer);  //���������
        } catch (IOException e) {
            e.printStackTrace();
            saveErrorLog(e);  //���������־
        }
    }
    static void storage2(ByteArrayOutputStream bas, Socket tBoxSocket, FileOutputStream fos, byte[] buffer, int len){
        bas.write(buffer,0,len);  //��buffer�е�����ѭ��д���ֽ������������ÿ��2048
        byte[] frame = bas.toByteArray();
        if((frame[0]==0x23)&&(frame[1]==0x23)){  //��ȡ��2323��ͷ��ʾ������һ֡���ģ�ִ���±ߵ����
            short length= (short) (MyTool.combineByte(frame[22],frame[23]) + 25);  //length��ʾ�ܳ���
            if(frame.length==length){  //������㳤�Ⱥ�ʵ�ʳ��Ȳ�һ�£��˳�
                GB32960 fra = new GB32960(frame);  //����һ��GB32960�������ڴ���������
                System.out.println("���="+fra.data.vehicle.mileage);
                switch (fra.head.cmd) {
                    case 0x01: case 0x04:
                        String message6 = "\n����"+(fra.head.cmd==0x01?"�������":"����ǳ�")+".............\n";
                        System.out.print(message6);
                        try {
                            fos.write(message6.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                            saveErrorLog(e);
                        }
                        answer(fra, frame, tBoxSocket,fos);  //Ĭ��checkSumB��ȷ���Ӧ��
                        break;
                    case 0x02: case 0x03:
                        String message7 = "\n����"+(fra.head.cmd==0x02?"ʵʱ�ϴ���":"��������")+"..............\n";
                        System.out.print(message7);
                        try {
                            fos.write(message7.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                            saveErrorLog(e);
                        }
                        if(!fra.checkSumB){  //���checkSumB������Ӧ��
                            answer(fra, frame, tBoxSocket,fos);
                        }
                        break;
                    default:
                        String message8 = "\n-----------------���౨�ģ�������������----------------\n";
                        System.out.print(message8);
                        try {
                            fos.write(message8.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            e.printStackTrace();
                            saveErrorLog(e);
                        }
                        break;
                }  //switch (fra.head.cmd)
                String message9 = " checkSum= "+(fra.checkSumB?"true":"false")+"   ���Ľ��ճɹ�\n";
                System.out.print(message9);
                //printFrame(frame);  //��ӡ��count֡����
                try {
                    fos.write(message9.getBytes(StandardCharsets.UTF_8));
                    fos.write(hex2Str(frame).getBytes(StandardCharsets.UTF_8));  //�ѱ���д���ļ�
                    fos.write("\n".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                    saveErrorLog(e);
                }
            }   //���㳤�ȣ����ĳ�����ȷ
            else {  //���ĳ��ȴ���
                String message5 ="\n----------------------����--���ĳ��ȴ���--����------------------\n";
                System.out.print(message5);
                //printFrame(frame);
                try {
                    fos.write(message5.getBytes(StandardCharsets.UTF_8));
                    fos.write(hex2Str(frame).getBytes(StandardCharsets.UTF_8));  //�ѱ���д���ļ�
                    fos.write("\n".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    e.printStackTrace();
                    saveErrorLog(e);  //���������־
                }
            }
        }  //if 2323
        else { //����û2323��ͷ
            String message4 ="\n---------------------------û�ҵ�2323��ͷ�ı���--------------------\n";
            System.out.print(message4);
            //printFrame(frame);
            try {
                fos.write(message4.getBytes(StandardCharsets.UTF_8));
                fos.write(hex2Str(frame).getBytes(StandardCharsets.UTF_8)); //�ѱ���д���ļ�
                fos.write("\n".getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
                saveErrorLog(e);  //���������־
            }
        }
        String message10 = "���������д��"+bas.size()+"�ֽ�����.\n";
        System.out.print(message10);
        try {
            fos.write(message10.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
            saveErrorLog(e);  //���������־
        }
        bas.reset();  //��ձ��������
    }   //storage2
}   //class APPServer