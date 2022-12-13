package org.yinlianlei.dice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import java.io.BufferedReader;

public class ArkDiceLogue{
    String[] dialogues;
    final static ArkDiceRoll dice = new ArkDiceRoll();
    final static String[] rollResultList = {"大成功","极难成功","困难成功","较难成功","成功","失败","大失败"};

    ArkDiceLogue(){
        dialogues = getInitString();
    }
    //取得csv文件中的对话设定
    private String[] getInitString(){//获取初始化csv文件//如果没有就直接返回null
        Path p = Path.of("data/monologue.csv");
        try{
            BufferedReader br = Files.newBufferedReader(p);
            String line;
            ArrayList<String> stringList = new ArrayList<String>();
            if((line = br.readLine()) == null){//将文件header消除
                br.close();
                return null;
            }
            while((line = br.readLine()) != null){
                String[] temp = line.split(",");
                stringList.add(temp[1]);
            }
            String[] re = stringList.toArray(new String[0]);
            br.close();
            return re;
        }catch(Exception e){
            System.out.println(p.toAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    //用于进行判断
    public String replayMain(String input,String SenderQQ){
        String re = null;
        //对输入的数据进行反应
        re = replayFunc(input, SenderQQ);
        //System.out.println(re);

        String[] reList = re.split("\\|");//对结果进行处理

        switch(reList[0]){
            case "rk":re=replayRk(reList[1],0);break;
            case "rka":re=replayRk(reList[1],1);break;
            case "ra":re=replayRk(reList[1],0);break;
            case "rh":re="rh|"++replayRh(reList[1]);break;
            case "r":re=replayR(reList[1]);break;
            default:
                re = "ERROR!出现问题了，请联系骰主进行解决！";
        }

        return re;
    }

    //进行回复的设定
    private String replayFunc(String msg,String SenderQQ){
        ArrayList<String> returnData = new ArrayList<String>();//获得roll的结果
        String ReString = null;//进行返回用的字符串
        if(msg.charAt(0) == '.' || msg.charAt(0) == '。'){
            String cmd = msg.substring(1);//去掉.和。
            if(cmd.charAt(0) == 'r'){
                if(cmd.contains("rk")){//判断是不是要进行判定
                    int tf = cmd.charAt(2) == 'a'?1:0;//攻击是否
                    returnData = dice.rollDiaglue(cmd.substring(tf+2), tf, SenderQQ);
                    
                    //相应处理
                    ReString = tf==1?"rka":"rk";
                    ReString += "|"+String.join(",", returnData);//先这样
                }else if(cmd.contains("ra")){//ra技能判定，讲实话ark中好像就没见过
                    returnData = dice.rollDiaglue(cmd.substring(2), 0, SenderQQ);
                    ReString = "ra|"+ String.join(",", returnData);
                }else if(cmd.contains("rh")){//暗骰
                    ReString = "rh|"+dice.rollndn("1d100");
                }else{
                    ReString = "r|" + dice.rollndn(cmd.substring(1));
                }
            }
        }else if(msg.contains("@2683380854")){ //at bot的信息

            //process string
        }

        //返回格式：判断类型|第一个检定项目-roll/标准值/随即战斗数-结果,第二个....
        return ReString;
    }

    //roll结果处理
    //输入为:鉴定项目-roll/标准值-战斗随机值-结果代数
    private String rollResult(String input){
        //System.out.println(input);
        String[] result = input.split("-");
        String re = result[0];
        //进行处理
        int resultTemp = Integer.valueOf(result[3]);
        re += "D100="+result[1]+"的结果为: " + rollResultList[resultTemp]+", "+dialogues[resultTemp];
        return re;
    }
    
    //rk及rka的回复处理
    private String replayRk(String input,int atf){//第二个参数表达是rka吗
        String re = null;
        String[] inputList = input.split(",");//进行二次切分

        if(inputList.length == 1){//如果不是联合检定则直接处理
            re = "@sender"+dialogues[7]+"\n";//进行技能判定
            re += rollResult(inputList[0]);
            re += atf == 1?"(2D10="+inputList[0].split("-")[2]+")":"";//如果是rka则加上随机值
        }else{
            String temp = "";
            re = "@sender"+dialogues[9]+"\n";//进行技能判定
            for(String i : inputList){
                re += rollResult(i);
                temp += i.split("-")[3];
                re += atf == 1?"(2D10="+i.split("-")[2]+")\n":'\n';//如果是rka则加上随机值
            }

            if(temp.contains("5") || temp.contains("6")){//结果代数如果有5，6就失败
                re += dialogues[11];//联合检定失败
            }else{ 
                re += dialogues[10];//联合检定成功
            }
        }

        return re;
    }

    //rh的回复处理
    private String replayRh(String input){
        String re = null;
        re = dialogues[13]+"|D100=";
        re += input.split("=")[1];
        return re;
    }

    //r的回复处理
    private String replayR(String input){
        String re = null;
        re = dialogues[12]+"D100="+input.split("=")[1];
        return re;
    }

}

//对话标号
    /*
    0-大成功
    1-极难成功
    2-困难成功
    3-较难成功
    4-一般成功
    5-失败
    6-大失败
    7-bot开启
    8-bot关闭
    9-bot到来
    10-bot离开
    11-reply开启
    12-replay关闭
    13-send发送消息
    14-ob join加入
    15-ob exit推出
    16-ob list列表
    17-ob clr清除所有ob
    18-pc tag绑定
    19-pc show显示绑定属性
    20-pc show未绑定角色
    21-pc nn修改绑定角色昵称
    22-pc cpy复制指定pc
    23-pc list列出所有pl的所有pc
    24-pc del删除pc
    25-pc stat查看pc掷骰记录
    26-pc clr消除pc掷骰记录
    27-st 设定pc属性
    28-st 修改属性
    29-st show展示某项属性
    30-st show展示所有属性
    31-rk 进行技能判断
    32-rka 进行行动判断
    33-r roll点
    34-rh 暗骰
    35-welcome 欢迎词
    36-cheat 赌博作弊//xu~
*/
