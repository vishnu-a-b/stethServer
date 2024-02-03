/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import static com.example.demo.ServerApplication.CSockets;
import static com.example.demo.ServerApplication.Cis;
import static com.example.demo.ServerApplication.Cos;
import static com.example.demo.ServerApplication.ESockets;
import static com.example.demo.ServerApplication.Eis;
import static com.example.demo.ServerApplication.Eos;
import static com.example.demo.ServerApplication.status;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSelector {
    public void selector(Socket connectionsocket)
    {
        try
        {
            InputStream is = connectionsocket.getInputStream();
            OutputStream os= connectionsocket.getOutputStream();
            System.out.println("connection accepted");
            byte [] temp = new byte[2];
        
            Thread t = new Thread()
            {
                public void run()
                {
                    try 
                    {
                        while(is.read(temp)>0  )
                        {
                            if(temp[0]=='C' && temp[1]=='0')
                            {
                                for(int i=0;i<10;i++)
                                {
                                    if(!status[i][0])
                                    {
                                        CSockets[i]=connectionsocket;
                                        ServerApplication.patient[i] = new CThread();
                                        ServerApplication.patient[i].threadnum=i;
                                        ServerApplication.patient[i].is = Cis[i]= is;
                                        ServerApplication.patient[i].os = Cos[i]= os;
                                        status[i][0]=true;
                                        ServerApplication.patient[i].start();
                                        break;
                                    }
                                    else if(i==9)
                                    {
                                        System.out.println("No avalable threads");
                                        byte[] temporary = new byte [3];
                                        temporary[0]='E';temporary[0]='R';temporary[0]='R';
                                        connectionsocket.getOutputStream().write(temporary);
                                        connectionsocket.close();
                                        
                                    }
                                }
                                break;
                            }
                            else if(temp[0]=='E' && temp[1]=='0')
                            {
                                for(int i=0;i<10;i++)
                                {
                                    if(!status[i][1])
                                    {
                                        ESockets[i]=connectionsocket;
                                        System.out.println(i);
                                        ServerApplication.doctor[i] = new EThread();
                                        ServerApplication.doctor[i].threadnum=i;
                                        ServerApplication.doctor[i].is = Eis[i] = is;
                                        ServerApplication.doctor[i].os = Eos[i] = os;
                                        status[i][1]=true;
                                        ServerApplication.doctor[i].start();
                                        break;
                                    }
                                    else if(i==9)
                                    {
                                        System.out.println("No avalable threads");
                                        byte[] temporary = new byte [3];
                                        temporary[0]='E';temporary[0]='R';temporary[0]='R';
                                        connectionsocket.getOutputStream().write(temporary);
                                        connectionsocket.close();
                                    }
                                }
                                break;
                            }
                            else if(temp[0]=='E')
                            {
                                
                                char ch = (char) temp[1];
                                System.out.println("reconnection"+ch);
                                int i = Character.getNumericValue(ch);
                                System.out.println(i);
                                if(status[i-1][1]==false)
                                {
                                    i-=1;
                                    System.out.println("Thread available");
                                    ESockets[i]=connectionsocket;
                                    System.out.println(i);
                                    ServerApplication.doctor[i] = new EThread();
                                    ServerApplication.doctor[i].threadnum=i;
                                    ServerApplication.doctor[i].is = Eis[i] = is;
                                    ServerApplication.doctor[i].os = Eos[i] = os;
                                    status[i][1]=true;
                                    ServerApplication.doctor[i].start();
                                    break;
                                }
                                else
                                {
                                    System.out.println("Thread not available");
                                    
                                    byte[] temporary = new byte [3];
                                    temporary[0]='E';temporary[0]='R';temporary[0]='R';
                                    connectionsocket.getOutputStream().write(temporary);
                                    connectionsocket.close();
                                    
                                }
                                break;
                            }
                            else if(temp[0]=='C')
                            {
                                System.out.println("Reconnection cthread");
                                char ch = (char) temp[0];
                                int i = Character.getNumericValue(ch);
                                System.out.println(i);
                                if(status[i-1][0]==false)
                                {
                                    i-=1;
                                    System.out.println("Thread available");
                                    CSockets[i]=connectionsocket;
                                    System.out.println(i);
                                    ServerApplication.patient[i] = new CThread();
                                    ServerApplication.patient[i].threadnum=i;
                                    ServerApplication.patient[i].is = Cis[i] = is;
                                    ServerApplication.patient[i].os = Cos[i] = os;
                                    status[i][0]=true;
                                    ServerApplication.patient[i].start();
                                    break;
                                }
                                else
                                {
                                    System.out.println("No C thread available ");
                                    byte[] temporary = new byte [3];
                                    temporary[0]='E';temporary[0]='R';temporary[0]='R';
                                    connectionsocket.getOutputStream().write(temporary);
                                    connectionsocket.close();
                                }
                                break;
                            }
//                System.out.println("testingggggg");
                        }       } catch (IOException ex) {
                        Logger.getLogger(ClientSelector.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };
            t.start();
        }catch (IOException e)
        {
            System.out.println("Error in allocating thread");
        }
        
    }
    
}
