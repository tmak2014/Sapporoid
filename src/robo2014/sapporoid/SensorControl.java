package robo2014.sapporoid;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SensorControl {
/* 2013.11.08 RangeSensor Tateishi */
//    private static final int LEFT = 0;
//    private static final int RIGHT = 1;
    private static final int X = 0;
    private static final int Y = 1;
    private static final int Z = 2;
    private static final int GYRO_HALF = 32767;
    public static final int GYRO_STAND = 0;
    public static final int GYRO_FRONT_DOWN = 1;
    public static final int GYRO_BACK_DOWN = 2;
    public static final int GYRO_LEFT_DOWN = 3;
    public static final int GYRO_RIGHT_DOWN = 4;
    private static final int DETECT_INTERVAL = 100;
    private int mNextGyroCnt = 0;
    
    volatile private static SensorControl instance = null;
    private myThread mThread = null;
    private int mGyro = GYRO_STAND;
    private Context mContext = null;
    private int[] mAccel = { 0, 0, 0 };
    
    public native int I2cSensorInit();
    public native int I2cGetAccelX();
    public native int I2cGetAccelY();
    public native int I2cGetAccelZ();
    public native int I2cGetGyroX();
    public native int I2cGetGyroY();
    public native int I2cGetRange( int lr );

    private FileReader file_1 = null;
    private FileReader file_2 = null;
    final private String kDevicePath = "/sys/devices/virtual/gpio/";
    private int[] mIrdaSensor = { 0, 0 };
    private long mInterval = DETECT_INTERVAL;

    private int mRange = 0;
    
    static {
        System.loadLibrary("i2cTools");
    }
    
    public static SensorControl getInstance() {
        if( instance == null ){
            instance = new SensorControl();
        }
        return instance;
    }

    public void initSensor( Context context ){
        I2cSensorInit();
        mContext = context;
        mThread = new myThread(); 
        mThread.start();
    }
    
    public void stopSensor(){
        mThread.stopRun();
    }
    
    public void retrieveGyro(){
        int pre = mGyro;
        String sound = "";
        synchronized( mAccel ){
            mAccel[X] = I2cGetAccelX();
            mAccel[Y] = I2cGetAccelY();
            mAccel[Z] = I2cGetAccelZ();
        }
        if( true ){
            Log.d("SVC", "GYRO  X:" + mAccel[X] + " Y:" + mAccel[Y] + " Z:" + mAccel[Z]);
        }

        if(true){
            if( mAccel[Y] > GYRO_HALF && mAccel[Y] < 55000 ){
                if( mGyro != GYRO_FRONT_DOWN && ++mNextGyroCnt > 5 ){
                    mGyro = GYRO_FRONT_DOWN;
                    sound = "sound/sen_mi_robo_bato01.mp3";
                    mNextGyroCnt = 0;
                }
            }else if( mAccel[Y] > 7000 && mAccel[Y] < GYRO_HALF ){
                if( mGyro != GYRO_BACK_DOWN && ++mNextGyroCnt > 5 ){
                    mGyro = GYRO_BACK_DOWN;
                    sound = "sound/sen_mi_robo_bato02.mp3";
                    mNextGyroCnt = 0;
                }
            }else if( mAccel[X] > 16000 && mAccel[X] < GYRO_HALF ){
                if( mGyro != GYRO_LEFT_DOWN && ++mNextGyroCnt > 5 ){
                    mGyro = GYRO_LEFT_DOWN;
                    sound = "sound/meka_mi_roido02.mp3";
                    mNextGyroCnt = 0;
                }
            }else if( mAccel[X] > GYRO_HALF && mAccel[X] < 50000 ){
                if( mGyro != GYRO_RIGHT_DOWN && ++mNextGyroCnt > 5 ){
                    mGyro = GYRO_RIGHT_DOWN;
                    sound = "sound/meka_mi_roido04.mp3";
                    mNextGyroCnt = 0;
                }
            }else{
                if( mGyro != GYRO_STAND && ++mNextGyroCnt > 5 ){
                    mGyro = GYRO_STAND;
                    sound = "sound/jump01.mp3";
                    mNextGyroCnt = 0;
                }
            }
        }
        
        if( pre != mGyro ){
            playSound(sound, false);
        }
    }
    
    public void retrieveRange(){
        int res[] = { 0, 0 };
        mRange = I2cGetRange(0);
        
        if( true ){
            Log.d("SVC", "RANGE: " + mRange );
        }
    }

    private void playSound( String sound, boolean force ){
        if( force ){
            Intent intent = new Intent(SapporoidActivity.ACT_SOUND);
            intent.putExtra("file", sound);
            mContext.sendBroadcast(intent);
        }
    }
    
    public int[] getAccel(){
        return mAccel;
    }
    
    public int getPosture(){
        return mGyro;
    }

    public void finish(){
    	mThread.stopRun();
    	
        if( file_1 != null ){
            try {
                file_1.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        if( file_2 != null ){
            try {
                file_1.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private class myThread extends Thread{
        public boolean mRun = false;

        public myThread(){
            mRun = true;
        }

        public void startRun(){
            mRun = true;
        }

        public void stopRun(){
            mRun = false;
        }

        public void run() {
            while(true){
                if( mRun ){
                    retrieveGyro();
                    retrieveRange();
                    
                    try {
                        Thread.sleep(mInterval);
                    } catch (Exception e) {
                        e.printStackTrace();
                        mRun = false;
                    }
                }
            }
        }
    }
    
    
    
    
}
