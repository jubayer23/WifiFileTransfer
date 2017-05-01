package com.creative.wififiletransfer.service;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.creative.wififiletransfer.model.WiFiTransferModal;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class WiFiClientIPTransferService extends IntentService {


    public WiFiClientIPTransferService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }


    public WiFiClientIPTransferService() {
        super("WiFiClientIPTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();
        if (intent.getAction().equals(FileTransferService.ACTION_SEND_FILE)) {

            Log.d("DEBUG","Ip sending in process");
            String host = intent.getExtras().getString(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS);
            String InetAddress = intent.getExtras().getString(FileTransferService.inetaddress);

            Socket socket = new Socket();
            int port = intent.getExtras().getInt(FileTransferService.EXTRAS_GROUP_OWNER_PORT);

            try {

                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), FileTransferService.SOCKET_TIMEOUT);
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
               /*
                * Object that is used to send file name with extension and recieved on other side.
                */
                ObjectOutputStream oos = new ObjectOutputStream(stream);
                WiFiTransferModal transObj = new WiFiTransferModal(InetAddress);

                oos.writeObject(transObj);
                System.out.println("Sending request to Socket Server");

                oos.close();    //close the ObjectOutputStream after sending data.
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("DEBUG",String.valueOf(e.getCause()));
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (Exception e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

}
