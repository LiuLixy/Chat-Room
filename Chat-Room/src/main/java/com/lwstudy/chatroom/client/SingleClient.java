package com.lwstudy.chatroom.client;

import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.io.IOException;

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
            // 获取客户端输出流，向服务器发生信息
            printStream = new PrintStream(client.getOutputStream(), true);
            // 从键盘读取信息，发送给服务器
            Scanner scanner = new Scanner(System.in);
            scanner.useDelimiter("\n");
            while (true) {
                System.out.print("请输入内容: ");
                if (scanner.hasNext()) {
                    String str = scanner.nextLine();
                    printStream.println(str);
                    if (str.equals("byebye")) {
                        System.out.println("客户端退出...");
                        printStream.close();
                        scanner.close();
                        client.close();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

/**
 * 读取服务器线程
 */
class ReadFromServer implements Runnable {
    private Socket client;

    public ReadFromServer(Socket client) {
        super();
        this.client = client;
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(client.getInputStream());
            scanner.useDelimiter("\n");
            while (scanner.hasNext()) {
                System.out.println("服务器发来的信息为: " + scanner.nextLine());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class SingleClient {

    public static void main(String[] args) throws Exception {
        try {
            // 创建连接服务器的客户端套接字
            Socket client = new Socket("127.0.0.1", 6666);
            System.out.println("等待连接服务器...");
            Thread readThread = new Thread(new ReadFromServer(client));
            Thread writeThread = new Thread(new OutToServer(client));
            readThread.start();
            writeThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
