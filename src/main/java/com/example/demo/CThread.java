/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

/**
 *
 * @author icuhost
 */
class CThread extends Thread {

    int threadnum;
    InputStream is;
    OutputStream os;
    Socket soc;
    boolean state = true;
    int[] values = new int[10];
    public static int k;
    byte[] packet = new byte[3];
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
    final CloseableHttpClient httpClient = HttpClients.createDefault();
    public static String hospName, consultantName, patientName, dir, EpisodeID;
    public static String pathtoFile;
    public static double filesize;

    OutputStream playingfos;

    public void run() {

        System.out.println("CThread Started:" + (threadnum));
        reader.start();
        int temp = 0;
        for (int i = 0; i < 10; i++) {
            if (ServerApplication.status[i][1]) {
                values[i] = i;
                temp++;
                k = temp;
            }
        }
        if (temp == 0) {
            byte[] tem = new byte[2];
            tem[0] = 'N';
            tem[1] = 'O';
            System.out.println("No Doctors... closing connection");
            state = false;
            try {
                os.write(tem);
            } catch (IOException e) {
                System.out.println("Error wrinting No");
            }
        } else {
            byte[] tem = new byte[2];
            char c = (char) ((temp % 10));
            tem[0] = (byte) c;
            temp = temp / 10;
            c = (char) ((temp % 10));
            tem[1] = (byte) c;

            packet[0] = 'D';
            packet[1] = tem[0];
            packet[2] = tem[1];

            try {
                os.write(packet);
            } catch (IOException e) {
                System.out.println("Writing failed");
            }
            for (int i = 0; i < 10; i++) {

                c = (char) ((values[i] % 10));
                tem[0] = (byte) c;
                values[i] = values[i] / 10;
                c = (char) ((temp % 10));
                tem[1] = (byte) c;
                packet[0] = 'N';
                packet[1] = tem[0];
                packet[2] = tem[1];
                try {

                    if (ServerApplication.status[i][1]) {
                        os.write(packet);
//                        System.out.println("testinggggggggggg: "+i);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(CThread.class.getName()).log(Level.SEVERE, null, ex);
                }
//                    System.out.println("Writing doctor details");
            }
        }

    }

    Thread reader = new Thread() {
        Date date = new Date();
        String filename = dateFormat.format(date);
        File file = new File(filename + ".wav");
        File playingfile;

        int length;
        byte buffer1[] = new byte[1000];
        OutputStream fos;

        public void run() {

            try {
                System.out.println("trying to read ");
                if (!file.exists()) {
                    file.createNewFile();
                    fos = new FileOutputStream(file);
                    System.out.println("New File created");

                }
//              
                boolean flag1 = true;
                while ((length = is.read(buffer1)) > 0 && state) {
                    System.out.println("Reading data");
                    if (length == 3) {
                        if (buffer1[0] == 'D') {
                            int i;
                            char ch = (char) buffer1[1];
                            i = Character.getNumericValue(ch);
                            i *= 10;
                            ch = (char) buffer1[2];
                            i = i + Character.getNumericValue(ch);
                            i--;
                            os = ServerApplication.Eos[i];
                            System.out.println("Assigned thread to  dr: " + i);//////////////////////////////////////////////////////////fileeeeeeeeeeeeee
                            playingfile = new File("/home/ubuntu/web/epionex_master/public/STETH/" + Integer.toString(i) + ".wav");
//                            playingfile = new File("/home/jideesh/Desktop/Mobilexion/Portal/epionexportal/public/STETH/"+Integer.toString(i)+".wav");
                            if (!playingfile.exists()) {
                                playingfile.createNewFile();
                            }
                            FileWriter writer = new FileWriter(playingfile);
                            writer.write("");
                            playingfos = new FileOutputStream(playingfile);
                        }

                    } else if (length == 26 && buffer1[0] == 'M' && buffer1[1] == 'I') {
                        //Get episode mongo ID
                        String mongoID;
                        char[] arr = new char[24];
                        for (int i = 0; i < 24; i++) {
                            arr[i] = (char) buffer1[i + 2];
                        }
                        mongoID = new String(arr);
                        String res = sendPost(mongoID);

                        JSONObject jObject = new JSONObject(res);
                        hospName = jObject.getString("HospitalName");
                        consultantName = jObject.getString("ConsultantName");
                        patientName = jObject.getString("PatientName");
                        EpisodeID = jObject.getString("mongoId");

                        dir = ("/home/ubuntu/web/epionex_master/public/STETH/recordings");
//                        dir = ("/home/jideesh/Desktop/Mobilexion/Portal/epionexportal/public/STETH/recordings");
                        System.out.println(dir);
                        File ausfile = new File(dir + "/" + hospName);
                        if (!ausfile.exists()) {
                            if (ausfile.mkdir()) {
                                File a = new File(dir + "/" + hospName + "/" + consultantName);
                                if (!a.exists()) {
                                    if (a.mkdir()) {
//                                        System.out.println("success");
                                        File b = new File(dir + "/" + hospName + "/" + consultantName + "/" + patientName);
                                        if (!b.exists()) {
                                            if (b.mkdir()) {
                                                System.out.println("success");
                                            } else {
                                                System.out.println("Error");
                                            }
                                        } else {
                                            System.out.println("already exists folder");
                                        }
                                    } else {

                                    }
                                } else {
                                    File b = new File(dir + "/" + hospName + "/" + consultantName + "/" + patientName);
                                    if (!b.exists()) {
                                        if (b.mkdir()) {
                                            System.out.println("success");
                                        } else {
                                            System.out.println("Error");
                                        }
                                    } else {
                                        System.out.println("Already folder exists");
                                    }
                                }
                            } else {
                                System.out.println("Error in creating file");
                            }
                        } else {
                            System.out.println("else loop 1");
                            File a = new File(dir + "/" + hospName + "/" + consultantName);
                            if (!a.exists()) {
                                if (a.mkdir()) {
//                                        System.out.println("success");
                                    File b = new File(dir + "/" + hospName + "/" + consultantName + "/" + patientName);
                                    if (!b.exists()) {
                                        if (b.mkdir()) {
                                            System.out.println("success");
                                        } else {
                                            System.out.println("Error");
                                        }
                                    } else {
                                        System.out.println("file already exists");
                                    }
                                }
                            } else {
                                System.out.println("testingggg");
                                File b = new File(dir + "/" + hospName + "/" + consultantName + "/" + patientName);
                                if (!b.exists()) {
                                    if (b.mkdir()) {
                                        System.out.println("success");
                                    } else {
                                        System.out.println("Error");
                                    }
                                } else {
                                    System.out.println("file already exists");
                                }
                            }
                        }
                        byte[] ok = new byte[2];
                        ok[0] = 'O';
                        ok[1] = 'K';

                        ServerApplication.Cos[threadnum].write(ok);

                    } else {
                        //file delay issue cleared
//                        if(!flag1)

                        if (flag1) {
                            addWavHeader1((8000 * 60 * 20) + 44, 2, 4000, 64000, 8000 * 60 * 20, buffer1, playingfile);
                            flag1 = false;
                        }
                        try {
                            playingfos.write(buffer1, 0, length);
                            os.write(buffer1, 0, length);
//                            System.out.println("Writing success..............");
                            fos.write(buffer1, 0, length);
                        } catch (IOException e) {
                            System.out.println("Writing error" + e);
                        }

                    }
                }

                ServerApplication.status[threadnum][0] = false;
                fos.close();
                file = convertULawFileToWav(file);
                filesize = file.length();

                filename = dateFormat.format(date);
                file.renameTo(new File(dir + "/" + hospName + "/" + consultantName + "/" + patientName + "/" + filename + ".wav"));
                file.delete();
                pathtoFile = dir + "/" + hospName + "/" + consultantName + "/" + patientName + "/" + filename + ".wav";
                Thread t = new Thread() {
                    public void run() {
                        try {
                            String str = UpdateAusFile(EpisodeID, pathtoFile, filename, filesize);
                        } catch (UnsupportedEncodingException ex) {
                            Logger.getLogger(CThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                t.start();

                System.out.println("File closed");
                System.out.println("CThread Connection Closed " + threadnum);
            } catch (IOException e) {
                ServerApplication.status[threadnum][0] = false;
                System.out.println("CThread Connecton closed " + threadnum);
            }
        }

        private File convertULawFileToWav(File filename) {
            File file = filename;
            if (!file.exists()) {
                return file;
            }
            try {
                long fileSize = file.length();
                int frameSize = 2;
                float s = (float) 4000.0;
                long numFrames = fileSize / frameSize;
                AudioFormat audioFormat = new AudioFormat((float) 4000, 16, 1, true, false);
                AudioInputStream audioInputStream = new AudioInputStream(new FileInputStream(file), audioFormat, numFrames);
                File hh = new File("file.wav");
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, hh);
                return hh;
            } catch (IOException e) {
                e.printStackTrace();
                return file;
            }
        }
    };

    String sendPost(String mongoId) throws UnsupportedEncodingException {
        String str = "";
        HttpPost post = new HttpPost("https://epionex.com/steth/get-full-details");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("mongoId", mongoId));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            CloseableHttpResponse response = httpClient.execute(post);
            System.out.println(str = EntityUtils.toString(response.getEntity()));
//            str = EntityUtils.toString(response.getEntity());

        } catch (IOException | ParseException e) {
            System.out.println("Error in post");
            return str;
        }
        return str;
    }

    String UpdateAusFile(String mongoId, String path, String endtime, double length) throws UnsupportedEncodingException {
//        path = path.trim("/home/ubuntu/web/epionex_master/public/STETH/recordings");
        String str1 = "/home/ubuntu/web/epionex_master/public/STETH/recordings/";

        path = path.substring(str1.length());
        if (mongoId.contains("Error")) {
            return ("");
        } else {
            String str = " ";
            HttpPost post = new HttpPost("https://epionex.com/steth/upload-auscultation-file");
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("mongoId", mongoId));
            urlParameters.add(new BasicNameValuePair("filePath", path));
            urlParameters.add(new BasicNameValuePair("endTime", endtime));
            urlParameters.add(new BasicNameValuePair("Length", Double.toString(length)));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            try {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(post);
                System.out.println(str = EntityUtils.toString(response.getEntity()));
            } catch (IOException | ParseException e) {
                System.out.println("Error in post");
                return str;
            }
            return str;
        }
    }

    private File addWavHeader1(long totalDataLen, int channels, long longSampleRate, long byteRate, long totalAudioLen, byte[] buffer, File f) {

        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) (0x01);
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (0x40);
        header[29] = (byte) (0x1f);
        header[30] = (byte) (0x20);
        header[31] = (byte) (0x20);
        header[32] = (byte) (0x02);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        try {

            if (f.exists()) {

                playingfos.write(header);

            }

        } catch (IOException e) {
            System.out.println("Header creation error");

        }

        return f;
    }
}
