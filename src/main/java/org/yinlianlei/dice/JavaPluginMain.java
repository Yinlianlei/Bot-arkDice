package org.yinlianlei.dice;

import net.mamoe.mirai.Bot;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.event.events.FriendAddEvent;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.Group;

import java.sql.*;

import java.util.ArrayList;

/**
 * 使用 Java 请把
 * {@code /src/main/resources/META-INF.services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin}
 * 文件内容改成 {@code org.example.mirai.plugin.JavaPluginMain} <br/>
 * 也就是当前主类全类名
 *
 * 使用 Java 可以把 kotlin 源集删除且不会对项目有影响
 *
 * 在 {@code settings.gradle.kts} 里改构建的插件名称、依赖库和插件版本
 *
 * 在该示例下的 {@link JvmPluginDescription} 修改插件名称，id 和版本等
 *
 * 可以使用 {@code src/test/kotlin/RunMirai.kt} 在 IDE 里直接调试，
 * 不用复制到 mirai-console-loader 或其他启动器中调试
 */

public final class JavaPluginMain extends JavaPlugin {
    public static final JavaPluginMain INSTANCE = new JavaPluginMain();
    private static final ArkDiceSql sqlCur = new ArkDiceSql();
    private static ArkDiceLogue adl = new ArkDiceLogue();

    private JavaPluginMain() {
        super(new JvmPluginDescriptionBuilder("org.yinlianlei.dice", "0.1.0")
                .info("EG")
                .build());
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("日志");
        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            //监听群消息
            String msg = g.getMessage().contentToString();
            //@2683380854 Yes
            //getLogger().info(g.getMessage().contentToString());
            
            String senderQQ = String.valueOf(g.getSender().getId());
            String senderNick = String.valueOf(g.getSenderName());
            String re = adl.replayMain(msg,senderQQ);
            re = re.replace("@sender",senderNick);
            if(re.contains("rh")){
                String[] rhResult = re.split("\\|");
                g.getGroup().sendMessage(rhResult[1]);
                g.getSender().sendMessage(rhResult[2]);
            }else{
                g.getGroup().sendMessage(re);
            }
        });
        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            //监听好友消息
            //getLogger().info(f.getMessage().contentToString());
            //f.getSubject().sendMessage("23");
        });
        //自动同意好友申请
        GlobalEventChannel.INSTANCE.subscribeAlways(NewFriendRequestEvent.class, event -> {
            event.accept();
        });
    }
}

/**
            String msg = "233";

            User sender = g.getSender();
            sender.sendMessage(msg);//获取好友后进行回复

            //g.getSubject().sendMessage("23");
 */

/*
            String msg = g.getMessage().contentToString();
            String[] msgList = msg.split(" ");//进行以" "进行的切割
            for(String i : msgList){
                getLogger().info(i);
            }
*/
