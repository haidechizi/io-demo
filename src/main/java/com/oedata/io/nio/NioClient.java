package com.oedata.io.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

public class NioClient {

    private ByteBuffer readBuffer = ByteBuffer.allocateDirect(1024);

    private ByteBuffer writeBuffer = ByteBuffer.allocateDirect(1024);

    private Selector selector;


    public NioClient(String ip, int port) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();

        socketChannel.connect(new InetSocketAddress(ip, port));

        socketChannel.configureBlocking(false);

        selector = Selector.open();

        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);

    }

    public void connect() {
        try {
            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();

                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    if (key.isWritable()) {
                        synchronized (writeBuffer) {
                            writeBuffer.flip();
                            while (writeBuffer.hasRemaining()) {
                                socketChannel.write(writeBuffer);
                            }
                            writeBuffer.compact();
                        }

                    }
                    if (key.isReadable()) {
                        readBuffer.clear();
                        socketChannel.read(readBuffer);
                        readBuffer.flip();

                        String response = Charset.forName("utf-8").decode(readBuffer).toString();

                        System.out.println("收到服务端的反馈：" + response);
                    }
                    iterator.remove();
                }

            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void readLine() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String msg = null;
            while ((msg = bufferedReader.readLine()) != null) {
                synchronized (writeBuffer) {
                    writeBuffer.put((msg + "\r\n").getBytes());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException {
        final NioClient nioClient = new NioClient("127.0.0.1", 8888);

        Thread thread = new Thread(nioClient::readLine);
        thread.start();
        nioClient.connect();
    }
}
