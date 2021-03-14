package com.madf.frame.v02;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 客户端显示窗口
 */
public class ClientFrame extends Frame {

    TextArea ta = new TextArea();
    TextField tf = new TextField();

    Client c = null;

    public static final ClientFrame INSTANCE = new ClientFrame();

    private ClientFrame() {
        this.setSize(400, 300);
        this.setLocation(100, 20);
        this.add(ta, BorderLayout.CENTER);
        this.add(tf, BorderLayout.SOUTH);

        //监听输入框的动作，将信息发送到服务器
        tf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                c.send(tf.getText());
                tf.setText("");//将文本框置为空
            }
        });

        //添加窗口关闭的监听，关闭客户端与服务端的连接
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                c.closeClient();//客户端关闭给服务端发送消息
                System.exit(0);
            }
        });
    }

    private void connectToServer() {
        c = new Client();
        c.connect();
    }

    /**
     * 接收到数据后，更新客户端界面上显示的内容
     * @param msgAccepted
     */
    public void updateText(String msgAccepted) {
        this.ta.setText(ta.getText() + System.getProperty("line.separator") + msgAccepted);
    }

    public static void main(String[] args) {
        ClientFrame frame = ClientFrame.INSTANCE;
        frame.setVisible(true);//显示客户端窗口
        frame.connectToServer();//连接到服务端
    }

}
