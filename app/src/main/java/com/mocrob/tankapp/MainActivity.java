package com.mocrob.tankapp;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;
import android.view.Menu;
import android.view.MenuItem;
import com.mocrob.tankapp.VerticalSeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.*;
import android.os.AsyncTask;
import java.io.IOException;
import java.util.UUID;
import java.io.InputStream;
import java.io.OutputStream;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

import static java.sql.DriverManager.println;

public class MainActivity extends AppCompatActivity {

    VerticalSeekBar V1,V2,V3;

    Button LeftB, RightB, FireB;

    final int ArduinoData = 1;
    private static final int REQUEST_ENABLE_BT = 0;
    private ConnectedThred MyThred = null;
    private ConnectedThred MyThred1 = null;
    Handler h;

    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceConnect.EXTRA_ADDRESS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        V1 = (VerticalSeekBar) findViewById(R.id.LeftVerticalSeekBar);
        V2 = (VerticalSeekBar) findViewById(R.id.RightVerticalSeekBar);
        V3 = (VerticalSeekBar) findViewById(R.id.NoseVerticalSeekBar);
        V1.setProgress(130);
        V2.setProgress(130);
        V3.setProgress(130);

        FireB = findViewById(R.id.button);
        LeftB = findViewById(R.id.button2);
        RightB = findViewById(R.id.button3);

        new ConnectBT().execute();
        VerticalSeekBar.OnSeekBarChangeListener SbCL = new VerticalSeekBar.OnSeekBarChangeListener()
        {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {

                // btSocket.getOutputStream().write(("A:".toString()+String.valueOf(progress)).getBytes());
                MyThred.sendData("A".toString()+String.valueOf(progress)+"|".toString());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //MyThred.sendData("A:".toString()+String.valueOf(progress));

            }
        };

        VerticalSeekBar.OnSeekBarChangeListener SbCL1 = new VerticalSeekBar.OnSeekBarChangeListener()
        {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser==true)
                {
                    /*try {
                        btSocket.getOutputStream().write(("B:".toString() + String.valueOf(progress)+"|".toString()).getBytes());
                    }
                    catch (IOException e)
                    {
                        Log.e("MA","Err");
                    }*/
                    MyThred1.sendData("B".toString()+String.valueOf(progress)+"|".toString());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        VerticalSeekBar.OnSeekBarChangeListener SbCL2 = new VerticalSeekBar.OnSeekBarChangeListener()
        {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser==true)
                {
                    MyThred1.sendData("U".toString()+String.valueOf(progress)+"|".toString());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        V1.setOnSeekBarChangeListener(SbCL);
        V2.setOnSeekBarChangeListener(SbCL1);
        V3.setOnSeekBarChangeListener(SbCL2);

        FireB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    MyThred1.sendData("F1|".toString());
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    MyThred1.sendData("F0|".toString());
                }
                return false;
            }

        });

        LeftB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    MyThred1.sendData("R1|".toString());
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    MyThred1.sendData("R0|".toString());
                }
                return false;
            }

        });

        RightB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    MyThred1.sendData("R-1|".toString());
                }
                else if (event.getAction() == MotionEvent.ACTION_UP)
                {
                    MyThred1.sendData("R0|".toString());
                }
                return false;
            }

        });
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }

        private void msg(String s)
        {
            Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MyThred = new ConnectedThred(btSocket);
        MyThred.start();
        MyThred1 = new ConnectedThred(btSocket);
        MyThred1.start();
    }
    public void onPause() {
        super.onPause();

    }
    /*
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();

            //noinspection SimplifiableIfStatement
            if (id == R.id.action_settings) {
                return true;
            }

            return super.onOptionsItemSelected(item);
        }*/
    private class ConnectedThred extends Thread
    {
        private final BluetoothSocket copyBtSocket;
        private final OutputStream OutStrem;

        public ConnectedThred(BluetoothSocket socket){
            copyBtSocket = socket;
            OutputStream tmpOut = null;

            try{
                tmpOut = socket.getOutputStream();

            } catch (IOException e){}

            OutStrem = tmpOut;
            //InStrem = tmpIn;
        }

        public void sendData(String message) {
            byte[] msgBuffer = message.getBytes();
            //Log.d("LA", "***Отправляем данные: " + message + "***"  );

            try {
                OutStrem.write(msgBuffer);
            } catch (IOException e) {}
        }

        public void cancel(){
            try {
                copyBtSocket.close();
            }catch(IOException e){}
        }

        public Object status_OutStrem(){
            if (OutStrem == null){return null;
            }else{return OutStrem;}
        }
    }
}





