package com.oedata.io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NioServer {

    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

    private ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024);

    private int port;

    public NioServer(int port) {
        this.port = port;
    }

    public void startUp() {
        ServerSocketChannel serverSocketChannel = null;
        Selector selector = null;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            selector = Selector.open();

            serverSocketChannel.bind(new InetSocketAddress(this.port));
            serverSocketChannel.configureBlocking(false);

            serverSocketChannel.register(selector,SelectionKey.OP_ACCEPT);

            while (true) {
                if(selector.select() == 0) {
                    continue;
                }
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if(key.isAcceptable()) {
                        System.out.println("is acceptable");
                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(key.selector(),SelectionKey.OP_READ);

                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
                        byteBuffer.put("hi new Channel".getBytes());
                        byteBuffer.flip();
                        socketChannel.write(byteBuffer);
                    }
                    if(key.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) key.channel();
                        System.out.println("is readable");
                        readBuffer.clear();
                        socketChannel.read(readBuffer);
                        readBuffer.flip();

                        String receiveData = Charset.forName("utf-8").decode(readBuffer).toString();

                        System.out.println("receiveData :" + receiveData);

                        key.attach(receiveData);

                    }

                    if(key.isWritable()) {

                        System.out.println("is write");
                        SocketChannel socketChannel = (SocketChannel) key.channel();

                        String message = (String) key.attachment();

                        System.out.println(message);

                        key.attach(null);

                        writeBuffer.clear();
                        socketChannel.read(writeBuffer);
                        writeBuffer.flip();

                        while (writeBuffer.hasRemaining()) {
                            socketChannel.write(writeBuffer);
                        }

                    }

                    iterator.remove();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new NioServer(8888).startUp();
    }
}
