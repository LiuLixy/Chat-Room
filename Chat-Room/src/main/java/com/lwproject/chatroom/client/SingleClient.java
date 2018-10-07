package com.lwproject.chatroom.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author: LiuWang
 * @data: 2018/10/3 11:19
 */

/**
 * 客户端发送消息给服务器
 */
class OutToServer implements Runnable {
    private Socket client;

    public OutToServer(Socket client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {
        PrintStream printStream;
        try {
            // 获取客户端输出流，向服务器发送消息
            printStream = new PrintStream(client.getOutputStream(), true);
            // 从键盘读取信息，发送给服务器
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.print("请输入内容: ");
                if (scanner.hasNext()) {
                    String str = scanner.nextLine();
                    printStream.println(str);
                    if ("byebye".equals(str)) {
                        System.out.println("客户端退出...");
                        printStream.close();
                        scanner.close();
                        client.close();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 读取服务器发来的消息
 */
class ReadFromServer implements Runnable {

    private Socket client;

    public ReadFromServer(Socket client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {
        Scanner scanner;
        try {
            scanner = new Scanner(client.getInputStream());
            scanner.useDelimiter("\n");
            while (scanner.hasNext()) {
                System.out.println(scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class SingleClient {

    public static void main(String[] args) {
        try {
            Socket client = new Socket("127.0.0.1", 9999);
            System.out.println("等待连接到服务器...");
            Thread readThread = new Thread(new ReadFromServer(client));
            Thread writeThread = new Thread(new OutToServer(client));
            readThread.start();
            writeThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}