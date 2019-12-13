package com.example.mochimochila;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Looper;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivilegedAction;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef1;
    DatabaseReference myRef2;
    DatabaseReference myRef3;
    DatabaseReference myRef4;
    DatabaseReference index = database.getReference("valor");
    Button btnOn, btnOff;
    TextView txtArduino, txtString, tv_humedad;
    TextView tv_temperatura, tex1, tex2;
    ImageView imagen1, imagen2;
    Handler bluetoothIn;
    private FusedLocationProviderClient fusedLocationClient;
    final int handlerState = 0;             //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    int valor=0;
    String latitud, longitud;
    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;
    private static final String TAG = "MyActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        tex1=(TextView)findViewById(R.id.text1);
        tex2=(TextView)findViewById(R.id.text2);
        //Link the buttons and textViews to respective views
        btnOn = (Button) findViewById(R.id.buttonOn);
        txtString = (TextView) findViewById(R.id.txtString);
        tv_temperatura=(TextView) findViewById(R.id.tv_temperatura);
        tv_humedad= (TextView) findViewById(R.id.tv_humedad);
        imagen1 = (ImageView) findViewById(R.id.image1);
        imagen2 = (ImageView) findViewById(R.id.image2);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        database.getReference("valor").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "Valor cambiado");
                String appTitle = (dataSnapshot.getValue()).toString();
                valor = Integer.valueOf(appTitle);
                txtString.setText(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {          //if message is what we want

                    String readMessage = (String) msg.obj;     // msg.arg1 = bytes from connect thread

                    recDataString.append(readMessage);//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");
                    // determine the end-of-line
                    if (endOfLineIndex >= 0) {
                        // make sure there data before ~
                        String dataInPrint = recDataString.toString();    // extract string
                        System.out.println("Tamano "+dataInPrint.length()+"\n\nCadena "+dataInPrint);
                        try{


                            String temp=dataInPrint.substring(1,6), hum=dataInPrint.substring(7,12),
                                    pri=dataInPrint.substring(13,14), seg=dataInPrint.substring(15,16);
                            System.out.println("temp "+temp+"\nhum "+hum+"\npri "+pri+"\nseg "+seg);
                            if(pri.equals("1")){
                                tex1.setText("El compartimiento 1 esta ocupado");
                                imagen1.setImageResource(R.drawable.ocupado);
                            }else{
                                tex1.setText("El compartimiento 1 esta libre");
                                imagen1.setImageResource(R.drawable.libre);
                            }

                            if(seg.equals("1")){
                                tex2.setText("El compartimiento 2 esta ocupado");
                                imagen2.setImageResource(R.drawable.ocupado);
                            }else{
                                tex2.setText("El compartimiento 2 esta libre");
                                imagen2.setImageResource(R.drawable.libre);
                            }
                            Log.i(TAG, dataInPrint);
                            if(!temp.contains("+")){
                                myRef1 = database.getReference().child("temperatura").child("temperatura"+valor);
                                //myRef1 = database.getReference("temperatura"+valor);
                                myRef1.setValue(temp);
                                tv_temperatura.setText(temp);
                            }else{
                                myRef1 = database.getReference().child("temperatura").child("temperatura"+valor);
                                //myRef1 = database.getReference("temperatura"+valor);
                                myRef1.setValue("0");
                            }
                            if(!hum.contains("+")){
                                myRef2 = database.getReference().child("humedad").child("humedad"+valor);
                                //myRef2 = database.getReference("humedad"+valor);
                                myRef2.setValue(hum);
                                tv_humedad.setText(hum);
                            }else{
                                myRef2 = database.getReference().child("humedad").child("humedad"+valor);
                                //myRef2 = database.getReference("humedad"+valor);
                                myRef2.setValue("0");
                            }
                            if(!pri.contains("+")){
                                myRef3 = database.getReference().child("posprimero").child("pri"+valor);
                                //myRef3 = database.getReference("pospri"+valor);
                                myRef3.setValue(pri);
                            }else{
                                myRef3 = database.getReference().child("posprimero").child("pri"+valor);
                                //myRef3 = database.getReference("pospri"+valor);
                                myRef3.setValue("0");
                            }
                            if(!seg.contains("+")){
                                myRef4 = database.getReference().child("possegundo").child("seg"+valor);
                                //myRef4 = database.getReference("posseg"+valor);
                                //myRef4.setValue(seg);
                            }else{
                                myRef4 = database.getReference().child("possegundo").child("seg"+valor);
                                //myRef4 = database.getReference("posseg"+valor);
                                //myRef4.setValue("0");
                            }
                            fusedLocationClient.getLastLocation()
                                    .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            double x=location.getLatitude();
                                            double y=location.getLongitude();
                                            myRef3.setValue(x);
                                            myRef4.setValue(y);

                                            System.out.println("x: "+x+"    y:"+y);
                                            if (location != null) {
                                                // Logic to handle location object
                                            }
                                        }
                                    });
                            index.setValue((valor+1));
                        }catch (Exception e){

                        }

                        //txtString.setText("Datos recibidos = " + myRef.getRef().child("message"));
                        int dataLength = dataInPrint.length();       //get length of data received
                        //txtStringLength.setText("Tamaño del String = " + String.valueOf(dataLength));

                        if (recDataString.charAt(0) == '#')        //if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = recDataString.substring(1, 5);
                        }
                        recDataString.delete(0, recDataString.length());      //clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();




        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("1");    // Send "1" via Bluetooth
                Toast.makeText(getBaseContext(), "Encender el LED", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitud=mLastLocation.getLatitude()+"";
            longitud=mLastLocation.getLongitude()+"";
        }
    };
}