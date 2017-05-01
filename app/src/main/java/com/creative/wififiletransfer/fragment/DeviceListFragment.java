package com.creative.wififiletransfer.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.creative.wififiletransfer.R;
import com.creative.wififiletransfer.adapter.WiFiPeerListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by md.jubayer on 01/05/2017.
 */
public class DeviceListFragment extends Fragment implements WifiP2pManager.PeerListListener, AdapterView.OnItemClickListener, WifiP2pManager.ConnectionInfoListener {
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;
    private WiFiPeerListAdapter wiFiPeerListAdapter;
    private ListView listview;
    private WifiP2pInfo info;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, null);
        onInitiateDiscovery();
        return mContentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        wiFiPeerListAdapter = new WiFiPeerListAdapter(getActivity(), peers);
        listview = (ListView) mContentView.findViewById(R.id.list_device);
        listview.setAdapter(wiFiPeerListAdapter);
        listview.setOnItemClickListener(this);
    }


    /**
     * @return this device
     */
    public WifiP2pDevice getDevice() {
        return device;
    }

    /**
     * Initiate a connection with the peer.
     */
    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                    }
                });
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        wiFiPeerListAdapter.notifyDataSetChanged();
        if (peers.size() == 0) {
            return;
        }
    }

    public void clearPeers() {
        peers.clear();
        wiFiPeerListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        WifiP2pDevice device = peers.get(i);

        ((DeviceListFragment.DeviceActionListener) getActivity()).connect(device);
        //((DeviceActionListener) getActivity()).showDetails(device);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = wifiP2pInfo;

        Log.d("DEBUG_CONNECTION", String.valueOf(info.isGroupOwner));
        Log.d("DEBUG_CONNECTION_ADD", info.groupOwnerAddress.getHostAddress());

    }

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener {
        //void showDetails(WifiP2pDevice device);
        // void cancelDisconnect();
        void connect(WifiP2pDevice config);

        void disconnect();
    }
}