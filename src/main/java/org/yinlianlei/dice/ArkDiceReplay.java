package org.yinlianlei.dice;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class ArkDiceReplay {
    static ArrayList<String> replayContenet = new ArrayList<String>();
    static ArrayList<String> replayResponse = new ArrayList<String>();

    ArkDiceReplay(){
        getInitString();
    }

    //取得csv文件中的对话设定
    private String getInitString(){//获取初始化csv文件//如果没有就直接返回null
        Path p = Path.of("data/replay.csv");
        String re = null;
        try{
            BufferedReader br = Files.newBufferedReader(p);
            String line;
            if((line = br.readLine()) == null){//将文件header消除
                br.close();
                return null;
            }

            while((line = br.readLine()) != null){
                String[] temp = line.split(",");
                replayContenet.add(temp[1]);
                replayResponse.add(temp[2]);
            }
            br.close();
            return re;
        }catch(Exception e){
            System.out.println(p.toAbsolutePath());
            e.printStackTrace();
            return e.toString();
        }
    }

    //进行关键字回复
    public String keywordReplay(String msg){
        String re = "";
        if(replayContenet.contains(msg)){
            re = replayResponse.get(replayResponse.indexOf(msg));
        }else{
            for(String i : replayContenet){
                if(i.contains(msg)){
                    re = replayResponse.get(replayContenet.indexOf(i));
                }
            }
        }
        return re;
    }
}