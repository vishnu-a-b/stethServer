/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jideesh
 */
public class UDPThread extends Thread
{
    private byte[] buf = new byte[1002];
    public static DatagramPacket packet1, temppacket;
    SocketAddress sc;
    InetAddress test1;
    int port1;
    byte [] temp = new byte[1002];
    DatagramPacket packet = new DatagramPacket(buf, buf.length);
    
    public void run()
    {
        // =  new DatagramPacket(buf, buf.length);
        
        
        while(true)
        {
            try 
            {
                ServerApplication.dsocket.receive(packet);
                buf = packet.getData();
                temp = packet.getData();
                if(buf[0]=='E'&&buf[1]=='H'&&buf[2]=='B')
                {
//                    byte[] d = new byte[1000];
                    char ch = (char) buf[3];
                    int i = ch;
                    ServerApplication.daddr[i] = packet.getAddress();
                    ServerApplication.dport[i] = packet.getPort();
                    SocketAddress sc = packet.getSocketAddress();
                    ServerApplication.dstatus[i] = true;
                    
                    ServerApplication.address[i] = packet.getAddress();
                    char ff = (char) buf[0];
                    System.out.println("E packet received:"+i+", "+ff);
                    
                }
                else if(buf[0]=='C')
                {
                    char ch = (char) buf[1];
                    int j = Character.getNumericValue(ch);
                    
                    if(ServerApplication.dstatus[j])
                    {
//                        for(int i=0;i<1002;i++)
//                            System.out.print(","+buf[i]);
                        temppacket = new DatagramPacket(temp, temp.length);
                        packet.setData(buf);
                        packet.setAddress(ServerApplication.daddr[j]);
                        packet.setPort(ServerApplication.dport[j]);
                        
                        ServerApplication.dsocket.send(packet);
                        System.out.println("Packet sent successfully");
                    }
                    else
                        System.out.println("Data not sent");
                }
                else
                     System.out.println("UDP Data Arrived");
            } catch (IOException ex) {
                Logger.getLogger(UDPThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
