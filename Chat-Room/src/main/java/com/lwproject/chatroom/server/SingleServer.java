package com.lwproject.chatroom.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: LiuWang
 * @data: 2018/10/3 11:19
 */

/**
 * 服务器处理用户连接后的输入输出
 */
class ExecuteClientServer implements Runnable {

    private Map<String, Socket> clientMap = new HashMap<>();
    private Socket client;
    public static Pattern pattern = Pattern.compile("\r\n|\n|\r");

    public ExecuteClientServer(Map<String, Socket> clientMap, Socket client) {
        super();
        this.clientMap = clientMap;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            System.out.println("有新的客户端连接, 端口号为: " + client.getPort());
            // 获取客户端输入流，读取用户信息
            Scanner scanner = new Scanner(client.getInputStream());
            String str = null;
            while (true) {
                if (scanner.hasNext()) {
                    // 读取客户端输入的内容
                    str = scanner.nextLine();
                    Matcher matcher = pattern.matcher(str);
                    str = matcher.replaceAll("");
                    // 用户注册, 格式为：userName:Peter
                    if (str.startsWith("userName:")) {
                        String userName = str.split("\\:")[1];
                        registerUser(userName, client);
                        continue;
                    }
                    // 群聊, 格式：G:hello
                    if (str.startsWith("G:")) {
                        String msg = str.split("\\:")[1];
                        groupChat(msg);
                        continue;
                    }
                    // 私聊, 格式：P:Perter-hello
                    if (str.startsWith("P:")) {
                        String tmp = str.split("\\:")[1];
                        String userName = tmp.split("\\-")[0];
                        String msg = tmp.split("\\-")[1];
                        privateChat(userName, msg);
                        continue;
                    }
                    // 用户退出
                    if (str.contains("byebye")) {
                        String key = null;
                        // 找到需要删除的用户
                        for (String tmp : clientMap.keySet()) {
                            if (clientMap.get(tmp).equals(client)) {
                                key = tmp;
                            }
                        }
                        // 删除Map中指定的key对应的Socket
                        clientMap.remove(key);
                        System.out.println("用户" + key + "已下线...");
                        System.out.println("当前群聊人数为: " + clientMap.size());
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 由端口号获取到用户名
     *
     * @param socket
     * @return
     */
    public String getUserName(Socket socket) {
        String userName = null;
        for (String getKey : clientMap.keySet()) {
            if (clientMap.get(getKey).equals(socket)) {
                userName = getKey;
            }
        }
        return userName;
    }

    /**
     * 用户注册
     *
     * @param userName 用户名
     * @param client   用户对应的Socket
     */
    public void registerUser(String userName, Socket client) {
        clientMap.put(userName, client);
        System.out.println("用户: " + userName + " 上线了！");
        System.out.println("当前群聊人数为: " + clientMap.size());
    }

    /**
     * 群聊
     *
     * @param msg
     */
    public void groupChat(String msg) {
        // 遍历得到每个用户的Socket, 给每个用户发送消息
        for (Map.Entry<String, Socket> stringSocketEntry : clientMap.entrySet()) {
            try {
                Socket socket = stringSocketEntry.getValue();
                PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
                // 给每个客户端发消息
                printStream.println(getUserName(client) + "说: " + msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 私聊
     *
     * @param userName
     * @param msg
     */
    public void privateChat(String userName, String msg) {
        Socket privateToUser = clientMap.get(userName);
        try {
            PrintStream printStream = new PrintStream(privateToUser.getOutputStream());
            printStream.println(getUserName(client) + " 悄悄对你说: " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class SingleServer {

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            // 存储用户信息(用户名和Socket)
            Map<String, Socket> map = new HashMap<>();
            ExecutorService executorService = Executors.newFixedThreadPool(20);
            System.out.println("等待用户连接...");
            for (int i = 0; i < 20; i++) {
                Socket socket = serverSocket.accept();
                System.out.println("有新用户连接" + socket.getInetAddress() + "\t" + socket.getPort());
                executorService.execute(new ExecuteClientServer(map, socket));
            }
            executorService.shutdown();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
