package netTest;

import java.util.Arrays;

import static netTest.MyTool.*;
import static netTest.MyTool.combineByte;

public class GB32960 {  //创建tbox报文 的对象
    byte checkSum;  //储存数据帧的校验码
    boolean checkSumB; //是否校验成功，成功返回true
    byte serAnswer ; //服务器应答包 ,01成功，02校验错误，03VIN错误
    GBHead head = new GBHead();
    GBMain mainD = new GBMain();
    GBData data = new GBData();

    GB32960(byte[] frame){   //构造函数，传入参数
        try {
            checkSum = frame[frame.length-1];  //checkSum序号为总长度减一 位
            saveHead(frame);  //保存头部数据,所有情况都有，所以在外边执行
            checksumBool(frame);  //计算checksum，是否校验成功存入checkSumB
        }catch (Exception e){
            e.printStackTrace();
            saveErrorLog(e);  //保存错误日志
        }
        if(checkSumB){
            /*命令的主动发起方应答标志为0xFE,表示此包为命令包;当应答标志不是0xFE时,被动接收方应不应答。
            当命令的被动接收方应答标志不是0xFE时,此包表示为应答包。
            当服务端发送应答时,应变更应答标志,保留报文时间,删除其余报文内容,并重新计算校验位。
            命令标志：01登入，02上发，03补发，04登出，05 06不用管
            应答标志：  FE 表示为命令包。 应答指令：01成功，02设置错误，03VIN错误。
            翻译：车辆发0x01FE  平台发0x0101 ,车辆发0x04FE登出 平台发0x0401。  车辆发 0x02FE 和 0x03FE 只管收就行
            */
            serAnswer = 0x01;  //表示校验成功
            try {
                if(head.cmd==1){
                    saveMain(frame); //各个状态保存的主部数据不一致 ,只有登陆时保存完整数据
                }else if(head.cmd==4){//车辆登出只需要储存时间和登出流水号
                    mainD.date = Arrays.copyOfRange(frame,24,30); //存入时间 //不包括30
                    mainD.serialNum = combineByte(frame[30],frame[31]);  //存入流水号
                }else if(head.cmd==2||head.cmd==3){  //数据报文  的主部数据只有一个时间，30位开始就是整车数据。
                    mainD.date = Arrays.copyOfRange(frame,24,30); //存入时间 //不包括30
                    saveVehicle(frame);
                    saveMotor(frame);
                    saveTude(frame);
                    savePeakData(frame);
                    saveAlarmData(frame);
                }else{
                    //System.out.println("\n其他报文，服务器不响应......");
                }
            }catch (Exception e){
                e.printStackTrace();
                saveErrorLog(e);  //保存错误日志
            }
        }else {
            serAnswer = 0x02;  //表示checkSum错误
        }
    }  //构造函数
    /**
     * 采用 BCC(异或校验)法,校验范围从命令单元的第一个字节开始,同后一字节异或,直到校验码前一字节为止,校验码占用一个字节,
     * 当数据单元存在加密时,应先加密后校验,先校验后解密
     */
    void checksumBool(byte[] frame){
        checkSumB = (check(frame) == checkSum);
    }  //checksumBool
    byte check(byte[] by){  //BCC校验，传入一个帧，返回计算的校验值
        byte temp = by[2] ;  //命令单元起始字节为2
        for(int i=2;i<by.length-2;i++){ //为什么减2，因为最后一位是校验位，不进行异或运算
            temp = (byte)(temp^by[i+1]);
        }
        return temp;
    }
    byte[] answerByte(byte[] frame){  //应答包，注意：cmd为1和2才需要应答。 //返回应答的报文内容
        byte[] ansFrame = new byte[31];  //定义空的应答包
        System.arraycopy(frame,0,ansFrame,0,31);
        ansFrame[3] = serAnswer;  //传入应答标志位，把FE改为01或者02 03VIN错误
        //加密方式不变动，因为未加密。VIN不变动。时间不变动。长度变为6,。重新计算校验位。
        ansFrame[22] = 0x00; //更改数据长度
        ansFrame[23] = 0x06;//更改数据长度，因为时间的位数为6
        ansFrame[30] = check(ansFrame); //更改校验位
        return ansFrame;
    }
    void saveHead(byte[] b){  //保存头部数据
        head.start = new String(new byte[]{b[0],b[1]});
        head.cmd = b[2];
        head.answer = b[3];
        head.VIN = Arrays.toString(Arrays.copyOfRange(b, 4, 21));  //不包括21
        head.encryption = b[21];
        head.length= combineByte(b[22],b[23]);
    }
    void saveMain(byte[] b){  //保存主部数据
        mainD.date = Arrays.copyOfRange(b,24,30); //存入时间 //不包括30
        mainD.serialNum = combineByte(b[30],b[31]);  //存入流水号  //车辆登出只需要储存时间和登出流水号
        mainD.ICCID = Arrays.toString(Arrays.copyOfRange(b, 32, 52));  //存入ICCID
        mainD.cellNum = b[52];
        mainD.cellLength = b[53];
    }
    void saveVehicle(byte[] v){  //存入整车数据
        data.vehicle.id01 = v[30];//整车数据从30开始计算
        data.vehicle.vehicleModel = v[31];
        data.vehicle.chargeModel = v[32];
        data.vehicle.mode = v[33];
        data.vehicle.speed = combineByte(v[34],v[35])*0.1d;//乘以精度
        data.vehicle.mileage = combineByte(new byte[]{v[36],v[37],v[38],v[39]})*0.1d;
        data.vehicle.totalVolt = combineByte(v[40],v[41])*0.1d;
        data.vehicle.totalCurrent = (combineByte(v[42],v[43])*0.1d)-1000;//乘以精度减偏移量
        data.vehicle.SOC = v[44];
        data.vehicle.DCDCStatus = v[45];
        data.vehicle.accSts = getBooleanArray(v[46])[2];
        data.vehicle.brakeSts = getBooleanArray(v[46])[3];
        data.vehicle.gear = v[46]&0b00001111;//0空档 13倒档 14D档 15P档 //舍去高位得到低位0x1E 0001 1110 =14
        data.vehicle.resistance = combineByte(v[47],v[48]);
        data.vehicle.accPedal = v[49];
        data.vehicle.brakePedal = v[50];
    }
    void saveMotor(byte[] m){  //存入电机数据
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
    void saveTude(byte[] l){  //存入经纬度
        data.position.id05 = l[65];
        data.position.status = l[66];
        data.position.longitude= tude(new byte[]{l[67],l[68],l[69],l[70]}); //写入经度
        data.position.latitude= tude(new byte[]{l[71],l[72],l[73],l[74]}); //写入纬度
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
        data.alarm.maxAlarmLevel = a[91]; //报警等级
        data.alarm.genAlarmFlags = getBooleanArray(Arrays.copyOfRange(a,92,96)); //19项
        int N1 = data.alarm.cellFltTotal = a[96];//根据这个长度，后边的值会有变动。//如果为0，后边就不跟数据
        //data.alarm.cellFltList = ; //[]
        //data.alarm.motFltTotal = a[96+4*N1];

    }
    void saveFault(byte[] f){ //故障处理,如果值等于FF无效 FE异常

    }
    static class GBHead{  //头部数据
        String start;        //开始位置 0，长度 2位，起始符 2323 ## 表示帧头
        int cmd;           //开始位置 2，长度 1位，命令标志  01：车辆登入（上行） 02：实时信息上报（上行）03：补发 04登出
        int answer;        //开始位置 3，长度 1位，应答标志  FE应答标志命令包 01成功 02设置错误 03 VIN错误。
        String VIN;         /* 开始位置 4，长度 17位，车架号 LWLRM BNG5N L0979 92             String */
        int encryption;    //开始位置 21，长度 1位，0x01:数据不加密;0x02:数据经过 RSA 算法加密;0x03:数据经过AES128位算法加密;“0xFE”表 示 异 常,“0xFF”表 示 无 效,其 他 预留
        int length;       //开始位置 22，长度 2位，用于记录数据长度 最多65532 //combineByte方法把两个byte结合在一起
    }   //class Head
    static class GBMain{  //主部数据 //车辆登出只需要储存时间和登出流水号  //正常数据主部数据只有时间数据
        byte[] date;          //开始位置 24，长度 6位，用于记录时间        String
        int serialNum;        //开始位置30 ,长度2 ，登入流水号，每次登入就加一
        String ICCID;         //开始位置32，长度20，ICCID        String
        int cellNum;         // 开始位置52，长度1，可充电储能子系统数 n
        int cellLength;      //开始位置53，可充电储能系统编码长度 m,有效范围:0~50,“0”表示不上传该编码
        String cellEncoded;   //开始位置54，，可充电储能系统编码 String 长度n×m n个，每个m长度 可充电储能系统编码宜为终端从车辆获取的值
    }  //class GBMain
    /**精度偏移量计算方法    精度和偏移量多半用于需要小数点和负数的值，而网络数据只传输无符号16进制数，所以用精度和偏移量来控制小数点和正负
     * 16进制原始数据 : HEX,实际值 : Value ,精度 degree,偏移量 offset     表达式 Value = (HEX * degree) - offset
     * 例如 电流原始数据2BE8，转换成十进制11240，乘以0.1精度得1124，减去偏移量1000得  124安
     */
    static class GBData{  //数据位0x81
        Vehicle vehicle = new Vehicle();
        Motor motor = new Motor();
        Position position = new Position();
        peakData peak = new peakData();
        alarmData alarm = new alarmData();
        static class Vehicle{  //整车数据  13项
            int id01;  //开始位置30，长度1，默认01表示整车数据
            int vehicleModel; //开始位置31，长度1，车辆状态，0x01:车辆启动状态;0x02:熄火;0x03:其他状态;“0xFE”表示异常,“0xFF”表示无效
            int chargeModel;  //开始位置32，长度1，充电状态，0x01:停车充电;0x02:行驶充电;0x03:未充电状态;0x04:充电完成;“0xFE”表示异常,“0xFF”表示无效
            int mode;  //开始位置33，长度1,运行模式,0x01:纯电;0x02:混动;0x03:燃油;0xFE表示异常;0xFF表示无效
            double speed; //开始位置34，长度2,速度，有效值范围:0~2200(表示0km/h~220km/h),最小计量单元:0.1km/h,// “0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
            double mileage; //开始位置36，长度4，累计里程，有效值范围:0~9999999(表示0km~999999.9km),最小计量单元:0.1km。// “0xFF,0xFF,0xFF,0xFE”表 示 异 常,“0xFF,0xFF,0xFF,0xFF”表示无效
            double totalVolt;//开始位置40，长度2，总电压，有效值范围:0~10000(表示0V~1000V),最小计量单元:0.1V,“0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
            double totalCurrent;//开始位置42，长度2，总电流，有效值范围:0~20000(偏移量1000A,表示-1000A~+1000A),最小计量单元:0.1A,“0xFF,0xFE”表 示 异 常,“0xFF,0xFF”表示无效
            int SOC;//开始位置44，长度1，SOC，有效值范围:0~100(表示0%~100%),最小计量单元:1%,“0xFE”表示异常,“0xFF”表示无效
            int DCDCStatus;//开始位置45，长度1，DCDC工作状态,0x01:工作;0x02:断开,“0xFE”表示异常,“0xFF”表示无效
            int accSts; ///驱动状态
            int brakeSts; //制动状态
            int gear;//开始位置46，长度1，档位 bit7bit6预留，bit5驱动力1表示有，bit4制动力1表示有，bit3210:0空档 1一档 13倒档 14前进D档 15停车P档，如1E表示前进D档
            int resistance;//开始位置47，长度2，绝缘电阻，有效范围0~60000(表 示 0kΩ~60000kΩ),最小计量单元:1kΩ
            int accPedal;//开始位置49,长度1，加速踏板行程值，最大值100  //这两个数据为预留数据，提需求后改为行程值
            int brakePedal;//开始位置50，长度1，制动踏板开度，最大值100
        }   //class vehicle
        static class Motor{  //电机数据  9项
            int id02;   //开始位置51，长度1，默认02表示电机数据
            int motorNum;   //开始位置52，长度1，驱动电机个数  有效值1~253
            int motorOrder;   //开始位置53，长度1，驱动电机序号  有效值范围1~253
            int motorStatus;  //开始位置54，长度1，驱动电机状态 0x01:耗电;0x02:发电;0x03:关闭状态;0x04:准备状态“0xFE”表示异常,“0xFF”表示无效
            int IGBTTemp;  //开始位置55，长度1，驱动电机控制器温度,有效值范围:0~250(数值偏移量40℃,表示-40℃~+210 ℃),最小计量单元:1℃,“0xFE”表示异常,“0xFF”表示无效
            int motorSpd; //开始位置56，长度2,驱动电机转速，有 效 值 范 围:0 ~ 65531 (数值偏移量20000表示-20000r/min~45531r/min),最小计量单元:1r/min,“0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
            double motorTq; //开始位置58,长度2 ，驱动电机转矩,有效值范围:0~65531(数值偏移量20000表示-2000N·m~4553.1N·m),最小计量单元:0.1N·m,“0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
            int motorTemp;//开始位置60，长度1，驱动电机温度，有效值范围:0~250(数值偏移量40℃,表示-40℃~+210℃),最小计量单元:1℃,“0xFE”表示异常,“0xFF”表 示无效
            double inputVolt;//开始位置61，长度2，电机控制器输入电压，有效值范围:0~60000(表示0V~6000V),最小计量单元:0.1V,“0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
            double busCurrent;//开始位置63，长度2，电机控制器直流母线电流，有效值范围:0~20000(数值偏移量1000A,表示-1000A~+1000A),最小计量单元:0.1A,“0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
        }  //class motor
        static class Position{   //位置数据  3项
            int id05;    //开始位置65，长度1，默认05表示定位数据
            int status;    //开始位置66，长度1，定位状态 8个bit
            // bit0(最右边) 0:有效定位;1:无效定位  (当数据通信正常,而不能获取定位信息时,发送最后一次有效定位信息,并将定位状态置为无效。)
            // bit1  0:北纬;1:南纬        // bit2  0:东经;1:西经       // bit3~7 保留
            double longitude ;  //开始位置67，长度4，经度
            double latitude ;   //开始71，长度4，纬度
        }  //class Position
        static class peakData{  //极值数据06，开始位置 75+76  12项
            int id06;//开始位置75，默认06表示极值数据
            int HVBatNum;//开始位置76，长度1，最高电压电池子系统号   有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            int HVCellNum;//77,1 最高电压电池单体代号   有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            double cellHVolt;//78,2 电池单体电压最高值  有效值范围:0~15000(表示0V~15V),最小计量单元:0.001V,“0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
            int LVBatNum;//80 1 最低电压电池子系统号   有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            int LVCellNum;//81,1 最低电压电池单体代号  有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            double cellLVolt; //82,2 电池单体电压最低值   有效值范围:0~15000(表示0V~15V),最小计量单元:0.001V,“0xFF,0xFE”表示异常,“0xFF,0xFF”表示无效
            int HTempCellNum;//84,1 最高温度子系统号  有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            int HTempProbeNum;//85,1 最高温度探针序号  有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            int HTemp;//86,1 最高温度值   有效值范围:0~250(数 值 偏 移 量 40 ℃,表 示 -40 ℃ ~+210 ℃),最小计量单元:1 ℃,“0xFE”表示异常,“0xFF”表示无效
            int LTempCellNum;//87,1 最低温度子系统号  有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            int LTempProbeNum;//88,1 最低温度探针序号   有效值范围:1~250,“0xFE”表示异常,“0xFF”表示无效
            int LTemp;// 89,1 最低温度值   有效值范围:0~250(数 值 偏 移 量 40 ℃,表 示 -40 ℃ ~+210 ℃),最小计量单元:1 ℃,“0xFE”表示异常,“0xFF”表示无效
        }
        static class alarmData{  //报警数据07，开始位置 90+91   默认6项  注意：这一项也是可变长数据，无报警则是0，有报警那么下边会有报警项列表， 具体见国标GB32960
            int id07;   //开始位置90，默认07表示报警数据
            int maxAlarmLevel;//91,1  最高报警等级   为当前发生的故障中的最高等级值,有效值范围:0~3,“0”表示无故障;
            // “1”表示1级故障,指代不影响车辆正常行驶的故障;“2”表示2级故障,指代影响车辆性能,需驾驶员限制行驶的故障;
            // “3”表示3级故障,为最高级别故障,指代驾驶员应立即停车处理或请求救援的故障;具体等级对应的故障内容由厂商自行定义;“0xFE”表示异常,“0xFF”表示无效
            byte[] genAlarmFlags;//92,4 通用报警标志 19项 32位 32个故障
            int cellFltTotal;//96 1 可充电储能装置故障总数 N1 N1 个可充电储能装置故障,有效值范围:0~252,“0xFE”表示异常,“0xFF”表示无效
            int[] cellFltList;//可充电储能装置故障代码列表,长度4×N (长度乘以个数) 扩展性数据,由厂商自行定义,可充电储能装置故障个数等于可充电储能装置故障总数 N1
            int motFltTotal;// 驱动电机故障总数 N2 N2 个驱动电机故障,有效值范围:0~252,“0xFE”表示异常,“0xFF”表示无效
            int[] motFltList;//驱动电机故障代码列表 长度4×N2  厂商自行定义,驱动电机故障个数等于驱动电机故障总数 N2
            int engineFltTotal;//发动机故障总数 N3  N3 个驱动电机故障,有效值范围:0~252,“0xFE”表示异常,“0xFF”表示无效
            int[] engineFltList;//发动机故障列表 长度4×N3 厂商自行定义,发动机故障个数等于驱动电机故障总数 N3
            int otherFltTotal;//其他故障总数 N4  N4 个其 他 故 障,有 效 值 范 围:0~252,“0xFE”表 示 异 常,“0xFF”表示无效
            int[] otherFltList;// 其他故障代码列表 厂商自行定义,故障个数等于故障总数 N4
        }
        //可充电储能装置电压数据 ，开始位置104+105  163项  //注意，电压数据和温度数据是可变长数据，不能根据位置来判断数据，需要根据电池包的个数来决定报文长度
        //可充电储能装置温度数据,开始位置428+429  21项   //可变长数据，同上
    }  //class GBData
    private double tude(byte[] b){  //传入一个4位长度的字节数组，输出一个浮点型 经纬度  06 55 DA E7    76 54 32 10
        double re = combineByte(b);
        return re*0.000001f; //10的-6次方

    }  //tude

}  ///class GB32960