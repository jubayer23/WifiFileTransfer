package com.creative.wififiletransfer.adapter;

/**
 * Created by md.jubayer on 01/05/2017.
 */



import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.creative.wififiletransfer.R;

import java.util.List;

/**
 * Array adapter for ListFragment that maintains WifiP2pDevice list.
 */
public class WiFiPeerListAdapter extends BaseAdapter {
    private List<WifiP2pDevice> items;
    private Context _context;
    LayoutInflater inflater;

    /**
     * @param context
     * @param objects
     */
    public WiFiPeerListAdapter(Context context, List<WifiP2pDevice> objects) {
        this.items = objects;
        this._context = context;
         inflater = (LayoutInflater) _context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null) {

            view = inflater.inflate(R.layout.row_device, null);
            viewHolder = new ViewHolder();
            viewHolder.device_name = (TextView) view
                    .findViewById(R.id.device_name);
            viewHolder.device_details = (TextView) view
                    .findViewById(R.id.device_details);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        WifiP2pDevice device = items.get(i);
        if (device != null) {

            viewHolder.device_name.setText(device.deviceName);


            viewHolder.device_details.setText(getDeviceStatus(device.status));

        }


        return view;
    }


    private static String getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    public void addMore(List<WifiP2pDevice> items) {
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    private static class ViewHolder {

        private TextView device_name;
        private TextView device_details;
        // private TextView destination;
    }
}