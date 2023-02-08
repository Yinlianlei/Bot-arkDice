package org.yinlianlei.dice;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.*;

import net.mamoe.mirai.event.events.GroupMessageEvent;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.Group;

public class ArkDiceLogue{
    //用于存储收录的卡
    //QQ-PcInfo
    static HashMap<String, ArrayList<PcInfo>> pcList = new HashMap<String, ArrayList<PcInfo>>();
    static HashMap<String, Integer> pcTag = new HashMap<String, Integer>();
    String[] dialogues;
    final static ArkDiceRoll dice = new ArkDiceRoll(pcList,pcTag);
    final static String[] rollResultList = {"大成功","极难成功","困难成功","较难成功","成功","失败","大失败"};

    ArkDiceLogue(){
        dialogues = getInitString();
        File f = new File("data/pc");
        try {
            if(!f.exists()){
                f.createNewFile();
            }
            if(f.isDirectory()){
                File result[] = f.listFiles();
                for(File i : result){
                    readPcInfo(i.getName().split("\\.")[0]);
                }
            }
        } catch (Exception e) {
            //TODO: handle exception
            e.printStackTrace();
        }
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

    //对json文件进行写入
    public String writePcInfo(String json, GroupMessageEvent g){
        String fileName = "data/pc/"+String.valueOf(g.getSender().getId())+".json";
        Path p = Path.of(fileName);
        try {
            File f = new File(fileName);
            if(!f.exists()){
                f.createNewFile();
            }

            Charset cs = Charset.forName("UTF-8");
            BufferedWriter bw = Files.newBufferedWriter(p, cs, StandardOpenOption.TRUNCATE_EXISTING);

            bw.write(json);
            bw.close();
            return json;
        } catch (Exception e) {
            //TODO: handle exception
            System.err.println(p.toAbsolutePath());
            System.err.println(e.toString());
        }
        return json;
    }

    //对json文件进行读取
    public String readPcInfo(String qq){
        Path p = Path.of("data/pc/"+qq+".json");
        try{
            Charset cs = Charset.forName("UTF-8");
            BufferedReader br = Files.newBufferedReader(p, cs);
            //读取数据//由于是一行插入，所以可以直接读取
            String line = br.readLine();
            br.close();

            if(line == null){//判断是否存在数据    
                return null;
            }

            JSONObject json = JSON.parseObject(line);
            //System.out.println(json.toJSONString());
            ArrayList<PcInfo> itemValue = new ArrayList<PcInfo>();

            ArrayList<String> items = new ArrayList<String>();
            ArrayList<Integer> values = new ArrayList<Integer>();

            int tagValue = (int)json.get("tag");
            //System.out.println(tagValue);
            pcTag.put(qq, tagValue);
            
            JSONArray jsonArray = (JSONArray)json.get("pcItem");
            JSONArray jsonNick = (JSONArray)json.get("pcNick");

            for(int i = 0; i < jsonArray.size(); i++){
                JSONObject jsonObject = (JSONObject)jsonArray.get(i);
                for(Map.Entry<String, Object> entry : jsonObject.entrySet()){
                    items.add((String)entry.getKey());// 获得key
                    values.add(Integer.valueOf((String)entry.getValue()));// 获得value
                }

                PcInfo tempPcInfo = new PcInfo(qq,items,values, String.valueOf(jsonNick.get(i)));
                itemValue.add(tempPcInfo);
            }

            pcList.put(qq, itemValue);

            return "";
        }catch(Exception e){
            System.out.println(p.toAbsolutePath());
            e.printStackTrace();
        }
        return null;
    }

    //将属性转化为JSON能够识别的string格式
    public String map2json(GroupMessageEvent g){
        try{
            String re = "{\"tag\":"+ pcTag.get(String.valueOf(g.getSender().getId())) +",\"pcItem\":[";

            ArrayList<PcInfo> tempPcInfoList = pcList.get(String.valueOf(g.getSender().getId()));
            ArrayList<String> tempItemString = new ArrayList<String>();
            ArrayList<String> tempItemList = new ArrayList<String>();
            ArrayList<String> tempNickList = new ArrayList<String>();

            for(PcInfo i : tempPcInfoList){
                tempNickList.add("\""+i.pcNick+"\"");
                for(String j : i.itemList.keySet()){
                    tempItemString.add("\""+j+"\":"+"\"" + i.itemList.get(j) + "\"");
                }
                tempItemList.add("{" +String.join(",", tempItemString) + "}");
                tempItemString.clear();
            }

            re += String.join(",", tempItemList);
            re += "],";
            re += "\"pcNick\":[" + String.join(",", tempNickList) + "]}";

            return re;
        }catch (Exception e) {
            //TODO: handle exception
            System.err.println(e.toString());   
            return null;
        }
    }

    //录卡用
    public String extraAllItem(String msg){
        String re = "";

        //将数据全部抓取
        ArrayList<String> reList = new ArrayList<String>();//数据列表

        //获取
        Pattern pattern = Pattern.compile("\\d+.\\d|\\d*\\d");//正则表达式
        Matcher m = pattern.matcher(msg);
        while(m.find()){
            reList.add(m.group());
        }

        msg = msg.replaceAll("\\d+.\\d|\\d*\\d", "\\,");
        //System.out.println(msg);
        String[] temp = msg.split(",");
        ArrayList<String> itemList = new ArrayList<String>();
        for(int i = 0;i<temp.length;i++){
            itemList.add(temp[i]+"-"+reList.get(i));
        }
        re = String.join(",", itemList);

        return re;
    }

    //+-修改用
    public String updateItem(String msg, GroupMessageEvent g){
        String re = "";

        String qq = String.valueOf(g.getSender().getId());

        String[] temp = msg.split(",");

        Pattern pattern = Pattern.compile("\\d+.\\d|\\d*\\d");//正则表达式
        ArrayList<String> reList = new ArrayList<String>();

        for(String i : temp){
            String value = "",item = "";
            if(i.contains("+")){
                i = i.replaceFirst("\\+|\\-", "");

                Matcher m = pattern.matcher(i);
                if(m.find()){
                    value = m.group();
                }
                item = i.replaceFirst("\\d+.\\d|\\d*\\d", "");

                int tempValue = pcList.get(qq).get(0).itemList.get(item);
                tempValue += Integer.valueOf(value);

                System.out.println(tempValue);

                value = String.valueOf(tempValue);
            }else{
                i = i.replaceAll("\\+|\\-", "");

                Matcher m = pattern.matcher(i);
                if(m.find()){
                    value = m.group();
                }
                item = i.replaceFirst("\\d+.\\d|\\d*\\d", "");

                int tempValue = pcList.get(qq).get(pcTag.get(qq)).itemList.get(item);
                tempValue -= Integer.valueOf(value);
                value = String.valueOf(tempValue);
            }

            reList.add(item + "-" + value);
        }

        re = String.join(",", reList);

        return re;
    }

    //进行分割
    public String msgSub(String msg, GroupMessageEvent g){
        String re = null;
        
        if(msg.charAt(0) == 'r'){
            if(msg.contains("rka")){
                msg = msg.replaceFirst("rka", "");

                re = "rka" + "|" + dice.check(msg, 1, g);
            }else if(msg.contains("rk")){
                msg = msg.replaceFirst("rk", "");

                re = "rk" + "|" + dice.check(msg, 0, g);
            }else if(msg.contains("ra")){
                msg = msg.replaceFirst("ra", "");

                re = "ra" + "|" + dice.check(msg, 0, g);
            }else if(msg.contains("rh")){
                msg = msg.replaceFirst("rh", "");
                
                re = "rh|" + dice.roll();
            }else if(msg.contains("r")){
                msg = msg.replaceFirst("r", "");

                re = "r" + "|" + dice.rollndn(msg);
            }
        }else if(msg.charAt(0) == 's'){
            if(msg.contains("st")){
                //卡录入√
                //属性修改√
                msg = msg.replaceFirst("st", "");

                if(msg.contains("+") || msg.contains("-")){
                    re = "st" + "|"+ updateItem(msg, g);
                }else{
                    re = "st" + "|"+ extraAllItem(msg);
                }
            }
        }else if(msg.charAt(0) == 't'){
            if(msg.contains("tag")){
                msg = msg.substring(3);
                int tag = Integer.valueOf(msg);
                if(tag + 1 > pcList.get(String.valueOf(g.getSender().getId())).size()){
                    re = "tag|tag_out_of_range"; 
                }else{
                    pcTag.put(String.valueOf(g.getSender().getId()), tag);
                    re = "tag|"; 
                }
            }
        }else if(msg.contains("pc")){
            msg = msg.substring(2);
            if(msg.contains("list")){
                ArrayList<PcInfo> tempPcInfo = pcList.get(String.valueOf(g.getSender().getId()));
                if(tempPcInfo != null){
                    ArrayList<String> temp = new ArrayList<String>();
                    for(int i = 0; i < tempPcInfo.size();i++){
                        temp.add("[" + String.valueOf(i) + "]:" + tempPcInfo.get(i).pcNick);
                    }
                    re = "pc|list|" + String.join(",",temp);
                    temp.clear();
                }
            }else if(msg.contains("show")){

            }
        }else if(msg.contains("nn")){
            msg = msg.replace("nn","");
            String qq = String.valueOf(g.getSender().getId());
            PcInfo tempPcInfo = pcList.get(qq).get(pcTag.get(qq));

            String newNick = msg;
            String oldNick = tempPcInfo.pcNick;

            pcList.get(qq).get(pcTag.get(qq)).pcNick = newNick;

            re = "nn|已将:" + oldNick + " 改名为:" + newNick;
        }

        return re;
    }

    //进行分割后的处理
    public String msgReply(String msg, GroupMessageEvent g){
        String re = "";
        //进行处理
        //此为指令
        if(msg.charAt(0) == '.' || msg.charAt(0) == '。'){
            msg = msg.substring(1);
            msg = msg.replaceAll("\\ ", "");
            re = msgSub(msg, g);

            if(re == null || !re.contains("|")){
                return "@sender" + dialogues[15];
            }
            
            String[] temp = re.split("\\|");

            if(temp[1].compareTo("null")==0){
                return "@sender" + dialogues[15];
            }else{
                //项目_项目值_roll_攻击_结果
                //结果处理，进行联合判定的分割处理
                ArrayList<String> r = new ArrayList<String>();
                int dialoguesId;
                if(temp[0].compareTo("rh") == 0){//rh
                    dialoguesId = 10;
                    return re + dialogues[dialoguesId] + "|D100=" + String.valueOf(temp[1]);
                }else if(temp[0].compareTo("r") == 0){//r
                    dialoguesId = 11;
                    return "@sender" + dialogues[dialoguesId] + ":" + temp[1];
                }else if(temp[0].compareTo("st") == 0){//st
                    String qq = String.valueOf(g.getSender().getId());
                    ArrayList<String> itemList = new ArrayList<String>();
                    ArrayList<Integer> valueList = new ArrayList<Integer>();

                    for(String i : temp[1].split(",")){
                        String[] temp2 = i.split("-");
                        itemList.add(temp2[0]);
                        if(temp2[1].contains(".")){
                            valueList.add(Integer.valueOf(temp2[1].split("\\.")[0]));
                        }else{
                            valueList.add(Integer.valueOf(temp2[1]));
                        }
                    }

                    if(temp[1].length() >= 100){
                        PcInfo tempPcInfo = new PcInfo();

                        tempPcInfo.initItem(itemList, valueList);
                        tempPcInfo.qq = qq;
                        tempPcInfo.pcNick = "角色卡";

                        if(pcList.get(qq) == null){
                            ArrayList<PcInfo> tempPcInfoList = new ArrayList<PcInfo>();
                            tempPcInfoList.add(tempPcInfo);
                            pcList.put(qq, tempPcInfoList);
                        }else{
                            ArrayList<PcInfo> tempPcInfoList = pcList.get(qq);
                            tempPcInfoList.add(tempPcInfo);
                            pcList.put(qq, tempPcInfoList);
                        }

                        pcTag.put(qq, pcList.get(qq).size() -1);

                        dialoguesId = 16;
                    }else if(pcList.get(qq) != null){
                        PcInfo targetPc = pcList.get(qq).get(pcTag.get(qq));
                        for(int i = 0 ;i<itemList.size(); i++){
                            targetPc.updateItem(itemList.get(i), valueList.get(i));
                        }
                        dialoguesId = 17;
                    }else{
                        dialoguesId = 18;
                    }

                    if(dialoguesId == 18)
                        return "@sender " + dialogues[dialoguesId] + "\n";
                    
                    String writeText = map2json(g);
                    if(writeText != null){
                        writePcInfo(writeText, g);
                    }
                    if(dialoguesId == 17){
                        ArrayList<String> tempUpdateList = new ArrayList<String>();
                        for(int i = 0 ;i<itemList.size(); i++){
                            tempUpdateList.add(itemList.get(i)+":"+valueList.get(i));
                        }
                        return "@sender " + dialogues[dialoguesId] + "--" + String.join(",",tempUpdateList) + "\n";
                    }
                    
                    return "@sender " + dialogues[dialoguesId] + "\n";
                }else if(temp[0].compareTo("tag") == 0){//tag
                    if(temp[1].compareTo("tag_out_of_range") == 0){
                        dialoguesId = 20;
                    }else{
                        dialoguesId = 19;
                    }
                    return re = "@sender " + dialogues[dialoguesId] + "\n";
                }else if(temp[0].compareTo("pc") == 0){//pc
                    if(temp[1].compareTo("list") == 0){
                        dialoguesId = 22;
                        return re = "@sender " + dialogues[dialoguesId] + "\n" + temp[2] + "\n";
                    }else if(temp[1].compareTo("nn") == 0){
                        dialoguesId = 0;
                    }
                }else if(temp[0].compareTo("nn") == 0){//nn
                    dialoguesId = 23;

                    String writeText = map2json(g);
                    if(writeText != null){
                        writePcInfo(writeText, g);
                    }

                    return re = "@sender " + dialogues[dialoguesId] + "\n" + temp[1];
                }else if(temp[0].compareTo("template1") == 0){//template1
                
                }else{//rk,rka,ra
                    if(temp[0].compareTo("rk") != 0 && temp[0].compareTo("rka") != 0 && temp[0].compareTo("ra") != 0){
                        return "@sender" + dialogues[15];
                    }
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

                    re = "@sender " + dialogues[dialoguesId] + "\n";

                    r.clear();
                    for(String i : reFinal){
                        String[] t = i.split("_");
                        r.add(t[0] + ":" + t[1] + "/" + t[2] + "--" + dialogues[Integer.valueOf(t[4])] + (temp[0].compareTo("rka") == 0?    "   (2d10="+t[3]+")":""));
                    }
                }

                re += String.join("\n", r);
            }
        }
        return re;
    }
}

class PcInfo{//角色卡信息
    String qq;//qq号
    String pcNick;//角色昵称
    HashMap<String, Integer> itemList;//项目值

    PcInfo(){
        qq = "";
        pcNick = "角色卡";
        itemList = new HashMap<String, Integer>();
    }

    PcInfo(GroupMessageEvent g){
        qq = String.valueOf(g.getSender().getId());
        pcNick = "角色卡";
        itemList = new HashMap<String, Integer>();
    }

    PcInfo(GroupMessageEvent g, ArrayList<String> itemList, ArrayList<Integer> valueList){
        qq = String.valueOf(g.getSender().getId());
        pcNick = "角色卡";
        initItem(itemList, valueList);
    }


    PcInfo(GroupMessageEvent g, String[] items, Integer[] values){
        qq = String.valueOf(g.getSender().getId());
        pcNick = "角色卡";
        
        ArrayList<String> itemList = new ArrayList<String>();

        for(String i : items){
            itemList.add(i);
        }

        ArrayList<Integer> valueList = new ArrayList<Integer>();
        for(Integer i : values){
            valueList.add(i);
        }

        initItem(itemList, valueList);
    }

    PcInfo(String g, ArrayList<String> itemList, ArrayList<Integer> valueList, String nick){
        qq = g;
        pcNick = nick;
        initItem(itemList, valueList);
    }

    PcInfo(GroupMessageEvent g, ArrayList<String> itemList, ArrayList<Integer> valueList, String nick){
        qq = String.valueOf(g.getSender().getId());
        pcNick = nick;
        initItem(itemList, valueList);
    }

    //进行项目值初始化
    public Integer initItem(ArrayList<String> items, ArrayList<Integer> values){
        try {
            HashMap<String, Integer> re = new HashMap<String, Integer>();
            for(int i=0;i<items.size();i++){
                re.put(items.get(i), values.get(i));
            }
            this.itemList = re;
            return 0;
        } catch (Exception e) {
            System.out.println(e.toString());
            return e.hashCode();
        }
    }

    //取得项目值
    public Integer getItem(String item){
        try {
            Integer re = null;

            HashMap<String, Integer> temp = this.itemList;

            re = temp.get(item);
            if(re == null){
                return -1;
            }else{
                return re.intValue();
            }
        } catch (Exception e) {
            //TODO: handle exception
            System.out.println(e.toString());
            return -1;
        }
    }

    //进行项目更新
    public Integer updateItem(String item, Integer value){
        try {
            HashMap<String, Integer> temp = this.itemList;

            //确定是否存在这个属性值
            if(temp.get(item) == null){
                //如果没有则增加
                temp.put(item, value);
            }else{
                //如果有的话则修改
                temp.replace(item, value);
                this.itemList = temp;
            }
            return 0;
        } catch (Exception e) {
            //TODO: handle exception
            System.out.println(e.toString());
            return e.hashCode();
        }
    }

    //进行项目删除
    public Integer removeItem(String item){
        try {
            HashMap<String, Integer> temp = this.itemList;

            //确定是否存在这个属性值
            if(temp.get(item) == null){
                return -1;
            }
            temp.remove(item);
            this.itemList = temp;

            return 0;
        } catch (Exception e) {
            //TODO: handle exception
            System.out.println(e.toString());
            return e.hashCode();
        }
    }

}