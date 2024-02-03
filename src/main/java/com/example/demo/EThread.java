
package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author icuhost
 */
class EThread extends Thread{
    int threadnum;
    InputStream is;
    OutputStream os;
    Socket soc;
    boolean connection = true;
    byte[] b = new byte[2];
    public void run()
    {
        System.out.println("EThread Started:"+(threadnum));
        b[0]='E';b[1]=(byte) threadnum;
        try
        {
            System.out.println("Writing response: "+b[0]+", "+b[1]);
//                soc.getOutputStream().write(b);
            os.write(b);
            reader.start();
        }catch(IOException e)
        {
             System.out.println("Writing error "+threadnum);   
        }
        
    }
    
    Thread reader = new Thread()
    {
        int length;
        byte buffer1[] = new byte[800];
        
        public void run()
        {
            try
            {
                System.out.println("trying to read ");
                
                while((length = is.read(buffer1))>0)
                {
//                    
                    if(buffer1[0]=='E' && buffer1[1]=='N' && buffer1[2] == 'D')
                    {
                        System.out.println("End of session");
                        connection = false;
//                        break;
                    }
                    else if(buffer1[0]=='E' &&  !connection)
                    {
                        connection = true;
                        System.out.println("Thread restarted:"+threadnum);
                        os.write(b);
                    }
                    else if(connection)
                    {
                        System.out.println("data arrived");
                    }
                    
                }
                if(length== (-1))
                {
                    ServerApplication.status[threadnum][1]=false;
                    ServerApplication.dstatus[threadnum]= false;
                    System.out.println("EThread Connection Closed "+threadnum);
                }
            }catch (IOException e)
            {
                ServerApplication.status[threadnum][1]=false;
                ServerApplication.dstatus[threadnum]=false;
                System.out.println("EThread Connecton closed "+threadnum); 
            }
        }
    };
    
}
