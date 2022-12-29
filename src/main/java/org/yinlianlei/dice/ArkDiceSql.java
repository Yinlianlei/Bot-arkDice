package org.yinlianlei.dice;

import java.sql.*;
import java.util.ArrayList;
import com.mysql.jdbc.*;

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

    int insert(String qq){
        Savepoint sp = null;
        try{
            String cmd = "update pcList set `using` = 0 where qq = '"+qq+"'";

            conn.setAutoCommit(false);//开启事务
            sp = conn.setSavepoint();//在这里设置事务回滚点
            
            Statement stmt = conn.createStatement();
            
            int R = stmt.executeUpdate(cmd);

            //R.close();
            stmt.close();
            conn.commit();//提交
            return R;
        }catch(Exception e){
            e.printStackTrace();
            try{
                conn.rollback(sp);
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
        return -1;
    }

    int tagPc(String pid,String qq){
        Savepoint sp = null;
        try{
            String[] cmd = 
                        {
                        "update pcList set `using` = 0 where qq = '"+qq+"'",
                        "update pcList set `using` = 1 where qq = '"+qq+"' and pcId = "+pid+""
                        };//+item+" from DicePc where qq = '"+qq+"' and `using` = 1";
            
            conn.setAutoCommit(false);//开启事务
            sp = conn.setSavepoint();//在这里设置事务回滚点
            
            Statement stmt = conn.createStatement();
            
            
            for(String i : cmd){
                int R = stmt.executeUpdate(i);
            }

            stmt.close();
            conn.commit();//提交
            return 0;
        }catch(Exception e){
            e.printStackTrace();
            try{
                conn.rollback(sp);
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
        return -1;
    }

    ArrayList<String> getPc(String qq){
        Savepoint sp = null;
        try{
            String cmd = "select * from pcList where qq = '"+qq+"'";//+item+" from DicePc where qq = '"+qq+"' and `using` = 1";
            
            conn.setAutoCommit(false);//开启事务
            sp = conn.setSavepoint();//在这里设置事务回滚点

            Statement stmt = conn.createStatement();
            
            ResultSet R = stmt.executeQuery(cmd);

            ArrayList<String> re = new ArrayList<String>();
            while(R.next()){
                String[] temp = {R.getString(1),R.getString(3),R.getString(4)};
                re.add(String.join("-",temp));
            }

            R.close();
            stmt.close();
            return re;
        }catch(Exception e){
            e.printStackTrace();
            try{
                conn.rollback(sp);
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
        return null;
    }

    int getPcValue(String item,String qq){//输入Pc项目及对应Plqq号
        Savepoint sp = null;
        try{
            String cmd = "select "+item+" from DicePc where qq = '"+qq+"' and `using` = 1";

            conn.setAutoCommit(false);//开启事务
            sp = conn.setSavepoint();//在这里设置事务回滚点

            Statement stmt = conn.createStatement();
            ResultSet R = stmt.executeQuery(cmd);

            int re = 0;
            while(R.next()){
                re = Integer.valueOf(R.getString(item));
            }

            R.close();
            stmt.close();
            return re;
        }catch(Exception e){
            e.printStackTrace();
            try{
                conn.rollback(sp);
            }catch(Exception e1){
                e1.printStackTrace();
            }
        }
        return -1;
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