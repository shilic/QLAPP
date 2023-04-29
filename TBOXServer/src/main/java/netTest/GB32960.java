package netTest;

import java.util.Arrays;

import static netTest.MyTool.*;
import static netTest.MyTool.combineByte;

public class GB32960 {  //����tbox���� �Ķ���
    byte checkSum;  //��������֡��У����
    boolean checkSumB; //�Ƿ�У��ɹ����ɹ�����true
    byte serAnswer ; //������Ӧ��� ,01�ɹ���02У�����03VIN����
    GBHead head = new GBHead();
    GBMain mainD = new GBMain();
    GBData data = new GBData();

    GB32960(byte[] frame){   //���캯�����������
        try {
            checkSum = frame[frame.length-1];  //checkSum���Ϊ�ܳ��ȼ�һ λ
            saveHead(frame);  //����ͷ������,����������У����������ִ��
            checksumBool(frame);  //����checksum���Ƿ�У��ɹ�����checkSumB
        }catch (Exception e){
            e.printStackTrace();
            saveErrorLog(e);  //���������־
        }
        if(checkSumB){
            /*�������������Ӧ���־Ϊ0xFE,��ʾ�˰�Ϊ�����;��Ӧ���־����0xFEʱ,�������շ�Ӧ��Ӧ��
            ������ı������շ�Ӧ���־����0xFEʱ,�˰���ʾΪӦ�����
            ������˷���Ӧ��ʱ,Ӧ���Ӧ���־,��������ʱ��,ɾ�����౨������,�����¼���У��λ��
            �����־��01���룬02�Ϸ���03������04�ǳ���05 06���ù�
            Ӧ���־��  FE ��ʾΪ������� Ӧ��ָ�01�ɹ���02���ô���03VIN����
            ���룺������0x01FE  ƽ̨��0x0101 ,������0x04FE�ǳ� ƽ̨��0x0401��  ������ 0x02FE �� 0x03FE ֻ���վ���
            */
            serAnswer = 0x01;  //��ʾУ��ɹ�
            try {
                if(head.cmd==1){
                    saveMain(frame); //����״̬������������ݲ�һ�� ,ֻ�е�½ʱ������������
                }else if(head.cmd==4){//�����ǳ�ֻ��Ҫ����ʱ��͵ǳ���ˮ��
                    mainD.date = Arrays.copyOfRange(frame,24,30); //����ʱ�� //������30
                    mainD.serialNum = combineByte(frame[30],frame[31]);  //������ˮ��
                }else if(head.cmd==2||head.cmd==3){  //���ݱ���  ����������ֻ��һ��ʱ�䣬30λ��ʼ�����������ݡ�
                    mainD.date = Arrays.copyOfRange(frame,24,30); //����ʱ�� //������30
                    saveVehicle(frame);
                    saveMotor(frame);
                    saveTude(frame);
                    savePeakData(frame);
                    saveAlarmData(frame);
                }else{
                    //System.out.println("\n�������ģ�����������Ӧ......");
                }
            }catch (Exception e){
                e.printStackTrace();
                saveErrorLog(e);  //���������־
            }
        }else {
            serAnswer = 0x02;  //��ʾcheckSum����
        }
    }  //���캯��
    /**
     * ���� BCC(���У��)��,У�鷶Χ�����Ԫ�ĵ�һ���ֽڿ�ʼ,ͬ��һ�ֽ����,ֱ��У����ǰһ�ֽ�Ϊֹ,У����ռ��һ���ֽ�,
     * �����ݵ�Ԫ���ڼ���ʱ,Ӧ�ȼ��ܺ�У��,��У������
     */
    void checksumBool(byte[] frame){
        checkSumB = (check(frame) == checkSum);
    }  //checksumBool
    byte check(byte[] by){  //BCCУ�飬����һ��֡�����ؼ����У��ֵ
        byte temp = by[2] ;  //���Ԫ��ʼ�ֽ�Ϊ2
        for(int i=2;i<by.length-2;i++){ //Ϊʲô��2����Ϊ���һλ��У��λ���������������
            temp = (byte)(temp^by[i+1]);
        }
        return temp;
    }
    byte[] answerByte(byte[] frame){  //Ӧ�����ע�⣺cmdΪ1��2����ҪӦ�� //����Ӧ��ı�������
        byte[] ansFrame = new byte[31];  //����յ�Ӧ���
        System.arraycopy(frame,0,ansFrame,0,31);
        ansFrame[3] = serAnswer;  //����Ӧ���־λ����FE��Ϊ01����02 03VIN����
        //���ܷ�ʽ���䶯����Ϊδ���ܡ�VIN���䶯��ʱ�䲻�䶯�����ȱ�Ϊ6,�����¼���У��λ��
        ansFrame[22] = 0x00; //�������ݳ���
        ansFrame[23] = 0x06;//�������ݳ��ȣ���Ϊʱ���λ��Ϊ6
        ansFrame[30] = check(ansFrame); //����У��λ
        return ansFrame;
    }
    void saveHead(byte[] b){  //����ͷ������
        head.start = new String(new byte[]{b[0],b[1]});
        head.cmd = b[2];
        head.answer = b[3];
        head.VIN = Arrays.toString(Arrays.copyOfRange(b, 4, 21));  //������21
        head.encryption = b[21];
        head.length= combineByte(b[22],b[23]);
    }
    void saveMain(byte[] b){  //������������
        mainD.date = Arrays.copyOfRange(b,24,30); //����ʱ�� //������30
        mainD.serialNum = combineByte(b[30],b[31]);  //������ˮ��  //�����ǳ�ֻ��Ҫ����ʱ��͵ǳ���ˮ��
        mainD.ICCID = Arrays.toString(Arrays.copyOfRange(b, 32, 52));  //����ICCID
        mainD.cellNum = b[52];
        mainD.cellLength = b[53];
    }
    void saveVehicle(byte[] v){  //������������
        data.vehicle.id01 = v[30];//�������ݴ�30��ʼ����
        data.vehicle.vehicleModel = v[31];
        data.vehicle.chargeModel = v[32];
        data.vehicle.mode = v[33];
        data.vehicle.speed = combineByte(v[34],v[35])*0.1d;//���Ծ���
        data.vehicle.mileage = combineByte(new byte[]{v[36],v[37],v[38],v[39]})*0.1d;
        data.vehicle.totalVolt = combineByte(v[40],v[41])*0.1d;
        data.vehicle.totalCurrent = (combineByte(v[42],v[43])*0.1d)-1000;//���Ծ��ȼ�ƫ����
        data.vehicle.SOC = v[44];
        data.vehicle.DCDCStatus = v[45];
        data.vehicle.accSts = getBooleanArray(v[46])[2];
        data.vehicle.brakeSts = getBooleanArray(v[46])[3];
        data.vehicle.gear = v[46]&0b00001111;//0�յ� 13���� 14D�� 15P�� //��ȥ��λ�õ���λ0x1E 0001 1110 =14
        data.vehicle.resistance = combineByte(v[47],v[48]);
        data.vehicle.accPedal = v[49];
        data.vehicle.brakePedal = v[50];
    }
    void saveMotor(byte[] m){  //����������
        data.motor.id02 = m[51];
        data.motor.motorNum = m[52];
        data.motor.motorOrder = m[53];
        data.motor.motorStatus = m[54];
        data.motor.IGBTTemp = m[55]-40;
        data.motor.motorSpd = combineByte(m[56],m[57])-20000;
        data.motor.motorTq = combineByte(m[58],m[59])*0.1d-2000;
        data.motor.motorTemp = m[60]-40;
        data.motor.inputVolt = combineByte(m[61],m[62])*0.1d;
        data.motor.busCurrent = combineByte(m[63],m[64])*0.1d-1000;
    }
    void saveTude(byte[] l){  //���뾭γ��
        data.position.id05 = l[65];
        data.position.status = l[66];
        data.position.longitude= tude(new byte[]{l[67],l[68],l[69],l[70]}); //д�뾭��
        data.position.latitude= tude(new byte[]{l[71],l[72],l[73],l[74]}); //д��γ��
    }
    void savePeakData(byte[] p){
        data.peak.id06 = p[75];
        data.peak.HVBatNum = p[76];
        data.peak.HVCellNum = p[77];
        data.peak.cellHVolt = combineByte(p[78],p[79])*0.001d;
        data.peak.LVBatNum = p[80];
        data.peak.LVCellNum = p[81];
        data.peak.cellLVolt = combineByte(p[82],p[83])*0.001d;
        data.peak.HTempCellNum = p[84];
        data.peak.HTempProbeNum = p[85];
        data.peak.HTemp = p[86]-40;
        data.peak.LTempCellNum = p[87];
        data.peak.LTempProbeNum = p[88];
        data.peak.LTemp = p[89]-40;
    }
    void saveAlarmData(byte[] a){
        data.alarm.id07 = a[90];
        data.alarm.maxAlarmLevel = a[91]; //�����ȼ�
        data.alarm.genAlarmFlags = getBooleanArray(Arrays.copyOfRange(a,92,96)); //19��
        int N1 = data.alarm.cellFltTotal = a[96];//����������ȣ���ߵ�ֵ���б䶯��//���Ϊ0����߾Ͳ�������
        //data.alarm.cellFltList = ; //[]
        //data.alarm.motFltTotal = a[96+4*N1];

    }
    void saveFault(byte[] f){ //���ϴ���,���ֵ����FF��Ч FE�쳣

    }
    static class GBHead{  //ͷ������
        String start;        //��ʼλ�� 0������ 2λ����ʼ�� 2323 ## ��ʾ֡ͷ
        int cmd;           //��ʼλ�� 2������ 1λ�������־  01���������루���У� 02��ʵʱ��Ϣ�ϱ������У�03������ 04�ǳ�
        int answer;        //��ʼλ�� 3������ 1λ��Ӧ���־  FEӦ���־����� 01�ɹ� 02���ô��� 03 VIN����
        String VIN;         /* ��ʼλ�� 4������ 17λ�����ܺ� LWLRM BNG5N L0979 92             String */
        int encryption;    //��ʼλ�� 21������ 1λ��0x01:���ݲ�����;0x02:���ݾ��� RSA �㷨����;0x03:���ݾ���AES128λ�㷨����;��0xFE���� ʾ �� ��,��0xFF���� ʾ �� Ч,�� �� Ԥ��
        int length;       //��ʼλ�� 22������ 2λ�����ڼ�¼���ݳ��� ���65532 //combineByte����������byte�����һ��
    }   //class Head
    static class GBMain{  //�������� //�����ǳ�ֻ��Ҫ����ʱ��͵ǳ���ˮ��  //����������������ֻ��ʱ������
        byte[] date;          //��ʼλ�� 24������ 6λ�����ڼ�¼ʱ��        String
        int serialNum;        //��ʼλ��30 ,����2 ��������ˮ�ţ�ÿ�ε���ͼ�һ
        String ICCID;         //��ʼλ��32������20��ICCID        String
        int cellNum;         // ��ʼλ��52������1���ɳ�索����ϵͳ�� n
        int cellLength;      //��ʼλ��53���ɳ�索��ϵͳ���볤�� m,��Ч��Χ:0~50,��0����ʾ���ϴ��ñ���
        String cellEncoded;   //��ʼλ��54�����ɳ�索��ϵͳ���� String ����n��m n����ÿ��m���� �ɳ�索��ϵͳ������Ϊ�ն˴ӳ�����ȡ��ֵ
    }  //class GBMain
    /**����ƫ�������㷽��    ���Ⱥ�ƫ�������������ҪС����͸�����ֵ������������ֻ�����޷���16�������������þ��Ⱥ�ƫ����������С���������
     * 16����ԭʼ���� : HEX,ʵ��ֵ : Value ,���� degree,ƫ���� offset     ���ʽ Value = (HEX * degree) - offset
     * ���� ����ԭʼ����2BE8��ת����ʮ����11240������0.1���ȵ�1124����ȥƫ����1000��  124��
     */
    static class GBData{  //����λ0x81
        Vehicle vehicle = new Vehicle();
        Motor motor = new Motor();
        Position position = new Position();
        peakData peak = new peakData();
        alarmData alarm = new alarmData();
        static class Vehicle{  //��������  13��
            int id01;  //��ʼλ��30������1��Ĭ��01��ʾ��������
            int vehicleModel; //��ʼλ��31������1������״̬��0x01:��������״̬;0x02:Ϩ��;0x03:����״̬;��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int chargeModel;  //��ʼλ��32������1�����״̬��0x01:ͣ�����;0x02:��ʻ���;0x03:δ���״̬;0x04:������;��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int mode;  //��ʼλ��33������1,����ģʽ,0x01:����;0x02:�춯;0x03:ȼ��;0xFE��ʾ�쳣;0xFF��ʾ��Ч
            double speed; //��ʼλ��34������2,�ٶȣ���Чֵ��Χ:0~2200(��ʾ0km/h~220km/h),��С������Ԫ:0.1km/h,// ��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
            double mileage; //��ʼλ��36������4���ۼ���̣���Чֵ��Χ:0~9999999(��ʾ0km~999999.9km),��С������Ԫ:0.1km��// ��0xFF,0xFF,0xFF,0xFE���� ʾ �� ��,��0xFF,0xFF,0xFF,0xFF����ʾ��Ч
            double totalVolt;//��ʼλ��40������2���ܵ�ѹ����Чֵ��Χ:0~10000(��ʾ0V~1000V),��С������Ԫ:0.1V,��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
            double totalCurrent;//��ʼλ��42������2���ܵ�������Чֵ��Χ:0~20000(ƫ����1000A,��ʾ-1000A~+1000A),��С������Ԫ:0.1A,��0xFF,0xFE���� ʾ �� ��,��0xFF,0xFF����ʾ��Ч
            int SOC;//��ʼλ��44������1��SOC����Чֵ��Χ:0~100(��ʾ0%~100%),��С������Ԫ:1%,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int DCDCStatus;//��ʼλ��45������1��DCDC����״̬,0x01:����;0x02:�Ͽ�,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int accSts; ///����״̬
            int brakeSts; //�ƶ�״̬
            int gear;//��ʼλ��46������1����λ bit7bit6Ԥ����bit5������1��ʾ�У�bit4�ƶ���1��ʾ�У�bit3210:0�յ� 1һ�� 13���� 14ǰ��D�� 15ͣ��P������1E��ʾǰ��D��
            int resistance;//��ʼλ��47������2����Ե���裬��Ч��Χ0~60000(�� ʾ 0k��~60000k��),��С������Ԫ:1k��
            int accPedal;//��ʼλ��49,����1������̤���г�ֵ�����ֵ100  //����������ΪԤ�����ݣ���������Ϊ�г�ֵ
            int brakePedal;//��ʼλ��50������1���ƶ�̤�忪�ȣ����ֵ100
        }   //class vehicle
        static class Motor{  //�������  9��
            int id02;   //��ʼλ��51������1��Ĭ��02��ʾ�������
            int motorNum;   //��ʼλ��52������1�������������  ��Чֵ1~253
            int motorOrder;   //��ʼλ��53������1������������  ��Чֵ��Χ1~253
            int motorStatus;  //��ʼλ��54������1���������״̬ 0x01:�ĵ�;0x02:����;0x03:�ر�״̬;0x04:׼��״̬��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int IGBTTemp;  //��ʼλ��55������1����������������¶�,��Чֵ��Χ:0~250(��ֵƫ����40��,��ʾ-40��~+210 ��),��С������Ԫ:1��,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int motorSpd; //��ʼλ��56������2,�������ת�٣��� Ч ֵ �� Χ:0 ~ 65531 (��ֵƫ����20000��ʾ-20000r/min~45531r/min),��С������Ԫ:1r/min,��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
            double motorTq; //��ʼλ��58,����2 ���������ת��,��Чֵ��Χ:0~65531(��ֵƫ����20000��ʾ-2000N��m~4553.1N��m),��С������Ԫ:0.1N��m,��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
            int motorTemp;//��ʼλ��60������1����������¶ȣ���Чֵ��Χ:0~250(��ֵƫ����40��,��ʾ-40��~+210��),��С������Ԫ:1��,��0xFE����ʾ�쳣,��0xFF���� ʾ��Ч
            double inputVolt;//��ʼλ��61������2����������������ѹ����Чֵ��Χ:0~60000(��ʾ0V~6000V),��С������Ԫ:0.1V,��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
            double busCurrent;//��ʼλ��63������2�����������ֱ��ĸ�ߵ�������Чֵ��Χ:0~20000(��ֵƫ����1000A,��ʾ-1000A~+1000A),��С������Ԫ:0.1A,��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
        }  //class motor
        static class Position{   //λ������  3��
            int id05;    //��ʼλ��65������1��Ĭ��05��ʾ��λ����
            int status;    //��ʼλ��66������1����λ״̬ 8��bit
            // bit0(���ұ�) 0:��Ч��λ;1:��Ч��λ  (������ͨ������,�����ܻ�ȡ��λ��Ϣʱ,�������һ����Ч��λ��Ϣ,������λ״̬��Ϊ��Ч��)
            // bit1  0:��γ;1:��γ        // bit2  0:����;1:����       // bit3~7 ����
            double longitude ;  //��ʼλ��67������4������
            double latitude ;   //��ʼ71������4��γ��
        }  //class Position
        static class peakData{  //��ֵ����06����ʼλ�� 75+76  12��
            int id06;//��ʼλ��75��Ĭ��06��ʾ��ֵ����
            int HVBatNum;//��ʼλ��76������1����ߵ�ѹ�����ϵͳ��   ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int HVCellNum;//77,1 ��ߵ�ѹ��ص������   ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            double cellHVolt;//78,2 ��ص����ѹ���ֵ  ��Чֵ��Χ:0~15000(��ʾ0V~15V),��С������Ԫ:0.001V,��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
            int LVBatNum;//80 1 ��͵�ѹ�����ϵͳ��   ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int LVCellNum;//81,1 ��͵�ѹ��ص������  ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            double cellLVolt; //82,2 ��ص����ѹ���ֵ   ��Чֵ��Χ:0~15000(��ʾ0V~15V),��С������Ԫ:0.001V,��0xFF,0xFE����ʾ�쳣,��0xFF,0xFF����ʾ��Ч
            int HTempCellNum;//84,1 ����¶���ϵͳ��  ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int HTempProbeNum;//85,1 ����¶�̽�����  ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int HTemp;//86,1 ����¶�ֵ   ��Чֵ��Χ:0~250(�� ֵ ƫ �� �� 40 ��,�� ʾ -40 �� ~+210 ��),��С������Ԫ:1 ��,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int LTempCellNum;//87,1 ����¶���ϵͳ��  ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int LTempProbeNum;//88,1 ����¶�̽�����   ��Чֵ��Χ:1~250,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int LTemp;// 89,1 ����¶�ֵ   ��Чֵ��Χ:0~250(�� ֵ ƫ �� �� 40 ��,�� ʾ -40 �� ~+210 ��),��С������Ԫ:1 ��,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
        }
        static class alarmData{  //��������07����ʼλ�� 90+91   Ĭ��6��  ע�⣺��һ��Ҳ�ǿɱ䳤���ݣ��ޱ�������0���б�����ô�±߻��б������б� ���������GB32960
            int id07;   //��ʼλ��90��Ĭ��07��ʾ��������
            int maxAlarmLevel;//91,1  ��߱����ȼ�   Ϊ��ǰ�����Ĺ����е���ߵȼ�ֵ,��Чֵ��Χ:0~3,��0����ʾ�޹���;
            // ��1����ʾ1������,ָ����Ӱ�쳵��������ʻ�Ĺ���;��2����ʾ2������,ָ��Ӱ�쳵������,���ʻԱ������ʻ�Ĺ���;
            // ��3����ʾ3������,Ϊ��߼������,ָ����ʻԱӦ����ͣ������������Ԯ�Ĺ���;����ȼ���Ӧ�Ĺ��������ɳ������ж���;��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            byte[] genAlarmFlags;//92,4 ͨ�ñ�����־ 19�� 32λ 32������
            int cellFltTotal;//96 1 �ɳ�索��װ�ù������� N1 N1 ���ɳ�索��װ�ù���,��Чֵ��Χ:0~252,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int[] cellFltList;//�ɳ�索��װ�ù��ϴ����б�,����4��N (���ȳ��Ը���) ��չ������,�ɳ������ж���,�ɳ�索��װ�ù��ϸ������ڿɳ�索��װ�ù������� N1
            int motFltTotal;// ��������������� N2 N2 �������������,��Чֵ��Χ:0~252,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int[] motFltList;//����������ϴ����б� ����4��N2  �������ж���,����������ϸ���������������������� N2
            int engineFltTotal;//�������������� N3  N3 �������������,��Чֵ��Χ:0~252,��0xFE����ʾ�쳣,��0xFF����ʾ��Ч
            int[] engineFltList;//�����������б� ����4��N3 �������ж���,���������ϸ���������������������� N3
            int otherFltTotal;//������������ N4  N4 ���� �� �� ��,�� Ч ֵ �� Χ:0~252,��0xFE���� ʾ �� ��,��0xFF����ʾ��Ч
            int[] otherFltList;// �������ϴ����б� �������ж���,���ϸ������ڹ������� N4
        }
        //�ɳ�索��װ�õ�ѹ���� ����ʼλ��104+105  163��  //ע�⣬��ѹ���ݺ��¶������ǿɱ䳤���ݣ����ܸ���λ�����ж����ݣ���Ҫ���ݵ�ذ��ĸ������������ĳ���
        //�ɳ�索��װ���¶�����,��ʼλ��428+429  21��   //�ɱ䳤���ݣ�ͬ��
    }  //class GBData
    private double tude(byte[] b){  //����һ��4λ���ȵ��ֽ����飬���һ�������� ��γ��  06 55 DA E7    76 54 32 10
        double re = combineByte(b);
        return re*0.000001f; //10��-6�η�

    }  //tude

}  ///class GB32960