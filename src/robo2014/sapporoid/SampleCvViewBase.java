package robo2014.sapporoid;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.VideoCapture;
import org.opencv.highgui.Highgui;

import robo2014.sapporoid.webserver.SapporoidHTTPD;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public abstract class SampleCvViewBase extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "Sample::SurfaceView";
    private boolean DEBUG = true;
    
    private Thread              mThread = null;
    private boolean             mIsAttached;
    private SurfaceHolder       mHolder;
    private VideoCapture        mCamera;
    private FpsMeter            mFps;
    protected long                mCountDown = 0;
    protected long                mNow;
    private Paint               mCountDownPaint;
    protected int               mStatus = 0;
    protected int               mGameStep = 0;
    protected int               mServoAction = 0;
    protected int               mDownSts = 0;
    protected int               mMirrorL = 0;
    protected int               mMirrorR = 0;
    protected int               mHandL = 0;
    protected int               mHandR = 0;
    protected int               mMotorL = 0;
    protected int               mManual = 1;
    protected int               mDisplay = 0;
    private Paint               mStatusPaint;
    private boolean             mJpegOutput = true;
    private final ByteArrayOutputStream mJpegCompressionBuffer = new ByteArrayOutputStream();
    private volatile byte[]     mJpegData;
    private String              mPictBase64 = "";
    private int                 mCnt = 0;
    SapporoidHTTPD              mHttpd = null;
    protected Mat               mRgba = null;      //�擾����J�����摜

    public static final int DETPOS_CENTER_FAR = 0;
    public static final int DETPOS_LEFT_FAR = 1;
    public static final int DETPOS_RIGHT_FAR = 2;
    public static final int DETPOS_CENTER_MIDDLE = 3;
    public static final int DETPOS_LEFT_MIDDLE = 4;
    public static final int DETPOS_RIGHT_MIDDLE = 5;
    public static final int DETPOS_LEFT_NEAR = 6;
    public static final int DETPOS_RIGHT_NEAR = 7;
    public static final int DETPOS_LEFT_HAND = 8;
    public static final int DETPOS_RIGHT_HAND = 9;
    
    protected int mDetectPos = DETPOS_CENTER_FAR;

    public SampleCvViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mFps = new FpsMeter();
        mCountDownPaint = new Paint();
        mCountDownPaint.setColor(Color.CYAN);
        mCountDownPaint.setStrokeWidth(12.0f);
        mCountDownPaint.setStyle(Paint.Style.STROKE);
        mCountDownPaint.setTextSize(400);

        mStatusPaint = new Paint();
        mStatusPaint.setColor(Color.MAGENTA);
        mStatusPaint.setStrokeWidth(5.0f);
        mStatusPaint.setStyle(Paint.Style.STROKE);
        mStatusPaint.setTextSize(40);
        
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    public SampleCvViewBase(Context context) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mFps = new FpsMeter();
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
        Log.i(TAG, "surfaceCreated");
        synchronized (this) {
            if (mCamera != null && mCamera.isOpened()) {
//                Log.i(TAG, "before mCamera.getSupportedPreviewSizes()");
//                List<Size> sizes = mCamera.getSupportedPreviewSizes();
//                Log.i(TAG, "after mCamera.getSupportedPreviewSizes()");
//                int mFrameWidth = width;
//                int mFrameHeight = height;
//
//                // selecting optimal camera preview size
//                {
//                    double minDiff = Double.MAX_VALUE;
//                    for (Size size : sizes) {
//                        if (Math.abs(size.height - height) < minDiff) {
//                            mFrameWidth = (int) size.width;
//                            mFrameHeight = (int) size.height;
//                            minDiff = Math.abs(size.height - height);
//                        }
//                    }
//                }
                  mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, 320);
                  mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, 240);
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surfaceCreated");
        mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
        if (mCamera.isOpened()) {
/* 2013.11.07 App Finish Change Tateishi */
//            (new Thread(this)).start();
            mIsAttached = true;
            mThread = new Thread(this);
            mThread.start();
        } else {
            mCamera.release();
            mCamera = null;
            Log.e(TAG, "Failed to open native camera");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surfaceDestroyed");
        /* 2013.11.07 App Finish Change Tateishi */
        mIsAttached = false;
        if (mCamera != null) {
            synchronized (this) {
                mCamera.release();
                mCamera = null;
            }
        }
        /* 2013.11.07 App Finish Change Tateishi */
        while (mThread != null && mThread.isAlive());
    }

    protected abstract Bitmap processFrame(VideoCapture capture);

    public void run() {
        Log.i(TAG, "Starting processing thread");
        mFps.init();

/* 2013.11.07 App Finish Change Tateishi */
//      while (true) {
      while (mIsAttached) {
/* 2013.11.07 OutOfMemory Tateishi */
            Bitmap bmp = null;

            synchronized (this) {
                if (mCamera == null)
                    break;

                if (!mCamera.grab()) {
                    Log.e(TAG, "mCamera.grab() failed");
                    break;
                }

                bmp = processFrame(mCamera);
                if( mDisplay == 0 ){
                    mFps.measure();
                }
            }

            if (bmp != null) {
                Canvas canvas = mHolder.lockCanvas();
                Canvas bmpCanvas = new Canvas(bmp);

                if (canvas != null) {
                    Rect br = new Rect(0, 0, bmp.getWidth(), bmp.getHeight());
                    Rect cr = new Rect(0, 0, canvas.getWidth(), canvas.getHeight());
//                    canvas.scale(-1.0f, 0.0f);
                    if( mCountDown != 0 ){
                        bmpCanvas.drawText( String.valueOf(5 - (mNow - mCountDown)/1000), 200, 400, mCountDownPaint);
                    }else if( mDisplay != 1 ){
                        mFps.draw(canvas, (canvas.getWidth() - bmp.getWidth()) / 2, 0);
                        bmpCanvas.drawText( String.valueOf(mGameStep), 40, 40, mStatusPaint);
                        bmpCanvas.drawText( String.valueOf(mStatus), 120, 40, mStatusPaint);
                        bmpCanvas.drawText( String.valueOf(mManual), 40, 120, mStatusPaint);
                        bmpCanvas.drawText( String.valueOf(mMotorL), 120, 120, mStatusPaint);
                        bmpCanvas.drawText( String.valueOf(mHandR), 40, 200, mStatusPaint);
                        bmpCanvas.drawText( String.valueOf(mHandL), 120, 200, mStatusPaint);
                        bmpCanvas.drawText( String.valueOf(mMirrorL), 200, 40, mStatusPaint);
                    }
                    canvas.drawBitmap(bmp, br, cr, null);
                    
//                    if( mJpegOutput && ++mCnt > 5){
                    Log.d("BASE64", "Canvas Draw" );
                    if( mHttpd != null && mHttpd.isPictGet() && mJpegOutput ){
//                        mCnt = 0;
                        mJpegData = compressBmpToJpeg(bmp);
                        mHttpd.setPictBase64( Base64.encodeToString(mJpegData, Base64.DEFAULT) );
                    }
                    
                    mHolder.unlockCanvasAndPost(canvas);
                }
                bmp.recycle();
//                System.gc();
                bmp = null;
            }
        }

//        Log.i(TAG, "Finishing processing thread");
        try {
            Thread.sleep(150);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    protected void setOutput( boolean set ){
        mJpegOutput = set;
        if( !set ){
            mPictBase64 = "";
        }
    }
    
    public void setHttpd( SapporoidHTTPD httpd ){
        mHttpd = httpd;
    }
    
    private byte[] compressBmpToJpeg( Bitmap bitmap ) {
        mJpegCompressionBuffer.reset();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, mJpegCompressionBuffer); 
        return mJpegCompressionBuffer.toByteArray();
     }
    
//    public String getPictBase64(){
//        return mPictBase64;
//    }
//    
//    public void resetPictBase64(){
//        mPictBase64 = null;
//    }
}