package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.exit;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {

    public static ServerSocket socket;
    public static DatagramSocket dsocket;
    public static Socket connectionsocket;
    public static Socket[] ESockets = new Socket[10];
    public static Socket[] CSockets = new Socket[10];
    public static InetAddress[] address = new InetAddress[10];
    public static boolean status[][] = new boolean[10][2];
    public static InputStream[] Eis = new InputStream[10];
    public static InputStream[] Cis = new InputStream[10];
    public static OutputStream[] Eos = new OutputStream[10];
    public static OutputStream[] Cos = new OutputStream[10];

    public static CThread patient[] = new CThread[10];
    public static EThread doctor[] = new EThread[10];

    public static InetAddress[] daddr = new InetAddress[10];
    public static int[] dport = new int[10];
    public static boolean[] dstatus = new boolean[10];

    public static void main(String[] args) throws IOException {
        UDPThread udp = new UDPThread();
//        long pid = ProcessHandle.current().pid();
//        System.out.println("PID is:"+pid);

        SpringApplication.run(ServerApplication.class, args);
        try {
            socket = new ServerSocket(1236);
//            dsocket = new DatagramSocket(1235);
//            udp.start();
            System.out.println("Starting UDP Socket in port 1235");

        } catch (IOException ex) {
            System.out.println("Cant open... Port is used by another program");
            exit(0);
        }

//       
        for (int i = 0; i < 10; i++) {
            status[i][1] = false;
            status[i][0] = false;
            dstatus[i] = false;
        }
        while (true) {
            System.out.println("Waiting for connection");
            connectionsocket = socket.accept();
            ClientSelector obj = new ClientSelector();
            obj.selector(connectionsocket);
        }
    }

}
