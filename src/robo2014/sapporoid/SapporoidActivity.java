package robo2014.sapporoid;

import robo2014.sapporoid.webserver.SapporoidHTTPD;
import robo2014.sapporoid.Robo;
import robo2014.sapporoid.RobotController;
import robo2014.sapporoid.webserver.NanoHTTPD;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class SapporoidActivity extends Activity implements OnCheckedChangeListener, OnClickListener, OnSeekBarChangeListener{
    private final int DISP_WIDTH = 640;
    private final int DISP_HEIGHT = 480;
    
    private final int STS_IDLE = 0;
    private final int STS_AUTO = 1;
    private final int STS_MANUAL = 2;

    public static final int MANUAL_DIR_STOP = 0;
    public static  final int MANUAL_DIR_FRONT = 1;
    public static  final int MANUAL_DIR_BACK =  2;
    public static  final int MANUAL_DIR_LEFT =  3;
    public static  final int MANUAL_DIR_RIGHT = 4;

    private final int ACT_MAX = 20;
    
    private final int HNDL_START_CAMERA = 1;
    
    private static final String SOUND_FLD_1 = "sound/robo/";
    private static final String SOUND_FLD_2 = "sound/gundam/";
    private static final String SOUND_FLD_3 = "sound/game/";
    private static final String SOUND_FLD_4 = "sound/opt1/";
    private static final String SOUND_FLD_5 = "sound/opt2/";
    
    private int port_no = 0;
    private int mStatus = STS_IDLE;
    private ToggleButton mToggle1;
    private ToggleButton mToggle2;
//    private ToggleButton mToggle2;
    private FdView mView = null;
    private Point mPoint = new Point(DISP_WIDTH/2,DISP_HEIGHT/2);
    private boolean mClick = false;
    private int[] mControlList = {0,0,0,0};
    
    private ColorSettingDialog mColorDialog = null;
    private ServoSetting2Dialog mServoDialog = null;
    private SettingDialog mSetting = null;
    private Setting2Dialog mSetting2 = null;
    private Setting3Dialog mSetting3 = null;
    private Setting4Dialog mSetting4 = null;
    static final String SETTING_VLAUES = "setting_values";
    static final String SERVO_VLAUES = "servo_values";
    private final int MAX_COLOR_VALUE = 255;
    SharedPreferences mPref = null;
    SharedPreferences mPrefServo = null;

    private Robo mRobo = null;
    private RobotController mRoboControl = null;
    private int mTestAction = 0;
    
    private static final int PORT = 8765;
    private SapporoidHTTPD server = null;
    private MediaPlayer mp = null;
    private String mSoundFolder = SOUND_FLD_1;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch( msg.what ){
                case HNDL_START_CAMERA:
//                    startCamera();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.initial_view);
        
//    	try {
//			Runtime.getRuntime().exec("chmod 666 /dev/video0");
//	    	Runtime.getRuntime().exec("chmod 666 /dev/ttyUSB0");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        changeFilePermission("/dev/ttyUSB0");
        
/* Sapporoid2013 Add Start Tateishi 2nd */
        // ��ʃ��b�N����
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.setFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
/* Sapporoid2013 Add End Tateishi 2nd */
        
    }

//    private void changeFilePermission(final String path) {
//        final File file = new File(path);
//        file.setReadable(true, false);
//        file.setExecutable(true, false);
//        file.setWritable(true, false);
//    }
    
    @Override
    protected void onResume() {
//        suCommand("chmod 777 /dev/video0");
		try {
//			final String[] sCommand = {"su","-c","chmod 777 /dev/ttyUSB0"};
			Process process = Runtime.getRuntime().exec("/system/bin/chmod 777 /dev/ttyUSB0");
//			DataOutputStream writer = new DataOutputStream(process.getOutputStream());
//			writer = new DataOutputStream(process.getOutputStream());
//			writer.writeBytes("/sbin/suchmod.sh");
//			writer.writeBytes("\n");
//			writer.writeBytes("exit\n");
//			writer.flush();
// 		    writer.writeBytes("chmod 666 /dev/ttyUSB0\n");
// 		   writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

        mRobo = new Robo(this);
        mRoboControl = new RobotController(mRobo);

        IntentFilter filter = new IntentFilter();
        filter.addAction(START_CAMERA);
        filter.addAction(CLEAR_ITEM);
        filter.addAction(MOD_ITEM);
        filter.addAction(REM_ITEM);
        filter.addAction(ADD_ACTION);
        filter.addAction(REM_ACTION);
        filter.addAction(MOVE_START);
        filter.addAction(MOVE_STOP);
        filter.addAction(ACT_TO_LOG);
        filter.addAction(SET_AREA);
        filter.addAction(MOVE_START_SEP);
        filter.addAction(SET_180);
        filter.addAction(ACT_SPEECH);
        filter.addAction(ACT_SOUND);
        filter.addAction(ACT_TEST);
        filter.addAction(ACT_DEBUG);
        filter.addAction(ACT_START);
        filter.addAction(ACT_STOP);
        filter.addAction(SET_COLOR_LIMIT);
        filter.addAction(APP_EXIT);
        filter.addAction(START_SERVER);
        filter.addAction(STOP_SERVER);
        filter.addAction(APP_TANK_EVT);
        filter.addAction(SET_SOUND_FOLDER);
        filter.addAction(ACT_MANUAL);
        filter.addAction(MOTION_EDIT);
        registerReceiver(mIntentReceiver, filter);
        
        readSharedPreferencesServo();
//        mRoboControl.action( RobotController.ACT_ARM_DEFAULT, RobotController.ACT_LEG_DEFAULT, 1 );
        
        startHttpd();
        mHandler.sendMessageDelayed( mHandler.obtainMessage(HNDL_START_CAMERA), 3000);
        soundPlay("sound/sen_ka_heirappa01.mp3");
        super.onResume();
    }

    private void startCamera(){
        if( mView == null ){
            setContentView(R.layout.activity_sapporoid);
            mView = (FdView)this.findViewById(R.id.FdView);
            mView.setRobotController(mRoboControl);
            if( server != null ){
                mView.setHttpd(server);
            }

            //Button Set
            mToggle1 = (ToggleButton)this.findViewById(R.id.toggleButton1);
            mToggle1.setOnCheckedChangeListener(this);
            mToggle2 = (ToggleButton)this.findViewById(R.id.toggleButton2);
            mToggle2.setOnCheckedChangeListener(this);
            Button btr = (Button)this.findViewById(R.id.btn_set_color);
            Button bts4 = (Button)this.findViewById(R.id.Setting4);
            Button bts5 = (Button)this.findViewById(R.id.Setting5);
            Button bt1 = (Button)this.findViewById(R.id.button1);
//            Button bt4 = (Button)this.findViewById(R.id.button4);
            btr.setOnClickListener(this);
            bts4.setOnClickListener(this);
            bts5.setOnClickListener(this);
            bt1.setOnClickListener(this);
//            bt4.setOnClickListener(this);
            
            readSharedPreferences();
        }
    }
    
    public static final String START_CAMERA = "robo.START_CAMERA";
    static final String SET_AREA = "robo.SET_AREA";
    static final String MOD_ITEM = "robo.MOD_ITEM";
    static final String CLEAR_ITEM = "robo.CLEAR_ITEM";
    static final String REM_ITEM = "robo.REM_ITEM";
    static final String ADD_ACTION = "robo.ADD_ACTION";
    static final String REM_ACTION = "robo.REM_ACTION";
    static final String MOVE_START = "robo.MOVE_START";
    public static final String MOVE_START_SEP = "robo.MOVE_START_SEP";
    public static final String MOVE_STOP = "robo.MOVE_STOP";
    public static final String SET_180 = "robo.SET_180";
    static final String ACT_TO_LOG = "robo.ACT_TO_LOG";
    static final String ACT_SPEECH = "robo.ACT_SPEECH";
    public static final String ACT_SOUND = "robo.ACT_SOUND";
    public static final String ACT_TEST = "robo.ACT_TEST";
    public static final String ACT_DEBUG = "robo.ACT_DEBUG";
    public static final String ACT_START = "robo.ACT_START";
    public static final String ACT_STOP = "robo.ACT_STOP";
    public static final String SET_COLOR_LIMIT = "robo.SET_COLOR_LIMIT";
    static final String APP_EXIT = "robo.APP_EXIT";
    public static final String APP_TANK_EVT = "robo.APP_TANK_EVT";
    public static final String SET_SOUND_FOLDER = "robo.SET_SOUND_FOLDER";
    public static final String ACT_MANUAL = "robo.ACT_MANUAL";
           
    static final String START_SERVER = "robo.action.START_SERVER";
    static final String STOP_SERVER = "robo.action.STOP_SERVER";
    static final String MOTION_EDIT = "robo.action.MOTION_EDIT";
    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(START_CAMERA)) {
                startCamera();
            }else if( intent.getAction().equals(MOTION_EDIT) ){
            	if( mView != null ){
            		String str = intent.getStringExtra("motion");
            		mView.setMotionEdit(str);
            	}
//            }else if (intent.getAction().equals(MOD_ITEM)) {
//                String actName = intent.getStringExtra("name");
//                int index = intent.getIntExtra("index", -1);
//                String data = intent.getStringExtra("data");
//                if( index >= 0 ){
////                    MoveAction ma = mRoboControl.getAction(actName);
////                    if( ma != null && ma.getActionItem(index) != null ){
//                        modifyItem( actName, index, data );
////                    }else{
////                        String[] strs = data.split(":");
////                        addAction( actName, strs );
//                    }
////                }else{
////                    addItem( actName, data );
//                }
//            }else if (intent.getAction().equals(CLEAR_ITEM)) {
////                mRoboControl.clearAllAction();
//                SharedPreferences.Editor editor = mPrefServo.edit();
//                editor.clear().commit();
//            }else if (intent.getAction().equals(REM_ITEM)) {
//                String actName = intent.getStringExtra("name");
//                int index = intent.getIntExtra("index", -1);
//                if( index >= 0 ){
//                    remItem( actName, index );
//                }
//            }else if (intent.getAction().equals(ADD_ACTION)) {
//                String actName = intent.getStringExtra("name");
//                String data = intent.getStringExtra("data");
//                String[] strs = data.split(":");
//                addAction( actName, strs );
//            }else if (intent.getAction().equals(REM_ACTION)) {
//                String actName = intent.getStringExtra("name");
//                remAction( actName );
//            }else if (intent.getAction().equals(MOVE_START)) {
//                String actName = intent.getStringExtra("name");
//                receiveDisp( "MOVE_START: " + actName );
//            }else if (intent.getAction().equals(MOVE_STOP)) {
//                receiveDisp( "MoveStop:" );
//                mRoboControl.stopRobot();
//            }else if (intent.getAction().equals(SET_AREA)) {
//                int index = intent.getIntExtra("index", -1);
//                String data = intent.getStringExtra("data");
////                mViewController.setSettingValue( index, data );
//                SharedPreferences.Editor editor = mPref.edit();
//                editor.putString( String.valueOf(index), data );
//                editor.commit();
//            }else if (intent.getAction().equals(MOVE_START_SEP)) {
//                String armName = intent.getStringExtra("armName");
//                String legName = intent.getStringExtra("legName");
//                receiveDisp( "MOVE_START: " + armName + " : " + legName );
//                mRoboControl.actionForce( armName, legName );
//            }else if (intent.getAction().equals(ACT_TO_LOG)) {
//                String actName = intent.getStringExtra("name");
//                showActToLog(actName);
//            }else if (intent.getAction().equals(SET_180)) {
//                Toast.makeText(SapporoidActivity.this, "set 180 mode", Toast.LENGTH_SHORT).show();
//            }else if (intent.getAction().equals(ACT_SPEECH)) {
//                String str = intent.getStringExtra("text");
//                if( str != null ){
//                    Toast.makeText(SapporoidActivity.this, str, Toast.LENGTH_SHORT).show();
////                    SapporoidSpeaker.postSpeech(SapporoidWalker.this, str);
//                }else{
//                    Toast.makeText(SapporoidActivity.this, "Text is null.", Toast.LENGTH_SHORT).show();
////                    SapporoidSpeaker.postSpeech(SapporoidWalker.this, "Text is null.");
//                }
//            }else if (intent.getAction().equals(ACT_SOUND)) {
//                String str = intent.getStringExtra("file");
//                if( str != null ){
//                    Toast.makeText(SapporoidActivity.this, "Play Sound: " + str, Toast.LENGTH_SHORT).show();
//                    soundPlay(str);
//                }else{
//                    Toast.makeText(SapporoidActivity.this, "Sound is null.", Toast.LENGTH_SHORT).show();
////                    SapporoidSpeaker.postSpeech(SapporoidActivity.this, "Sound is null.");
//                }
//            }else if(intent.getAction().equals(ACT_TEST)){
//                String str = intent.getStringExtra("value");
//                if( str != null ){
//                }else{
//                    Log.d("SVT", "value = null");
//                }
//            }else if(intent.getAction().equals(ACT_DEBUG)){
//                String str = intent.getStringExtra("value");
//                if( str != null ){
//                    mView.setDebugFlag(Integer.valueOf(str));
//                    Log.d("SVT", "setDebugFlag value = " + String.valueOf(str));
//                }else{
//                    int item = intent.getIntExtra("item", 0);
//                    String set = intent.getStringExtra("set");
//                    String[] strs = set.split(",");
//                    SharedPreferences.Editor editor = mPref.edit();
//                    switch( item ){
//                        case FdView.SETTING_COLOR_RED_R:
//                            if( strs.length >= 3 ){
//                                if( mView != null ){
//                                    mView.setSettingValue( FdView.SETTING_COLOR_RED_R, Integer.valueOf(strs[0]) );
//                                    mView.setSettingValue( FdView.SETTING_COLOR_RED_G, Integer.valueOf(strs[1]) );
//                                    mView.setSettingValue( FdView.SETTING_COLOR_RED_B, Integer.valueOf(strs[2]) );
//                                }
//                                editor.putInt( String.valueOf(FdView.SETTING_COLOR_RED_R), Integer.valueOf(strs[0]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_COLOR_RED_G), Integer.valueOf(strs[1]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_COLOR_RED_B), Integer.valueOf(strs[2]) );
//                                editor.commit();
//                            }
//                            break;
//                        case FdView.SETTING_MOTOR_POWER:
//                            if( strs.length >= 4 ){
//                                if( mView != null ){
//                                    mView.setSettingValue( FdView.SETTING_MOTOR_POWER_GET, Integer.valueOf(strs[0]) );
//                                    mView.setSettingValue( FdView.SETTING_MOTOR_POWER, Integer.valueOf(strs[1]) );
//                                    mView.setSettingValue( FdView.SETTING_ESCAPE_STRAIGHT, Integer.valueOf(strs[2]) );
//                                    mView.setSettingValue( FdView.SETTING_DETECT_LINE, Integer.valueOf(strs[3]) );
//                                }
//                                editor.putInt( String.valueOf(FdView.SETTING_MOTOR_POWER_GET), Integer.valueOf(strs[0]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_MOTOR_POWER), Integer.valueOf(strs[1]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_ESCAPE_STRAIGHT), Integer.valueOf(strs[2]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_DETECT_LINE), Integer.valueOf(strs[3]) );
//                                editor.commit();
//                            }
//                            break;
//                        case FdView.SETTING_MT30:
//                            if( strs.length >= 4 ){
//                                if( mView != null ){
//                                mView.setSettingValue( FdView.SETTING_MT30, Integer.valueOf(strs[0]) );
//                                    mView.setSettingValue( FdView.SETTING_MT60, Integer.valueOf(strs[1]) );
//                                    mView.setSettingValue( FdView.SETTING_MT90, Integer.valueOf(strs[2]) );
//                                    mView.setSettingValue( FdView.SETTING_MT180, Integer.valueOf(strs[3]) );
//                                }
//                                editor.putInt( String.valueOf(FdView.SETTING_MT30), Integer.valueOf(strs[0]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_MT60), Integer.valueOf(strs[1]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_MT90), Integer.valueOf(strs[2]) );
//                                editor.putInt( String.valueOf(FdView.SETTING_MT180), Integer.valueOf(strs[3]) );
//                                editor.commit();
//                            }
//                            break;
//                        case FdView.SETTING_HAND_GET_RANGE:
//                            if( strs.length == 1 ){
//                                if( mView != null ){
//                                    mView.setSettingValue( FdView.SETTING_HAND_GET_RANGE, Integer.valueOf(strs[0]) );
//                                }
//                                editor.putInt( String.valueOf(FdView.SETTING_HAND_GET_RANGE), Integer.valueOf(strs[0]) );
//                                editor.commit();
//                            }
//                            break;
//                        case FdView.SETTING_LINE_DETECT:
//                            if( strs.length == 1 ){
//                                if( mView != null ){
//                                    mView.setSettingValue( FdView.SETTING_LINE_DETECT, Integer.valueOf(strs[0]) );
//                                }
//                                editor.putInt( String.valueOf(FdView.SETTING_LINE_DETECT), Integer.valueOf(strs[0]) );
//                                editor.commit();
//                            }
//                            break;
//                        case FdView.SETTING_STEP4_LR:
//                            if( strs.length == 1 ){
//                                if( mView != null ){
//                                    mView.setSettingValue( FdView.SETTING_STEP4_LR, Integer.valueOf(strs[0]) );
//                                }
//                                editor.putInt( String.valueOf(FdView.SETTING_STEP4_LR), Integer.valueOf(strs[0]) );
//                                editor.commit();
//                            }
//                            break;
//                        default:
//                            break;
//                    }
//                }
            }else if(intent.getAction().equals(ACT_START)){
                soundPlay("sound/sen_ka_heirappa01.mp3");
                startCamera();
                mView.setManual(0);
//                mView.StartCountdown(true);
            }else if(intent.getAction().equals(ACT_STOP)){
                soundPlay("sound/ata_a06.mp3");
                mView.setManual(1);
//                mView.StartCountdown(false);
//            }else if(intent.getAction().equals(SET_COLOR_LIMIT)){
//                String str = intent.getStringExtra("value");
//                if( str != null ){
//                    String [] strs = str.split(",");
//                    if( strs.length >= 2 ){
//                    }
//                }
//            }else if(intent.getAction().equals(APP_EXIT)){
////                SapporoidActivity.this.moveTaskToBack(true);
//                android.os.Process.killProcess(android.os.Process.myPid());
//            }else if (intent.getAction().equals(START_SERVER)) {
////                if (server != null){
////                    server.stop();
////                    server = null;
////                }
////                try {
////                    server = new MyHTTPD();
////                    server.start();
////                    if( mView != null ){
////                        mView.setOutput(true);
////                    }
////                  } catch (IOException e) {
////                    e.printStackTrace();
////                  }
//            }else if (intent.getAction().equals(STOP_SERVER)) {
////                if (server != null){
////                    if( mView != null ){
////                        mView.setOutput(false);
////                    }
////                    server.stop();
////                    server = null;
////                }
//            }else if(intent.getAction().equals(APP_TANK_EVT)){
//                String str = intent.getStringExtra("event");
//                if( str != null ){
//                }
//            }else if(intent.getAction().equals(SET_SOUND_FOLDER)){
//                String str = intent.getStringExtra("value");
//                mSoundFolder = "sound/" + str + "/";
//                soundPlay( mSoundFolder + "101.mp3");
            }else if(intent.getAction().equals(ACT_MANUAL)){
                int dir = intent.getIntExtra("value", MANUAL_DIR_STOP);
//                manualControl( dir );
                switch( dir ){
                case MANUAL_DIR_FRONT:
                	mView.actManual(Robo.ACT_WALK2);
                	break;
                case MANUAL_DIR_LEFT:
                	mView.actManual(Robo.ACT_TURN_LEFT);
                	break;
                case MANUAL_DIR_RIGHT:
                	mView.actManual(Robo.ACT_TURN_RIGHT);
                	break;
                case MANUAL_DIR_BACK:
                case MANUAL_DIR_STOP:
                	mView.actManual(Robo.ACT_DEFAULT);
                	break;
                }
            }
        }
    };
    
    private void soundPlay( String path ){
        if( !path.startsWith("sound/") ){
            path = mSoundFolder + path;
        }
        
        AssetFileDescriptor assetFileDescritor;
        
        try {
            assetFileDescritor = SapporoidActivity.this.getAssets().openFd( path );
            if( assetFileDescritor != null ){
                Log.d("ASSETSTEST", assetFileDescritor.toString());
            }
            if( mp != null ){
                if( mp.isLooping() ){
                    mp.stop();
                }
                mp.reset();
            }else{
                mp = new MediaPlayer();
                mp.reset();
            }
            mp.setDataSource( assetFileDescritor.getFileDescriptor(), 
            assetFileDescritor.getStartOffset(), assetFileDescritor.getLength() );
            mp.setLooping(false);

            mp.setAudioStreamType(AudioManager.STREAM_MUSIC); 
            assetFileDescritor.close(); 
            mp.prepare();
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }     
    }

    private void startHttpd(){
        if (server != null) {
            server.stop();
            server = null;
        }
        try {
            server = new SapporoidHTTPD(this);
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
            server = null;
        }
    }
    
    private void receiveDisp( String str ){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        Log.d("SERVO_TEST", str);
    }
    
    private void modifyItem( String actName, int index, String data ){
//        MoveAction ma = mRoboControl.getAction(actName);
//        String [] strs = data.split(",");
//        
//        if( ma != null ){
//            ActionItem ai = ma.getActionItem(index);
//            if( strs.length >= 14 ){
//                Log.d("SAPPOROID", actName + " RepeatCnt=" + strs[13]);
//                ma.setRepeatCount( Integer.valueOf(strs[13]) );
//            }
//            
//            if( ai != null && ai.mItem.length <= strs.length ){
//                for( int i=0; i<ai.mItem.length; i++ ){
//                    ai.mItem[i] = Integer.valueOf(strs[i]);
//                }
//                receiveDisp( "modifyItem: " + data);
//                
//                SharedPreferences.Editor editor = mPrefServo.edit();
//                editor.putString(actName + ":" + index, data);
//                editor.commit();
//            }
//        }
    }

    private void addItem( String actName, String data ){
//        MoveAction ma = mRoboControl.getAction(actName);
//        if( ma != null ){
//            ma.addActionItem(data);
//            receiveDisp( "addItem1: " + data);
//            
//            SharedPreferences.Editor editor = mPrefServo.edit();
//            editor.putString(actName + ":" + ma.getActionItemCount(), data);
//            editor.commit();
//        }
    }

    private void addItem( String actName, int index, String data ){
//        MoveAction ma = mRoboControl.getAction(actName);
//        if( ma != null ){
//            SharedPreferences.Editor editor = mPrefServo.edit();
//            editor.putString(actName + ":" + index, data);
//            editor.commit();
//            
//            ma.addActionItem(index, data);
//            receiveDisp( "addItem2: " + data);
//        }
    }
    
    private void remItem( String actName, int index ){
//        MoveAction ma = mRoboControl.getAction(actName);
//        if( ma != null ){
//            ActionItem ai = ma.getActionItem(index);
//            if( ai != null ){
//                ma.remActionItem(index);
//                receiveDisp( "remItem: " + String.valueOf(index));
//                
//                SharedPreferences.Editor editor = mPrefServo.edit();
//                for( int i=index+1; i<ACT_MAX; i++ ){
//                    String str = mPrefServo.getString( actName + ":" + i, "none");
//                    if( !str.equals("none") ){
//                        editor.putString(actName + ":" + (i-1), str);
//                    }else{
//                        editor.remove(actName + ":" + (i-1) );
//                        break;
//                    }
//                }
//                editor.commit();
//            }
//        }
    }
    
    private void addAction( String actName, String [] data ){
//        MoveAction act = new MoveAction(actName);
//        SharedPreferences.Editor editor = mPrefServo.edit();
//        
//        for( int i=0; i<data.length; i++ ){
//            act.addActionItem(data[i]);
//            editor.putString(actName + ":" + i, data[i]);
//        }
//        mRoboControl.addAction(act);
//        editor.commit();
        
//        for( int i=0; i<data.length; i++ ){
//            receiveDisp( "addAction:" + data[i]);
//        }
    }
    
    private void remAction( String actName ){
//        MoveAction ma = mRoboControl.getAction(actName);
//        if( ma != null ){
//            mRoboControl.removeAction(actName);
//            receiveDisp( "remAction:");
//            
//            SharedPreferences.Editor editor = mPrefServo.edit();
//            for( int i=0; i<ACT_MAX; i++){
//                if( mPrefServo.contains(actName + ":" + i) ){
//                    editor.remove(actName + ":" + i);
//                }
//            }
//            editor.commit();
//        }
    }
    
    private void showActToLog( String actName ){
//        MoveAction ma = mRoboControl.getAction(actName);
//        String str = "";
//        
//        if( ma != null ){
//            Log.d("SERVO_TEST", actName + " RepeatCnt=" + ma.getRepeatCount() );
//            for( int i=0; i<ma.getActionItemCount(); i++ ){
//                ActionItem ai = ma.getActionItem(i);
//                if( ai != null ){
//                    str = ai.getString();
//                }
//                Log.d("SERVO_TEST", actName + "[" + String.valueOf(i) + "}: " + str);
//            }
//        }
    }
    
    private void readSharedPreferencesServo(){
        //�e��ݒ�l
        mPref = getSharedPreferences(SETTING_VLAUES, MODE_PRIVATE);
//        
//        //�T�[�{�����l
//        if( true ){
//            mPrefServo = getSharedPreferences(SERVO_VLAUES, MODE_WORLD_WRITEABLE | MODE_WORLD_READABLE);
//            HashMap<String,Integer> map = new HashMap<String,Integer>();
//            String [] parts;
//            Map<String, ?> keys = mPrefServo.getAll();
//            if (keys.size() > 0) {
//                for (String key : keys.keySet()) {
//                    parts = key.split(":");
//                    if( !map.containsKey(parts[0]) ){
//                        map.put(parts[0], Integer.valueOf(parts[1]));
//                    }else{
//                        if( Integer.valueOf(parts[1]) > map.get(parts[0]) ){
//                            map.put(parts[0], Integer.valueOf(parts[1]));
//                        }
//                    }
//                }
//            }
//            
//            if (map.size() > 0) {
//                for (String key : map.keySet()) {
//                    MoveAction act = new MoveAction(key);
//                    for( int i=0; i<map.get(key)+1; i++ ){
//                        act.addActionItem(mPrefServo.getString(key + ":" + String.valueOf(i), "90,90,90,90,90,90,90,90,90,90,90,90,0"));
//                    }
//                    mRoboControl.addAction(act);
//                }
//            }
//        }else{
//            MoveAction armAct;
//            MoveAction legAct;
//            
//         // arm_Default =======================================================
//            armAct = new MoveAction("arm_Default");
//            armAct.addActionItem("90,90,80,95,100,90,90,90,95,60,90,0,40");
//            mRoboControl.addAction(armAct);
//
//            // arm_bring_left =======================================================
//            armAct = new MoveAction("arm_bring_left");
//            armAct.addActionItem("100,80,10,95,160,90,90,90,95,60,90,0,100");
//            mRoboControl.addAction(armAct);
//
//            // arm_bring_both =======================================================
//            armAct = new MoveAction("arm_bring_both");
//            armAct.addActionItem("100,80,10,95,160,80,100,165,95,20,90,0,100");
//            mRoboControl.addAction(armAct);
//
//            // arm_put_back =======================================================
//            armAct = new MoveAction("arm_put_back");
//            armAct.addActionItem("175,115,20,90,85,5,65,160,95,80,90,0,50");
//            mRoboControl.addAction(armAct);
//
//            // leg_Default =======================================================
//            legAct = new MoveAction("leg_Default");
//            legAct.addActionItem("90,80,75,88,90,85,90,90,92,85,90,90,100");
//            mRoboControl.addAction(legAct);
//
//            // arm_WakeUpUp =======================================================
//            armAct = new MoveAction("arm_WakeUpUp");
//            armAct.addActionItem("90,90,90,90,90,90,90,90,95,90,90,90,50");
//            armAct.addActionItem("90,90,90,90,90,90,90,90,95,90,90,90,50");
//            armAct.addActionItem("90,90,90,90,90,90,90,90,95,90,90,90,50");
//            mRoboControl.addAction(armAct);
//
//            // leg_WakeUpUp =======================================================
//            legAct = new MoveAction("leg_WakeUpUp");
//            legAct.addActionItem("90,90,90,90,90,90,90,90,90,90,90,90,50");
//            legAct.addActionItem("90,100,90,90,90,90,80,90,90,90,90,90,50");
//            legAct.addActionItem("90,90,90,90,90,90,90,90,90,90,90,90,50");
//            mRoboControl.addAction(legAct);
//
//            // arm_WakeUpDown =======================================================
//            armAct = new MoveAction("arm_WakeUpDown");
//            armAct.addActionItem("90,90,90,90,90,90,90,90,95,90,90,90,100");
//            armAct.addActionItem("90,90,120,90,90,90,90,60,95,90,90,90,100");
//            armAct.addActionItem("90,90,90,90,90,90,90,90,95,90,90,90,100");
//            mRoboControl.addAction(armAct);
//
//            // leg_WakeUpDown =======================================================
//            legAct = new MoveAction("leg_WakeUpDown");
//            legAct.addActionItem("90,90,90,90,90,90,90,90,90,90,90,90,100");
//            legAct.addActionItem("90,90,120,90,90,90,90,60,90,90,90,90,100");
//            legAct.addActionItem("90,90,90,90,90,90,90,90,90,90,90,90,100");
//            mRoboControl.addAction(legAct);
//
//            // leg_Walk1 =======================================================
//            legAct = new MoveAction("leg_Walk1");
//            legAct.addActionItem("65,78,80,80,65,75,110,125,80,70,90,90,50");
//            legAct.addActionItem("70,76,75,85,65,75,80,95,70,70,90,90,40");
//            legAct.addActionItem("95,74,75,80,90,95,85,95,80,90,90,90,50");
//            legAct.addActionItem("100,60,45,105,100,110,97,95,95,95,90,90,50");
//            legAct.addActionItem("95,101,75,125,95,105,98,100,100,95,90,90,40");
//            legAct.addActionItem("80,90,80,90,80,80,110,100,105,80,90,90,50");
//            mRoboControl.addAction(legAct);
//
//            // leg_Walk2 =======================================================
//            legAct = new MoveAction("leg_Walk2");
//            legAct.addActionItem("65,80,80,80,65,75,110,125,80,70,90,90,60");
//            legAct.addActionItem("70,80,75,85,65,75,85,95,80,70,90,90,60");
//            legAct.addActionItem("90,75,75,80,85,90,85,95,80,85,90,90,60");
//            legAct.addActionItem("100,60,45,105,100,110,95,95,95,100,90,90,60");
//            legAct.addActionItem("100,95,80,100,100,110,95,95,100,100,90,90,60");
//            legAct.addActionItem("85,90,80,90,85,85,105,100,105,85,90,90,60");
//            mRoboControl.addAction(legAct);
//
//            // arm_get_left =======================================================
//            armAct = new MoveAction("arm_get_left");
//            armAct.addActionItem("175,115,100,95,65,90,90,90,95,60,90,0,60");
//            armAct.addActionItem("175,115,100,95,65,90,90,90,95,60,90,0,60");
//            armAct.addActionItem("175,115,120,95,45,90,90,90,95,60,90,0,60");
//            armAct.addActionItem("100,100,120,95,45,90,90,90,95,60,90,0,60");
//            armAct.addActionItem("100,80,30,95,125,90,90,90,95,60,90,0,60");
//            armAct.addActionItem("100,80,10,95,160,90,90,90,95,60,90,0,60");
//            mRoboControl.addAction(armAct);
//
//            // leg_get =======================================================
//            legAct = new MoveAction("leg_get");
//            legAct.addActionItem("90,80,75,88,90,85,90,90,92,85,90,90,60");
//            legAct.addActionItem("90,30,5,100,90,90,145,170,80,90,90,90,60");
//            legAct.addActionItem("90,30,5,100,90,90,145,170,80,90,90,90,30");
//            legAct.addActionItem("90,30,5,100,90,90,145,170,80,90,90,90,60");
//            legAct.addActionItem("90,80,75,88,90,85,90,90,92,85,90,90,60");
//            legAct.addActionItem("90,80,75,88,90,85,90,90,92,85,90,90,60");
//            mRoboControl.addAction(legAct);
//
//            // arm_get_right =======================================================
//            armAct = new MoveAction("arm_get_right");
//            armAct.addActionItem("100,80,10,95,160,5,65,70,95,95,90,0,60");
//            armAct.addActionItem("100,80,10,95,160,5,65,70,95,95,90,0,60");
//            armAct.addActionItem("100,80,10,95,160,5,65,50,95,110,90,0,30");
//            armAct.addActionItem("100,80,10,95,160,80,80,50,95,110,90,0,60");
//            armAct.addActionItem("100,80,10,95,160,80,100,140,95,45,90,0,60");
//            armAct.addActionItem("100,80,10,95,160,80,100,165,95,20,90,0,60");
//            mRoboControl.addAction(armAct);
//
//            // leg_turn_left =======================================================
//            legAct = new MoveAction("leg_turn_left");
//            legAct.addActionItem("65,80,80,80,65,75,110,125,80,70,90,90,60");
//            legAct.addActionItem("70,80,75,85,65,75,75,95,70,70,90,90,60");
//            legAct.addActionItem("90,75,75,80,85,90,85,95,80,85,90,90,60");
//            legAct.addActionItem("90,80,75,85,90,85,90,90,95,85,90,90,60");
//            mRoboControl.addAction(legAct);
//
//            // leg_turn_right =======================================================
//            legAct = new MoveAction("leg_turn_right");
//            legAct.addActionItem("100,70,55,105,100,110,95,95,95,100,90,90,60");
//            legAct.addActionItem("100,105,85,115,100,110,95,95,100,100,90,90,60");
//            legAct.addActionItem("85,90,80,90,85,85,105,100,105,85,90,90,60");
//            legAct.addActionItem("90,85,80,85,90,90,95,95,95,90,90,90,60");
//            mRoboControl.addAction(legAct);
//
//            // leg_get_Walk_left =======================================================
//            legAct = new MoveAction("leg_get_Walk_left");
//            legAct.addActionItem("65,78,80,80,65,75,110,125,80,70,90,90,60");
//            legAct.addActionItem("70,76,75,85,65,75,80,95,70,70,90,90,60");
//            legAct.addActionItem("95,74,75,80,90,95,85,95,80,90,90,90,60");
//            legAct.addActionItem("100,60,45,105,100,110,97,95,95,95,90,90,60");
//            legAct.addActionItem("95,101,75,125,95,105,98,100,100,95,90,90,60");
//            legAct.addActionItem("80,90,80,90,80,80,110,100,105,80,90,90,60");
//            mRoboControl.addAction(legAct);
//
//            // leg_get_Walk_both =======================================================
//            legAct = new MoveAction("leg_get_Walk_both");
//            legAct.addActionItem("65,78,80,80,65,75,110,125,80,70,90,90,60");
//            legAct.addActionItem("70,76,75,85,65,75,80,95,70,70,90,90,60");
//            legAct.addActionItem("95,74,75,80,90,95,85,95,80,90,90,90,60");
//            legAct.addActionItem("100,60,45,105,100,110,97,95,95,95,90,90,60");
//            legAct.addActionItem("95,101,75,125,95,105,98,100,100,95,90,90,60");
//            legAct.addActionItem("80,90,80,90,80,80,110,100,105,80,90,90,60");
//            mRoboControl.addAction(legAct);
//
//            // leg_get_Walk_both2 =======================================================
//            legAct = new MoveAction("leg_get_Walk_both2");
//            legAct.addActionItem("65,80,80,80,65,75,110,125,80,70,90,90,60");
//            legAct.addActionItem("70,80,75,85,65,75,85,95,80,70,90,90,60");
//            legAct.addActionItem("90,75,75,80,85,90,85,95,80,85,90,90,60");
//            legAct.addActionItem("100,60,45,105,100,110,95,95,95,100,90,90,60");
//            legAct.addActionItem("100,95,80,100,100,110,95,95,100,100,90,90,60");
//            legAct.addActionItem("85,90,80,90,85,85,105,100,105,85,90,90,60");
//            mRoboControl.addAction(legAct);
//
//            // arm_put =======================================================
//            armAct = new MoveAction("arm_put");
//            armAct.addActionItem("100,80,10,95,160,80,100,165,95,20,90,0,100");
//            armAct.addActionItem("100,80,10,95,160,80,100,165,95,20,90,0,100");
//            armAct.addActionItem("100,80,30,95,125,80,100,140,95,45,90,0,100");
//            armAct.addActionItem("100,100,120,95,45,80,80,50,95,110,90,0,100");
//            armAct.addActionItem("175,115,120,95,45,5,5,65,50,95,110,0,100");
//            armAct.addActionItem("90,90,80,95,100,90,90,90,95,60,90,0,100");
//            mRoboControl.addAction(armAct);
//
//            // leg_put =======================================================
//            legAct = new MoveAction("leg_put");
//            legAct.addActionItem("90,80,75,88,90,85,90,90,92,85,90,90,100");
//            legAct.addActionItem("90,30,10,100,90,90,145,165,80,90,90,90,100");
//            legAct.addActionItem("90,30,10,100,90,90,145,165,80,90,90,90,100");
//            legAct.addActionItem("90,30,10,100,90,90,145,165,80,90,90,90,100");
//            legAct.addActionItem("90,35,10,100,90,90,140,165,80,90,90,90,100");
//            legAct.addActionItem("90,80,75,88,90,85,90,90,92,85,90,90,100");
//            mRoboControl.addAction(legAct);
//
//            // leg_back =======================================================
//            legAct = new MoveAction("leg_back");
//            legAct.addActionItem("65,78,80,80,65,75,110,125,80,70,90,90,50");
//            legAct.addActionItem("80,90,80,90,80,80,110,100,105,80,90,90,50");
//            legAct.addActionItem("95,101,75,125,95,105,98,100,100,95,90,90,50");
//            legAct.addActionItem("100,60,45,105,100,110,97,95,95,95,90,90,50");
//            legAct.addActionItem("95,74,75,80,90,95,85,95,80,90,90,90,50");
//            legAct.addActionItem("90,80,75,88,90,85,90,90,92,85,90,90,50");
//            mRoboControl.addAction(legAct);
//        }
    }
    
    private void readSharedPreferences(){

        //360�J���� �Z���^�[�ʒu
//      mView.setSettingValue(mView.SETTING_CAM_CENTER_X, mPref.getInt(String.valueOf(mView.SETTING_CAM_CENTER_X), 200) );
//      mView.setSettingValue(mView.SETTING_CAM_CENTER_Y, mPref.getInt(String.valueOf(mView.SETTING_CAM_CENTER_Y), 128) );
        mView.setSettingValue(mView.SETTING_CAM_CENTER_X, 16 );
        mView.setSettingValue(mView.SETTING_CAM_CENTER_Y, 12 );

        //����J���� �Z���^�[�ʒu
//      mView.setSettingValue(mView.SETTING_DOWN_CENTER_X, mPref.getInt(String.valueOf(mView.SETTING_DOWN_CENTER_X), 176) );
//      mView.setSettingValue(mView.SETTING_DOWN_CENTER_Y, mPref.getInt(String.valueOf(mView.SETTING_DOWN_CENTER_Y), 165) );
        mView.setSettingValue(mView.SETTING_DOWN_CENTER_X, 17 );
        mView.setSettingValue(mView.SETTING_DOWN_CENTER_Y, 16 );
        
        //�Ԑݒ�
        mView.setSettingValue(mView.SETTING_COLOR_RED_R, mPref.getInt(String.valueOf(mView.SETTING_COLOR_RED_R), 0) );
        mView.setSettingValue(mView.SETTING_COLOR_RED_G, mPref.getInt(String.valueOf(mView.SETTING_COLOR_RED_G), 47) );
        mView.setSettingValue(mView.SETTING_COLOR_RED_B, mPref.getInt(String.valueOf(mView.SETTING_COLOR_RED_B), 47) );
        //���ݒ�
        mView.setSettingValue(mView.SETTING_COLOR_WHITE_R, mPref.getInt(String.valueOf(mView.SETTING_COLOR_WHITE_R), 1) );
        mView.setSettingValue(mView.SETTING_COLOR_WHITE_G, mPref.getInt(String.valueOf(mView.SETTING_COLOR_WHITE_G), 1) );
        mView.setSettingValue(mView.SETTING_COLOR_WHITE_B, mPref.getInt(String.valueOf(mView.SETTING_COLOR_WHITE_B), 0) );
        //���ݒ�
        mView.setSettingValue(mView.SETTING_COLOR_BLACK_R, mPref.getInt(String.valueOf(mView.SETTING_COLOR_BLACK_R), 26) );
        mView.setSettingValue(mView.SETTING_COLOR_BLACK_G, mPref.getInt(String.valueOf(mView.SETTING_COLOR_BLACK_G), 26) );
        mView.setSettingValue(mView.SETTING_COLOR_BLACK_B, mPref.getInt(String.valueOf(mView.SETTING_COLOR_BLACK_B), 91) );
        
        mView.setSettingValue( mView.SETTING_CONTROL_TIME, mPref.getInt(String.valueOf(mView.SETTING_CONTROL_TIME), 1));
        mView.setSettingValue( mView.SETTING_TARGET_LEVEL, mPref.getInt(String.valueOf(mView.SETTING_TARGET_LEVEL), 5));
        mView.setSettingValue( mView.SETTING_SEARCH_TIME, mPref.getInt(String.valueOf(mView.SETTING_SEARCH_TIME), 57));
        mView.setSettingValue( mView.SETTING_SEARCH_FWD_L, mPref.getInt(String.valueOf(mView.SETTING_SEARCH_FWD_L), 60) );
        mView.setSettingValue( mView.SETTING_SEARCH_FWD_R, mPref.getInt(String.valueOf(mView.SETTING_SEARCH_FWD_R), 60) );
        mView.setSettingValue( mView.SETTING_SEARCH_TURN_L, mPref.getInt(String.valueOf(mView.SETTING_SEARCH_TURN_L), 60) );
        mView.setSettingValue( mView.SETTING_SEARCH_TURN_R, mPref.getInt(String.valueOf(mView.SETTING_SEARCH_TURN_R), -60) );
        
        mView.setSettingValue( mView.SETTING_MIRROR_LX, mPref.getInt(String.valueOf(mView.SETTING_MIRROR_LX), 7) );
        mView.setSettingValue( mView.SETTING_MIRROR_LY, mPref.getInt(String.valueOf(mView.SETTING_MIRROR_LY), 23) );
        mView.setSettingValue( mView.SETTING_MIRROR_RX, mPref.getInt(String.valueOf(mView.SETTING_MIRROR_RX), 23) );
        mView.setSettingValue( mView.SETTING_MIRROR_RY, mPref.getInt(String.valueOf(mView.SETTING_MIRROR_RY), 23) );
        
        mView.setSettingValue( mView.SETTING_MOTOR_POWER, mPref.getInt(String.valueOf(mView.SETTING_MOTOR_POWER), 78) );
        mView.setSettingValue( mView.SETTING_MOTOR_POWER_GET, mPref.getInt(String.valueOf(mView.SETTING_MOTOR_POWER_GET), 60) );
        mView.setSettingValue( mView.SETTING_DETECT_LINE, mPref.getInt(String.valueOf(mView.SETTING_DETECT_LINE), 63) );
        mView.setSettingValue( mView.SETTING_ESCAPE_STRAIGHT, mPref.getInt(String.valueOf(mView.SETTING_ESCAPE_STRAIGHT), -56) );
        mView.setSettingValue( mView.SETTING_ESCAPE_TURN_L, mPref.getInt(String.valueOf(mView.SETTING_ESCAPE_TURN_L), 63) );
        mView.setSettingValue( mView.SETTING_ESCAPE_TURN_R, mPref.getInt(String.valueOf(mView.SETTING_ESCAPE_TURN_R), 73) );

        mView.setSettingValue( mView.SETTING_CONTROL_MODE, mPref.getInt(String.valueOf(mView.SETTING_CONTROL_MODE), 0) );
        mView.setSettingValue( mView.SETTING_STRUGGLE_TIME, mPref.getInt(String.valueOf(mView.SETTING_STRUGGLE_TIME), 30) );
        mView.setSettingValue( mView.SETTING_HANGER_ORDER, mPref.getInt(String.valueOf(mView.SETTING_HANGER_ORDER), 1) );
        mView.setSettingValue( mView.SETTING_DISPLAY, mPref.getInt(String.valueOf(mView.SETTING_DISPLAY), 0) );
        
        mView.setSettingValue( FdView.SETTING_MT30, mPref.getInt(String.valueOf(FdView.SETTING_MT30), 30/2) );
        mView.setSettingValue( FdView.SETTING_MT60, mPref.getInt(String.valueOf(FdView.SETTING_MT60), 60/2) );
        mView.setSettingValue( FdView.SETTING_MT90, mPref.getInt(String.valueOf(FdView.SETTING_MT90), 90/2) );
        mView.setSettingValue( FdView.SETTING_MT180, mPref.getInt(String.valueOf(FdView.SETTING_MT180), 180/2) );
        mView.setSettingValue( FdView.SETTING_MT360, mPref.getInt(String.valueOf(FdView.SETTING_MT360), 360/2) );
        
        mView.setSettingValue( FdView.SETTING_HAND_GET_RANGE, mPref.getInt(String.valueOf(FdView.SETTING_HAND_GET_RANGE), 4500) );
        mView.setSettingValue( FdView.SETTING_LINE_DETECT, mPref.getInt(String.valueOf(FdView.SETTING_LINE_DETECT), 1950) );
        mView.setSettingValue( FdView.SETTING_STEP4_LR, mPref.getInt(String.valueOf(FdView.SETTING_STEP4_LR), 0) );
        
//        mView.setSettingValue( mView.SETTING_DETECT_FIELD, mPref.getString(String.valueOf(mView.SETTING_DETECT_FIELD), "20,21,25,21") );
//        mView.setSettingValue( mView.SETTING_LEFTHAND_AREA, mPref.getString(String.valueOf(mView.SETTING_LEFTHAND_AREA), "-36,16,-22,35") );
//        mView.setSettingValue( mView.SETTING_RIGHTHAND_AREA, mPref.getString(String.valueOf(mView.SETTING_RIGHTHAND_AREA), "22,16,36,35") );
//        mView.setSettingValue( mView.SETTING_LEFTOUTNEAR_AREA, mPref.getString(String.valueOf(mView.SETTING_LEFTOUTNEAR_AREA), "-47,12,-32,27") );
//        mView.setSettingValue( mView.SETTING_RIGHTOUTNEAR_AREA, mPref.getString(String.valueOf(mView.SETTING_RIGHTOUTNEAR_AREA), "36,8,51,30") );
//        mView.setSettingValue( mView.SETTING_CENTERNEAR_AREA, mPref.getString(String.valueOf(mView.SETTING_CENTERNEAR_AREA), "-27,26,17,48") );
//        mView.setSettingValue( mView.SETTING_LEFTNEAR_AREA, mPref.getString(String.valueOf(mView.SETTING_LEFTNEAR_AREA), "-47,26,-27,48") );
//        mView.setSettingValue( mView.SETTING_RIGHTNEAR_AREA, mPref.getString(String.valueOf(mView.SETTING_RIGHTNEAR_AREA), "17,26,51,48") );
//        mView.setSettingValue( mView.SETTING_CENTERFAR_AREA, mPref.getString(String.valueOf(mView.SETTING_CENTERFAR_AREA), "-32,48,18,100") );
//        mView.setSettingValue( mView.SETTING_LEFTOUTFAR_AREA, mPref.getString(String.valueOf(mView.SETTING_LEFTOUTFAR_AREA), "-78,12,-47,27") );
//        mView.setSettingValue( mView.SETTING_RIGHTOUTFAR_AREA, mPref.getString(String.valueOf(mView.SETTING_RIGHTOUTFAR_AREA), "51,8,70,30") );
//        mView.setSettingValue( mView.SETTING_LEFTFAR_AREA, mPref.getString(String.valueOf(mView.SETTING_LEFTFAR_AREA), "-78,26,-32,100") );
//        mView.setSettingValue( mView.SETTING_RIGHTFAR_AREA, mPref.getString(String.valueOf(mView.SETTING_RIGHTFAR_AREA), "18,26,70,100") );
//        mView.setSettingValue( mView.SETTING_LEFTHANDREJECT_AREA, mPref.getString(String.valueOf(mView.SETTING_LEFTHANDREJECT_AREA), "-80,16,-20,50") );
//        mView.setSettingValue( mView.SETTING_DOWN_AREA1, mPref.getString(String.valueOf(mView.SETTING_DOWN_AREA1), "-50,0,-45,5") );
//        mView.setSettingValue( mView.SETTING_DOWN_AREA2, mPref.getString(String.valueOf(mView.SETTING_DOWN_AREA2), "45,0,50,5") );
//        mView.setSettingValue( mView.SETTING_DOWN_AREA3, mPref.getString(String.valueOf(mView.SETTING_DOWN_AREA3), "-50,0,-45,45") );
//        mView.setSettingValue( mView.SETTING_DOWN_AREA4, mPref.getString(String.valueOf(mView.SETTING_DOWN_AREA4), "45,0,50,45") );
        
//          mView.setSettingValue( mView.SETTING_DETECT_FIELD, "20,21,25,21" );
//          mView.setSettingValue( mView.SETTING_LEFTHAND_AREA, "-160,80,-120,120" );
//          mView.setSettingValue( mView.SETTING_RIGHTHAND_AREA, "120,80,160,120" );
//          mView.setSettingValue( mView.SETTING_LEFTOUTNEAR_AREA, "-160,-60,-120,40" );
//          mView.setSettingValue( mView.SETTING_RIGHTOUTNEAR_AREA, "120,-60,160,40" );
//          mView.setSettingValue( mView.SETTING_CENTERNEAR_AREA, "-120,-20,120,80" );
//          mView.setSettingValue( mView.SETTING_LEFTNEAR_AREA, "-120,80,-1,120" );
//          mView.setSettingValue( mView.SETTING_RIGHTNEAR_AREA, "1,80,120,120" );
//          mView.setSettingValue( mView.SETTING_CENTERFAR_AREA, "-120,-120,120,-20" );
//          mView.setSettingValue( mView.SETTING_LEFTOUTFAR_AREA, "-160,-120,-120,-20" );
//          mView.setSettingValue( mView.SETTING_RIGHTOUTFAR_AREA, "120,-120,160,-20" );
//          mView.setSettingValue( mView.SETTING_LEFTFAR_AREA, "-120,-120,120,-20" );
//          mView.setSettingValue( mView.SETTING_RIGHTFAR_AREA, "-120,-120,120,-20" );
//          mView.setSettingValue( mView.SETTING_LEFTHANDREJECT_AREA, "0,0,0,0" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA1, "-50,0,-45,5" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA2, "45,0,50,5" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA3, "-50,0,-45,45" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA4, "45,0,50,45" );
        
//          mView.setSettingValue( mView.SETTING_DETECT_FIELD, "2,2,2,2" );
//          mView.setSettingValue( mView.SETTING_LEFTHAND_AREA, "-3,1,-2,3" );
//          mView.setSettingValue( mView.SETTING_RIGHTHAND_AREA, "2,1,3,3" );
//          mView.setSettingValue( mView.SETTING_LEFTOUTNEAR_AREA, "-4,1,-3,2" );
//          mView.setSettingValue( mView.SETTING_RIGHTOUTNEAR_AREA, "3,1,5,3" );
//          mView.setSettingValue( mView.SETTING_CENTERNEAR_AREA, "-2,2,1,4" );
//          mView.setSettingValue( mView.SETTING_LEFTNEAR_AREA, "-4,2,-2,4" );
//          mView.setSettingValue( mView.SETTING_RIGHTNEAR_AREA, "1,2,5,4" );
//          mView.setSettingValue( mView.SETTING_CENTERFAR_AREA, "-3,4,1,10" );
//          mView.setSettingValue( mView.SETTING_LEFTOUTFAR_AREA, "-7,1,-4,2" );
//          mView.setSettingValue( mView.SETTING_RIGHTOUTFAR_AREA, "5,1,7,3" );
//          mView.setSettingValue( mView.SETTING_LEFTFAR_AREA, "-7,2,-3,10" );
//          mView.setSettingValue( mView.SETTING_RIGHTFAR_AREA, "1,2,7,10" );
//          mView.setSettingValue( mView.SETTING_LEFTHANDREJECT_AREA, "-8,1,-2,5" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA1, "-5,0,-4,1" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA2, "4,0,5,1" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA3, "-5,0,-4,4" );
//          mView.setSettingValue( mView.SETTING_DOWN_AREA4, "4,0,5,4" );
    }

    @Override
    protected void onDestroy() {
//        mView.forceStop();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
//        mView.forceStop();
        this.unregisterReceiver(mIntentReceiver);
        super.onPause();
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
        if( arg0.getId() == R.id.toggleButton1 ){
            if( arg1 == true ){
                //Auto
                mToggle2.setChecked(false);
                showDialog(0);
                mStatus = STS_AUTO;
            }else{
                mView.StartCountdown(false);
                mView.forceStop();
                mStatus = STS_IDLE;
            }
        }else if(arg0.getId() == R.id.toggleButton2){
            //Manual
            if( arg1 == true ){
                mToggle1.setChecked(false);
                mStatus = STS_MANUAL;
            }else{
                mView.forceStop();
                mStatus = STS_IDLE;
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dlg_start, null);
        Button bt = (Button)view.findViewById(R.id.btn_start);
        bt.setOnClickListener(this);
        dialog.setView(view);
        Dialog dlg = dialog.create();
        return dlg;
    }

    @Override
    public void onClick(View arg0) {
        if( arg0.getId() == R.id.initial_button ){
            startCamera();
            mView.setManual(0);
        }else if( arg0.getId() == R.id.initial_button2 ){
            startCamera();
            mView.setManual(1);
        }else if( arg0.getId() == R.id.btn_start ){
            removeDialog(0);
            mView.StartCountdown(true);
        }else if(arg0.getId() == R.id.btn_set_color){
            mColorDialog = new ColorSettingDialog(this);
            mColorDialog.show();
            mColorDialog.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mColorDialog.dismiss();
                }
            });
/*            
        }else if(arg0.getId() == R.id.Setting){
            mSetting = new SettingDialog(SapporoidActivity.this);
            mSetting.show();
            mSetting.findViewById(R.id.button_SB_OK).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    TextView tv1 = (TextView)mSetting.findViewById(R.id.st1_bar1_value);
                    TextView tv2 = (TextView)mSetting.findViewById(R.id.st1_bar2_value);
                    TextView tv3 = (TextView)mSetting.findViewById(R.id.st1_bar3_value);
                    TextView tv4 = (TextView)mSetting.findViewById(R.id.st1_bar4_value);
                    TextView tv5 = (TextView)mSetting.findViewById(R.id.st1_bar5_value);
                    TextView tv6 = (TextView)mSetting.findViewById(R.id.st1_bar6_value);
                    TextView tv7 = (TextView)mSetting.findViewById(R.id.st1_bar7_value);
                    
                    EditText et1 = (EditText)mSetting.findViewById(R.id.editText1);
                    EditText et2 = (EditText)mSetting.findViewById(R.id.editText2);
                    EditText et3 = (EditText)mSetting.findViewById(R.id.editText3);
                    EditText et4 = (EditText)mSetting.findViewById(R.id.editText4);
                    mView.setSettingValue( mView.SETTING_CONTROL_TIME, Integer.valueOf(tv1.getText().toString()));
                    mView.setSettingValue( mView.SETTING_TARGET_LEVEL, Integer.valueOf(tv2.getText().toString()));
                    mView.setSettingValue( mView.SETTING_SEARCH_TIME, Integer.valueOf(tv3.getText().toString()));
                    mView.setSettingValue( mView.SETTING_SEARCH_FWD_L, Integer.valueOf(tv4.getText().toString()));
                    mView.setSettingValue( mView.SETTING_SEARCH_FWD_R, Integer.valueOf(tv5.getText().toString()));
                    mView.setSettingValue( mView.SETTING_SEARCH_TURN_L, Integer.valueOf(tv6.getText().toString()));
                    mView.setSettingValue( mView.SETTING_SEARCH_TURN_R, Integer.valueOf(tv7.getText().toString()));
                    mView.setSettingValue( mView.SETTING_MIRROR_LX, Integer.valueOf(et1.getText().toString()));
                    mView.setSettingValue( mView.SETTING_MIRROR_LY, Integer.valueOf(et2.getText().toString()));
                    mView.setSettingValue( mView.SETTING_MIRROR_RX, Integer.valueOf(et3.getText().toString()));
                    mView.setSettingValue( mView.SETTING_MIRROR_RY, Integer.valueOf(et4.getText().toString()));
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_CONTROL_TIME), Integer.valueOf(tv1.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_TARGET_LEVEL), Integer.valueOf(tv2.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_SEARCH_TIME), Integer.valueOf(tv3.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_SEARCH_FWD_L), Integer.valueOf(tv4.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_SEARCH_FWD_R), Integer.valueOf(tv5.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_SEARCH_TURN_L), Integer.valueOf(tv6.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_SEARCH_TURN_R), Integer.valueOf(tv7.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_MIRROR_LX), Integer.valueOf(et1.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_MIRROR_LY), Integer.valueOf(et2.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_MIRROR_RX), Integer.valueOf(et3.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_MIRROR_RY), Integer.valueOf(et4.getText().toString()) );
                    editor.commit();

                    mSetting.dismiss();
                }
            });
        }else if( arg0.getId() == R.id.Setting2 ){
            mSetting2 = new Setting2Dialog(SapporoidActivity.this);
            mSetting2.show();
            mSetting2.findViewById(R.id.btn_ok).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    TextView tv1 = (TextView)mSetting2.findViewById(R.id.st2_bar1_value);
                    TextView tv2 = (TextView)mSetting2.findViewById(R.id.st2_bar2_value);
                    TextView tv3 = (TextView)mSetting2.findViewById(R.id.st2_bar3_value);
                    TextView tv4 = (TextView)mSetting2.findViewById(R.id.st2_bar4_value);
                    TextView tv5 = (TextView)mSetting2.findViewById(R.id.st2_bar5_value);
                    TextView tv6 = (TextView)mSetting2.findViewById(R.id.st2_bar6_value);
                    mView.setSettingValue( mView.SETTING_MOTOR_POWER, Integer.valueOf(tv1.getText().toString()));
                    mView.setSettingValue( mView.SETTING_MOTOR_POWER_GET, Integer.valueOf(tv2.getText().toString()));
                    mView.setSettingValue( mView.SETTING_DETECT_LINE, Integer.valueOf(tv3.getText().toString()));
                    mView.setSettingValue( mView.SETTING_ESCAPE_STRAIGHT, Integer.valueOf(tv4.getText().toString()));
                    mView.setSettingValue( mView.SETTING_ESCAPE_TURN_L, Integer.valueOf(tv5.getText().toString()));
                    mView.setSettingValue( mView.SETTING_ESCAPE_TURN_R, Integer.valueOf(tv6.getText().toString()));

                    SharedPreferences.Editor editor = mPref.edit();   
                    editor.putInt( String.valueOf(mView.SETTING_MOTOR_POWER), Integer.valueOf(tv1.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_MOTOR_POWER_GET), Integer.valueOf(tv2.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_DETECT_LINE), Integer.valueOf(tv3.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_ESCAPE_STRAIGHT), Integer.valueOf(tv4.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_ESCAPE_TURN_L), Integer.valueOf(tv5.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_ESCAPE_TURN_R), Integer.valueOf(tv6.getText().toString()) );
                    editor.commit();

                    mSetting2.dismiss();
                }
            });
        }else if(arg0.getId() == R.id.Setting3){
            mSetting3 = new Setting3Dialog(SapporoidActivity.this);
            mSetting3.show();
            mSetting3.findViewById(R.id.button_SB_OK).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    RadioGroup rg1 = (RadioGroup)mSetting3.findViewById(R.id.radioGroup1);
                    RadioGroup rg4 = (RadioGroup)mSetting3.findViewById(R.id.radioGroup4);
                    RadioGroup rg5 = (RadioGroup)mSetting3.findViewById(R.id.radioGroup5);
                    EditText et1 = (EditText)mSetting3.findViewById(R.id.StruggleTime);
                    
                    int set1 = 0;
                    int set4 = 0;
                    int set5 = 0;
                    
                    if( rg1.getCheckedRadioButtonId() == R.id.radio1 ){
                        set1 = 1;
                    }
                    
                    if( rg4.getCheckedRadioButtonId() == R.id.radio1 ){
                        set4 = 1;
                    }else if(rg4.getCheckedRadioButtonId() == R.id.radio2 ){
                        set4 = 2;
                    }else if(rg4.getCheckedRadioButtonId() == R.id.radio3 ){
                        set4 = 3;
                    }

                    if( rg5.getCheckedRadioButtonId() == R.id.radio1 ){
                        set5 = 1;
                    }else if(rg5.getCheckedRadioButtonId() == R.id.radio2 ){
                        set5 = 2;
                    }
                    
                    mView.setSettingValue( mView.SETTING_CONTROL_MODE, set1 );
                    mView.setSettingValue( mView.SETTING_STRUGGLE_TIME, Integer.valueOf(et1.getText().toString()) );
                    mView.setSettingValue( mView.SETTING_HANGER_ORDER, set4 );
                    mView.setSettingValue( mView.SETTING_DISPLAY, set5 );
        
                    SharedPreferences.Editor editor = mPref.edit();   
                    editor.putInt( String.valueOf(mView.SETTING_CONTROL_MODE), set1 );
                    editor.putInt( String.valueOf(mView.SETTING_STRUGGLE_TIME), Integer.valueOf(et1.getText().toString()) );
                    editor.putInt( String.valueOf(mView.SETTING_HANGER_ORDER), set4 );
                    editor.putInt( String.valueOf(mView.SETTING_DISPLAY), set5 );
                    editor.commit();
        
                    mSetting3.dismiss();
                }
            });
*/            
        } else if(arg0.getId() == R.id.Setting4){
            mSetting4 = new Setting4Dialog(SapporoidActivity.this);
            mSetting4.show();
            mSetting4.findViewById(R.id.button1).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_CAM_CENTER_X);
                    mView.setSettingValue( mView.SETTING_CAM_CENTER_X, val-1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_CAM_CENTER_X), val-1 );
                    editor.commit();
                }
            });
            
            mSetting4.findViewById(R.id.button2).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_CAM_CENTER_Y);
                    mView.setSettingValue( mView.SETTING_CAM_CENTER_Y, val-1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_CAM_CENTER_Y), val-1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button3).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_CAM_CENTER_X);
                    mView.setSettingValue( mView.SETTING_CAM_CENTER_X, val+1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_CAM_CENTER_X), val+1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button4).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_CAM_CENTER_Y);
                    mView.setSettingValue( mView.SETTING_CAM_CENTER_Y, val+1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_CAM_CENTER_Y), val+1 );
                    editor.commit();
                }
            });
            

            mSetting4.findViewById(R.id.button6).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_DOWN_CENTER_X);
                    mView.setSettingValue( mView.SETTING_DOWN_CENTER_X, val-1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_DOWN_CENTER_X), val-1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button7).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_DOWN_CENTER_Y);
                    mView.setSettingValue( mView.SETTING_DOWN_CENTER_Y, val-1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_DOWN_CENTER_Y), val-1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button8).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_DOWN_CENTER_X);
                    mView.setSettingValue( mView.SETTING_DOWN_CENTER_X, val+1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_DOWN_CENTER_X), val+1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button9).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_DOWN_CENTER_Y);
                    mView.setSettingValue( mView.SETTING_DOWN_CENTER_Y, val+1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_DOWN_CENTER_Y), val+1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button10).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_DETECT_LINE);
                    mView.setSettingValue( mView.SETTING_DETECT_LINE, val-1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_DETECT_LINE), val-1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button11).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    int val = mView.getSettingValue( mView.SETTING_DETECT_LINE);
                    mView.setSettingValue( mView.SETTING_DETECT_LINE, val+1 );
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_DETECT_LINE), val+1 );
                    editor.commit();
                }
            });
            mSetting4.findViewById(R.id.button12).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    EditText et1 = (EditText)mSetting4.findViewById(R.id.editText1); 
                    EditText et2 = (EditText)mSetting4.findViewById(R.id.editText2);
                    int t1 = Integer.valueOf(et1.getText().toString());
                    int t2 = Integer.valueOf(et2.getText().toString());
                    
                    mView.setSettingValue( mView.SETTING_CONTROL_TIME, t1);
                    mView.setSettingValue( mView.SETTING_TARGET_LEVEL, t2);
                    
                    SharedPreferences.Editor editor = mPref.edit();
                    editor.putInt( String.valueOf(mView.SETTING_CONTROL_TIME), t1 );
                    editor.putInt( String.valueOf(mView.SETTING_TARGET_LEVEL), t2 );
                    editor.commit();
                }
            });
            
            mSetting4.findViewById(R.id.button5).setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    mSetting4.dismiss();
                }
            });
        }else if(arg0.getId() == R.id.Setting5){
            mServoDialog = new ServoSetting2Dialog(this);
            mServoDialog.show();
        }else if( arg0.getId() == R.id.button1 ){
//            mRoboControl.actionForce( RobotController.ACT_ARM_DEFAULT, RobotController.ACT_LEG_DEFAULT, 1 );
/*            
        }else if( arg0.getId() == R.id.button2 ){
//            mRoboControl.action( RobotController.ACT_WAKEUP_UP, 1 );
            if( mTestAction++ > 4 ){
                mTestAction = 0;
            }
            switch(mTestAction){
                case 0:
                    mRoboControl.action( RobotController.ACT_TURN_LEFT, 1 );
                    break;
                case 1:
                    mRoboControl.action( RobotController.ACT_TURN_RIGHT, 1 );
                    break;
                case 2:
                    mRoboControl.action( RobotController.ACT_GET_LEFT, 1 );
                    break;
                case 3:
                    mRoboControl.action( RobotController.ACT_GET_WALK_LEFT, 1 );
                    break;
                case 4:
                    mRoboControl.action( RobotController.ACT_PUT, 1 );
                    break;
            }
*/
/*            
        }else if( arg0.getId() == R.id.button3 ){
//            mRoboControl.action( RobotController.ACT_WALK1, 1 );
            if( mView.getCameraSts() == FdView.CAM_STS_DOWN ){
                mView.MoveCamera(FdView.CAM_STS_TOP);
            }else if( mView.getCameraSts() == FdView.CAM_STS_FRONT ){
                mView.MoveCamera(FdView.CAM_STS_DOWN);
            }else{
                mView.MoveCamera(FdView.CAM_STS_FRONT);
            }
*/
        }else if( arg0.getId() == R.id.button4 ){
//            mView.setEffect();
/*            
        }else if( arg0.getId() == R.id.button5 ){
            mView.modEffect();
*/            
        }else if( arg0.getId() == R.id.button_action ){
//            Spinner spn1 = (Spinner)mServoDialog.findViewById(R.id.spinner1);
//            Spinner spn2 = (Spinner)mServoDialog.findViewById(R.id.spinner2);
//            String str1 = (String)spn1.getSelectedItem();
//            String str2 = (String)spn2.getSelectedItem();
//            mRoboControl.actionForce( str1, str2, 1 );
        }else if( arg0.getId() == R.id.button_stop ){
            mRoboControl.MoveStop();
        }else if( arg0.getId() == R.id.button_close ){
            mServoDialog.dismiss();
        }else if( arg0.getId() == R.id.button_camera ){
            Spinner spn3 = (Spinner)mServoDialog.findViewById(R.id.spinner3);
            String str = (String)spn3.getSelectedItem();
//            if( str.equals("TOP") ){
//                mView.MoveCamera(FdView.CAM_STS_TOP);
//            }else if( str.equals("FRONT") ){
//                mView.MoveCamera(FdView.CAM_STS_FRONT);
//            }else if( str.equals("DOWN") ){
//                mView.MoveCamera(FdView.CAM_STS_DOWN);
//            }
        }
    }

    private void setAngle(int angle, int speed){
        int [] iList = { 0, 3, 0, 0, 0, 0, 0, 0, 0 };
        iList[2] = 255;
        iList[3] = 3;
        iList[4] = 4;
        iList[5] = 2;
        iList[6] = 0;
        iList[7] = angle;
        iList[8] = speed;
        
        for(int i=0; i<12; i++){
            iList[6] = i;
            iList[3] = 3;
//            mView.TestControl(iList);
            iList[3] = 1;
//            mView.TestControl(iList);
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if( mStatus == STS_AUTO ){
            
        }else if( mStatus == STS_MANUAL ){
            if( event.getAction() == MotionEvent.ACTION_MOVE ){
                if( mClick ){
                    mControlList[0] = port_no;
                    mControlList[1] = 1;
                    int power, direction;
                    
                    power = (mPoint.y - (int)event.getY()) / ((DISP_HEIGHT/2)/100);
                    if( power > 100 ){
                        power = 100;
                    }else if( power < -100 ){
                        power = -100;
                    }
                    direction = (mPoint.x - (int)event.getX()) / ((DISP_WIDTH/2)/100);
                    if( direction > 100 ){
                        direction = 100;
                    }else if( direction < -100 ){
                        direction = -100;
                    }
                    mControlList[2] = power;
                    mControlList[3] = power;
                    if( direction > 0 ){
                        if( mControlList[3] < 0 ){
                            mControlList[3] += direction;
                        }else{
                            mControlList[3] -= direction;
                        }
                    }else{
                        if( mControlList[2] > 0 ){
                            mControlList[2] += direction;
                        }else{
                            mControlList[2] -= direction;
                        }
                    }
                    
//                    port_no = mView.ManualControl(mControlList);
                }
            }else if( event.getAction() == MotionEvent.ACTION_DOWN ){
                mPoint.set((int)event.getX(), (int)event.getY());
                mClick = true;
            }else if( event.getAction() == MotionEvent.ACTION_UP ){
                mClick = false;
                mView.forceStop();
            }
        }
        return super.onTouchEvent(event);
    }

    private void manualControl( int dir ){
        mControlList[0] = port_no;
        mControlList[1] = 1;
        switch( dir ){
            case MANUAL_DIR_FRONT:
                mControlList[2] = 70;
                mControlList[3] = 70;
                break;
            case MANUAL_DIR_BACK:
                mControlList[2] = -60;
                mControlList[3] = -60;
                break;
            case MANUAL_DIR_LEFT:
                mControlList[2] = -60;
                mControlList[3] = 60;
                break;
            case MANUAL_DIR_RIGHT:
                mControlList[2] = 60;
                mControlList[3] = -60;
                break;
            default:
                mControlList[2] = 0;
                mControlList[3] = 0;
        }

        if( mView != null ){
//            mView.ManualControl(mControlList);
        }
    }
    
    private class ServoSetting2Dialog extends Dialog
    {
        public ServoSetting2Dialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setTitle("Action Test");
            this.setContentView(R.layout.servotest2);
            
            Spinner spn1 = (Spinner)mServoDialog.findViewById(R.id.spinner1);
            Spinner spn2 = (Spinner)mServoDialog.findViewById(R.id.spinner2);
            Spinner spn3 = (Spinner)mServoDialog.findViewById(R.id.spinner3);
            
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(SapporoidActivity.this, android.R.layout.simple_spinner_item);
            ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(SapporoidActivity.this, android.R.layout.simple_spinner_item);
            ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(SapporoidActivity.this, android.R.layout.simple_spinner_item);
            adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item);
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_item);
            adapter3.setDropDownViewResource(android.R.layout.simple_spinner_item);
            adapter3.add("TOP");
            adapter3.add("FRONT");
            adapter3.add("DOWN");
            
//            HashMap<String,MoveAction> keys = mRoboControl.getActionMap();
//            if (keys.size() > 0) {
//                for (String key : keys.keySet()) {
//                    if( key.contains("arm_")){
//                        adapter1.add(key);
//                    }else if( key.contains("leg_")){
//                        adapter2.add(key);
//                    }
//                }
//            }
            spn1.setAdapter(adapter1);
            spn2.setAdapter(adapter2);
            spn3.setAdapter(adapter3);
            
            Button bt1 = (Button)mServoDialog.findViewById(R.id.button_action);
            Button bt2 = (Button)mServoDialog.findViewById(R.id.button_stop);
            Button bt3 = (Button)mServoDialog.findViewById(R.id.button_close);
            Button bt4 = (Button)mServoDialog.findViewById(R.id.button_camera);
            bt1.setOnClickListener( SapporoidActivity.this );
            bt2.setOnClickListener( SapporoidActivity.this );
            bt3.setOnClickListener( SapporoidActivity.this );
            bt4.setOnClickListener( SapporoidActivity.this );
        }
        @Override
        public void dismiss() {
            super.dismiss();
        }
    }
        
    private class Setting4Dialog extends Dialog
    {
        public Setting4Dialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setTitle("Servo Test1");
            this.setContentView(R.layout.servotest);
            
            EditText et1 = (EditText)mSetting4.findViewById(R.id.editText1); 
            EditText et2 = (EditText)mSetting4.findViewById(R.id.editText2);
            et1.setText(String.valueOf(mView.getSettingValue(mView.SETTING_CONTROL_TIME)));
            et2.setText(String.valueOf(mView.getSettingValue(mView.SETTING_TARGET_LEVEL)));
        }
        @Override
        public void dismiss() {
            super.dismiss();
        }
    }
    
    private class ColorSettingDialog extends Dialog
    {
        public ColorSettingDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setTitle("Color Setting");
            this.setContentView(R.layout.color_setting);
            
            //Red
            SeekBar bar1 = (SeekBar)mColorDialog.findViewById(R.id.red_r_seekBar);
            bar1.setMax(MAX_COLOR_VALUE);
            bar1.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar1.setProgress(mView.getSettingValue(mView.SETTING_COLOR_RED_R));
            TextView tv1 = (TextView)mColorDialog.findViewById(R.id.red_r_value);
            tv1.setText("R:" + mView.getSettingValue(mView.SETTING_COLOR_RED_R));
            
            SeekBar bar2 = (SeekBar)mColorDialog.findViewById(R.id.red_g_seekBar);
            bar2.setMax(MAX_COLOR_VALUE);
            bar2.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar2.setProgress(mView.getSettingValue(mView.SETTING_COLOR_RED_G));
            TextView tv2 = (TextView)mColorDialog.findViewById(R.id.red_g_value);
            tv2.setText("G:" + mView.getSettingValue(mView.SETTING_COLOR_RED_G));

            SeekBar bar3 = (SeekBar)mColorDialog.findViewById(R.id.red_b_seekBar);
            bar3.setMax(MAX_COLOR_VALUE);
            bar3.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar3.setProgress(mView.getSettingValue(mView.SETTING_COLOR_RED_B));
            TextView tv3 = (TextView)mColorDialog.findViewById(R.id.red_b_value);
            tv3.setText("B:" + mView.getSettingValue(mView.SETTING_COLOR_RED_B));
            
            //White
            SeekBar bar4 = (SeekBar)mColorDialog.findViewById(R.id.white_r_seekBar);
            bar4.setMax(MAX_COLOR_VALUE);
            bar4.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar4.setProgress(mView.getSettingValue(mView.SETTING_COLOR_WHITE_R));
            TextView tv4 = (TextView)mColorDialog.findViewById(R.id.white_r_value);
            tv4.setText("R:" + mView.getSettingValue(mView.SETTING_COLOR_WHITE_R));
            
            SeekBar bar5 = (SeekBar)mColorDialog.findViewById(R.id.white_g_seekBar);
            bar5.setMax(MAX_COLOR_VALUE);
            bar5.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar5.setProgress(mView.getSettingValue(mView.SETTING_COLOR_WHITE_G));
            TextView tv5 = (TextView)mColorDialog.findViewById(R.id.white_g_value);
            tv5.setText("G:" + mView.getSettingValue(mView.SETTING_COLOR_WHITE_G));

            SeekBar bar6 = (SeekBar)mColorDialog.findViewById(R.id.white_b_seekBar);
            bar6.setMax(MAX_COLOR_VALUE);
            bar6.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar6.setProgress(mView.getSettingValue(mView.SETTING_COLOR_WHITE_B));
            TextView tv6 = (TextView)mColorDialog.findViewById(R.id.white_b_value);
            tv6.setText("B:" + mView.getSettingValue(mView.SETTING_COLOR_WHITE_B));
            
            //Black
            SeekBar bar7 = (SeekBar)mColorDialog.findViewById(R.id.black_r_seekBar);
            bar7.setMax(MAX_COLOR_VALUE);
            bar7.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar7.setProgress(mView.getSettingValue(mView.SETTING_COLOR_BLACK_R));
            TextView tv7 = (TextView)mColorDialog.findViewById(R.id.black_r_value);
            tv7.setText("R:" + mView.getSettingValue(mView.SETTING_COLOR_BLACK_R));
            
            SeekBar bar8 = (SeekBar)mColorDialog.findViewById(R.id.black_g_seekBar);
            bar8.setMax(MAX_COLOR_VALUE);
            bar8.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar8.setProgress(mView.getSettingValue(mView.SETTING_COLOR_BLACK_G));
            TextView tv8 = (TextView)mColorDialog.findViewById(R.id.black_g_value);
            tv8.setText("G:" + mView.getSettingValue(mView.SETTING_COLOR_BLACK_G));

            SeekBar bar9 = (SeekBar)mColorDialog.findViewById(R.id.black_b_seekBar);
            bar9.setMax(MAX_COLOR_VALUE);
            bar9.setOnSeekBarChangeListener(SapporoidActivity.this);
            bar9.setProgress(mView.getSettingValue(mView.SETTING_COLOR_BLACK_B));
            TextView tv9 = (TextView)mColorDialog.findViewById(R.id.black_b_value);
            tv9.setText("B:" + mView.getSettingValue(mView.SETTING_COLOR_BLACK_B));
        }

        @Override
        public void dismiss() {
            super.dismiss();
        }
    }
    
    private class SettingDialog extends Dialog
    {
        public SettingDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setTitle("Setting");
            this.setContentView(R.layout.setting);
            
            //SeekBar�̏���
            SeekBar sb1 = (SeekBar)mSetting.findViewById(R.id.st1_seekBar_1);
            sb1.setMax(100);
            sb1.setProgress(mView.getSettingValue(mView.SETTING_CONTROL_TIME));
            SeekBar sb2 = (SeekBar)mSetting.findViewById(R.id.st1_seekBar_2);
            sb2.setMax(30);
            sb2.setProgress(mView.getSettingValue(mView.SETTING_TARGET_LEVEL));
            SeekBar sb3 = (SeekBar)mSetting.findViewById(R.id.st1_seekBar_3);
            sb3.setMax(1000);
            sb3.setProgress(mView.getSettingValue(mView.SETTING_SEARCH_TIME));
            SeekBar sb4 = (SeekBar)mSetting.findViewById(R.id.st1_seekBar_4);
            sb4.setMax(200);
            sb4.setProgress(mView.getSettingValue(mView.SETTING_SEARCH_FWD_L) + 100);
            SeekBar sb5 = (SeekBar)mSetting.findViewById(R.id.st1_seekBar_5);
            sb5.setMax(200);
            sb5.setProgress(mView.getSettingValue(mView.SETTING_SEARCH_FWD_R) + 100);
            SeekBar sb6 = (SeekBar)mSetting.findViewById(R.id.st1_seekBar_6);
            sb6.setMax(200);
            sb6.setProgress(mView.getSettingValue(mView.SETTING_SEARCH_TURN_L) + 100);
            SeekBar sb7 = (SeekBar)mSetting.findViewById(R.id.st1_seekBar_7);
            sb7.setMax(200);
            sb7.setProgress(mView.getSettingValue(mView.SETTING_SEARCH_TURN_R) + 100);
            EditText et1 = (EditText)mSetting.findViewById(R.id.editText1);
            et1.setText(String.valueOf(mView.getSettingValue(mView.SETTING_MIRROR_LX)));
            EditText et2 = (EditText)mSetting.findViewById(R.id.editText2);
            et2.setText(String.valueOf(mView.getSettingValue(mView.SETTING_MIRROR_LY)));
            EditText et3 = (EditText)mSetting.findViewById(R.id.editText3);
            et3.setText(String.valueOf(mView.getSettingValue(mView.SETTING_MIRROR_RX)));
            EditText et4 = (EditText)mSetting.findViewById(R.id.editText4);
            et4.setText(String.valueOf(mView.getSettingValue(mView.SETTING_MIRROR_RY)));
            
            
            //TextView�̏���
            TextView tv1 = (TextView)mSetting.findViewById(R.id.st1_bar1_value);
            tv1.setText(String.valueOf(mView.getSettingValue(mView.SETTING_CONTROL_TIME)));
            TextView tv2 = (TextView)mSetting.findViewById(R.id.st1_bar2_value);
            tv2.setText(String.valueOf(mView.getSettingValue(mView.SETTING_TARGET_LEVEL)));
            TextView tv3 = (TextView)mSetting.findViewById(R.id.st1_bar3_value);
            tv3.setText(String.valueOf(mView.getSettingValue(mView.SETTING_SEARCH_TIME)));
            TextView tv4 = (TextView)mSetting.findViewById(R.id.st1_bar4_value);
            tv4.setText(String.valueOf(mView.getSettingValue(mView.SETTING_SEARCH_FWD_L)));
            TextView tv5 = (TextView)mSetting.findViewById(R.id.st1_bar5_value);
            tv5.setText(String.valueOf(mView.getSettingValue(mView.SETTING_SEARCH_FWD_R)));
            TextView tv6 = (TextView)mSetting.findViewById(R.id.st1_bar6_value);
            tv6.setText(String.valueOf(mView.getSettingValue(mView.SETTING_SEARCH_TURN_L)));
            TextView tv7 = (TextView)mSetting.findViewById(R.id.st1_bar7_value);
            tv7.setText(String.valueOf(mView.getSettingValue(mView.SETTING_SEARCH_TURN_R)));
            
            sb1.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb2.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb3.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb4.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb5.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb6.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb7.setOnSeekBarChangeListener(SapporoidActivity.this);
            
        }

        @Override
        public void dismiss() {
            super.dismiss();
        }
    }

    private class Setting2Dialog extends Dialog
    {
        public Setting2Dialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setTitle("Setting2");
            this.setContentView(R.layout.setting2);

            //SeekBar�̏���
            SeekBar sb1 = (SeekBar)mSetting2.findViewById(R.id.st2_seekBar_1);
            sb1.setMax(200);
            sb1.setProgress(mView.getSettingValue(mView.SETTING_MOTOR_POWER) + 100);
            SeekBar sb2 = (SeekBar)mSetting2.findViewById(R.id.st2_seekBar_2);
            sb2.setMax(200);
            sb2.setProgress(mView.getSettingValue(mView.SETTING_MOTOR_POWER_GET) + 100);
            SeekBar sb3 = (SeekBar)mSetting2.findViewById(R.id.st2_seekBar_3);
            sb3.setMax(200);
            sb3.setProgress(mView.getSettingValue(mView.SETTING_DETECT_LINE) + 100);
            SeekBar sb4 = (SeekBar)mSetting2.findViewById(R.id.st2_seekBar_4);
            sb4.setMax(200);
            sb4.setProgress(mView.getSettingValue(mView.SETTING_ESCAPE_STRAIGHT) + 100);
            SeekBar sb5 = (SeekBar)mSetting2.findViewById(R.id.st2_seekBar_5);
            sb5.setMax(200);
            sb5.setProgress(mView.getSettingValue(mView.SETTING_ESCAPE_TURN_L) + 100);
            SeekBar sb6 = (SeekBar)mSetting2.findViewById(R.id.st2_seekBar_6);
            sb6.setMax(200);
            sb6.setProgress(mView.getSettingValue(mView.SETTING_ESCAPE_TURN_R) + 100);
            
            //TextView�̏���
            TextView tv1 = (TextView)mSetting2.findViewById(R.id.st2_bar1_value);
            tv1.setText(String.valueOf(mView.getSettingValue(mView.SETTING_MOTOR_POWER)));
            TextView tv2 = (TextView)mSetting2.findViewById(R.id.st2_bar2_value);
            tv2.setText(String.valueOf(mView.getSettingValue(mView.SETTING_MOTOR_POWER_GET)));
            TextView tv3 = (TextView)mSetting2.findViewById(R.id.st2_bar3_value);
            tv3.setText(String.valueOf(mView.getSettingValue(mView.SETTING_DETECT_LINE)));
            TextView tv4 = (TextView)mSetting2.findViewById(R.id.st2_bar4_value);
            tv4.setText(String.valueOf(mView.getSettingValue(mView.SETTING_ESCAPE_STRAIGHT)));
            TextView tv5 = (TextView)mSetting2.findViewById(R.id.st2_bar5_value);
            tv5.setText(String.valueOf(mView.getSettingValue(mView.SETTING_ESCAPE_TURN_L)));
            TextView tv6 = (TextView)mSetting2.findViewById(R.id.st2_bar6_value);
            tv6.setText(String.valueOf(mView.getSettingValue(mView.SETTING_ESCAPE_TURN_R)));
            
            sb1.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb2.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb3.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb4.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb5.setOnSeekBarChangeListener(SapporoidActivity.this);
            sb6.setOnSeekBarChangeListener(SapporoidActivity.this);
        }

        @Override
        public void dismiss() {
            super.dismiss();
        }
    }

    private class Setting3Dialog extends Dialog
    {
        public Setting3Dialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setTitle("Setting3");
            this.setContentView(R.layout.setting3);

            //RadioButton�̏���
            RadioGroup rg1 = (RadioGroup)mSetting3.findViewById(R.id.radioGroup1);
            RadioGroup rg4 = (RadioGroup)mSetting3.findViewById(R.id.radioGroup4);
            RadioGroup rg5 = (RadioGroup)mSetting3.findViewById(R.id.radioGroup5);
            RadioButton rb;
            
            if( mView.getSettingValue(mView.SETTING_CONTROL_MODE) == 0 ){
                rb = (RadioButton)rg1.findViewById(R.id.radio0);
                rb.setChecked(true);
            }else{
                rb = (RadioButton)rg1.findViewById(R.id.radio1);
                rb.setChecked(true);
            }

            if( mView.getSettingValue(mView.SETTING_HANGER_ORDER) == 0 ){
                rb = (RadioButton)rg4.findViewById(R.id.radio0);
                rb.setChecked(true);
            }else if( mView.getSettingValue(mView.SETTING_HANGER_ORDER) == 1 ){
                rb = (RadioButton)rg4.findViewById(R.id.radio1);
                rb.setChecked(true);
            }else if( mView.getSettingValue(mView.SETTING_HANGER_ORDER) == 2 ){
                rb = (RadioButton)rg4.findViewById(R.id.radio2);
                rb.setChecked(true);
            }else{
                rb = (RadioButton)rg4.findViewById(R.id.radio3);
                rb.setChecked(true);
            }

            if( mView.getSettingValue(mView.SETTING_DISPLAY) == 0 ){
                rb = (RadioButton)rg5.findViewById(R.id.radio0);
                rb.setChecked(true);
            }else if( mView.getSettingValue(mView.SETTING_DISPLAY) == 1 ){
                rb = (RadioButton)rg5.findViewById(R.id.radio1);
                rb.setChecked(true);
            }else{
                rb = (RadioButton)rg5.findViewById(R.id.radio2);
                rb.setChecked(true);
            }
            
            EditText et1 = (EditText)mSetting3.findViewById(R.id.StruggleTime);
            et1.setText(String.valueOf(mView.getSettingValue(mView.SETTING_STRUGGLE_TIME)));
        }

        @Override
        public void dismiss() {
            super.dismiss();
        }
    }
    
    @Override
    public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
        TextView tmpText;
        SharedPreferences.Editor editor = mPref.edit();
        
        if( arg0.getId() == R.id.red_r_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_RED_R, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.red_r_value);
                tmpText.setText("R: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_RED_R), arg1 );
                editor.commit();
        }else if( arg0.getId() == R.id.red_g_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_RED_G, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.red_g_value);
                tmpText.setText("G: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_RED_G), arg1 );
                editor.commit();
        }else if( arg0.getId() == R.id.red_b_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_RED_B, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.red_b_value);
                tmpText.setText("B: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_RED_B), arg1 );
                editor.commit();   
        }else if( arg0.getId() == R.id.white_r_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_WHITE_R, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.white_r_value);
                tmpText.setText("R: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_WHITE_R), arg1 );
                editor.commit();   
        }else if( arg0.getId() == R.id.white_g_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_WHITE_G, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.white_g_value);
                tmpText.setText("G: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_WHITE_G), arg1 );
                editor.commit();   
        }else if( arg0.getId() == R.id.white_b_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_WHITE_B, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.white_b_value);
                tmpText.setText("B: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_WHITE_B), arg1 );
                editor.commit();   
        }else if( arg0.getId() == R.id.black_r_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_BLACK_R, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.black_r_value);
                tmpText.setText("R: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_BLACK_R), arg1 );
                editor.commit();   
        }else if( arg0.getId() == R.id.black_g_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_BLACK_G, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.black_g_value);
                tmpText.setText("G: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_BLACK_G), arg1 );
                editor.commit();   
        }else if( arg0.getId() == R.id.black_b_seekBar ){
                mView.setSettingValue( mView.SETTING_COLOR_BLACK_B, arg1);
                tmpText = (TextView)mColorDialog.findViewById(R.id.black_b_value);
                tmpText.setText("B: " + arg1);
                editor.putInt( String.valueOf(mView.SETTING_COLOR_BLACK_B), arg1 );
                editor.commit();   
        }else if( arg0.getId() == R.id.st1_seekBar_1 ){
                mView.setSettingValue( mView.SETTING_CONTROL_TIME, arg1);
                tmpText = (TextView)mSetting.findViewById(R.id.st1_bar1_value);
                tmpText.setText( String.valueOf(arg1) );
        }else if( arg0.getId() == R.id.st1_seekBar_2 ){
                mView.setSettingValue( mView.SETTING_TARGET_LEVEL, arg1);
                tmpText = (TextView)mSetting.findViewById(R.id.st1_bar2_value);
                tmpText.setText( String.valueOf(arg1) );
        }else if( arg0.getId() == R.id.st1_seekBar_3 ){
                mView.setSettingValue( mView.SETTING_SEARCH_TIME, arg1);
                tmpText = (TextView)mSetting.findViewById(R.id.st1_bar3_value);
                tmpText.setText( String.valueOf(arg1) );
        }else if( arg0.getId() == R.id.st1_seekBar_4 ){
                mView.setSettingValue( mView.SETTING_SEARCH_FWD_L, arg1);
                tmpText = (TextView)mSetting.findViewById(R.id.st1_bar4_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st1_seekBar_5 ){
                mView.setSettingValue( mView.SETTING_SEARCH_FWD_R, arg1);
                tmpText = (TextView)mSetting.findViewById(R.id.st1_bar5_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st1_seekBar_6 ){
                mView.setSettingValue( mView.SETTING_SEARCH_TURN_L, arg1);
                tmpText = (TextView)mSetting.findViewById(R.id.st1_bar6_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st1_seekBar_7 ){
                mView.setSettingValue( mView.SETTING_SEARCH_TURN_R, arg1);
                tmpText = (TextView)mSetting.findViewById(R.id.st1_bar7_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st2_seekBar_1 ){
                mView.setSettingValue( mView.SETTING_MOTOR_POWER, arg1);
                tmpText = (TextView)mSetting2.findViewById(R.id.st2_bar1_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st2_seekBar_2 ){
                mView.setSettingValue( mView.SETTING_MOTOR_POWER_GET, arg1);
                tmpText = (TextView)mSetting2.findViewById(R.id.st2_bar2_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st2_seekBar_3 ){
                mView.setSettingValue( mView.SETTING_DETECT_LINE, arg1);
                tmpText = (TextView)mSetting2.findViewById(R.id.st2_bar3_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st2_seekBar_4 ){
                mView.setSettingValue( mView.SETTING_ESCAPE_STRAIGHT, arg1);
                tmpText = (TextView)mSetting2.findViewById(R.id.st2_bar4_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st2_seekBar_5 ){
                mView.setSettingValue( mView.SETTING_ESCAPE_TURN_L, arg1);
                tmpText = (TextView)mSetting2.findViewById(R.id.st2_bar5_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }else if( arg0.getId() == R.id.st2_seekBar_6 ){
                mView.setSettingValue( mView.SETTING_ESCAPE_TURN_R, arg1);
                tmpText = (TextView)mSetting2.findViewById(R.id.st2_bar6_value);
                tmpText.setText( String.valueOf(arg1 - 100) );
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        
    }    
    
    public FdView getFdView() {
        return mView;
    }
}