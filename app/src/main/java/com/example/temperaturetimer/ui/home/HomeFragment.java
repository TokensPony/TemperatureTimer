package com.example.temperaturetimer.ui.home;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.temperaturetimer.R;
import com.example.temperaturetimer.BT.*;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static com.example.temperaturetimer.R.*;

public class HomeFragment extends Fragment {

    private final static int REQUEST_ENABLE_BT = 1;

    public BluetoothAdapter mBluetoothAdapter;

    private Bundle savedState = null;
    private HomeViewModel homeViewModel;
    private RadioGroup tempButtons;
    private RadioButton fahrButton;
    private RadioButton celButton;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("onCreateTest", "The onCreate worked!");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        //THis is a temporary stupid thing
        BluetoothDevice mDevice = null;

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.i("Bluetooth Test","Found: " + deviceName + ", " + deviceHardwareAddress);
                if(deviceName.compareTo("BBC micro:bit") == 0){
                    Log.i("Bluetooth Test", "Found Micro Bit");
                    mDevice = device;
                }
            }
        }

        //ConnectThread(last);

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(layout.fragment_home, container, false);
        //final TextView textView = root.findViewById(R.id.text_home);
        tempButtons = (RadioGroup) root.findViewById(id.tempButtons);
        fahrButton = (RadioButton) root.findViewById(id.fahrTemp);
        celButton = (RadioButton) root.findViewById(id.celTemp);

        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                //textView.setText(s);
            }
        });

        tempButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case id.fahrTemp:
                        Log.i("fahr", "Fahrenheit Selected");
                        Toast.makeText(getActivity(), "Fahrenheit Selected",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case id.celTemp:
                        Log.i("cel", "Celcius selected");
                        Toast.makeText(getActivity(), "Celcius Selected",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        return root;
    }

}



//private class ConnectThread extends Thread {
//    private final BluetoothSocket mmSocket;
//    private final BluetoothDevice mmDevice;
//    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
//    public ConnectThread(BluetoothDevice device) {
//        BluetoothSocket tmp = null;
//        mmDevice = device;
//        try {
//            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
//        } catch (IOException e) { }
//        mmSocket = tmp;
//    }
//    public void run() {
//        mBluetoothAdapter.cancelDiscovery();
//        try {
//            mmSocket.connect();
//        } catch (IOException connectException) {
//            try {
//                mmSocket.close();
//            } catch (IOException closeException) { }
//            return;
//        }
//    }
//    public void cancel() {
//        try {
//            mmSocket.close();
//        } catch (IOException e) { }
//    }