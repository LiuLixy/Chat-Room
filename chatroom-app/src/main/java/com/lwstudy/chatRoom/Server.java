package com.lwstudy.chatRoom;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Server {
    private JFrame frame;
    private JTextArea contentArea;
    private JTextField txtMessage;
    private JTextField txtMax;
    private JTextField txtPort;
    private JButton btnStart;
    private JButton btnStop;
    private JButton btnSend;
    private JPanel northPanel;
    private JPanel southPanel;
    private JScrollPane rightPanel;
    private JScrollPane leftPanel;
    private JSplitPane centerSplit;
    private JList userList;
    private DefaultListModel listModel;

    private ServerSocket serverSocket;
    private ServerThread serverThread;

    private ArrayList<ClientThread> clients;

    private boolean isStart = false;

    // 主方法,程序执行入口
    public static void main(String[] args) {
        new Server();
    }

    // 执行消息发送
    public void send() {
        if (!isStart) {
            JOptionPane.showMessageDialog(frame, "服务器还未启动,不能发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (clients.size() == 0) {
            JOptionPane.showMessageDialog(frame, "没有用户在线,不能发送消息！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String message = txtMessage.getText().trim();
        if (message == null || message.equals("")) {
            JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        // 群发服务器消息
        sendServerMessage(message);
        contentArea.append("服务器说：" + txtMessage.getText() + "\r\n");
        txtMessage.setText(null);
    }

    // 构造方法
    public Server() {
        frame = new JFrame("服务器");
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setForeground(Color.blue);
        txtMessage = new JTextField();
        txtMax = new JTextField("30");
        txtPort = new JTextField("6666");
        btnStart = new JButton("启动");
        btnStop = new JButton("停止");
        btnSend = new JButton("发送");
        btnStop.setEnabled(false);
        listModel = new DefaultListModel();
        userList = new JList(listModel);

        southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(new TitledBorder("写消息"));
        southPanel.add(txtMessage, "Center");
        southPanel.add(btnSend, "East");
        leftPanel = new JScrollPane(userList);
        leftPanel.setBorder(new TitledBorder("在线用户"));

        rightPanel = new JScrollPane(contentArea);
        rightPanel.setBorder(new TitledBorder("消息显示区"));

        centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,
                rightPanel);
        centerSplit.setDividerLocation(100);
        northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 6));
        northPanel.add(new JLabel("人数上限"));
        northPanel.add(txtMax);
        northPanel.add(new JLabel("端口"));
        northPanel.add(txtPort);
        northPanel.add(btnStart);
        northPanel.add(btnStop);
        northPanel.setBorder(new TitledBorder("配置信息"));

        frame.setLayout(new BorderLayout());
        frame.add(northPanel, "North");
        frame.add(centerSplit, "Center");
        frame.add(southPanel, "South");
        frame.setSize(600, 400);
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        frame.setLocation((screenWidth - frame.getWidth()) / 2,
                (screenHeight - frame.getHeight()) / 2);
        frame.setVisible(true);

        // 关闭窗口时事件
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (isStart) {
                    // 关闭服务器
                    closeServer();
                }
                // 退出程序
                System.exit(0);
            }
        });

        // 文本框按回车键时事件
        txtMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });

        // 单击发送按钮时事件
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                send();
            }
        });

        // 单击启动服务器按钮时事件
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (isStart) {
                    JOptionPane.showMessageDialog(frame, "服务器已处于启动状态，不要重复启动！",
                            "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int max;
                int port;
                try {
                    try {
                        max = Integer.parseInt(txtMax.getText());
                    } catch (Exception e1) {
                        throw new Exception("人数上限为正整数！");
                    }
                    if (max <= 0) {
                        throw new Exception("人数上限为正整数！");
                    }
                    try {
                        port = Integer.parseInt(txtPort.getText());
                    } catch (Exception e1) {
                        throw new Exception("端口号为正整数！");
                    }
                    if (port <= 0) {
                        throw new Exception("端口号 为正整数！");
                    }
                    serverStart(max, port);
                    contentArea.append("服务器已成功启动!人数上限：" + max + ",端口：" + port
                            + "\r\n");
                    JOptionPane.showMessageDialog(frame, "服务器成功启动!");
                    btnStart.setEnabled(false);
                    txtMax.setEnabled(false);
                    txtPort.setEnabled(false);
                    btnStop.setEnabled(true);
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, exc.getMessage(),
                            "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 单击停止服务器按钮时事件
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!isStart) {
                    JOptionPane.showMessageDialog(frame, "服务器还未启动，无需停止！", "错误",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    closeServer();
                    btnStart.setEnabled(true);
                    txtMax.setEnabled(true);
                    txtPort.setEnabled(true);
                    btnStop.setEnabled(false);
                    contentArea.append("服务器成功停止!\r\n");
                    JOptionPane.showMessageDialog(frame, "服务器成功停止！");
                } catch (Exception exc) {
                    JOptionPane.showMessageDialog(frame, "停止服务器发生异常！", "错误",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    // 启动服务器
    public void serverStart(int max, int port) throws java.net.BindException {
        try {
            clients = new ArrayList<ClientThread>();
            serverSocket = new ServerSocket(port);
            serverThread = new ServerThread(serverSocket, max);
            serverThread.start();
            isStart = true;
        } catch (BindException e) {
            isStart = false;
            throw new BindException("端口号已被占用，请换一个！");
        } catch (Exception e1) {
            e1.printStackTrace();
            isStart = false;
            throw new BindException("启动服务器异常！");
        }
    }

    // 关闭服务器
    @SuppressWarnings("deprecation")
    public void closeServer() {
        try {
            if (serverThread != null) {
                // 停止服务器线程
                serverThread.stop();
            }
            for (int i = clients.size() - 1; i >= 0; i--) {
                // 给所有在线用户发送关闭命令
                clients.get(i).getWriter().println("CLOSE");
                clients.get(i).getWriter().flush();
                // 释放资源
                // 停止此条为客户端服务的线程
                clients.get(i).stop();
                serverSocket.close();
                serverSocket.close();
                serverSocket.close();
                clients.remove(i);
            }
            if (serverSocket != null) {
                // 关闭服务器端连接
                serverSocket.close();
            }
            // 清空用户列表
            listModel.removeAllElements();
            isStart = false;
        } catch (IOException e) {
            e.printStackTrace();
            isStart = true;
        }
    }

    // 群发服务器消息
    public void sendServerMessage(String message) {
        for (int i = clients.size() - 1; i >= 0; i--) {
            clients.get(i).getWriter().println("服务器：" + message);
            clients.get(i).getWriter().flush();
        }
    }

    // 服务器线程
    class ServerThread extends Thread {
        private ServerSocket serverSocket;
        // 人数上限
        private int max;

        // 服务器线程的构造方法
        public ServerThread(ServerSocket serverSocket, int max) {
            this.serverSocket = serverSocket;
            this.max = max;
        }

        public void run() {
            // 不停的等待客户端的链接
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    // 如果已达人数上限
                    if (clients.size() == max) {
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(socket.getInputStream()));
                        PrintWriter printWriter = new PrintWriter(socket
                                .getOutputStream());
                        // 接收客户端的基本用户信息
                        String inf = bufferedReader.readLine();
                        StringTokenizer st = new StringTokenizer(inf, "@");
                        User user = new User(st.nextToken(), st.nextToken());
                        // 反馈连接成功信息
                        printWriter.println("MAX@服务器：对不起，" + user.getName()
                                + user.getIp() + "，服务器在线人数已达上限，请稍后尝试连接！");
                        printWriter.flush();
                        // 释放资源
                        bufferedReader.close();
                        printWriter.close();
                        socket.close();
                        continue;
                    }
                    ClientThread client = new ClientThread(socket);
                    // 开启对此客户端服务的线程
                    client.start();
                    clients.add(client);
                    // 更新在线列表
                    listModel.addElement(client.getUser().getName());
                    contentArea.append(client.getUser().getName()
                            + client.getUser().getIp() + "上线!\r\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 为一个客户端服务的线程
    class ClientThread extends Thread {
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private User user;

        public BufferedReader getReader() {
            return reader;
        }

        public PrintWriter getWriter() {
            return writer;
        }

        public User getUser() {
            return user;
        }

        // 客户端线程的构造方法
        public ClientThread(Socket socket) {
            try {
                this.socket = socket;
                reader = new BufferedReader(new InputStreamReader(socket
                        .getInputStream()));
                writer = new PrintWriter(socket.getOutputStream());
                // 接收客户端的基本用户信息
                String inf = reader.readLine();
                StringTokenizer st = new StringTokenizer(inf, "@");
                user = new User(st.nextToken(), st.nextToken());
                // 反馈连接成功信息
                writer.println(user.getName() + user.getIp() + "与服务器连接成功!");
                writer.flush();
                // 反馈当前在线用户信息
                if (clients.size() > 0) {
                    String temp = "";
                    for (int i = clients.size() - 1; i >= 0; i--) {
                        temp += (clients.get(i).getUser().getName() + "/" + clients
                                .get(i).getUser().getIp())
                                + "@";
                    }
                    writer.println("USERLIST@" + clients.size() + "@" + temp);
                    writer.flush();
                }
                // 向所有在线用户发送该用户上线命令
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println(
                            "ADD@" + user.getName() + user.getIp());
                    clients.get(i).getWriter().flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 不断接收客户端的消息，进行处理。
        @Override
        public void run() {
            String message = null;
            while (true) {
                try {
                    // 接收客户端消息
                    message = reader.readLine();
                    // 下线命令
                    if (message.equals("CLOSE")) {
                        contentArea.append(this.getUser().getName()
                                + this.getUser().getIp() + "下线!\r\n");
                        // 断开连接释放资源
                        reader.close();
                        writer.close();
                        socket.close();

                        // 向所有在线用户发送该用户的下线命令
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            clients.get(i).getWriter().println(
                                    "DELETE@" + user.getName());
                            clients.get(i).getWriter().flush();
                        }
                        // 更新在线列表
                        listModel.removeElement(user.getName());

                        // 删除此条客户端服务线程
                        for (int i = clients.size() - 1; i >= 0; i--) {
                            if (clients.get(i).getUser() == user) {
                                ClientThread temp = clients.get(i);
                                // 删除此用户的服务线程
                                clients.remove(i);
                                // 停止这条服务线程
                                temp.interrupt();
                                return;
                            }
                        }
                    } else {
                        // 转发消息
                        dispatcherMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 群发消息
        public void dispatcherMessage(String message) {
            StringTokenizer stringTokenizer = new StringTokenizer(message, "@");
            String source = stringTokenizer.nextToken();
            String owner = stringTokenizer.nextToken();
            String content = stringTokenizer.nextToken();
            message = source + "说：" + content;
            contentArea.append(message + "\r\n");
            // 群发
            if (owner.equals("ALL")) {
                for (int i = clients.size() - 1; i >= 0; i--) {
                    clients.get(i).getWriter().println(message+"(多人发送)");
                    clients.get(i).getWriter().flush();
                }
            }
        }
    }
}

