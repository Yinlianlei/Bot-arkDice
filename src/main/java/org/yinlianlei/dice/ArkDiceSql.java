package org.yinlianlei.dice;

import java.sql.*;

public class ArkDiceSql{
    private final String url = "jdbc:mysql://47.98.226.235/arkDice?useSSL=true&characterEncoding=utf8";//若不设置encodoing则会导致输出为非中文字符
    private final String user = "Ark";
    private final String password = "ArkDice";
    private static Connection conn = null;
    
    ArkDiceSql() {
        try {
            conn = DriverManager.getConnection(url, user, password);// mysql连接
            System.out.println("Mysql init finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void close_connect() {// 关闭连接
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //下一步，添加相关sql操作
}

/*
// this is the templete of sql
public String execute(String cmd){
        try{
            Statement stmt = conn.createStatement();
            ResultSet R = stmt.executeQuery(cmd);

            String re = "";
            while(R.next()){
                re += R.getString("nick");
            }

            stmt.close();
            return re;
        }catch(Exception e){
            e.printStackTrace();
        }
        return "";
    }

 */