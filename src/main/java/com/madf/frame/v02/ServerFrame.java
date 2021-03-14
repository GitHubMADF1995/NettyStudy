package com.madf.frame.v02;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 服务端显示窗口
 */
public class ServerFrame extends Frame {

    public static final ServerFrame INSTANCE = new ServerFrame();

    TextArea taLeft = new TextArea();
    TextArea taRight = new TextArea();
    Server server = new Server();

    private ServerFrame() {
        this.setSize(800, 600);
        this.setLocation(300, 30);
        Panel p = new Panel(new GridLayout(1, 2));
        p.add(taLeft);
        p.add(taRight);
        this.add(p);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.closeServer();
                System.exit(0);
            }
        });
    }

    public void updateServerMsg(String msg) {
        this.taLeft.setText(taLeft.getText() + msg + System.getProperty("line.separator"));
    }

    public void updateClientMsg(String msg) {
        this.taRight.setText(taRight.getText() + msg + System.getProperty("line.separator"));
    }

    public static void main(String[] args) {
        ServerFrame.INSTANCE.setVisible(true);
        ServerFrame.INSTANCE.server.serverStart();
    }

}
