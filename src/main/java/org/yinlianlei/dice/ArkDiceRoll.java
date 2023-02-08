package org.yinlianlei.dice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mamoe.mirai.event.events.GroupMessageEvent;

public class ArkDiceRoll{
    static HashMap<String, ArrayList<PcInfo>> pcList;
    static HashMap<String, Integer> pcTag;
    Pattern patternNum = Pattern.compile("\\d+");
    final static int[] difficultLevel = {0,10,25,40};//困难度
    final static String[] difficultString = {"一般","较难","困难","极难"};//困难程度
    //负数直接大失败,0-大成功，1-极难成功，2-困难成功，3-较难成功，4-成功，5-失败，6-大失败
    final static String items = "身体素质生理强度反应机动精神意志经验智慧源石技艺个人魅力生命值体型力量/DB物理抗性法术抗性先攻耐力搏斗拳术暗器刀剑杖术枪术软兵重钝器盾术弓弩投掷射击炮术爆破无人机操作兵械操作取悦交涉威吓心理学乐理厨艺艺术写作调香急救开锁载具驾驶骑术锻造智械使用机械能力铳械改造妙手乔装聆听嗅觉感知侦察阅读领航跳跃攀爬潜行追踪游泳估价经商会计动物驯养占卜博物天灾学源石技艺理论源石理论动物学植物学数学化学物理学神秘学药理学毒理学营养学工程学地质地理学医学法学社会历史学军事理论母语: 通用语炎国语维多利亚语东国语乌萨斯语萨米语雷姆必拓语伊比利亚语萨尔贡语卡西米尔语叙拉古语莱塔尼亚语冬灵语(旧)玻利瓦尔语米诺斯语拉特兰语魔族语豁免值感染值恶化值一恶化值二恶化值三恶化值四恶化值五恶化值六技力源石技艺命中率信誉评级信誉体质物理强度源石技艺适应性HPhpHpsize博学撬锁侦查驾驶意志";
    final static int[] result = {0,1,2,3,4,5,6};

    public class DiceRollColum{//用于记录log用
        String qq;//用于记录QQ。同时方便查询pl表
        String item;//类型
        int itemValue;//标准值
        int addValue = 0;//修正值
        int difficultValue = 0;//困难度0-一般，1-较难，2-困难，3-较难
        int roll;//roll到的点数
        int attack = 0;//战斗随机数
        int rollResult = 0;//roll点数的结局
    }

    ArkDiceRoll(HashMap<String, ArrayList<PcInfo>> t,HashMap<String, Integer> tt){pcList = t;pcTag = tt;};//init sql class

    //项目检定
    public String check(String msg, int ttk,GroupMessageEvent g){
        ArrayList<String> rollDataAll = new ArrayList<String>();
        String re = "";
        
        for(String i : msg.split("&")){
            DiceRollColum rollData = new DiceRollColum();
            //检验项目
            rollData.item = extraItem(i);

            //项目值
            rollData.itemValue = extraItemValue(i, rollData.item, g);
            if(rollData.itemValue == -1){
                return null;
            }

            //roll点数
            rollData.roll = roll();

            //困难度
            rollData.difficultValue = extraDifficult(i);

            //增加值
            rollData.addValue = extraAddValue(i);

            //攻击增加
            if(ttk == 1){
                rollData.attack = attackRoll();
            }else{
                rollData.attack = 0;
            }
            
            //结果判定
            rollData = judgeResult(rollData);

            re = "";

            //项目_项目值_roll_攻击_结果
            re += rollData.item + "_" + rollData.roll + "_" + rollData.itemValue  + "_" + rollData.attack + "_" + rollData.rollResult;
            rollDataAll.add(re);
        }

        re = String.join(",", rollDataAll);

        return re;
    }

    //结果判定
    public DiceRollColum judgeResult(DiceRollColum rollDate){
        int re = 0;

        int stand = rollDate.itemValue + rollDate.addValue + rollDate.attack;
        if(rollDate.roll < 5){
            re = 0;
        }else if(rollDate.roll > 95){
            re = 6;
        }else if(stand < rollDate.roll){
            re = 5;
        }else if(stand > rollDate.roll){
            if(rollDate.difficultValue != 0){
                re = 4 - rollDate.difficultValue;
            }else{
                int temp = stand - rollDate.roll;
                for(int i = 3; i >= 0; i--){
                    if(temp > difficultLevel[i] ){
                        re = 4 - i;
                        break;
                    }
                }
            }
        }

        rollDate.rollResult = re;
        return rollDate;
    }

    //修改完成
    public String rollndn(String input){//输入指令：.r n#ndn
        //此为默认值
        int times = 1;//进行几次投掷
        int num = 1;//一次投几个骰子
        int dice = 100;//一个骰子有多少面

        input = input.replaceAll(" ", "");//消除空格
        input = input.split("\\,|\\，")[0];//将后续的描述清除掉

        //System.out.println(input);

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

        int addValue = extraAddValue(input);

        //设定随机值
        Random r = new Random();
        String re = "";//初始化返回值
        ArrayList<String> reList = new ArrayList<String>();
        //int total = 0;//总数值
        for(int i = 0;i<times;i++){
            String tempStr = "";
            int sum = 0;
            for(int j = 0;j<num;j++){
                int tempNum = (r.nextInt(dice)+1 + addValue);
                sum += tempNum;
                tempStr += "+"+String.valueOf(tempNum);
            }
            //total += sum;
            //System.out.println(tempStr+"111");
            reList.add(tempStr.replaceFirst("\\+", "")+"="+String.valueOf(sum));
        }
        re += String.join(",", reList);
        reList.clear();
        //System.out.println(re);
        return re;
    }

    //检定项目//√
    public String extraItem(String msg){
        String temp = msg;
        for(String i : difficultString){
            temp = temp.replaceAll(i, "");
        }

        temp = temp.split("\\+|\\-")[0];//消除修订值

        temp = temp.replaceAll("\\d", "");

        return temp;
    }

    //项目检定值获取//√
    public int extraItemValue(String msg, String item , GroupMessageEvent g){
        String temp = msg;
        for(String i : difficultString){
            temp = temp.replaceAll(i, "");
        }

        temp = temp.split("\\+|\\-")[0];//消除修订值

        int re = -1;//返回-1表示出错
        if(items.contains(temp)){
            //sql//操作
            String qq = String.valueOf(g.getSender().getId());
            re = pcList.get(qq).get(pcTag.get(qq)).itemList.get(temp);
        }else{
            Pattern pattern = Pattern.compile("\\d+.\\d|\\d*\\d");//正则表达式
            Matcher m = pattern.matcher(temp);
            while(m.find()){
                re = Integer.valueOf(m.group());
            }
        }

        return re;
    }

    //获取难度值//√
    public int extraDifficult(String msg){
        for(int i = 0;i<4;i++){
            if(msg.contains(difficultString[i]))
                return i;
        }
        return 0;
    }

    //获取修订值//√
    public int extraAddValue(String msg){
        String[] temp = msg.split("\\+|\\-",2);
        if(temp.length == 1)
            return 0;
        int re = Integer.valueOf(temp[1]);
        return msg.contains("-")?-re:re;
    }

    //输入最大值，然后进行反馈
    public int roll(int max){
        Random r = new Random();
        return r.nextInt(max) + 1;
    }
    
    //roll点的重载
    public int roll(){
        Random r = new Random();
        return r.nextInt(100) + 1;
    }
    
    //是否为rka
    public int attackRoll(){
        Random r = new Random();
        return r.nextInt(10) + r.nextInt(10) + 2;
    }
}