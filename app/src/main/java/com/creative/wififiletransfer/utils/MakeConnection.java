package com.creative.wififiletransfer.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.creative.wififiletransfer.MainActivity;
import com.creative.wififiletransfer.model.WiFiTransferModal;
import com.creative.wififiletransfer.service.FileTransferService;
import com.creative.wififiletransfer.service.WiFiClientIPTransferService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by comsol on 01-May-17.
 */
public class MakeConnection implements WifiP2pManager.ConnectionInfoListener {

    private MainActivity context;
    private WifiP2pInfo info;
    private static String group_owner_address = null;
    private static boolean isThisDeviceActAsAserver = false;
    private static ProgressDialog mProgressDialog;

    public static String WiFiClientIp = "";
    public static String WiFiServerIp = "";

    static long ActualFilelength = 0;
    static int Percentage = 0;

    static Boolean ClientCheck = false;

    private static final String selectedfilePath = "/storage/emulated/0/DCIM/Camera/20170426_200820.jpg";

    public MakeConnection(MainActivity context) {
        this.context = context;

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {

        this.info = wifiP2pInfo;
        // The owner IP is now known.
        //context.hideProgressBar();

        String GroupOwner = info.groupOwnerAddress.getHostAddress();
        if (GroupOwner != null && !GroupOwner.equals("")) {
            group_owner_address = GroupOwner;
        }

        if (info.groupFormed && info.isGroupOwner) {

            Log.d("DEBUG", "I am Server");
            /*
        	 * set shaerdprefrence which remember that device is server.
        	 */
            isThisDeviceActAsAserver = true;

            /*new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();*/
            FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
                    context, FileTransferService.PORT);
            if (FileServerobj != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    FileServerobj.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            new String[]{null});
                    // FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,Void);
                } else
                    FileServerobj.execute();
            }
        } else {
            // The other device acts as the client. In this case, we enable the
            // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
            Log.d("DEBUG", "I am Client");

            if (!ClientCheck) {
                firstConnectionMessage firstObj = new firstConnectionMessage();
                if (firstObj != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        firstObj.executeOnExecutor(
                                AsyncTask.THREAD_POOL_EXECUTOR,
                                new String[]{null});
                    } else
                        firstObj.execute();
                }
            }

            FileServerAsyncTask FileServerobj = new FileServerAsyncTask(
                    context, FileTransferService.PORT);
            if (FileServerobj != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    FileServerobj.executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            new String[]{null});
                } else
                    FileServerobj.execute();

            }

        }
    }


    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    static Handler handler;

    public  class FileServerAsyncTask extends AsyncTask<String, String, String> {

        //        private TextView statusText;
        private Context mFilecontext;
        private String Extension, Key;
        private File EncryptedFile;
        private long ReceivedFileLength;
        private int PORT;
        public  String FolderName = "JUBAYER";

        public FileServerAsyncTask(Context context, int port) {
            this.mFilecontext = context;
//            this.statusText = (TextView) statusText;
            handler = new Handler();
            this.PORT = port;
//			myTask = new FileServerAsyncTask();
            if (mProgressDialog == null)
                mProgressDialog = new ProgressDialog(mFilecontext,
                        ProgressDialog.THEME_HOLO_LIGHT);
        }


        @Override
        protected String doInBackground(String... params) {
            try {
                // init handler for progressdialog
                ServerSocket serverSocket = new ServerSocket(PORT);
                Log.d("DEBUG_CLIENT_PRE", WiFiClientIp);

                Socket client = serverSocket.accept();
                //the below code will not execute untill any response is come from client


                WiFiClientIp = client.getInetAddress().getHostAddress();

                Log.d("DEBUG_CLIENT_IP", WiFiClientIp);

                ObjectInputStream ois = new ObjectInputStream(
                        client.getInputStream());
                WiFiTransferModal obj = null;
                // obj = (WiFiTransferModal) ois.readObject();
                String InetAddress;
                try {
                    obj = (WiFiTransferModal) ois.readObject();
                    InetAddress = obj.getInetAddress();
                    if (InetAddress != null
                            && InetAddress
                            .equalsIgnoreCase(FileTransferService.inetaddress)) {
                        //SharedPreferencesHandler.setStringValues(mFilecontext,
                        //         "WiFiClientIp", WiFiClientIp);
                        //set boolean true which identifiy that this device will act as server.
                        // SharedPreferencesHandler.setStringValues(mFilecontext,
                        //        "ServerBoolean", "true");

                        isThisDeviceActAsAserver = true;
                        ois.close(); // close the ObjectOutputStream object
                        // after saving
                        serverSocket.close();

                        return "Demo";
                    }
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                final Runnable r = new Runnable() {

                    public void run() {
                        // TODO Auto-generated method stub
                        mProgressDialog.setMessage("Receiving...");
                        mProgressDialog.setIndeterminate(false);
                        mProgressDialog.setMax(100);
                        mProgressDialog.setProgress(0);
                        mProgressDialog.setProgressNumberFormat(null);
//						mProgressDialog.setCancelable(false);
                        mProgressDialog
                                .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        mProgressDialog.show();
                    }
                };
                handler.post(r);

                final File f = new File(
                        Environment.getExternalStorageDirectory() + "/"
                                + FolderName + "/"
                                + obj.getFileName());

                File dirs = new File(f.getParent());
                if (!dirs.exists())
                    dirs.mkdirs();
                f.createNewFile();

				/*
				 * Recieve file length and copy after it
				 */
                this.ReceivedFileLength = obj.getFileLength();

                InputStream inputstream = client.getInputStream();


                copyReceivingdFile(inputstream, new FileOutputStream(f),
                        ReceivedFileLength);
                ois.close(); // close the ObjectOutputStream object after saving
                // file to storage.
                serverSocket.close();

				/*
				 * Set file related data and decrypt file in postExecute.
				 */
                this.Extension = obj.getFileName();
                this.EncryptedFile = f;

                return f.getAbsolutePath();
            } catch (IOException e) {
                //Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (!result.equalsIgnoreCase("Demo")) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                    mFilecontext.startActivity(intent);
                } else {
            		/*
					 * To initiate socket again we are intiating async task
					 * in this condition.
					 */


                    FileServerAsyncTask FileServerobj = new
                            FileServerAsyncTask(mFilecontext, FileTransferService.PORT);
                    if (FileServerobj != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            FileServerobj.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{null});

                        } else FileServerobj.execute();

                    }


                    startSendingFile();
                }
//                statusText.setText("File copied - " + result);

            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mFilecontext);
            }
        }

    }


    private  void startSendingFile() {

        String Extension = "";
        String host = null;
        int sub_port = -1;

        if (selectedfilePath != null) {
            File f = new File(selectedfilePath);
            System.out.println("file name is   ::" + f.getName());
            Long FileLength = f.length();
            ActualFilelength = FileLength;
            try {
                Extension = f.getName();
                Log.e("Name of File-> ", "" + Extension);
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            //CommonMethods.e("", "path is null");
            return;
        }

        Intent serviceIntent = new Intent(context, FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, "content://media/external/images/media/28263\n" +
                "                                                               \n" +
                "                                                               [ 05-01 22:45:10.958 29653:29653 E/         ]\n" +
                "                                                               inside the check -- >");




        //-----------------------------
        if (WiFiClientIp != null && !WiFiClientIp.equals("")) {
            // Get Client Ip Address and send data
            host = WiFiClientIp;
            sub_port = FileTransferService.PORT;
            serviceIntent
                    .putExtra(
                            FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                            WiFiClientIp);
//    							serviceIntent
//    									.putExtra(
//    											WiFiFileTransferService.EXTRAS_GROUP_OWNER_PORT1,
//    											WiFiFileTransferService.CLIENTPORT);

        }

        serviceIntent.putExtra(FileTransferService.Extension, Extension);

        serviceIntent.putExtra(FileTransferService.Filelength,
                ActualFilelength + "");
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, FileTransferService.PORT);
        if (host != null && sub_port != -1) {
            showprogress("Sending...");
            context.startService(serviceIntent);
        } else {
            DismissProgressDialog();
        }

    }

    public static boolean copySendingFile(InputStream inputStream, OutputStream out) {
        long total = 0;
        long test = 0;
        byte buf[] = new byte[FileTransferService.ByteSize];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
                try {
                    total += len;
                    if (ActualFilelength > 0) {
                        Percentage = (int) ((total * 100) / ActualFilelength);
                    }
                    // Log.e("Percentage--->>> ", Percentage+"   FileLength" +
                    // EncryptedFilelength+"    len" + len+"");
                    mProgressDialog.setProgress(Percentage);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    Percentage = 0;
                    ActualFilelength = 0;
                }
            }
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }

            out.close();
            inputStream.close();
        } catch (IOException e) {
            //Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    public static boolean copyReceivingdFile(InputStream inputStream,
                                             OutputStream out, Long length) {

        byte buf[] = new byte[FileTransferService.ByteSize];
        byte Decryptedbuf[] = new byte[FileTransferService.ByteSize];
        String Decrypted;
        int len;
        long total = 0;
        int progresspercentage = 0;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                try {
                    out.write(buf, 0, len);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    total += len;
                    if (length > 0) {
                        progresspercentage = (int) ((total * 100) / length);
                    }
                    mProgressDialog.setProgress(progresspercentage);
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                    if (mProgressDialog != null) {
                        if (mProgressDialog.isShowing()) {
                            mProgressDialog.dismiss();
                        }
                    }
                }
            }
            // dismiss progress after sending
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            // Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }


    /*
    * Async class that has to be called when connection establish first time. Its main motive is to send blank message
    * to server so that server knows the IP address of client to send files Bi-Directional.
    */
    class firstConnectionMessage extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            Intent serviceIntent = new Intent(context,
                    WiFiClientIPTransferService.class);

            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);

            if (info.groupOwnerAddress.getHostAddress() != null) {
                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                        info.groupOwnerAddress.getHostAddress());

                serviceIntent.putExtra(
                        FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                        FileTransferService.PORT);
                serviceIntent.putExtra(FileTransferService.inetaddress,
                        FileTransferService.inetaddress);

            }

            context.startService(serviceIntent);

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            if (result != null) {
                if (result.equalsIgnoreCase("success")) {
                    ClientCheck = true;
                }
            }

        }

    }


    public static void DismissProgressDialog() {
        try {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public void showprogress(final String task) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context,
                    ProgressDialog.THEME_HOLO_LIGHT);
        }
        Handler handle = new Handler();
        final Runnable send = new Runnable() {

            public void run() {
                // TODO Auto-generated method stub
                mProgressDialog.setMessage(task);
                // mProgressDialog.setProgressNumberFormat(null);
                // mProgressDialog.setProgressPercentFormat(null);
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setMax(100);
//				mProgressDialog.setCancelable(false);
                mProgressDialog.setProgressNumberFormat(null);
                mProgressDialog
                        .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                mProgressDialog.show();
            }
        };
        handle.post(send);
    }
}
