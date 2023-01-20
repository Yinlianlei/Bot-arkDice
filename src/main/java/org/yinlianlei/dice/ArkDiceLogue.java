package org.yinlianlei.dice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.io.BufferedReader;

import net.mamoe.mirai.event.events.AbstractMessageEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArkDiceLogue{
    String[] dialogues;
    final static ArkDiceSql sql = new ArkDiceSql();
    final static ArkDiceRoll dice = new ArkDiceRoll(sql);
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

    //进行分割
    public String msgSub(AbstractMessageEvent event,String msg){
        String re = null;
        
        if(msg.charAt(0) == 'r'){
            if(msg.contains("rka")){
                msg = msg.replaceFirst("rka", "");

                re = "rka" + "|" + dice.check(event, msg, 1);
            }else if(msg.contains("rk")){
                msg = msg.replaceFirst("rk", "");

                re = "rk" + "|" + dice.check(event, msg, 0);
            }else if(msg.contains("ra")){
                msg = msg.replaceFirst("ra", "");

                re = "ra" + "|" + dice.check(event, msg, 0);
            }else if(msg.contains("rh")){
                msg = msg.replaceFirst("rh", "");
                
                re = "rh|" + dice.roll();
            }else if(msg.contains("r")){
                msg = msg.replaceFirst("r", "");

                re = "r" + "|" + dice.rollndn(msg);
            }
        }else if(msg.charAt(0) == 's'){
            if(msg.contains("st")){
                //卡录入
                //属性修改
            }
        }
        return re;
    }

        //进行分割后的处理
    public String msgReply(AbstractMessageEvent event,String msg){
        String re = "";
        //进行处理
        //此为指令
        if(msg.charAt(0) == '.' || msg.charAt(0) == '。'){
            msg = msg.substring(1);
            msg = msg.replaceAll("\\ ", "");
            re = msgSub(event, msg);
            

            String[] temp = re.split("\\|");

            if(temp[1].compareTo("null")==0){
                return "@sender" + dialogues[15];
            }else{
                //项目_项目值_roll_攻击_结果
                //结果处理，进行联合判定的分割处理
                ArrayList<String> r = new ArrayList<String>();
                int dialoguesId;
                if(temp[0].compareTo("rh") == 0){
                    dialoguesId = 10;
                    return dialogues[dialoguesId] + "|D100=" + String.valueOf(temp[1]);
                }else if(temp[0].compareTo("r") == 0){
                    dialoguesId = 11;
                    return "@sender" + dialogues[dialoguesId] + ":" + temp[1];
                }else{
                    for(String i : temp[1].split(",")){
                        String[] result = i.split("_");

                        //后续要加上
                        r.add(result[0] + ":" + result[3] + "-" + result[2] + "-" + result[4]);
                    }
                
                    String[] reFinal = temp[1].split(",");

                    if(reFinal.length != 1){
                        dialoguesId = 13; //联合判定
                        for(String i : reFinal){
                            if(Integer.valueOf(i.split("_")[4]) > 4){
                                dialoguesId += 1;
                            }
                        }
                    }else if(temp[0].compareTo("rka") == 0){
                        dialoguesId = 7;
                    }else if(temp[0].compareTo("rk") == 0){
                        dialoguesId = 8;
                    }else if(temp[0].compareTo("ra") == 0){
                        dialoguesId = 9;
                    }else{
                        dialoguesId = 15;
                        return "@sender" + dialogues[dialoguesId];
                    }

                    re = " @sender" + dialogues[dialoguesId] + "\n";

                    r.clear();
                    for(String i : reFinal){
                        String[] t = i.split("_");
                        r.add(t[0] + ":" + t[1] + "/" + t[2] + "--" + dialogues[Integer.valueOf(t[4])] + (temp[0].compareTo("rka") == 0?    "   (2d10="+t[3]+")":""));
                    }
                }
                
                re += String.join("\n", r);
            }
        }else{//此为自动回复

        }

        return re;
    }

}