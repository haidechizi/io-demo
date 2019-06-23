package com.oedata.io.bio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BioClient {

    private String ip;
    private int port;

    public BioClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void connect() {
        Socket socket = null;
        ObjectInputStream objectInputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {

            socket = new Socket(this.ip, this.port);

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

            User user = new User("mic", 18);

            objectOutputStream.writeObject(user);

            objectInputStream = new ObjectInputStream(socket.getInputStream());

            user = (User) objectInputStream.readObject();

            System.out.println("收到服务端的反馈：" + user);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        new BioClient("127.0.0.1", 8888).connect();
    }
}
