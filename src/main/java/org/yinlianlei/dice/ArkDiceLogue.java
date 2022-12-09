package org.yinlianlei.dice;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import java.io.BufferedReader;

public class ArkDiceLogue{
    String[] dialogues;
    ArkDiceLogue(){}
    //取得csv文件中的对话设定
    private String[] getInitString(){//获取初始化csv文件//如果没有就直接返回null
        try{
            BufferedReader br = Files.newBufferedReader(Path.of("./data/monologue.csv"));
            String line;
            ArrayList<String> stringList = new ArrayList<String>();
            if((line = br.readLine()) == null){//将文件header消除
                return null;
            }
            while((line = br.readLine()) != null){
                String[] temp = line.split(",");
                stringList.add(temp[1]);
            }
            String[] re = stringList.toArray(new String[0]);
            return re;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
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
