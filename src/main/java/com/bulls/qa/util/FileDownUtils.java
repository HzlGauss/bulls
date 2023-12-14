package com.bulls.qa.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;

public class FileDownUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileDownUtils.class);

    public static File downloadFile(String urlPath, String downloadDir, String fileFullName) {
        File file = null;
        try {
            // 建立链接从请求中获取数据
//            URLConnection con = url.openConnection();
            BufferedInputStream bin = new BufferedInputStream(downloadFile(urlPath));

            // 指定存放位置(有需求可以自定义)
            String path = downloadDir + File.separatorChar + fileFullName;
            file = new File(path);
            // 校验文件夹目录是否存在，不存在就创建一个目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            OutputStream out = new FileOutputStream(file);
            int size = 0;
            int len = 0;
            byte[] buf = new byte[2048];
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
            }
            // 关闭资源
            bin.close();
            out.close();
            logger.info("文件下载成功:{}",file.getName());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("文件下载失败:{}",file.getName());
        }
        return file;

    }

    public static InputStream downloadFile(String urlPath) {
        try {
            return getConn(urlPath).getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static HttpURLConnection getConn(String urlPath) {
        URL url = null;
        HttpURLConnection httpURLConnection = null;
        try {
            // 统一资源
            url = new URL(urlPath);
            // 连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            // http的连接类
            httpURLConnection = (HttpURLConnection) urlConnection;
            //设置超时
            httpURLConnection.setConnectTimeout(1000 * 5);
            //设置请求方式，默认是GET
            httpURLConnection.setRequestMethod("GET");
            // 设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            // 打开到此 URL引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();
            // 文件大小
            int fileLength = httpURLConnection.getContentLength();

            // 打印文件大小
           logger.info("下载的文件大小为:" + fileLength / (1024) + "KB");

            return httpURLConnection;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void main(String[] args) {
        // 指定资源地址，下载文件测试
        downloadFile("https://console.duibatest.com.cn/miria/forward/file/check_file/qiho-center-normal-test-5696b9bcb9-n5kx4?appName=qiho-center&k8sId=5&path=%2Froot%2Flogs%2Fqiho-center%2Fapplication.log", "C:\\Users\\Duiba\\Desktop\\", "appliaction.log");

    }
}