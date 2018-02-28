package com.liuxi.spark;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8333);
            Socket socket = null;
            Random random = new Random(100);
            while (true) {
                socket = serverSocket.accept();
                while (socket.isConnected()) {
                    // 向服务器端发送数据
                    OutputStream os = socket.getOutputStream();
                    DataOutputStream bos = new DataOutputStream(os);
                    //每隔20ms发送一次数据
                    while (true) {
                        int ram = random.nextInt(100);
                        System.out.println(ram);
                        String openKey = ram % 2 == 0 ? "" : ("opendid-" + ram);
                        String str = "{\"pageCount\":\"1\",\"ipCityId\":\"2\",\"loginKey\":\"\",\"openid\":\"" + openKey + "\",\"ip\":\"121.221.1.1\",\"ipCountryId\":\"1\",\"sessionId\":\"22\",\"label\":\"323\",\"uuid\":\"323\",\"brVersion\":\"33\",\"loginCount\":\"32\",\"ipProvinceId\":\"2\",\"platCode\":\"32\",\"ipCountyid\":\"23\",\"productCode\":\"23\",\"browser\":\"232\",\"fromPage\":\"2323\",\"chnCode\":\"323\",\"action\":\"2323\",\"visitDate\":\"22\",\"category\":\"222\",\"value\":\"22\"}\r\n";

                        bos.write(str.getBytes());
                        bos.flush();
                        System.out.println(str);
                        //20ms发送一次数据
                        try {
                            Thread.sleep(25L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                //10ms检测一次连接
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
