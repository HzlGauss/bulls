package com.bulls.qa.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SqlUtils {
    private static final Logger logger = LoggerFactory.getLogger(SqlUtils.class);


    final String JDBCDRIVER = "com.mysql.cj.jdbc.Driver";
    private Connection conn;
    private Statement sta;
    private ResultSet result;


    public int executeSQL(String sql, String ip, String port, String db, String user, String password) {
        int result = 0;
        try {
            if (!createConn(ip, port, db, user, password)) {
                return result;
            }
            logger.info("执行sql:{}", sql);
            result = sta.executeUpdate(sql);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sta.close();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return result;
    }


    public List<Map<String, Object>> QuerySQL2(String sql, String ip, String port, String db, String user, String password) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            if (!createConn(ip, port, db, user, password)) {
                return list;
            }
            logger.info("执行sql:{}", sql);
            result = sta.executeQuery(sql);
            ResultSetMetaData md = result.getMetaData();
            int columnCount = md.getColumnCount();
            while (result.next()) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), result.getObject(i));
                }
                list.add(rowData);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeConn();
            } catch (SQLException throwables) {
                logger.error("连接关闭失败");
                throwables.printStackTrace();
            }
        }

        return list;
    }


    public int ExecuteSQL(String ip, String port, String db, String user, String password, String sql) {
        int result = 0;
        try {
            if (!createConn(ip, port, db, user, password)) {
                return result;
            }
            result = sta.executeUpdate(sql);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                sta.close();
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }


        return result;
    }


    public List<Map<String, Object>> QuerySQL(String ip, String port, String db, String user, String password,
                                              String sql) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            if (!createConn(ip, port, db, user, password)) {
                return list;
            }
            logger.info("执行sql:{}", sql);
            result = sta.executeQuery(sql);
            ResultSetMetaData md = result.getMetaData();
            int columnCount = md.getColumnCount();
            while (result.next()) {
                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    rowData.put(md.getColumnName(i), result.getObject(i));
                }
                list.add(rowData);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeConn();
            } catch (SQLException throwables) {
                logger.error("连接关闭失败");
                throwables.printStackTrace();
            }
        }

        return list;
    }


    private boolean createConn(String ip, String port, String db, String user, String password) throws Exception {
        Class.forName(JDBCDRIVER);
        boolean isConnected = true;
        conn = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + port + "/" + db, user, password);
        if (!conn.isClosed()) {
        } else {
            logger.error("数据库连接失败");
            isConnected = false;
            return isConnected;
        }
        sta = conn.createStatement();
        return isConnected;
    }


    public void closeConn() throws SQLException {
        result.close();
        sta.close();
        conn.close();
    }

    public static void main(String[] args) throws Exception {
        /*SqlUtils sqlUtils = new SqlUtils();

        String ip = "47.111.157.152";
        String port = "3306";
        String db = "qiho?autoReconnect=true&testOnBorrow=true&testWhileIdle=true&serverTimezone=UTC";
        String user = "tuia_test";
        String password = "JB8C6wwZ7dQWYvquzsobu2Yfn";
        ip = "172.16.40.234";
        port = "5306";
        password = "JB8C6wwZ7dQWYvquzsobu2Yfn";
        QiHoConfig qiHoConfig = new QiHoConfig();
        qiHoConfig.setIp(ip);
        qiHoConfig.setPort(port);
        qiHoConfig.setDb(db);
        qiHoConfig.setUser(user);
        qiHoConfig.setPassword(password);
        sqlUtils.qiHoConfig = qiHoConfig;

        String sql = "SELECT * FROM tb_qiho_channel_info where order_id in (SELECT order_id FROM ( SELECT order_id FROM tb_qiho_order where order_status='CLOSED' order by gmt_create desc limit 10) as a);";
        sql = "select * from tb_qiho_order_snapshot where order_id='2202006081410100025D00002871' order by gmt_create desc limit 10;";
        sql = "";

        List<Map<String, Object>> list = sqlUtils.QuerySQL(sql);
        System.out.println(list);*/

    }
}