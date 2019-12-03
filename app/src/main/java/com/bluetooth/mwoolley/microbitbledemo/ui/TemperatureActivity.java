package com.bluetooth.mwoolley.microbitbledemo.ui;
/*
 * Author: Martin Woolley
 * Twitter: @bluetooth_mdw
 *
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bluetooth.mwoolley.microbitbledemo.bluetooth.BleAdapterService;
import com.bluetooth.mwoolley.microbitbledemo.bluetooth.ConnectionStatusListener;
import com.bluetooth.mwoolley.microbitbledemo.Constants;
import com.bluetooth.mwoolley.microbitbledemo.MicroBit;
import com.bluetooth.mwoolley.microbitbledemo.R;
import com.bluetooth.mwoolley.microbitbledemo.Utility;
import com.bluetooth.mwoolley.microbitbledemo.TemperatureConfig;
import com.bluetooth.mwoolley.microbitbledemo.TinyDB;

import java.util.ArrayList;

public class TemperatureActivity extends AppCompatActivity implements ConnectionStatusListener {

    private BleAdapterService bluetooth_le_adapter;

    private int exit_step = 0;
    private boolean exiting = false;

    //Is fahrenheit currently selected
    private boolean fahr = true;

    private RadioGroup tempButtons;
    private RadioButton fahrButton;
    private RadioButton celButton;

    private Spinner timeDelayOption;

    private EditText lowerTempBox;
    private EditText upperTempBox;
    private Button setTempButton;

    private Vibrator vibrator;
    private long[] mVibratePattern = new long[]{0, 400, 200, 400};

    private CountDownTimer delayTimer;
    private boolean alarmActive = false;
    private boolean timerStarted = false;

    private float lowerTemp = 65;
    private float upperTemp = 75;

    private ArrayList<TemperatureConfig> tempSettings;
    private ArrayList<String> names;
    private ArrayAdapter<String> arrayAdapter;
    private Spinner tempConfigOptions;
    private Button setTempConfig;
    private TinyDB settingsDB;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.d(Constants.TAG, "onServiceConnected");
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(mMessageHandler);

            if (bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.TEMPERATURESERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.TEMPERATURE_CHARACTERISTIC_UUID), true)) {
                showMsg(Utility.htmlColorGreen("Temperature notifications ON"));
            } else {
                showMsg(Utility.htmlColorRed("Failed to set Temperature notifications ON"));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_temperature);

        exiting = false;

        // read intent data
        final Intent intent = getIntent();
        MicroBit.getInstance().setConnection_status_listener(this);

        // connect to the Bluetooth smart service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        //Grabs buttons and other UI Elements
        tempButtons = (RadioGroup) findViewById(R.id.tempButtons);
        fahrButton = (RadioButton) findViewById(R.id.fahrTemp);
        celButton = (RadioButton) findViewById(R.id.celTemp);

        timeDelayOption = (Spinner) findViewById(R.id.timeDurations);
        timeDelayOption.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                delayTimer = setUpTimer(getTimeDuration(), 1000);
                String selectedTime = timeDelayOption.getSelectedItem().toString();
                Toast.makeText(TemperatureActivity.this, "Delay timer set for " + selectedTime + " minutes",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        lowerTempBox = (EditText) findViewById(R.id.lower_temperature_limit);
        upperTempBox = (EditText) findViewById(R.id.upper_temperature_limit);
        setTempButton = (Button) findViewById(R.id.setTempButton);
        setTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTemperature();
            }
        });

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        tempConfigOptions = (Spinner) findViewById(R.id.ConfigList);

        tempSettings = new ArrayList<TemperatureConfig>();
        names = new ArrayList<String>();


        settingsDB = new TinyDB(this);

        //Code for testing the temperature config and saving features.

        tempSettings.add(new TemperatureConfig("test1", 60, 70, 1));
        tempSettings.add(new TemperatureConfig("test2", 60, 70, 1));

        //settingsDB.putListObject("tempSettings", tempSettings);

        //tempSettings = settingsDB.getListObject("tempSettings", TemperatureConfig.class);


        //tempSettings = settingsDB.getListObject("tempSettings", TemperatureConfig.class);

        //Populate the settings spinner
        for(TemperatureConfig x: tempSettings){
            Log.i("ArrayList Test", x.configName);
            names.add(x.configName);
        }

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, names);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tempConfigOptions.setAdapter(arrayAdapter);

        delayTimer = setUpTimer(getTimeDuration(), 1000);

        //Applies listeners to the radio buttons. Allows for the temperature setting to be changed on the fly.
        tempButtons.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.fahrTemp:
                        fahr = true;
                        Log.i("fahr", "Fahrenheit Selected");
                        Toast.makeText(TemperatureActivity.this, "Fahrenheit Selected",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.celTemp:
                        fahr = false;
                        Log.i("cel", "Celcius selected");
                        Toast.makeText(TemperatureActivity.this, "Celcius Selected",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        if (vibrator.hasVibrator()) {
            vibrator.vibrate(500); // for 500 ms
        }
    }

    public void setTemperature(){
        if(!isEmpty(lowerTempBox) && !isEmpty(upperTempBox)){
            lowerTemp = Float.valueOf(lowerTempBox.getText().toString());
            upperTemp = Float.valueOf(upperTempBox.getText().toString());
            Toast.makeText(TemperatureActivity.this, "Temperature set",
                    Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(TemperatureActivity.this, "Invalid Temperatures",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }

    public CountDownTimer setUpTimer(long length, long interval){
        CountDownTimer newTimer = new CountDownTimer(length, interval) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i("Timer test", millisUntilFinished + "");
            }

            @Override
            public void onFinish() {
                alarmActive = true;
            }
        };
        return newTimer;
    }

    public long getTimeDuration(){
        return (Long.valueOf(timeDelayOption.getSelectedItem().toString()) * 60000) + 3000;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // may already have unbound. No API to check state so....
            unbindService(mServiceConnection);
        } catch (Exception e) {
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        shutdownSteps();
    }

    //Disconnects Microbit after shutdown
    private void shutdownSteps() {
        exiting=true;
        exit_step++;
        if (MicroBit.getInstance().isMicrobit_connected()) {
            switch (exit_step) {
                case 1:
                    Log.d(Constants.TAG, "Disabling Button 1 State notifications");
                    bluetooth_le_adapter.setNotificationsState(Utility.normaliseUUID(BleAdapterService.TEMPERATURESERVICE_SERVICE_UUID), Utility.normaliseUUID(BleAdapterService.TEMPERATURE_CHARACTERISTIC_UUID), false);
                    break;
                default:
                    finish();
                    try {
                        unbindService(mServiceConnection);
                    } catch (Exception e) {
                    }

            }
        } else {
            finish();
            try {
                unbindService(mServiceConnection);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_magnetometer, menu);
        return true;
    }

    // Service message handler�//////////////////
    private Handler mMessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            String descriptor_uuid = "";
            byte[] b = null;
            TextView value_text = null;

            switch (msg.what) {
                case BleAdapterService.GATT_DESCRIPTOR_WRITTEN:
                    Log.d(Constants.TAG, "Handler received descriptor written result");
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    descriptor_uuid = bundle.getString(BleAdapterService.PARCEL_DESCRIPTOR_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    Log.d(Constants.TAG, "descriptor " + descriptor_uuid + " of characteristic " + characteristic_uuid + " of service " + service_uuid.toString() + " written OK:0x" + Utility.byteArrayAsHexString(b));
                    Log.d(Constants.TAG,"exiting="+exiting);
                    if (exiting) {
                        shutdownSteps();
                    }
                    break;

                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    service_uuid = bundle.getString(BleAdapterService.PARCEL_SERVICE_UUID);
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    byte temperature = b[0];
                    float fTemperature = temperature;
                    Log.d(Constants.TAG, "Value=" + Utility.byteArrayAsHexString(b));
                    if (characteristic_uuid.equalsIgnoreCase((Utility.normaliseUUID(BleAdapterService.TEMPERATURE_CHARACTERISTIC_UUID)))) {
                        Log.d(Constants.TAG, "Temperature received: " + temperature);

                        TextView temp = (TextView) TemperatureActivity.this.findViewById(R.id.temperature);
                        if(fahr) {
                            //Convert Celecius temp to fahrenheit
                            fTemperature = (fTemperature * ((float) 9/5) + 32);
                            fTemperature = Math.round(fTemperature);
                            Log.d(Constants.TAG, "Temperature converted: " + fTemperature);
                            temp.setText(fTemperature + "°");
                        }
                        temp.setText(fTemperature + "°");
                        //Log.d("Temp Input", lowerTempBox.getText().toString());

                        //If temperature is out of range, make phone vibrate
                        if(fTemperature < lowerTemp || fTemperature > upperTemp){
                            if(!timerStarted){
                                timerStarted = true;
                                delayTimer.start();
                            }
                            if(alarmActive) {
                                Toast.makeText(TemperatureActivity.this, "Temperature Out of Range.",
                                        Toast.LENGTH_SHORT).show();
                                vibrator.vibrate(mVibratePattern, -1);
                            }
                        }else{
                            delayTimer.cancel();
                            timerStarted = false;
                            alarmActive = false;
                        }

                    }
                    break;
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(Utility.htmlColorRed(text));
            }
        }
    };

    //Displays strings of text at the top of the screen
    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) TemperatureActivity.this.findViewById(R.id.message)).setText(Html.fromHtml(msg));
            }
        });
    }

    @Override
    public void connectionStatusChanged(boolean connected) {
        if (connected) {
            showMsg(Utility.htmlColorGreen("Connected"));
        } else {
            showMsg(Utility.htmlColorRed("Disconnected"));
        }
    }

    @Override
    public void serviceDiscoveryStatusChanged(boolean new_state) {

    }
}