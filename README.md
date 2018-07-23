# Chat Room

### Chat-Room（不带界面的聊天室）

上传的两个聊天室代码，其中Chat-Room为不带UI界面的聊天室，其功能包括用户注册，群聊，私聊以及用户退出。

##### 用户注册

当用户在控制台输入 userName:Peter(Peter为用户名，输入任意字符串都可)时，完成用户注册

如图：

![注册1](https://github.com/LiuLixy/image/blob/master/1532179688568.png)

![注册2](https://github.com/LiuLixy/image/blob/master/1532179701421.png)

##### 群聊

当用户在控制台输入G:message(message为输入的聊天内容)即发送为群聊信息，服务器会给每一个在聊天室的客户端发送信息，由于目前就一台计算机，无法完成该操作，就不截图啦。

##### 私聊

当用户在控制台输入 P:userName-message(此处userName为用户想要私聊消息的用户，message为消息内容)时，就对指定的用户进行私聊，由于此聊天室没有其他用户，此方法会报错。

![私聊](https://github.com/LiuLixy/image/blob/master/1532180027819.png)

##### 用户退出

当用户在控制台输入 byebye 时，就退出此聊天室

![用户退出1](https://github.com/LiuLixy/image/blob/master/1532180348859.png)![用户退出2](https://github.com/LiuLixy/image/blob/master/1532180362513.png)



### chatroom-app（带界面的局域网聊天室）

运行截图如下：

运行 Server 程序即可出现以下界面，单击启动即可启动服务器，人数上限与端口号都是默认值，如果有需要也可更改。

![服务器启动](https://github.com/LiuLixy/image/blob/master/%E6%9C%8D%E5%8A%A1%E5%99%A8%E5%90%AF%E5%8A%A8.png)

运行 Client 程序即可出现以下界面，单击连接即可连接服务器加入聊天室，端口号需要与服务器端口号一致。在聊天框中输入内容，敲下回车或者单击发送即可向聊天室内成员群发消息，单击断开或者关闭窗口即可退出聊天室。

![客户端连接服务器](https://github.com/LiuLixy/image/blob/master/%E5%AE%A2%E6%88%B7%E7%AB%AF%E8%BF%9E%E6%8E%A5%E6%9C%8D%E5%8A%A1%E5%99%A8.png)

![聊天界面](https://github.com/LiuLixy/image/blob/master/%E8%81%8A%E5%A4%A9%E7%95%8C%E9%9D%A2.jpg)

![客户端断开服务器](https://github.com/LiuLixy/image/blob/master/%E5%AE%A2%E6%88%B7%E7%AB%AF%E6%96%AD%E5%BC%80%E6%9C%8D%E5%8A%A1%E5%99%A8.png)



