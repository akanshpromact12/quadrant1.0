package com.gametime.quadrant;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by Akansh on 01-05-2018.
 */

public class SendReceiveFromNetwork {
    private String messageToSend = "";
    private String returnMsg = "";
    private static final String TAG = SendReceiveFromNetwork.class.getName();

    public SendReceiveFromNetwork(String messageToSend) {
        this.messageToSend = messageToSend;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public String startMyTask() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new SendAndReceiveFromNetworkCall().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new SendAndReceiveFromNetworkCall().execute();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return returnMsg;
    }

    public class SendAndReceiveFromNetworkCall extends AsyncTask<Void, Void, String> {
        String msg = "";

        @Override
        protected String doInBackground(Void... strings) {
            /*try {
                Socket socket;
                if (SocketHandler.getSocket() == null) {
                    InetAddress host = InetAddress.getByName("10.1.81.144");
                    int port = 2004;
                    socket = new Socket(host, port);
                    SocketHandler.setSocket(socket);
                } else {
                    socket = SocketHandler.getSocket();
                    if (socket != null) {
                        Log.d(TAG, "The socket is configured properly....");
                    } else {
                        Log.d(TAG, "There's some problem with the socket...");
                    }
                }
                //----------------Send Message----------------
                OutputStream os = socket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);

                bw.write(messageToSend);
                bw.flush();
                Log.d(TAG, "Message sent to server: " + messageToSend);

                //----------------Receive Message----------------
                //Get the return message from the server
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
            *//*String message = br.readLine();
            System.out.println("Message received from the server : " +message);*//*
                    // else, add to buffer
                    sb.append(c);
                    System.out.println("Inside while loop 2....");
                    System.out.println("Message received from the server : " + sb);
                    if (br.read() < 0) {
                        break;
                    }
                }
                //System.out.println("Message received from the server : " +sb.toString());
                os.close();
                osw.close();
                bw.close();
                is.close();
                isr.close();
                br.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }*/

            return msg;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            returnMsg = s;
        }
    }
}
