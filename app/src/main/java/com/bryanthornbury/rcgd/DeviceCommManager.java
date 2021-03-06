package com.bryanthornbury.rcgd;

import android.content.Context;
import android.util.Log;

import com.bryanthornbury.rcgd.bluetooth.BluetoothSerialService;
import com.bryanthornbury.rcgd.bluetooth.SerialDataStream;

import java.io.IOException;

/**
 * The Device Comm manager is responsible for initiating,
 * coordinating, and maintaining bluetooth communications
 * with the vehicle.
 * Created by mozoby on 4/4/15.
 */
public class DeviceCommManager implements IManager {

    public static final String TAG = "DeviceCommManager";

    private Context context;
    private static String address = "98:4F:EE:03:83:21";

    private BluetoothSerialService mBt = null;
    private SerialDataStream mDataStream = null;

    public DeviceCommManager(Context c){
        this.context = c;
        init();
        sendTestData();
    }

    public void init() {
        mBt = new BluetoothSerialService(context);
        if(mBt.initialize()){
            mDataStream=mBt.connectToMAC(address);
        }else{

            Log.d(TAG, "AHHHHHHHHH Error initializing Bluetooth");
        }
    }

    public void sendTestData(){
//        int i = 0;
//        while(i++ < 10){
//            this.writeData(XboxControllerInput.A, 1111);
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }


        try {
            this.mDataStream.mOutput.write(new byte[] {115,105,109,97,108});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeData(XboxControllerInput input, int value){
        if(this.mBt.isInitialized()){
            byte[] b = new byte[10];
            b[0] = (byte) 0x02; //start of text
            b[9] = (byte) 0x03; //end of text

            //first four bytes are the key, which is only one byte long
            b[1] = 0;
            b[2] = 0;
            b[3] = 0;
            b[4] = (byte)input.getKey();

            //Second four bytes are the value
            b[5] = (byte) ((0xFF000000 & value) >> 12 );
            b[6] = (byte) ((0x00FF0000 & value) >> 8 );
            b[7] = (byte) ((0x0000FF00 & value) >> 4 );
            b[8] = (byte) ((0x000000FF & value));



            try {
                this.mDataStream.mOutput.write(b);
            } catch (IOException e) {
                Log.e("Write Data", "Failed to Write Data", e);
            }
        }else{
            Log.d(TAG, "Unable to write data, not initialized.");
        }
    }

    @Override
    public void pause() {
        try {
            mBt.closeSocket();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void resume() {
        if(!mBt.isInitialized()){
            mDataStream=mBt.connectToMAC(address);
        }else{
            mBt.initialize();
            Log.d(TAG,"Error: Bluetooth not initialized");
        }
    }

    @Override
    public void dispose() {
        //nothing :)
    }
}
