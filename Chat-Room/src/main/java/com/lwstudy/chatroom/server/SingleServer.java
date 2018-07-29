package com.lwstudy.chatroom.server;

import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 服务器处理用户连接后的输入输出处理
 */
class ExecuteClientServer implements Runnable {

    private Map<String, Socket> clientMap = new HashMap<String, Socket>();

    private Socket client;

    public ExecuteClientServer(Socket client, Map<String, Socket> clientMap) {
        super();
        this.client = client;
        this.clientMap = clientMap;
    }
    @Override
    public void run() {
        try {
            System.out.println("有客户端连接, 端口号为: " + client.getPort());
            // 获取客服端输入流，读取用户信息
            Scanner scanner = new Scanner(client.getInputStream());
            String str = null;
            // scanner.useDelimiter("\n");
            while (true) {
                if (scanner.hasNext()) {
                    // 读取客户端输入的内容
                    str = scanner.nextLine();
                    Pattern pattern = Pattern.compile("\r\n|\n|\r");
                    Matcher matcher = pattern.matcher(str);
                    str = matcher.replaceAll("");
                    // 用户注册
                    // 格式为 userName:Peter
                    if (str.startsWith("userName")) {
                        String userName = str.split("\\:")[1];
                        registerUser(userName, client);
                        continue;
                    }
                    // 群聊
                    else if (str.startsWith("G:")) {
                        String msg = str.split("\\:")[1];
                        groupChat(msg);
                        continue;
                    }
                    // 私聊
                    // 格式：P:yuisama-hello
                    else if (str.startsWith("P:")) {
                        String tmp = str.split("\\:")[1];
                        String userName = tmp.split("-")[0];
                        String msg = tmp.split("\\-")[1];
                        privateChat(userName, msg);
                        continue;
                    }
                    // 用户退出
                    // 此时有 socket, 需要找到 key
                    else if (str.contains("byebye")) {
                        String key = null;
                        for (String tmp : clientMap.keySet()) {
                            if (clientMap.get(tmp).equals(client)) {
                                key = tmp;
                            }
                        }
                        // 删除 Map 中指定 key 对应的 socket
                        clientMap.remove(key);
                        System.out.println("用户 " + key + " 已下线...");
                        System.out.println("当前群聊在线人数为: " + clientMap.size());
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取key值（即由端口号找到用户名）
     *
     * @param socket
     * @return
     */
    public String getUserName(Socket socket) {
        String userName = null;
        for(String getKey : clientMap.keySet()) {
            if(clientMap.get(getKey).equals(socket)) {
                userName = getKey;
            }
        }
        return userName;
    }

    /**
     * 用户注册方法
     * @param userName 用户名
     * @param client 用户名对应的Socket
     */
    public void registerUser(String userName, Socket client) {
        clientMap.put(userName, client);
        System.out.println("用户 " + userName + " 上线了! ");
        System.out.println("当前群聊在线人数为: " + clientMap.size());
    }
    /**
     * 群聊
     * @param msg 群聊发送信息
     */
    public void groupChat(String msg) {
        Iterator<Map.Entry<String, Socket>> iterator = clientMap.entrySet().iterator();
        while (iterator.hasNext()) {
            try {
                Socket socket = iterator.next().getValue();
                PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
                printStream.print(getUserName(client)+"说: "+msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 私聊方法
     */
    public void privateChat(String userName, String msg) {
        Socket privateUser = clientMap.get(userName);
        try {
            PrintStream printStream = new PrintStream(privateUser.getOutputStream());
            printStream.println(getUserName(client)+"悄悄对你说: "+msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

public class SingleServer {

    public static void main(String[] args) throws Exception {

        ServerSocket serverSocket = new ServerSocket(6666);
        // 存取用户信息（用户名和Socket）
        Map<String, Socket> map = new HashMap<String, Socket>();
        // 创建一个固定大小的线程池，大小为20
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        System.out.println("等待用户连接中...");
        try {
            for(int i = 0;i < 20;i ++) {
                Socket socket = serverSocket.accept();
                System.out.println("有新的用户连接："+socket.getInetAddress()+"\t"+socket.getPort());
                executorService.execute(new ExecuteClientServer(socket, map));
            }
            executorService.shutdown();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
