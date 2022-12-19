package org.yinlianlei.dice;

import net.mamoe.mirai.event.events.AbstractMessageEvent;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

//用于进行roll点
class ArkDiceRoll{
    Pattern patternNum = Pattern.compile("\\d+");
    final static int[] difficultLevel = {0,10,25,40};//困难度
    final static String[] difficultString = {"一般","较难","困难","极难"};//困难程度
    //负数直接大失败,0-大成功，1-极难成功，2-困难成功，3-较难成功，4-成功，5-失败，6-大失败
    final static int[] result = {0,1,2,3,4,5,6};

    public class DiceRollColum{//用于记录log用
        String qq;//用于记录QQ。同时方便查询pl表
        String colum;//类型
        int standValue;//标准值
        int addValue = 0;//修正值
        int difficultValue = 0;//困难度0-一般，1-较难，2-困难，3-较难
        int roll;//roll到的点数
        int attack = 0;//战斗随机数
        int rollResult = 0;//roll点数的结局
    }

    ArkDiceRoll(){}

    public String rollndn(String input){//输入指令：.r n#ndn
        //此为默认值
        int times = 1;//进行几次投掷
        int num = 1;//一次投几个骰子
        int dice = 100;//一个骰子有多少面

        input = input.replaceAll("\\ ", "");//消除空格
        input = input.split("\\,|\\，")[0];//将后续的描述清除掉

        Matcher m = patternNum.matcher(input);//此处为获取数值

        //一下三个为获取投骰子的数值赋值
        if(input.contains("#") && m.find()){
            times = Integer.valueOf(m.group());
            input = input.split("#")[1];
        }
        
        if(input.contains("d")){
            String[] temp = input.split("d");
            if(temp.length != 0){
                if(temp[0].compareTo("") != 0 && m.find()){
                    num = Integer.valueOf(m.group());
                }
            
                if(temp[1].compareTo("") != 0 && m.find()){
                    dice = Integer.valueOf(m.group());
                }
            }
        }else{
            m.find();
            dice = Integer.valueOf(m.group());
        }

        //System.out.println(times+" "+num+" "+dice);

        //设定随机值
        Random r = new Random();
        String re = "";//初始化返回值
        ArrayList<String> reList = new ArrayList<String>();
        //int total = 0;//总数值
        for(int i = 0;i<times;i++){
            String tempStr = "";
            int sum = 0;
            for(int j = 0;j<num;j++){
                int tempNum = (r.nextInt(dice)+1);
                sum += tempNum;
                tempStr += "+"+String.valueOf(tempNum);
            }
            //total += sum;
            //System.out.println(tempStr);
            reList.add(tempStr.replaceFirst("\\+", "")+"="+String.valueOf(sum));
        }
        re += String.join("\n", reList);
        reList.clear();
        //System.out.println(re);
        return re;
    }

    public ArrayList<String> rollDiaglue(String cmd,int tf,String SenderQQ){//cmd输入：n# [难度] [项目] [修正值]//tf是否是rka
        ArrayList<String> re;
        int number = 1;
        if(cmd.contains("#")){
            String[] temp = cmd.replaceAll("\\ ", "").split("#");
            number = Integer.valueOf(temp[0]);
            re = new ArrayList<String>();
            for(int i =0;i<number;i++)
                re.addAll(rollFunc(temp[1],tf,SenderQQ));
        }else{
            re = rollFunc(cmd,tf,SenderQQ);
        }

        return re;
    }

    private ArrayList<String> rollFunc(String cmd,int tf,String SenderQQ){//输入指令：难度，标准值，增值
        ArrayList<DiceRollColum> cmdList = difficultFind(cmd,SenderQQ);

        //此处为判断roll难度，并判断是否成功，已经成功难度
        for(DiceRollColum i : cmdList){
            i.roll = roll();
            i.attack = tf == 1?rollka():0;//战斗否
            int tempValue = i.standValue + i.addValue + i.attack - difficultLevel[i.difficultValue];//减少运算量用
            //System.out.println(tempValue);
            if(tempValue < 0 || i.roll > 95){//判断大失败
                i.rollResult = 6;
            }else if(i.roll  > tempValue){//判断是否失败
                i.rollResult = 5;
            }else if(i.roll < 6){//大成功
                i.rollResult = 0;
                //将成功写入数据库//需要判断是否是在水群，不然不加//加个当前群状态就好
            }else if(i.difficultValue != 0 && i.roll < tempValue){
                i.rollResult = 4 - i.difficultValue;
            }else{//非难度要求成功，但是分为四种情况
                for(int j=3;j >= 0;j--){
                    int k = i.standValue + i.addValue - i.roll - difficultLevel[j];
                    if(k >= 0){
                        i.rollResult = 4-j;
                        break;
                    }
                }
                //将成功写入数据库
            }
        }

        ArrayList<String> re = new ArrayList<String>();

        for(int i =0;i<cmdList.size();i++){//对数值进行处理，并使其能够传出去
            String tmp = "";
            DiceRollColum tempOne = cmdList.get(i);
            //判断类型-roll值/标准值-rka随机值-结果代数
            tmp += tempOne.colum+"-"+tempOne.roll+"/"+tempOne.standValue+"-"+tempOne.attack+"-"+tempOne.rollResult;
            re.add(tmp);
        }

        return re;
    }

    private ArrayList<DiceRollColum> difficultFind(String cmd,String SenderQQ){
        ArrayList<DiceRollColum> rollList = new ArrayList<DiceRollColum>();

        String[] cmdList = cmd.split("&");

        for(String i :cmdList){//此处用于处理分割后的语句
            DiceRollColum tempOne = new DiceRollColum();
            i = i.replaceAll("\\ ", "");//消除空格

            String[] in = i.replaceFirst("\\+|-", " ").split(" ");
            
            Matcher m = patternNum.matcher(in[0]);//此处为获取标准值
            if(m.find()){
                tempOne.standValue = Integer.valueOf(m.group());
            }else{
                tempOne.standValue = 95;//查询数据库或是pc数值处理
            }

            if(in.length == 2){
                m = patternNum.matcher(in[1]);//此处为获取修正值
                if(m.find()){ //处理修正值
                    if(i.contains("-"))
                        tempOne.addValue = - Integer.valueOf(m.group());
                    else
                        tempOne.addValue = Integer.valueOf(m.group());
                }
            }

            //System.out.println(tempOne.standValue+" "+tempOne.addValue);

            i = i.replaceAll("\\d", "");//除掉数字//并获得属性
            i = i.replaceAll("\\+|-", "");//除掉+-符号

            tempOne.colum = i;

            int c = 0;
            for(int j=0;j<4;j++){//此处为获取难度
                if(i.contains(difficultString[j])){
                    tempOne.difficultValue = j;
                }else
                    c+=1;
            }
            if(c==4)//如果没有就直接为一般难度
                tempOne.difficultValue = 0;

            rollList.add(tempOne);
            //System.out.println(tempOne.colum+" "+tempOne.difficultValue);
        }

        return rollList;
    }

    private int roll(){//返回值为roll到的点数
        Random r = new Random();
        return r.nextInt(100)+1;
    }

    private int rollka(){
        Random r = new Random();
        return r.nextInt(10)+r.nextInt(10)+2;
    }

}