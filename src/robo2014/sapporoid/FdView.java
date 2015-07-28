package robo2014.sapporoid;

import org.opencv.android;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

public class FdView extends SampleCvViewBase {
    private final int LEFT     = 0;
    private final int RIGHT     = 1;
    
    //Status
    private final int STS_IDLE       = 0;
    private final int STS_GO         = 1;
    private final int STS_BACK       = 2;
    private final int STS_TURN_LEFT  = 3;
    private final int STS_TURN_RIGHT = 4;
    
    private int MT_30  = 30 / 2;
    private int MT_60  = 60 / 2;
    private int MT_90  = 90 / 2;
    private int MT_120  = 120 / 2;
    private int MT_150  = 150 / 2;
    private int MT_180  = 180 / 2;
    private int MT_360  = 360 / 2;
    
    private final int CONTROL_TIME = 4;     //Back����Œ莞��
    
    private final int BMP_WIDTH = 320;      //��ʃT�C�Y
    private final int BMP_HEIGHT = 240; 
    private final int MINI_SIZE = 20;       //�摜���ʗp�k���T�C�Y
    private final int OFST = 7;             //�^�[�Q�b�g�F���p�I�t�Z�b�g
    private final int TARGET_MAX = 5;      //�F������^�[�Q�b�g�ő吔
    private final int TARGET_LEVEL = 4;     //�^�[�Q�b�g�Ƃ��ĔF������_�̐�
    private final int CIRCLE_MAX = TARGET_MAX;  //�~��`��\�ȍő吔
    private final int HANGER_MAX = 3;       //�i�[�ɍő吔
    private final int HANGER_AREA = 220;    //�i�[�G���AY���W
    private final int FIELD_LINE_MAX = BMP_WIDTH/MINI_SIZE; //�t�B�[���h�F����ő吔
    private int BACK_TIME = 60;  //���C���𓥂񂾏ꍇ�̉������
    
    public int        mSts = STS_IDLE;      //�X�e�[�^�X
    public int        mMoveCnt = 0;
    private Mat                 mRgba;      //�擾����J�����摜
    private Mat                 mMiniMat;   //�摜���ʗp�摜
    private Size                mMiniSize;  //�摜�k���T�C�Y
    private int px = MINI_SIZE;             //�摜���ʗp�k���T�C�Y
    private int py = MINI_SIZE;
    private int r,g,b,cnt;                  //�摜�F���p���[�J���ϐ�
    private final int w = BMP_WIDTH/MINI_SIZE;  //�摜�F���p���[�J���ϐ�
    private final int h = BMP_HEIGHT/MINI_SIZE; //�摜�F���p���[�J���ϐ�
    private Paint               mPaintCircle;   //�^�[�Q�b�g�~�`��y�C���g
    private Paint               mPaintCircle2;   //�^�[�Q�b�g�~�`��y�C���g
    private Paint               mPaintCircle3;   //�^�[�Q�b�g�~�`��y�C���g
    private Paint               mPaintText;     //�^�[�Q�b�g�~�`��y�C���g
    private Paint               mPaintLine;     //�^�[�Q�b�g��`��y�C���g
    private Paint               mPaintField;    //�t�B�[���h��`��y�C���g
    private int [] mBuffer;                 //�J�����摜�擾�o�b�t�@
    private int [] mLink;                   //�摜���ʗp�o�b�t�@(�_�ɐԂ��אڂ���ꍇ�Ɋi�[)
    private int [] mTarget;                 //�^�[�Q�b�g�i�[�p�o�b�t�@
    private int [] mTargetKind;             //�^�[�Q�b�g�T�C�Y�i�[�p�o�b�t�@(�Ԃ��_�̏W�܂���܂Ƃ߂�)
    private int mTgtCnt;                    //�^�[�Q�b�g��
    private Rect [] mRects = new Rect[CIRCLE_MAX];  //�^�[�Q�b�g�~�i�[
    private boolean [] mbHanger = { false, false, false };  //�i�[�ɂ̃^�[�Q�b�g�L���t���O
    private boolean [] mbTargetHanger = { false, false, false };  //�^�[�Q�b�g�̊i�[�Ɋi�[�ς݃t���O
    private boolean [] mbHandBring = { false, false };
    
    private int [] miHanger = { -1, -1, -1 };   //�i�[�ς݃^�[�Q�b�gID
    private int [] mMirror = {0, 0};            //���C������p
    private final int MIRROR_CNT = 1;           //���C�������
    
    private long now = 0;                       //���{���䎞�Ԕ���p
    private int[] mControlList = {0,0,0,0};     //���{����p���X�g
    private int mTempCnt = 0;
    private int mEscapeTime = 0;
    private int mEscapeMode = STS_IDLE;
    private int mStep4_LR = LEFT;
    
    int mBackTimer = 0;         //���C���𓥂񂾏ꍇ�̉������
    int mTargetCnt = 0;         //�^�[�Q�b�g�����������ꍇ�̏�������
    private int mTargetLevel = TARGET_LEVEL;    //�^�[�Q�b�g�Ƃ��ĔF������_�̐�
    
    int mControlTime = CONTROL_TIME;        //���{�R���g���[������
    
    int mMotorPower = 80;
    int mMotorPowerGet = 70;
    int mDetectLine = 70;
    int mEscapeStraight = -80;
    int mEscapeTurnL = 60;
    int mEscapeTurnR = -60;
    
    private int[] mSendList = null;
    private int mDebugFlag = 0;
    
    public static final int SETTING_COLOR_RED_R = 0;
    public static final int SETTING_COLOR_RED_G = 1;
    public static final int SETTING_COLOR_RED_B = 2;
    public final int SETTING_COLOR_WHITE_R = 3;
    public final int SETTING_COLOR_WHITE_G = 4;
    public final int SETTING_COLOR_WHITE_B = 5;
    public final int SETTING_COLOR_BLACK_R = 6;
    public final int SETTING_COLOR_BLACK_G = 7;
    public final int SETTING_COLOR_BLACK_B = 8;
    public final int SETTING_CONTROL_TIME = 9;      //���{�R���g���[�����Ԑݒ�p
    public final int SETTING_SEARCH_TIME = 10;        //�^�[�Q�b�g�����������ꍇ�̏������Ԑݒ�p
    public final int SETTING_TARGET_LEVEL = 11;      //�^�[�Q�b�g�Ƃ��ĔF������_�̐��ݒ�p
    public final int SETTING_SEARCH_FWD_L = 12;           //�^�[�Q�b�g�����������ꍇ�̏����@�����[�^�o�͐ݒ�p
    public final int SETTING_SEARCH_FWD_R = 13;           //�^�[�Q�b�g�����������ꍇ�̏����@�����[�^�o�͐ݒ�p
    public final int SETTING_SEARCH_TURN_L = 14;          //�^�[�Q�b�g�����������ꍇ�̏����A�����[�^�o�͐ݒ�p
    public final int SETTING_SEARCH_TURN_R = 15;          //�^�[�Q�b�g�����������ꍇ�̏����A�����[�^�o�͐ݒ�p
    public final int SETTING_MIRROR_LX = 16;         //���C���F�����W�ݒ�p
    public final int SETTING_MIRROR_LY = 17;         //���C���F�����W�ݒ�p
    public final int SETTING_MIRROR_RX = 18;         //���C���F�����W�ݒ�p
    public final int SETTING_MIRROR_RY = 19;        //���C���F�����W�ݒ�p
    public static final int SETTING_MOTOR_POWER = 20;      //���{�R���g���[�����Ԑݒ�p
    public static final int SETTING_MOTOR_POWER_GET = 21;      //���{�R���g���[�����Ԑݒ�p
    public static final int SETTING_DETECT_LINE = 22;      //���{�R���g���[�����Ԑݒ�p
    public static final int SETTING_ESCAPE_STRAIGHT = 23;      //���{�R���g���[�����Ԑݒ�p
    public static final int SETTING_ESCAPE_TURN_L = 24;      //���{�R���g���[�����Ԑݒ�p
    public final int SETTING_ESCAPE_TURN_R = 25;      //���{�R���g���[�����Ԑݒ�p
    public final int SETTING_CONTROL_MODE = 26;
    public final int SETTING_STRUGGLE_TIME = 27;
    public final int SETTING_HANGER_ORDER = 28;
    public final int SETTING_DISPLAY = 29;
    public static final int SETTING_MT30 = 30;
    public static final int SETTING_MT60 = 31;
    public static final int SETTING_MT90 = 32;
    public static final int SETTING_MT180 = 33;
    public static final int SETTING_MT360 = 34;
    public static final int SETTING_HAND_GET_RANGE = 35;
    public static final int SETTING_LINE_DETECT = 36;
    public static final int SETTING_STEP4_LR = 37;
    
    //�F���ʗp�ϐ�
    //�Ԏ��ʗp
    private int m_Value_r_R = 200;              //�ԔF���p
    private int m_Value_r_G = 50;               //�ԔF���p
    private int m_Value_r_B = 50;               //�ԔF���p
    //�����ʗp
    private int m_Value_w_R = 200;              //���F���p
    private int m_Value_w_G = 200;              //���F���p
    private int m_Value_w_B = 200;              //���F���p
    //�����ʗp 
    private int m_Value_b_R = 50;               //���F���p
    private int m_Value_b_G = 50;               //���F���p
    private int m_Value_b_B = 50;               //���F���p
    //��`�l
    static final int PROGRESS_SET_RGB_RED = 1;      //�ԔF���ݒ�p
    static final int PROGRESS_SET_RGB_WHITE = 2;    //���F���ݒ�p
    static final int PROGRESS_SET_RGB_BLACK = 3;    //���F���ݒ�p

    private int mControlMode = 0;
    private int mStruggleTime = 30;
    private int mHangerOrder = 1;
    private long mGetCountTime = 0;
    
    private final int CENTER_X = 8;
    private final int CENTER_Y = 8;
    private final int LEFT_NEAR_X = 3;
    private final int LEFT_NEAR_Y = 8;
    private final int RIGHT_NEAR_X = 13;
    private final int RIGHT_NEAR_Y = 8;
    private final int CENTER_FAR_X = 8;
    private final int CENTER_FAR_Y = 3;
    private final int LEFT_FAR_X = 3;
    private final int LEFT_FAR_Y = 3;
    private final int RIGHT_FAR_X = 13;
    private final int RIGHT_FAR_Y = 3;
    private int[] detect_Red = { 0, 0 };
    private int[] detect_near_center = { 0, 0 };
    private int[] detect_far = { -1, 0 };
    private int[][] detect_far_array = {
    		{ 0, 0 },	//center
    		{ 0, 0 },	//left
    		{ 0, 0 },	//right
    		{ 0, 0 },	//near left
    		{ 0, 0 },	//near right
    };
    
    //���[�^�R���g���[�����C�u����
    public native int MotorControl(int[] list);
    static {
        System.loadLibrary("MotorControl");
    }
    
    public FdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public FdView(Context context) {
        super(context);
    }

    @Override
    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        super.surfaceChanged(_holder, format, width, height);

        for(int i=0; i<CIRCLE_MAX; i++ ){
            mRects[i] = new Rect();
        }
        
        mPaintCircle = new Paint();
        mPaintCircle.setColor(Color.GREEN);
        mPaintCircle.setStyle(Paint.Style.STROKE);
        mPaintCircle.setStrokeWidth( 5.0f );
        mPaintText = new Paint();
        mPaintText.setColor(Color.GREEN);
        mPaintLine = new Paint();
        mPaintLine.setColor(Color.YELLOW);
        mPaintField = new Paint();
        mPaintField.setColor(Color.BLUE);

        mMiniSize = new Size( BMP_WIDTH/MINI_SIZE, BMP_HEIGHT/MINI_SIZE );
        mBuffer = new int[(BMP_WIDTH/MINI_SIZE)*(BMP_HEIGHT/MINI_SIZE)];
        mLink = new int[(BMP_WIDTH/MINI_SIZE)*(BMP_HEIGHT/MINI_SIZE)];
        mTarget = new int[(BMP_WIDTH/MINI_SIZE)*(BMP_HEIGHT/MINI_SIZE)];
        mTargetKind = new int[TARGET_MAX];
        
        mPaintCircle2 = new Paint();
        mPaintCircle2.setColor(Color.RED);
        mPaintCircle2.setStyle(Paint.Style.STROKE);
        mPaintCircle2.setStrokeWidth( 5.0f );

        mPaintCircle3 = new Paint();
        mPaintCircle3.setColor(Color.YELLOW);
        mPaintCircle3.setStyle(Paint.Style.STROKE);
        mPaintCircle3.setStrokeWidth( 5.0f );

        synchronized (this) {
            // initialize Mats before usage
            mRgba = new Mat();
            mMiniMat = new Mat();
        }
    }

    private void playSound( String sound ){
        Intent intent = new Intent(SapporoidActivity.ACT_SOUND);
        intent.putExtra("file", sound);
        getContext().sendBroadcast(intent);
    }
    
    public void setDebugFlag( int flag ){
        mDebugFlag = flag;
        switch( mDebugFlag ){
            case 0:
                mbHandBring[LEFT] = false;
                mbHandBring[RIGHT] = false;
                break;
            case 1:
                mbHandBring[LEFT] = true;
                mbHandBring[RIGHT] = false;
                break;
            case 2:
                mbHandBring[LEFT] = false;
                mbHandBring[RIGHT] = true;
                break;
            case 3:
                mbHandBring[LEFT] = true;
                mbHandBring[RIGHT] = true;
                break;
            default:
        }
    }
    
    private void setStatus( int sts ){
    	mSts = sts;
    }
    
    @Override
    protected Bitmap processFrame(VideoCapture capture) {
        if( mCountDown != 0 ){
//            mNow = System.currentTimeMillis();
//            if( (mNow - mCountDown)/1000 > 4 ){
                mCountDown = 0;
                setStatus(STS_GO);
//            }
        }
        
        capture.retrieve(mRgba, Highgui.CV_CAP_ANDROID_COLOR_FRAME_BGRA);
        Imgproc.resize(mRgba, mMiniMat, mMiniSize);
        
        Bitmap bmp = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Bitmap bmp2 = Bitmap.createBitmap(mMiniMat.cols(), mMiniMat.rows(), Bitmap.Config.ARGB_8888);
        
        checkActionDone();

        if (android.MatToBitmap(mRgba, bmp)){
//            if( bmp != null ){
//                return bmp;
//            }
            if( android.MatToBitmap(mMiniMat, bmp2) ){
                Canvas canvas = new Canvas(bmp);
                bmp2.getPixels(mBuffer, 0, w, 0, 0, w, h);
                mTgtCnt = 0;
                cnt = 0;
                
                //�F����
//                for( int y=0; y<h; y++ ){
//                    for( int x=0; x<w; x++ ){
//                        r = Color.red(mBuffer[x + (y*w)]);
//                        g = Color.green(mBuffer[x + (y*w)]);
//                        b = Color.blue(mBuffer[x + (y*w)]);
//                        
//                        if( r < m_Value_b_R && g < m_Value_b_G && b < m_Value_b_B ){
//                        	
//                        	
//                        	
//                        }
//                    }
//                }
                ;
                if( detectRed() ){
                	canvas.drawCircle( detect_Red[0] * MINI_SIZE, detect_Red[1] * MINI_SIZE, MINI_SIZE, mPaintCircle3);
                }else{
	                //detect center Line
	                int [] center = detect( CENTER_X, CENTER_Y, 3 );
	                if( center[0] > 0 ){
	                	detect_near_center[0] = center[0];
	                	detect_near_center[1] = center[1];
	                }else{
	                	//lost
	                	detect_near_center[0] = -1;
	                }
	                
	                //detect far Line
	                int [] far = detect( CENTER_FAR_X, CENTER_FAR_Y, 3 );
	                if( far[0] > 0 ){
	                	detect_far_array[0][0] = far[0];
	                	detect_far_array[0][1] = far[1];
	                }else{
	                	detect_far_array[0][0] = -1;
	                }
	            	far = detect( LEFT_FAR_X, LEFT_FAR_Y, 3 );
	                if( far[0] > 0 ){
	                	detect_far_array[1][0] = far[0];
	                	detect_far_array[1][1] = far[1];
	                }else{
	                	detect_far_array[1][0] = -1;
	                }
	            	far = detect( RIGHT_FAR_X, RIGHT_FAR_Y, 3 );
	                if( far[0] > 0 ){
	                	detect_far_array[2][0] = far[0];
	                	detect_far_array[2][1] = far[1];
	                }else{
	                	detect_far_array[2][0] = -1;
	                }
	            	far = detect( LEFT_NEAR_X, LEFT_NEAR_Y, 3 );
	                if( far[0] > 0 ){
	                	detect_far_array[3][0] = far[0];
	                	detect_far_array[3][1] = far[1];
	                }else{
	                	detect_far_array[3][0] = -1;
	                }
	            	far = detect( RIGHT_NEAR_X, RIGHT_NEAR_Y, 3 );
	                if( far[0] > 0 ){
	                	detect_far_array[4][0] = far[0];
	                	detect_far_array[4][1] = far[1];
	                }else{
	                	detect_far_array[4][0] = -1;
	                }
	                
	                if( detect_near_center[0] > 0 ){
	                	canvas.drawCircle( detect_near_center[0] * MINI_SIZE, detect_near_center[1] * MINI_SIZE, MINI_SIZE, mPaintCircle);
	                }
	                detect_far = getDetectFar(); 
	                if( detect_far[0] > 0 ){
	                	canvas.drawCircle( detect_far[0] * MINI_SIZE, detect_far[1] * MINI_SIZE, MINI_SIZE, mPaintCircle2);
	                }
                }
            }
                
//            if( !isAction ){
                //�w�莞�Ԃ��Ƃɐ��䂷��B
                if( System.currentTimeMillis() - now > 500 ){
                	if( mManual != 1 ){
                		RobotControl( );
                	}
                    now = System.currentTimeMillis();
                }
//            }
            return bmp;
        }

        bmp.recycle();
        bmp2.recycle();
        return null;
    }
    
    private int[] getDetectFar(){
    	int max = -1;
    	int tmp = 0;
    	for( int i=0; i<5; i++ ){
    		if( detect_far_array[i][0] > 0 ){
    			tmp = Math.abs(detect_far_array[i][0] - CENTER_X);
    			if( tmp > max && (max < 0 || i < 3 )){
    				max = i;
    			}
    		}
    	}
    	
    	if( max < 0 ){
    		return new int[]{ -1, 0 };
    	}else{
    		return detect_far_array[max];
    	}
    }
    
    private boolean isBlack( int x, int y ){
    	boolean ret = false;
    	int i = x + (y*w);
    	if( i < 0 || i >= mBuffer.length ) return false;
    	
        r = Color.red(mBuffer[i]);
        g = Color.green(mBuffer[i]);
        b = Color.blue(mBuffer[i]);
        if( r < m_Value_b_R && g < m_Value_b_G && b < m_Value_b_B ){
        	ret = true;
        }
        return ret;
    }
    
    private boolean isRed( int x, int y ){
    	boolean ret = false;
    	int i = x + (y*w);
    	if( i < 0 || i >= mBuffer.length ) return false;
    	
        r = Color.red(mBuffer[i]);
        g = Color.green(mBuffer[i]);
        b = Color.blue(mBuffer[i]);
        if( r > m_Value_r_R && r > g + m_Value_r_G && r > b + m_Value_r_B ){
        	ret = true;
        }
        return ret;
    }

    private boolean detectRed(){
        detect_Red[0] = -1;
        detect_Red[1] = -1;
        for( int y=h-1; y>-1; y-- ){
            for( int x=w-1; x>-1; x-- ){
            	if( isRed( x, y ) ){
            		detect_Red[0] = x;
            		detect_Red[1] = y;
            		return true;
            	}
            }
        }
    	return false;
    }
    
    private int [] detect( int centerX, int centerY, int size){
    	int [] ret = {-1, 0};
    	
        for( int y=0; y<size; y++ ){
            for( int x=0; x<size; x++ ){
            	if( isBlack( centerX + x, centerY + y ) ){
            		if( ret[0] < 0 || 
            			Math.abs(ret[0] - CENTER_X) < Math.abs(centerX + x - CENTER_X) ){
            			ret[0] = centerX + x;
            			ret[1] = centerY + y;
            		}
            	}
            	if( isBlack( centerX - x, centerY + y ) ){
            		if( ret[0] < 0 || 
                			Math.abs(ret[0] - CENTER_X) < Math.abs(centerX + x - CENTER_X) ){
	            		ret[0] = centerX - x;
	            		ret[1] = centerY + y;
            		}
            	}
            	if( isBlack( centerX - x, centerY - y ) ){
            		if( ret[0] < 0 || 
                			Math.abs(ret[0] - CENTER_X) < Math.abs(centerX + x - CENTER_X) ){
	            		ret[0] = centerX - x;
	            		ret[1] = centerY - y;
            		}
            	}
            	if( isBlack( centerX + x, centerY - y ) ){
            		if( ret[0] < 0 || 
                			Math.abs(ret[0] - CENTER_X) < Math.abs(centerX + x - CENTER_X) ){
	            		ret[0] = centerX + x;
	            		ret[1] = centerY - y;
            		}
            	}
            }
        }
        
        return ret;
    }
    
    public void forceStop(){
    	//stop
    }
    
    public void StartCountdown( boolean set ){
        if( set == true ){
            mCountDown = System.currentTimeMillis();
        }else{
            mCountDown = 0;
            //stop
            setStatus(STS_IDLE);
        }
    }
    
    public void setManual( int set ){
    	mManual = set;
    }
    
    public void actManual( int act ){
    	mRoboControl.actionNext(act);
    	mGameStep = act;
    }
    
    @Override
    public void run() {
        super.run();

        synchronized (this) {
            // Explicitly deallocate Mats
            if (mRgba != null)
                mRgba.dispose();
            if (mMiniMat != null)
                mMiniMat.dispose();
            
            mRgba = null;
            mMiniMat = null;
        }
    }
    
    //���{�b�g����
    private void RobotControl(){
        boolean bReturn = false;
        int act = Robo.ACT_SEARCH;
        
        if( detect_Red[0] >= 0){
        	if( detect_Red[0] < w/3 ){
        		mStatus = 11;
        		act = Robo.ACT_TURN_LEFT;
        	}else if( detect_Red[0] > (w/3)*2 ){
        		mStatus = 12;
    			act = Robo.ACT_TURN_RIGHT;
    		}else{
        		mStatus = 13;
        		act = Robo.ACT_WALK2;
    		}
        }else{
	        if( detect_near_center[0] < 0 && detect_far[0] < 0 ){
	        	act = Robo.ACT_SEARCH;
	        	mStatus = 0;
	        }else if( detect_near_center[0] >= 0 && detect_far[0] < 0 ){
	        	act = Robo.ACT_WALK2;
        		mStatus = 1;
	        }else if( detect_near_center[0] < 0 && detect_far[0] >= 0l ){
	        	if( detect_far[1] > 5 ){
            	    if( detect_far[0] < (w/3)){
	        			act = Robo.ACT_TURN_LEFT;
	            		mStatus = 2;
	            	}else if( detect_far[0] > (w/3)*2 ){
	        			act = Robo.ACT_TURN_RIGHT;
	            		mStatus = 3;
	            	}else{
	            		mStatus = 4;
	            		act = Robo.ACT_WALK2;
	        		}
	        	}else{
	        		if( detect_far[0] <  6 ){
	            		mStatus = 5;
	        			act = Robo.ACT_WALK_LEFT;
	        		}else if( detect_far[0] >  10 ){
	            		mStatus = 6;
	        			act = Robo.ACT_WALK_RIGHT;
	        		}else{
	            		mStatus = 7;
	        			act = Robo.ACT_WALK2;
	        		}
	        	}
	        }else if( detect_near_center[0] >= 0 && detect_far[0] >= 0){
	    		if( detect_far[0] <  6 ){
            		mStatus = 8;
	    			act = Robo.ACT_WALK_LEFT;
	    		}else if( detect_far[0] >  10 ){
            		mStatus = 9;
	    			act = Robo.ACT_WALK_RIGHT;
	    		}else{
            		mStatus = 10;
	    			act = Robo.ACT_WALK2;
	    		}
	        }
        }
        
        mRoboControl.actionNext(act);
        mGameStep = act;
    }
    
    private void LogOut( String str ){
        Log.d("TANK_TEST", str );
    }
    
    //�e��ݒ�l�ݒ�
    public int getSettingValue( int type ){
        int ret = 0;
        
        switch(type){
            case SETTING_COLOR_RED_R:
                ret = m_Value_r_R;
                break;
            case SETTING_COLOR_RED_G:
                ret = m_Value_r_G;
                break;
            case SETTING_COLOR_RED_B:
                ret = m_Value_r_B;
                break;
            case SETTING_COLOR_WHITE_R:
                ret = m_Value_w_R;
                break;
            case SETTING_COLOR_WHITE_G:
                ret = m_Value_w_G;
                break;
            case SETTING_COLOR_WHITE_B:
                ret = m_Value_w_B;
                break;
            case SETTING_COLOR_BLACK_R:
                ret = m_Value_b_R;
                break;
            case SETTING_COLOR_BLACK_G:
                ret = m_Value_b_G;
                break;
            case SETTING_COLOR_BLACK_B:
                ret = m_Value_b_B;
                break;
            case SETTING_CONTROL_TIME:
                ret = mControlTime;
                break;
            case SETTING_SEARCH_TIME:
                break;
            case SETTING_TARGET_LEVEL:
                ret = mTargetLevel;
                break;
            case SETTING_SEARCH_FWD_L:
                break;
            case SETTING_SEARCH_FWD_R:
                break;
            case SETTING_SEARCH_TURN_L:
                break;
            case SETTING_SEARCH_TURN_R:
                break;
            case SETTING_MIRROR_LX:
                break;
            case SETTING_MIRROR_LY:
                break;
            case SETTING_MIRROR_RX:
                break;
            case SETTING_MIRROR_RY:
                break;
            case SETTING_MOTOR_POWER:
                ret = mMotorPower;
                break;
            case SETTING_MOTOR_POWER_GET:
                ret = mMotorPowerGet;
                break;
            case SETTING_DETECT_LINE:
                ret = mDetectLine;
                break;
            case SETTING_ESCAPE_STRAIGHT:
                ret = mEscapeStraight;
                break;
            case SETTING_ESCAPE_TURN_L:
                ret = mEscapeTurnL;
                break;
            case SETTING_ESCAPE_TURN_R:
                ret = mEscapeTurnR;
                break;
            case SETTING_CONTROL_MODE:
                ret = mControlMode;
                break;
            case SETTING_STRUGGLE_TIME:
                ret = mStruggleTime;
                break;
            case SETTING_HANGER_ORDER:
                ret = mHangerOrder;
                break;
            case SETTING_DISPLAY:
                ret = mDisplay;
                break;
            case SETTING_MT30:
                ret = MT_30;
                break;
            case SETTING_MT60:
                ret = MT_60;
                break;
            case SETTING_MT90:
                ret = MT_90;
                break;
            case SETTING_MT180:
                ret = MT_180;
                break;
            case SETTING_MT360:
                ret = MT_360;
                break;
            case SETTING_HAND_GET_RANGE:
                break;
            case SETTING_LINE_DETECT:
                break;
            case SETTING_STEP4_LR:
                ret = mStep4_LR;
                break;
            default:
                break;
        }                
        return ret;
    }
    
    //�e��ݒ�l�ݒ�
    public void setSettingValue(int type, int val){
        switch(type){
            case SETTING_COLOR_RED_R:
                m_Value_r_R = val;
                break;
            case SETTING_COLOR_RED_G:
                m_Value_r_G = val;
                break;
            case SETTING_COLOR_RED_B:
                m_Value_r_B = val;
                break;
            case SETTING_COLOR_WHITE_R:
                m_Value_w_R = val;
                break;
            case SETTING_COLOR_WHITE_G:
                m_Value_w_G = val;
                break;
            case SETTING_COLOR_WHITE_B:
                m_Value_w_B = val;
                break;
            case SETTING_COLOR_BLACK_R:
                m_Value_b_R = val;
                break;
            case SETTING_COLOR_BLACK_G:
                m_Value_b_G = val;
                break;
            case SETTING_COLOR_BLACK_B:
                m_Value_b_B = val;
                break;
            case SETTING_CONTROL_TIME:
                mControlTime = val;
                break;
            case SETTING_SEARCH_TIME:
                break;
            case SETTING_TARGET_LEVEL:
                mTargetLevel = val;
                break;
            case SETTING_SEARCH_FWD_L:
                break;
            case SETTING_SEARCH_FWD_R:
                break;
            case SETTING_SEARCH_TURN_L:
                break;
            case SETTING_SEARCH_TURN_R:
                break;
            case SETTING_MIRROR_LX:
                break;
            case SETTING_MIRROR_LY:
                break;
            case SETTING_MIRROR_RX:
                break;
            case SETTING_MIRROR_RY:
                break;
            case SETTING_MOTOR_POWER:
                mMotorPower = val;
                break;
            case SETTING_MOTOR_POWER_GET:
                mMotorPowerGet = val;
                break;
            case SETTING_DETECT_LINE:
                mDetectLine = val;
                break;
            case SETTING_ESCAPE_STRAIGHT:
                mEscapeStraight = val;
                break;
            case SETTING_ESCAPE_TURN_L:
                mEscapeTurnL = val;
                break;
            case SETTING_ESCAPE_TURN_R:
                mEscapeTurnR = val;
                break;
            case SETTING_CONTROL_MODE:
                mControlMode = val;
                break;
            case SETTING_STRUGGLE_TIME:
                mStruggleTime = val;
                break;
            case SETTING_HANGER_ORDER:
                mHangerOrder = val;
                break;
            case SETTING_DISPLAY:
                mDisplay = val;
                break;
            case SETTING_MT30:
                MT_30 = val;
                break;
            case SETTING_MT60:
                MT_60 = val;
                break;
            case SETTING_MT90:
                MT_90 = val;
                break;
            case SETTING_MT180:
                MT_180 = val;
                break;
            case SETTING_MT360:
                MT_360 = val;
                break;
            case SETTING_HAND_GET_RANGE:
                break;
            case SETTING_LINE_DETECT:
                break;
            case SETTING_STEP4_LR:
                mStep4_LR = val;
                LogOut("Step4 LR = " + mStep4_LR);
                break;
           default:
                break;
        }
    }

//################### Sapporoid2013 ####################
    public final int SETTING_CAM_CENTER_X = 30;
    public final int SETTING_CAM_CENTER_Y = 31;
    public final int SETTING_DOWN_CENTER_X = 32;
    public final int SETTING_DOWN_CENTER_Y = 33;
    public final int SETTING_DETECT_FIELD = 34;
    public final int SETTING_LEFTHAND_AREA = 35;
    public final int SETTING_RIGHTHAND_AREA = 36;
    public final int SETTING_LEFTOUTNEAR_AREA = 37;
    public final int SETTING_RIGHTOUTNEAR_AREA = 38;
    public final int SETTING_CENTERNEAR_AREA = 39;
    public final int SETTING_LEFTNEAR_AREA = 40;
    public final int SETTING_RIGHTNEAR_AREA = 41;
    public final int SETTING_CENTERFAR_AREA = 42;
    public final int SETTING_LEFTOUTFAR_AREA = 43;
    public final int SETTING_RIGHTOUTFAR_AREA = 44;
    public final int SETTING_LEFTFAR_AREA = 45;
    public final int SETTING_RIGHTFAR_AREA = 46;
    public final int SETTING_LEFTHANDREJECT_AREA = 47;
    public final int SETTING_DOWN_AREA1 = 48;
    public final int SETTING_DOWN_AREA2 = 49;
    public final int SETTING_DOWN_AREA3 = 50;
    public final int SETTING_DOWN_AREA4 = 51;
    
    private RobotController mRoboControl = null;
    static int [] mSettingDetectField = {20,21,25,21};
    static int [] mSettingLefthandArea = {-36,16,-22,35};
    static int [] mSettingRighthandArea = {22,16,36,35};
    static int [] mSettingLeftoutnearArea = {-47,12,-32,27};
    static int [] mSettingRightoutnearArea = {36,8,51,30};
    static int [] mSettingCenternearArea = {-27,26,17,48};
    static int [] mSettingLeftnearArea = {-47,26,-27,48};
    static int [] mSettingRightnearArea = {17,26,51,48};
    static int [] mSettingCenterfarArea = {-32,48,18,100};
    static int [] mSettingLeftoutfarArea = {-78,12,-47,27};
    static int [] mSettingRightoutfarArea = {51,8,70,30};
    static int [] mSettingLeftfarArea = {-78,26,-32,100};
    static int [] mSettingRightfarArea = {18,26,70,100};
    static int [] mSettingLefthandrejectArea = {-80,16,-20,50};
    static int [] mSettingDownArea1 = {-50,0,-45,5};
    static int [] mSettingDownArea2 = {45,0,50,5};
    static int [] mSettingDownArea3 = {-50,0,-45,45};
    static int [] mSettingDownArea4 = {45,0,50,45};
    private boolean isAction = false;
   
    public void setRobotController( RobotController robo ){
        mRoboControl = robo;
    }
    
    private void checkActionDone(){
        if( mRoboControl.isActionDone() ){
            isAction = false;
        }
    }
    
    public void setMotionEdit( String str ){
    	mRoboControl.setMotionEdit(str);
    }
}