package com.creative.wififiletransfer.receiver;

/**
 * Created by md.jubayer on 28/04/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

import com.creative.wififiletransfer.MainActivity;
import com.creative.wififiletransfer.R;
import com.creative.wififiletransfer.fragment.DeviceListFragment;
import com.creative.wififiletransfer.utils.MakeConnection;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private Channel channel;
    private MainActivity activity;

    /**
     * @param manager  WifiP2pManager system service
     * @param channel  Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       MainActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {

                if (activity.USER_TYPE.equals(activity.SENDER)) {
                    Log.d("DEBUG","SENDER DISCOVERY NEED LIST");
                    manager.requestPeers(channel, (PeerListListener) activity.getSupportFragmentManager()
                            .findFragmentByTag("DeviceListFragment"));
                } else {
                    Log.d("DEBUG","RECEIVER DISCOVERY NEED LIST");
                }

            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                MakeConnection makeConnection = new MakeConnection(activity);
                //DeviceListFragment fragment = (DeviceListFragment) activity.getSupportFragmentManager()
                //        .findFragmentByTag("DeviceListFragment");
                  manager.requestConnectionInfo(channel, makeConnection);
            } else {
                // It's a disconnect
                activity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
            //         .findFragmentById(R.id.frag_list);
            // fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
            //         WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}