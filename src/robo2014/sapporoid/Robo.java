package robo2014.sapporoid;

import android.content.Context;

public class Robo {
    public static final int MODE_STOP        = 0;
    public static final int MODE_NORMAL      = 1;
    public static final int MODE_WALK        = 2;
    public static final int MODE_RUN         = 3;
    public static final int MODE_WAKEUP_UP   = 4;
    public static final int MODE_WAKEUP_DOWN = 5;
    public static final int MODE_SEARCH      = 6;
    public static final int MODE_LIFT        = 7;
    public static final int MODE_CARRY       = 8;
    public static final int MODE_DROP        = 9;
    public static final int MODE_MAX         = 10;
    
    private static final int STS_NORMAL         = 0;
    private static final int STS_STAND_BOTH     = 0;
    private static final int STS_STAND_LEFT     = 1;
    private static final int STS_STAND_L_FW_R   = 1;
    private static final int STS_STAND_RIGHT    = 2;
    private static final int STS_STAND_CROUNCH  = 2;
    private static final int STS_STAND          = 100;
    private static final int STS_LAY_UP         = 101;
    private static final int STS_LAY_DOWN       = 101;
    private static final int STS_LAY_LEFT       = 101;
    private static final int STS_LAY_RIGHT      = 101;
    private static final int STS_LAY            = 200;
    
	public static final int ACT_DEFAULT         = 0;
	public static final int ACT_PRE_WALK        = 1;
	public static final int ACT_WALK1           = 2;
	public static final int ACT_WALK2           = 3;
	public static final int ACT_TURN_LEFT       = 4;
	public static final int ACT_TURN_RIGHT      = 5;
	public static final int ACT_SEARCH          = 4;
	public static final int ACT_WALK_LEFT       = 4;
	public static final int ACT_WALK_RIGHT      = 5;

    private static final String DEF_LEG = "def_leg";
    private static final String DEF_ARM = "def_arm";

    private SensorControl mSensor = null;
    private myThread mThread = null;
    private int mMode = MODE_NORMAL;
    private int mNextMode = MODE_NORMAL;
    private boolean isMoving = false;
    private int mSpeedMargin = 1000;
    private int mSpeedTimes = 10;

    
    private static final int ACT_STS_STOP    = 0;
    private static final int ACT_STS_DO      = 1;
    private static final int ACT_STS_DONE    = 2;
    
    private int mActFlag = ACT_STS_STOP;
    private int mSetSpeed = 0;
    private long mMoveTime = 0;
    private int mAction = -1;
    
	public native byte[] Walk(String list);
    static {
        System.loadLibrary("MotorControl");
    }

    public Robo( Context context ){
//        mSensor = SensorControl.getInstance();
//        mSensor.initSensor( context );
                
        mThread = new myThread();
        mThread.setPriority(Thread.MAX_PRIORITY);
        mThread.start();
    }
    
    public void destroyRobot(){
//        mSensor.finish();
        if( mThread != null ){
            mThread.stopRun();
        }
    }
    
    public void setMode( int mode ){
        mNextMode = mode;
    }
    
    public void MoveStop(){
    }
    
//    public int getPosture(){
//        return mSensor.getPosture();
//    }
    
    public boolean isActionDone(){
        boolean ret = false;
        
        if( mActFlag == ACT_STS_DONE ){
            ret = true;
            mActFlag = ACT_STS_STOP;
        }
        
        return ret;
    }
    
    public int setMoveAction( int action ){
        int iRet = 0;
        mAction = action;
        return iRet;
    }
    
    public void forceStop(){
        mMode = MODE_STOP;
    	String action = "8";
    	write(action);
    }
    
    public String write( String act ){
//    	String [] list = {
//    			"3,3,",
//    			"1,3,512,256,912,644,512,512,512,768,112,380,512,512,50",
//    			"1,3,1,2,3,4,5,6,7,8,9,10,11,12,13"
//    	};
//    	byte[] dst = Walk(list[act]);
    	byte[] dst = Walk( act );
    	String tmp = new String(dst);
    	int end = tmp.indexOf(10);
    	if( end > 0 ){
    		return tmp.substring(0, end);
    	}else{
    		return "";
    	}
    }
    
    private class myThread extends Thread{
        public boolean mRun = false;
        
        public myThread(){
            mRun = true;
        }
        
        public void stopRun(){
            mRun = false;
        }

        public void run() {
            while(true){
                if( mRun ){
//                    synchronized (this){
                        if( isMoving ){
                            long time = System.currentTimeMillis();
                            if( mMoveTime != 0 &&
                                ( time > mMoveTime + 1000) ){
                                isMoving = false;
                            }
                        }else{
                            if( action() ){
                                mMoveTime = System.currentTimeMillis();
                                isMoving = true;
                                mActFlag = ACT_STS_DO;
                            }else if( mActFlag == ACT_STS_DO ){
                                mActFlag = ACT_STS_DONE;
                            }
                        }
//                    }
                }
            }
        }
        
        private boolean action(){
            boolean bRet = false;
            
            if( mAction >= 0 ){
            	String action = "7," + String.valueOf(mAction) + ",1";
            	write(action);
            	mAction = -1;
            	bRet = true;
            }
            
            return bRet;
        }
    }
    
    public void setMotionEdit( String action ){
    	               //cmd,wait,c,1  ,2  ,3   ,4  ,5   ,6  ,7   ,8  ,9   ,10  
//    	String action = "11,3000,10,0,1,1,1,3,10,4,4,3,10,5,4,3,10,5,4,3,10,0,1";
//    	String action = "11,3000,10,0,1,1,1,3,30,4,5,3,20,5,4,3,2,4,3,5,3,3,10";
    	write(action);
    }
}
