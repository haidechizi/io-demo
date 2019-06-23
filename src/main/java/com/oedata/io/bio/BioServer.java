package com.oedata.io.bio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BioServer {

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(10,
            20, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), new ThreadPoolExecutor.CallerRunsPolicy());

    private int port;

    public BioServer(int port) {
        this.port = port;
    }

    public void startUp() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(this.port);

            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(() -> {
                    ObjectInputStream objectInputStream = null;
                    ObjectOutputStream objectOutputStream = null;
                    try {
                        objectInputStream = new ObjectInputStream(socket.getInputStream());
                        User user = (User) objectInputStream.readObject();

                        System.out.println("收到来自客户端的请求:" + user);
                        objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                        user.setAge(22);
                        objectOutputStream.writeObject(user);

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
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void main(String[] args) {
        new BioServer(8888).startUp();
    }
}
