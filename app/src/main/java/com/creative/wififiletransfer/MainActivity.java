package com.creative.wififiletransfer;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.creative.wififiletransfer.fragment.DeviceListFragment;
import com.creative.wififiletransfer.receiver.WiFiDirectBroadcastReceiver;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,DeviceListFragment.DeviceActionListener {

    public static final String TAG = "DEBUG";
    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;

    private Button btn_send, btn_receive;

    private ProgressDialog progressDialog = null;

    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;

    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static  String USER_TYPE = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        initialWifiBroadCast();
    }

    private void init() {

        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
        btn_receive = (Button) findViewById(R.id.btn_receive);
        btn_receive.setOnClickListener(this);
    }

    private void initialWifiBroadCast() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (!isWifiP2pEnabled) {
            Toast.makeText(MainActivity.this, "Wifi Not Enable. Please Enable It",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (id == R.id.btn_send) {

            USER_TYPE = SENDER;

            Fragment deviceListFragment = new DeviceListFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, deviceListFragment, "DeviceListFragment")
                    .commit();

            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Discovery Initiated",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                            Toast.LENGTH_SHORT).show();
                }
            });

        }

        if (id == R.id.btn_receive) {


            USER_TYPE = RECEIVER;

            progressDialog = new ProgressDialog(this);
            progressDialog.setIndeterminate(false);
            progressDialog.setMessage("Receving...");
            progressDialog.show();

            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Discovery Initiated",
                            Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(int reasonCode) {
                    Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
                            Toast.LENGTH_SHORT).show();
                }
            });

        }
    }


    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
     //   Log.d("DEBUG","resetData Called");
    }
    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().findFragmentByTag("DeviceListFragment").isAdded()) {
            getSupportFragmentManager().beginTransaction().
                    remove(getSupportFragmentManager().findFragmentByTag("DeviceListFragment")).commit();
        }else{
            super.onBackPressed();
        }

    }

    @Override
    public void connect(WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(MainActivity.this, "Press back to cancel",
                "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
        );
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void disconnect() {

    }

    public void hideProgressBar(){
        progressDialog.dismiss();
    }
}
