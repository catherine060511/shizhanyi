package com.huawei.subwaycharge;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.huawei.exam.ReturnCodeEnum;



/**
 * <p>Title: ������ʵ����</p>
 * ��������Ҫ�󷵻أ���������װ�������
 * ϵͳ�ṩ��3������ӿڣ���OpResult���ж��壬�뿼�����Ķ����塣
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2010</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ChargeImpl {


    // �����ṩ�޲������캯�����������ں������и�����Ҫ���ӳ�ʼ������
    // ������л���ֻ������һ��ChargeImplʵ��������������������������һֱʹ�����ʵ��
	List<ChargeLogInfo> list ;
	//List<DistanceInfo> distanceInfoList ;
	
    public ChargeImpl() {
    	list = new ArrayList<ChargeLogInfo>();
    	
    }

    /**
     * ������Ҫʵ�ֵĽӿ�
     * r����ӿڣ�ʵ��ϵͳ�ĳ�ʼ��
     *
     * @return OpResult��������
     */
    public OpResult opReset() {
    	list.clear();
    	System.out.println("list clear");
        return OpResult.createOpResult(ReturnCodeEnum.I00);
    }


    /**
     * ������Ҫʵ�ֵĽӿ�
     * q����ӿڣ�ʵ�ֿ۷���־�Ĳ�ѯ����
     *
     * @param cardNo int   ������
     * @param startHour int����ѯ��ʼʱ���Сʱ����
     * @param startMin int ����ѯ��ʼʱ��ķ��Ӳ���
     * @param endHour int  ����ѯ����ʱ���Сʱ����
     * @param endMin int   ����ѯ����ʱ��ķ��Ӳ���
     *
     * @return OpResult��������
     */
    public OpResult opQuery(int cardNo, int startHour, int startMin,
                            int endHour, int endMin) {

        //��ѯ�ɹ��뿼������OpResult��Ľӿ�OpResult createOpResult(ReturnCodeEnum returnCode, ChargeLogInfo[] logs);
    	if(cardNo >9 ||cardNo <0 || timeJudge(startHour,startMin,endHour,endMin)){
    		return OpResult.createOpResult(ReturnCodeEnum.E01);
    	}else{
    		/*
    		 * list�������򣬰���cardNo��"endHour+endMin"
    		 */
    		Collections.sort(list, new Comparator<ChargeLogInfo>(){
    			
    			public int compare(ChargeLogInfo logInfo1,ChargeLogInfo logInfo2){
    				if(logInfo1.getCardNo() == logInfo2.getCardNo()){
        				Integer loginfo1OutTime = normalizationTime(logInfo1.getOutHour(),logInfo1.getOutMinute());
        				Integer loginfo2OutTime = normalizationTime(logInfo2.getOutHour(),logInfo2.getOutMinute());
        				return loginfo1OutTime.compareTo(loginfo2OutTime);
    				}else{
    					return Integer.valueOf(logInfo1.getCardNo()).compareTo(Integer.valueOf(logInfo2.getCardNo()));
    				}
    					
    			}
    		});
  
        	
        	int newStartTime = normalizationTime(startHour,startMin);
    		int newEndTime = normalizationTime(endHour,endMin);
    		
    		if(cardNo == 0){
    			boolean flag = true;
    			for(ChargeLogInfo loginfo:list){
    				//loginfo ����ĳ�վʱ��
    				int loginfoOutTime = normalizationTime(loginfo.getOutHour(),loginfo.getOutMinute());
    				if(loginfoOutTime>= newStartTime && loginfoOutTime<= newEndTime){	
    					System.out.println(loginfo.toString());
    					flag = false;
    					return OpResult.createOpResult(ReturnCodeEnum.I20);
    				}
    			}
    			if(flag)
    				return OpResult.createOpResult(ReturnCodeEnum.E21);
    		}else{
       			boolean flag = true;
    			for(ChargeLogInfo loginfo:list){
    				int loginfoCardNo = loginfo.getCardNo();
    				int loginfoOutTime = normalizationTime(loginfo.getOutHour(),loginfo.getOutMinute());
    				if(loginfoCardNo == cardNo && loginfoOutTime>= newStartTime && loginfoOutTime<= newEndTime){	
    					System.out.println(loginfo.toString());
    					flag = false;
    					return OpResult.createOpResult(ReturnCodeEnum.I20);
    				}
    			}
    			if(flag)
    				return OpResult.createOpResult(ReturnCodeEnum.E21);
    		}
    	}
   
        return OpResult.createOpResult(ReturnCodeEnum.E01);
    }


    /**
     * ������Ҫʵ�ֵĽӿ�
     * c����ӿڣ�ʵ������۷ѵĹ���
     *
     * @param  chargeCmd ChargeCmdInfo:��ѯ�������Ϣ��
     * @param  distances DistanceInfo []:����վ�����̱�
     *                                 (ϵͳ�����ã��������ݴ�������Ϣ����������㷨��ȡ��ͬվ֮��ľ��룩
     *                                 ����վ�����̱���Ϣ������
     *                                 ("S6", "S5", 6)
     *                                 ("S3", "S4", 3)
     *                                 ("S1", "S2", 4)
     *                                 ("S4", "S5", 3)
     *                                 ("S3", "S2", 1)
     *
     * @return OpResult��������
     */
    public OpResult opCharge(ChargeCmdInfo ci, DistanceInfo[] distances) {

        //�۷ѳɹ�,�뿼������OpResult��Ľӿ�OpResult    createOpResult(ReturnCodeEnum returnCode, int cardNo, int moneyLeft);
    	
    	//ʱ���ʽ����
    	if(ci.getInHour()<0||ci.getInHour()>23||ci.getOutHour()<0||ci.getOutHour()>23||
    		ci.getInMinute()<0||ci.getInMinute()>59||ci.getOutMinute()<0||ci.getOutMinute()>59)
    	{
			// TODO: handle exception
			this.getLogInfo(ci, 0, false);
			return OpResult.createOpResult(ReturnCodeEnum.E01);
		
    	}
    	
    	if (( ci.getInHour()>ci.getOutHour() ) || (ci.getInHour()==ci.getOutHour() && ci.getInMinute()>ci.getOutMinute())){
    		
    	//ʱ���ϵ����
    		this.getLogInfo(ci, 0, false);
    		return OpResult.createOpResult(ReturnCodeEnum.E02);
    		
    	}
    	
		String[] strArray ={"S1","S2","S3","S4","S5","S6"};
    	byte bResult = 0;
		for (String temp : strArray) {
			if (temp.equals(ci.getInStation()) || temp.equals(ci.getOutStation())) {
				bResult += 1;
				if(temp.equals(ci.getInStation()) && temp.equals(ci.getOutStation())){
					bResult += 1;
					break;
				}
			}
		}
		if(bResult!=2){ //վ���쳣
			this.getLogInfo(ci, 0, false);
			return OpResult.createOpResult(ReturnCodeEnum.I10,ci.getCardNo(),ci.getCardMoney());
		}
    	
		//��������֮��ľ���
		
		int dis[]={4,1,3,2,6},distance=0;
		int in1=Integer.parseInt(ci.getInStation().substring(1));
		int in2=Integer.parseInt(ci.getOutStation().substring(1));
		if(in1>in2){
			int int_temp=in1;
			in1=in2;
			in2=int_temp;
		}
		for(int i=in1-1;i<in2-1;i++){
			distance=distance+dis[i];
		}
    	
		
		CardTypeEnum A=CardTypeEnum.A;
		CardTypeEnum B=CardTypeEnum.B;
//		CardTypeEnum C=CardTypeEnum.C;
		
		//����վΪͬһ�ص�
		if(in1 == in2){
			int time_interval=(ci.getOutHour()-ci.getInHour())*60+ci.getOutMinute()-ci.getInMinute();
			
			if(time_interval<=30){
				
				if(ci.getCardType()==A){ 
					//1.ͬһ�ص㣬ʱ����<=30min������Ʊ���۷ѳɹ�
					this.getLogInfo(ci, ci.getCardMoney(), true);
					return OpResult.createOpResult(ReturnCodeEnum.I11,ci.getCardNo(),0);
				}
				//2.ͬһ�ص㣬ʱ����<=30min���ǵ���Ʊ���۷�Ϊ0
				this.getLogInfo(ci, 0, true);
				if(ci.getCardMoney()>=20){
					return OpResult.createOpResult(ReturnCodeEnum.I11,ci.getCardNo(),ci.getCardMoney());
				}
				return OpResult.createOpResult(ReturnCodeEnum.I12,ci.getCardNo(),ci.getCardMoney());
				
			}
			else{
				
				if(ci.getCardType()==A){ //3.ͬһ�ص㣬ʱ����>30min������Ʊ���۷ѳɹ�/ʧ��
					
					if(ci.getCardMoney()>=3){
						this.getLogInfo(ci, ci.getCardMoney(), true);
						return OpResult.createOpResult(ReturnCodeEnum.I11,ci.getCardNo(),0);
						
					}
					else{
						this.getLogInfo(ci, 0, false);
						return OpResult.createOpResult(ReturnCodeEnum.I13,ci.getCardNo(),ci.getCardMoney());
					}
				}
				else{//4.ͬһ�ص㣬ʱ����>30min���ǵ���Ʊ���۷ѳɹ�/��ʾ����/ʧ��
					int remain=ci.getCardMoney()-3;
					//this.getLogInfo(ci, 0, false);
					return return_result(remain,3,ci);
					
				}

				
			}
			
		}
		//����վΪ��ͬ�ĵص�
		else{
			//�������Ʊ��
			int base_price;
			if(distance<=3) base_price=2;
			else if(distance<=5) base_price=3;
			else if(distance<=10) base_price=4;
			else	base_price=5;
			
			if(ci.getCardType()==A){ //5.��ͬ�ص㣬����Ʊ���۷ѳɹ�/ʧ��
				
				if(ci.getCardMoney()>=base_price)
				{
					this.getLogInfo(ci, ci.getCardMoney(), true);
					return OpResult.createOpResult(ReturnCodeEnum.I11,ci.getCardNo(),0);
				}else
				{
					this.getLogInfo(ci, 0, false);
					return OpResult.createOpResult(ReturnCodeEnum.I13,ci.getCardNo(),ci.getCardMoney());
				}
			}
			else{ 
				if((ci.getInHour()>=7 && ci.getInHour()<9) || (ci.getInHour()*60+ci.getInMinute()>=16*60+30 && ci.getInHour()*60+ci.getInMinute()<18*60+30)){
					//6.��ͬ�ص㣬�ǵ���Ʊ����վʱ��Ϊ[7:00,9:00����[16:30,18:30)ʱ�����κ��Ż�
					int remain=ci.getCardMoney()-base_price;
					return return_result(remain,base_price,ci);
				}
				else if((ci.getInHour()>=10 && ci.getInHour()<11) || (ci.getInHour()>=15 && ci.getInHour()<16)){
					//7.��ͬ�ص㣬�ǵ���Ʊ����վʱ��Ϊ[10:00,11:00����[15:00,16:00��ʱ��5���Ż�
					int discount_price=(int) Math.floor(0.5*base_price);
					int remain=ci.getCardMoney()-discount_price;
					return return_result(remain,discount_price,ci);
					
				}
				else{
					if(ci.getCardType()==B){
						//8.��ͬ�ص㣬���꿨������ʱ�䣬9���Ż�
						int discount_price=(int) Math.floor(0.9*base_price);
						int remain=ci.getCardMoney()-discount_price;
						return return_result(remain,discount_price,ci);
					}
					else{
						//9.��ͬ�ص㣬��ͨ��������ʱ�䣬���Ż�
						int remain=ci.getCardMoney()-base_price;
						return return_result(remain,base_price,ci);
					}
					
				}
				
			}
			
			
		}
    }
    
  //������ͬ�����ز�ͬ�Ľ��
    public OpResult return_result(int remain,int chargeMoney,ChargeCmdInfo ci) {
    	
		if(remain>=20){
			this.getLogInfo(ci, chargeMoney, true);
			return OpResult.createOpResult(ReturnCodeEnum.I11,ci.getCardNo(),remain);
		}
		else if(remain>=0){
			this.getLogInfo(ci, chargeMoney, true);
			return OpResult.createOpResult(ReturnCodeEnum.I12,ci.getCardNo(),remain);
		}else{
			this.getLogInfo(ci, 0, false);
		}return OpResult.createOpResult(ReturnCodeEnum.I13,ci.getCardNo(),ci.getCardMoney());
    }
    
    /**
     * 
     * @param ci cmd��
     * @param chargeMoney �շ�Ǯ
     * @param isCharged �Ƿ��շѳɹ�
     */
    public void getLogInfo(ChargeCmdInfo ci,int chargeMoney,boolean isCharged){
    	
    	ChargeLogInfo cLogInfo = 
    			new ChargeLogInfo(ci.getCardNo(), ci.getInHour(),
    					ci.getInMinute(), ci.getInStation(), ci.getOutHour(),
    					ci.getOutMinute(), ci.getOutStation(), chargeMoney, isCharged);
    	list.add(cLogInfo);
    	
    	if(list.size()>10){
    		//List����11��
    		list.remove(0);
    	}
    	//return cLogInfo;
    	
    	
    }
    /**
     * �ж�ʱ������Ƿ����쳣
     *
     * @param startHour int����ѯ��ʼʱ���Сʱ����
     * @param startMin int ����ѯ��ʼʱ��ķ��Ӳ���
     * @param endHour int  ����ѯ����ʱ���Сʱ����
     * @param endMin int   ����ѯ����ʱ��ķ��Ӳ���
     *
     * @return true or false;
     */
    public boolean timeJudge(int startHour, int startMin,
            int endHour, int endMin){
    	
    	int newStartTime = Integer.parseInt("startHour"+"startMin");
		int newEndTime = Integer.parseInt("endHour"+"endMin");
	
    	if(newStartTime>newEndTime || startHour<0 || startHour>23 || startMin<0 || startMin>59|| endHour<0 || endHour>23 || endMin<0 || endMin>59)
    		return true;
    	else 
    		return false;
    	
    }
    /**
     * �ж�ʱ������Ƿ����쳣
     *
     * @param startHour int����ѯ��ʼʱ���Сʱ����
     * @param startMin int ����ѯ��ʼʱ��ķ��Ӳ���
     * @param endHour int  ����ѯ����ʱ���Сʱ����
     * @param endMin int   ����ѯ����ʱ��ķ��Ӳ���
     *
     * @return true or false;
     */ 
    public int normalizationTime(int hour,int minute){
    	return hour*60+minute;
    }
    
}
