 /* Sapporoid2013 Add Start Tateishi */
package robo2014.sapporoid.webserver;

import robo2014.sapporoid.FdView;
import robo2014.sapporoid.SapporoidActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import robo2014.sapporoid.FdView;
import fi.iki.elonen.SimpleWebServer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.util.Log;

public class SapporoidHTTPD extends NanoHTTPD {

    private static final int PORT = 8080;
    
    private static final String URI_BASE       = "/robo";
    private static final String URI_BASE_PORT  = "/robo/port";
    private static final String URI_BASE_LAND  = "/robo/land";
/********************************************/
    private static final String URI_BASE_SETTING    = "/robo/setting";
    private static final String URI_BASE_MANUAL     = "/robo/manual";
    private static final String URI_BASE_POWER_OFF  = "/robo/power_off";
/********************************************/
    private static final String URI_BASE_OPERATION  = "/robo/operation";
    private static final String URI_BASE_TEST  = "/robo/test";
    private static final String URI_BASE_TEST2 = "/robo/test2";
    
    private static final String URI_AJAX        = "/ajax";
    private static final String URI_AJAX_IMAGE  = "/ajax/image";
    private static final String URI_AJAX_ACTION = "/ajax/action";

    private static final String URI_FILE_IMAGE = "/image";
    private static final String URI_FILE_SOUND = "/sound";
    private static final String URI_FILE_OTHER = "/file";
    
/********************************************/
    private static final String URI_ACTION_INIT_LINE    = "/act_init_line";
    private static final String URI_ACTION_INIT_RANGE   = "/act_init_range";
    private static final String URI_ACTION_CAMERA_START = "/act_camera_start";
    private static final String URI_ACTION_SET_SOUND    = "/act_set_sound";
    private static final String URI_ACTION_GAME_START   = "/act_game_start";
    private static final String URI_ACTION_GAME_END     = "/act_game_end";
    private static final String URI_ACTION_SET_180      = "/act_set_180";
    private static final String URI_ACTION_POWER_OFF    = "/act_power_off";
    private static final String URI_ACTION_SOUND_PLAY   = "/act_sound_play";
    
    private static final String URI_ACTION_MOVE_FRONT       = "/act_move_front";
    private static final String URI_ACTION_MOVE_BACK        = "/act_move_back";
    private static final String URI_ACTION_MOVE_LEFT        = "/act_move_left";
    private static final String URI_ACTION_MOVE_RIGHT       = "/act_move_right";
    private static final String URI_ACTION_DEFAULT          = "/act_default";
    private static final String URI_ACTION_CATCH_LEFT       = "/act_catch_left";
    private static final String URI_ACTION_CATCH_RIGHT      = "/act_catch_right";
    private static final String URI_ACTION_CATCH2_BOTH      = "/act_catch2_both";
    private static final String URI_ACTION_CATCH2_LEFT      = "/act_catch2_left";
    private static final String URI_ACTION_CATCH2_RIGHT     = "/act_catch2_right";
    private static final String URI_ACTION_PUT              = "/act_put";

//    private static final String URI_ACTION_S_READ           = "/act_slider-r";
//    private static final String URI_ACTION_S_GREEN          = "/act_slider-g";
//    private static final String URI_ACTION_S_BLUE           = "/act_slider-b";
//    private static final String URI_ACTION_S_GET            = "/act_slider-get";
//    private static final String URI_ACTION_S_GOAL           = "/act_slider-goal";
//    private static final String URI_ACTION_S_BACK           = "/act_slider-back";
//    private static final String URI_ACTION_S_TURN           = "/act_slider-turn";
//    private static final String URI_ACTION_S_30             = "/act_slider-30";
//    private static final String URI_ACTION_S_60             = "/act_slider-60";
//    private static final String URI_ACTION_S_90             = "/act_slider-90";
//    private static final String URI_ACTION_S_180            = "/act_slider-180";
    private static final String URI_ACTION_SET_SENSOR         = "/act_set_sensor";
    private static final String URI_ACTION_SET_COLOR          = "/act_set_color";
    private static final String URI_ACTION_SET_SPEED          = "/act_set_speed";
    private static final String URI_ACTION_SET_ANGLE_R        = "/act_set_angle_r";
    private static final String URI_ACTION_SET_ANGLE_L        = "/act_set_angle_l";
    private static final String URI_ACTION_SET_LAST_ANGLE     = "/act_set_last_angle";
    
    private static final String URI_ACTION_SPEACH           = "/act_speach";
    
    private static final String URI_ACTION_SHOW_START   = "/act_show";
/********************************************/
    
    private static final String URI_ACTION_1   = "/action1";
    private static final String URI_ACTION_2   = "/action2";
    private static final String URI_ACTION_3   = "/action3";
    private static final String URI_ACTION_4   = "/action4";
    private static final String URI_ACTION_5   = "/action5";
    private static final String URI_ACTION_6   = "/action6";
    private static final String URI_ACTION_7   = "/action7";
    private static final String URI_ACTION_8   = "/action8";
    private static final String URI_ACTION_9   = "/action9";
    private static final String URI_ACTION_10   = "/action10";
    private static final String URI_ACTION_11   = "/action11";
    private static final String URI_ACTION_12   = "/action12";
    private static final String URI_ACTION_13   = "/action13";
    private static final String URI_ACTION_14   = "/action14";
    private static final String URI_ACTION_15   = "/action15";
    private static final String URI_ACTION_16   = "/action16";
    private static final String URI_ACTION_17   = "/action17";
    private static final String URI_ACTION_18   = "/action18";
    private static final String URI_ACTION_19   = "/action19";

    private static final String URI_TANK_ACTION_1   = "/tank_action1";
    private static final String URI_TANK_ACTION_2   = "/tank_action2";
    private static final String URI_TANK_ACTION_3   = "/tank_action3";
    private static final String URI_TANK_ACTION_4   = "/tank_action4";
    private static final String URI_TANK_ACTION_5   = "/tank_action5";
    private static final String URI_TANK_ACTION_6   = "/tank_action6";
    private static final String URI_TANK_ACTION_7   = "/tank_action7";
    private static final String URI_TANK_ACTION_8   = "/tank_action8";
    private static final String URI_TANK_ACTION_9   = "/tank_action9";
    private static final String URI_TANK_ACTION_10   = "/tank_action10";
    private static final String URI_TANK_ACTION_11   = "/tank_action11";
    private static final String URI_TANK_ACTION_12   = "/tank_action12";
    private static final String URI_TANK_ACTION_13   = "/tank_action13";
    private static final String URI_TANK_ACTION_14   = "/tank_action14";
    private static final String URI_TANK_ACTION_15   = "/tank_action15";
    private static final String URI_TANK_ACTION_16   = "/tank_action16";
    private static final String URI_TANK_ACTION_17   = "/tank_action17";
    private static final String URI_TANK_ACTION_18   = "/tank_action18";
    private static final String URI_TANK_ACTION_19   = "/tank_action19";

    
    private static final String FILE_NAME_WEB_INDEX   = "web_index.html";
    
    private static final String FILE_NAME_WEB_DEFAULT = "web_port.html";
    private static final String FILE_NAME_WEB_PORT    = "web_port.html";
    private static final String FILE_NAME_WEB_LAND    = "web_land.html";
/********************************************/
    private static final String FILE_NAME_WEB_MANUAL  = "web_manual.html";
    private static final String FILE_NAME_WEB_SETTING = "web_setting.html";
    private static final String FILE_NAME_WEB_POWER_OFF = "web_power_off.html";
/********************************************/
    private static final String FILE_NAME_WEB_OPERATION    = "web_operation.html";
    
    private static final String FILE_NAME_WEB_TEST    = "test.html";
    private static final String FILE_NAME_WEB_TEST2   = "test_land.html";
    
//    private final Activity mAppActivity;
    private final SapporoidActivity mAppActivity;
    private volatile String mPictBase64 = "";
    private volatile String mPrePictBase64 = "";
    private boolean mDispCameraImage = false;
    private boolean mPictGetFlag = false;
//    volatile private boolean mPictSetFlag = false;
    /**
     * Common mime type for dynamic content: binary
     */
    private static final String MIME_DEFAULT_BINARY = "application/octet-stream";
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>() {{
        put("css", "text/css");
        put("htm", "text/html");
        put("html", "text/html");
        put("xml", "text/xml");
        put("java", "text/x-java-source, text/java");
        put("md", "text/plain");
        put("txt", "text/plain");
        put("asc", "text/plain");
        put("gif", "image/gif");
        put("jpg", "image/jpeg");
        put("jpeg", "image/jpeg");
        put("png", "image/png");
        put("mp3", "audio/mpeg");
        put("m3u", "audio/mpeg-url");
        put("mp4", "video/mp4");
        put("ogv", "video/ogg");
        put("flv", "video/x-flv");
        put("mov", "video/quicktime");
        put("swf", "application/x-shockwave-flash");
        put("js", "application/javascript");
        put("pdf", "application/pdf");
        put("doc", "application/msword");
        put("ogg", "application/x-ogg");
        put("zip", "application/octet-stream");
        put("exe", "application/octet-stream");
        put("class", "application/octet-stream");
    }};
    
    public SapporoidHTTPD(SapporoidActivity app) throws IOException {
        super(PORT);
        mAppActivity = app;
    }

    @Override
    public Response serve(String uri, Method method,
            Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
        
        System.out.println(method + " '" + uri + "' ");
        StringBuilder msg = new StringBuilder();
        
        if (uri.startsWith(URI_AJAX)) {
            if (uri.equals(URI_AJAX_IMAGE)) {
                msg.append("data:image/jpeg;base64,");
//                msg.append(getPictBase64());
//                mPictGetFlag = true;
//Yamauchi test
                getPictBase64(msg);
            }
            else if (uri.startsWith(URI_AJAX_ACTION)) {
/*                
                if (uri.endsWith(URI_ACTION_SHOW_START)) {
//Yamauchi Test
                    mDispCameraImage = true;
                    Log.d("BASE64", "mDispCameraImage true");
                }else if (uri.endsWith(URI_ACTION_POWER_OFF)) {
                    System.out.println("req power off");
//                    Intent intent = new Intent();
//                    intent.setClassName("com.sapporo.shutdowner", "ShutDowner");
//                    intent.putExtra("mode", "1");
//                    mAppActivity.getApplicationContext().startActivity(intent);
                    Intent intent = new Intent(SapporoidActivity.ACT_SOUND);
                    intent.putExtra("file", "sound/samurai_shout1.mp3");
                    mAppActivity.sendBroadcast(intent);
                    
                    Intent intent1=new Intent();
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
                    intent1.setClassName("com.sapporo.shutdowner","com.sapporo.shutdowner.ShutDowner");
                    intent1.putExtra("mode", "1");
                    mAppActivity.startActivity(intent1);
                }else if (uri.endsWith(URI_ACTION_1)) {
                    System.out.println("req action1");
                    Intent intent2 = new Intent(SapporoidActivity.SET_180);
                    mAppActivity.sendBroadcast(intent2);
                }else if (uri.endsWith(URI_ACTION_2)) {
                    System.out.println("req action2");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_STOP);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_3)) {
                    System.out.println("req action3");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_Default");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_4)) {
                    System.out.println("req action4");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_Walk3");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_5)) {
                    System.out.println("req action5");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_turn_left");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_6)) {
                    System.out.println("req action6");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_turn_right");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_7)) {
                    System.out.println("req action7");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_Walk_left");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_8)) {
                    System.out.println("req action8");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_WalkRight");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_9)) {
                    System.out.println("req action9");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_Left_Get");
                    intent2.putExtra("legName", "leg_Get");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_10)) {
                    System.out.println("req action10");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_Left_Bring");
                    intent2.putExtra("legName", "leg_Bring");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_11)) {
                    System.out.println("req action11");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_Put");
                    intent2.putExtra("legName", "leg_Put");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_12)) {
                    System.out.println("req action12");
                    Intent intent2 = new Intent(SapporoidActivity.START_CAMERA);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_13)) {
                    System.out.println("req action13");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_pre_Walk");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_14)) {
                    System.out.println("req action14");
//                    Intent intent2 = new Intent(SapporoidActivity.ACT_SOUND);
//                    intent2.putExtra("file", "sound/coin05.mp3");
                    Intent intent2 = new Intent(SapporoidActivity.ACT_TEST);
                    intent2.putExtra("value", "0");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_15)) {
                    System.out.println("req action15");
                    Intent intent2 = new Intent(SapporoidActivity.ACT_START);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_16)) {
                    System.out.println("req action16");
                    Intent intent2 = new Intent(SapporoidActivity.ACT_STOP);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_17)) {
                    System.out.println("req action17");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_Walk_getLeft");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_18)) {
                    System.out.println("req action18");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_Walk6");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_19)) {
                    System.out.println("req action19");
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("legName", "leg_Walk7");
                    mAppActivity.sendBroadcast(intent2);
                    
                } else if (uri.endsWith(URI_TANK_ACTION_1)) {
                } else if (uri.endsWith(URI_TANK_ACTION_2)) {
                } else if (uri.endsWith(URI_TANK_ACTION_3)) {
                } else if (uri.endsWith(URI_TANK_ACTION_4)) {
                } else if (uri.endsWith(URI_TANK_ACTION_5)) {
                } else if (uri.endsWith(URI_TANK_ACTION_6)) {
                } else if (uri.endsWith(URI_TANK_ACTION_7)) {
                } else if (uri.endsWith(URI_TANK_ACTION_8)) {
                } else if (uri.endsWith(URI_TANK_ACTION_9)) {
                } else if (uri.endsWith(URI_TANK_ACTION_10)) {
                } else if (uri.endsWith(URI_TANK_ACTION_11)) {
                } else if (uri.endsWith(URI_TANK_ACTION_12)) {
                } else if (uri.endsWith(URI_TANK_ACTION_13)) {
                } else if (uri.endsWith(URI_TANK_ACTION_14)) {
                    Intent intent2 = new Intent(SapporoidActivity.APP_TANK_EVT);
                    intent2.putExtra("event", "18");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_TANK_ACTION_15)) {
                    Intent intent2 = new Intent(SapporoidActivity.APP_TANK_EVT);
                    intent2.putExtra("event", "14");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_TANK_ACTION_16)) {
                    Intent intent2 = new Intent(SapporoidActivity.APP_TANK_EVT);
                    intent2.putExtra("event", "15");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_TANK_ACTION_17)) {
                    Intent intent2 = new Intent(SapporoidActivity.APP_TANK_EVT);
                    intent2.putExtra("event", "16");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_TANK_ACTION_18)) {
                    Intent intent2 = new Intent(SapporoidActivity.APP_TANK_EVT);
                    intent2.putExtra("event", "17");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_TANK_ACTION_19)) {
*/                    
/********************************************/
                // [web_index.html] Action------------------------------------------
                if (uri.endsWith(URI_ACTION_INIT_LINE)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_TEST);
                    intent2.putExtra("value", "0");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_INIT_RANGE)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_TEST);
                    intent2.putExtra("value", "6");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_CAMERA_START)) {
                    Intent intent2 = new Intent(SapporoidActivity.START_CAMERA);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_SET_SOUND)) {
                    String strSound = parms.get("arg1");
                    Intent intent2 = new Intent(SapporoidActivity.SET_SOUND_FOLDER);
                    intent2.putExtra("value", strSound);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_GAME_START)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_START);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_GAME_END)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_STOP);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_POWER_OFF)) {
                    System.out.println("req power off");
//                  Intent intent = new Intent();
//                  intent.setClassName("com.sapporo.shutdowner", "ShutDowner");
//                  intent.putExtra("mode", "1");
//                  mAppActivity.getApplicationContext().startActivity(intent);
                  Intent intent2 = new Intent(SapporoidActivity.ACT_SOUND);
                  intent2.putExtra("file", "sound/se_maoudamashii_system32.mp3");
                  mAppActivity.sendBroadcast(intent2);
                  
                  Intent intent1=new Intent();
                  intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
                  intent1.setClassName("com.sapporo.shutdowner","com.sapporo.shutdowner.ShutDowner");
                  intent1.putExtra("mode", "1");
                  mAppActivity.startActivity(intent1);
                  
                // [web_power_off.html] Action-------------------------------------------
                } else if (uri.endsWith(URI_ACTION_SOUND_PLAY)) {
                    String strSound = parms.get("arg1");
                    if( strSound != null ){
                        strSound += ".mp3";
                        Intent intent2 = new Intent(SapporoidActivity.ACT_SOUND);
                        intent2.putExtra("file", strSound);
                        mAppActivity.sendBroadcast(intent2);
                    }
                // Common Action-------------------------------------------
                } else if (uri.endsWith(URI_ACTION_SET_180)) {
                    
                } else if (uri.endsWith(URI_ACTION_DEFAULT)) {
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_Default");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                    
                    Intent intent3 = new Intent(SapporoidActivity.ACT_MANUAL);
                    intent3.putExtra("value", SapporoidActivity.MANUAL_DIR_STOP);
                    mAppActivity.sendBroadcast(intent3);                } else if (uri.endsWith(URI_ACTION_SHOW_START)) {
  //Yamauchi Test
                    mDispCameraImage = true;
                    Log.d("BASE64", "mDispCameraImage true");

                    
                // [web_port/land.html] Action-------------------------------------------
                } else if (uri.endsWith(URI_ACTION_SPEACH)) {
                    String strWord = parms.get("arg1");

                // [web_manual.html] Action-------------------------------------------
                } else if (uri.endsWith(URI_ACTION_MOVE_FRONT)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_MANUAL);
                    intent2.putExtra("value", SapporoidActivity.MANUAL_DIR_FRONT);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_MOVE_BACK)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_MANUAL);
                    intent2.putExtra("value", SapporoidActivity.MANUAL_DIR_BACK);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_MOVE_LEFT)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_MANUAL);
                    intent2.putExtra("value", SapporoidActivity.MANUAL_DIR_LEFT);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_MOVE_RIGHT)) {
                    Intent intent2 = new Intent(SapporoidActivity.ACT_MANUAL);
                    intent2.putExtra("value", SapporoidActivity.MANUAL_DIR_RIGHT);
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_CATCH_LEFT)) {
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_get_LM_RD0");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_CATCH_RIGHT)) {
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_get_LD_RM0");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_CATCH2_BOTH)) {
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_get_special");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_CATCH2_LEFT)) {
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_get_LM_RB0");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_CATCH2_RIGHT)) {
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_get_LB_RM0");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_PUT)) {
                    Intent intent2 = new Intent(SapporoidActivity.MOVE_START_SEP);
                    intent2.putExtra("armName", "arm_put");
                    intent2.putExtra("legName", "leg_Default");
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_SET_SENSOR)) {
                    int val_range = Integer.valueOf(parms.get("arg1"));
                    int val_color = Integer.valueOf(parms.get("arg2"));
                    
                    Intent intent2 = new Intent(SapporoidActivity.ACT_DEBUG);
                    intent2.putExtra("item", FdView.SETTING_HAND_GET_RANGE );
                    intent2.putExtra("set", String.valueOf(val_range) );
                    mAppActivity.sendBroadcast(intent2);
                    
                    Intent intent3 = new Intent(SapporoidActivity.ACT_DEBUG);
                    intent3.putExtra("item", FdView.SETTING_LINE_DETECT );
                    intent3.putExtra("set", String.valueOf(val_color) );
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_SET_COLOR)) {
                    int val_r = Integer.valueOf(parms.get("arg1"));
                    int val_g = Integer.valueOf(parms.get("arg2"));
                    int val_b = Integer.valueOf(parms.get("arg3"));
                    
                    Intent intent2 = new Intent(SapporoidActivity.ACT_DEBUG);
                    intent2.putExtra("item", FdView.SETTING_COLOR_RED_R );
                    intent2.putExtra("set", val_r + "," + val_g + "," + val_b );
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_SET_SPEED)) {
                    int val_get  = Integer.valueOf(parms.get("arg1"));
                    int val_goal = Integer.valueOf(parms.get("arg2"));
                    int val_back = Integer.valueOf(parms.get("arg3"));
                    int val_turn = Integer.valueOf(parms.get("arg4"));
                    
                    Intent intent2 = new Intent(SapporoidActivity.ACT_DEBUG);
                    intent2.putExtra("item", FdView.SETTING_MOTOR_POWER );
                    intent2.putExtra("set", val_get + "," + val_goal + "," + val_back + "," + val_turn );
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_SET_ANGLE_R)) {
                    int val_30  = Integer.valueOf(parms.get("arg1"));
                    int val_60  = Integer.valueOf(parms.get("arg2"));
                    int val_90  = Integer.valueOf(parms.get("arg3"));
                    int val_180 = Integer.valueOf(parms.get("arg4"));
                    
                    int a;
                    a=0;
                    
                } else if (uri.endsWith(URI_ACTION_SET_ANGLE_L)) {
                    int val_30  = Integer.valueOf(parms.get("arg1"));
                    int val_60  = Integer.valueOf(parms.get("arg2"));
                    int val_90  = Integer.valueOf(parms.get("arg3"));
                    int val_180 = Integer.valueOf(parms.get("arg4"));
                    
                    Intent intent2 = new Intent(SapporoidActivity.ACT_DEBUG);
                    intent2.putExtra("item", FdView.SETTING_MT30 );
                    intent2.putExtra("set", val_30 + "," + val_60 + "," + val_90 + "," + val_180 );
                    mAppActivity.sendBroadcast(intent2);
                } else if (uri.endsWith(URI_ACTION_SET_LAST_ANGLE)) {
                    String val  = parms.get("arg1"); // 0:left 1:Right

                    Intent intent2 = new Intent(SapporoidActivity.ACT_DEBUG);
                    intent2.putExtra("item", FdView.SETTING_STEP4_LR );
                    intent2.putExtra("set",  val );
                    mAppActivity.sendBroadcast(intent2);
/********************************************/
                }
                
            }
        } else if (uri.startsWith(URI_BASE)) {
            if (uri.equals(URI_BASE) || uri.equals(URI_BASE + "/") ) {
                readFileHtmlCreate(FILE_NAME_WEB_INDEX, msg);
            }else if (uri.startsWith(URI_BASE + URI_FILE_OTHER)) {
                return getFileResponce(uri);
            }else if (uri.startsWith(URI_BASE_PORT)) {
                if (uri.startsWith(URI_BASE_PORT + URI_FILE_IMAGE)
                    || uri.startsWith(URI_BASE_PORT + URI_FILE_SOUND)
                    || uri.startsWith(URI_BASE_PORT + URI_FILE_OTHER)) {
                    return getFileResponce(uri);
                } else if (uri.endsWith(URI_BASE_PORT)) {
                    readFileHtmlCreate(FILE_NAME_WEB_PORT, msg);
                }
            } else if (uri.startsWith(URI_BASE_LAND)) {
                if (uri.startsWith(URI_BASE_LAND + URI_FILE_IMAGE)
                    || uri.startsWith(URI_BASE_LAND + URI_FILE_SOUND)
                    || uri.startsWith(URI_BASE_LAND + URI_FILE_OTHER)) {
                    return getFileResponce(uri);
                } else if (uri.endsWith(URI_BASE_LAND)) {
                    readFileHtmlCreate(FILE_NAME_WEB_LAND, msg);
                }
/********************************************/
            } else if (uri.startsWith(URI_BASE_SETTING)) {
                readFileHtmlCreate(FILE_NAME_WEB_SETTING, msg);
            } else if (uri.startsWith(URI_BASE_MANUAL)) {
                readFileHtmlCreate(FILE_NAME_WEB_MANUAL, msg);
            } else if (uri.startsWith(URI_BASE_POWER_OFF)) {
                readFileHtmlCreate(FILE_NAME_WEB_POWER_OFF, msg);
/********************************************/
            } else if (uri.startsWith(URI_BASE_OPERATION)) {
                readFileHtmlCreate(FILE_NAME_WEB_OPERATION, msg);
            } else if (uri.startsWith(URI_BASE_TEST2)) {
                if (uri.startsWith(URI_BASE_TEST2 + URI_FILE_IMAGE)
                    || uri.startsWith(URI_BASE_TEST2 + URI_FILE_SOUND)) {
                    int pos = uri.lastIndexOf('/');
                    String filename = null;
                    if (pos >= 0) {
                        filename = uri.substring(pos + 1).toLowerCase();
                    }
                    return serveFile(filename, getMimeTypeForFile(uri));
//                    return serveFile(uri, headers, new File("./data/" + filename), getMimeTypeForFile(uri));
                } else {
                    readFileHtmlCreate(FILE_NAME_WEB_TEST2, msg);
                }
            } else if (uri.startsWith(URI_BASE_TEST)) {
                if (uri.startsWith(URI_BASE_TEST + URI_FILE_IMAGE)
                    || uri.startsWith(URI_BASE_TEST + URI_FILE_SOUND)) {
                    int pos = uri.lastIndexOf('/');
                    String filename = null;
                    if (pos >= 0) {
                        filename = uri.substring(pos + 1).toLowerCase();
                    }
                    return serveFile(filename, getMimeTypeForFile(uri));
//                    return serveFile(uri, headers, new File("./data/" + filename), getMimeTypeForFile(uri));
                } else {
                    readFileHtmlCreate(FILE_NAME_WEB_TEST, msg);
                }
//            } else {
//                readFileHtmlCreate(FILE_NAME_WEB_DEFAULT, msg);
            }
        }
        return new NanoHTTPD.Response(msg.toString());
//        return serveFile(uri, header, new File("./data"), true);
    }

    private Response getFileResponce(String uri) {
        int pos = uri.lastIndexOf('/');
        String filename = null;
        if (pos >= 0) {
            filename = uri.substring(pos + 1).toLowerCase();
        }
        return serveFile(filename, getMimeTypeForFile(uri));
    }
    
    private void readFileHtmlCreate(String filename, StringBuilder sb) {
        InputStream is = null;
        BufferedReader br = null;
        try {
            try {
                is = mAppActivity.getAssets().open(filename);
                br = new BufferedReader(new InputStreamReader(is));

                String str;
                while ((str = br.readLine()) != null) {
                    if (str.substring(0, 2).equals("//")) {
                        // Not append
                        continue;
                    } else if (str.equals("[camera_image_port]")) {
                        sb.append("<img id='camera_image' src='data:image/jpeg;base64,");
//                        sb.append(getPictBase64());
//                        mPictGetFlag = true;
//Yamauchi Test
                        getPictBase64(sb);
                        sb.append("' width='100%' height='auto' border='1'>");
                    } else if (str.equals("[camera_image_land]")) {
                        sb.append("<img id='camera_image' src='data:image/jpeg;base64,");
//                        sb.append(getPictBase64());
//                        mPictGetFlag = true;
//Yamauchi Test
                        getPictBase64(sb);
                        sb.append("' width='auto' height='100%' border='1'>");
                    } else if (str.equals("[camera_image_test]")) {
                        sb.append("<img id='camera_image' src='image/test.JPG' width='100%' height='auto'>");
                    } else if (str.indexOf("[val_range]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_HAND_GET_RANGE);
                        str = str.replace("[val_range]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_color]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_LINE_DETECT);
                        str = str.replace("[val_color]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_r]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_COLOR_RED_R);
                        str = str.replace("[val_r]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_g]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_COLOR_RED_G);
                        str = str.replace("[val_g]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_b]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_COLOR_RED_B);
                        str = str.replace("[val_b]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_get]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_MOTOR_POWER_GET);
                        str = str.replace("[val_get]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_goal]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_MOTOR_POWER);
                        str = str.replace("[val_goal]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_back]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_ESCAPE_STRAIGHT);
                        str = str.replace("[val_back]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_turn]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_DETECT_LINE);
                        str = str.replace("[val_turn]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_30_l]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(view.SETTING_MT30);
                        str = str.replace("[val_30_l]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_60_l]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(view.SETTING_MT60);
                        str = str.replace("[val_60_l]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_90_l]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(view.SETTING_MT90);
                        str = str.replace("[val_90_l]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_180_l]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(view.SETTING_MT180);
                        str = str.replace("[val_180_l]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_30_r]") != -1) {
                        int a = 0; // No used
                        str = str.replace("[val_30_r]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_60_r]") != -1) {
                        int a = 0; // No used
                        str = str.replace("[val_60_r]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_90_r]") != -1) {
                        int a = 0; // No used
                        str = str.replace("[val_90_r]", String.valueOf(a));
                        sb.append(str + "\n");
                    } else if (str.indexOf("[val_180_r]") != -1) {
                        int a = 0; // No used
                        str = str.replace("[val_180_r]", String.valueOf(a));
                    } else if (str.indexOf("[checked_l]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_STEP4_LR);
                        if (a == 0) {
                            str = str.replace("[checked_l]", "checked='checked'");
                        } else {
                            str = str.replace("[checked_l]", "");
                        }
                        sb.append(str + "\n");
                    } else if (str.indexOf("[checked_r]") != -1) {
                        FdView view = mAppActivity.getFdView();
                        int a = view.getSettingValue(FdView.SETTING_STEP4_LR);
                        if (a == 1) {
                            str = str.replace("[checked_r]", "checked='checked'");
                        } else {
                            str = str.replace("[checked_r]", "");
                        }
                        sb.append(str + "\n");
                    } else {
                        sb.append(str + "\n");
                    }
                }
            } finally {
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            System.out.println("readFileHtmlCreate error. filename:" + filename + "e:" + e);
        }
    }

    public synchronized void setPictBase64(String base64) {
        Log.d("BASE64", "setBase64 Start");
        if( !mPictGetFlag ){
            try {
                wait();  //availableがtrueの間、wait
            } catch (InterruptedException e) {
            }
        }
    
        this.mPictBase64 = base64;
        mPictGetFlag = false;
        Log.d("BASE64", "setBase64 End");
        notifyAll();
    }

    public boolean isPictGet(){
        boolean ret = mDispCameraImage && mPictGetFlag;
        Log.d("BASE64", "isPictGet() " + ret );
        return ret;
    }
    
    public synchronized String getPictBase64() {
        if( !mPictGetFlag ){
            mPrePictBase64 = mPictBase64;
            return mPictBase64;
        }else{
            Log.d("BASE64", "#prePict");
            return mPrePictBase64;
        }
    }

    public synchronized void getPictBase64( StringBuilder msg ) {
        Log.d("BASE64", "#getBase64 Start");
        if( mPictGetFlag ){
            try {
                wait();  //availableがtrueの間、wait
            } catch (InterruptedException e) {
            }
        }
        msg.append(mPictBase64);
        mPictGetFlag = true;
        notifyAll();
        Log.d("BASE64", "#getBase64 End");
    }
    
    public boolean isDispCameraImage() {
        return mDispCameraImage;
    }

//    public void setDispCameraImage(boolean dispCameraImage) {
//        this.mDispCameraImage = dispCameraImage;
//    }

    Response serveFile(String filename, String mime) {
        Response res;
        // /assets
        try {
            AssetManager as = mAppActivity.getResources().getAssets();
            InputStream is = as.open(filename);
            res = createResponse(Response.Status.OK, mime, is);
        } catch (IOException ioe) {
            // TODO ?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�黷?� catch ?�?�u?�?�?�?�?�?�b?�?�N
            res = createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }
        return res;
    }
    
    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        Response res;
        try {
            res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
        } catch (IOException ioe) {
            // TODO ?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�?�黷?� catch ?�?�u?�?�?�?�?�?�b?�?�N
            res = createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }
        return res;

//        try {
//            // Calculate etag
//            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());
//
//            // Support (simple) skipping:
//            long startFrom = 0;
//            long endAt = -1;
//            String range = header.get("range");
//            if (range != null) {
//                if (range.startsWith("bytes=")) {
//                    range = range.substring("bytes=".length());
//                    int minus = range.indexOf('-');
//                    try {
//                        if (minus > 0) {
//                            startFrom = Long.parseLong(range.substring(0, minus));
//                            endAt = Long.parseLong(range.substring(minus + 1));
//                        }
//                    } catch (NumberFormatException ignored) {
//                    }
//                }
//            }
//
//            // Change return code and add Content-Range header when skipping is requested
//            long fileLen = file.length();
//            if (range != null && startFrom >= 0) {
//                if (startFrom >= fileLen) {
//                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
//                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
//                    res.addHeader("ETag", etag);
//                } else {
//                    if (endAt < 0) {
//                        endAt = fileLen - 1;
//                    }
//                    long newLen = endAt - startFrom + 1;
//                    if (newLen < 0) {
//                        newLen = 0;
//                    }
//
//                    final long dataLen = newLen;
//                    FileInputStream fis = new FileInputStream(file) {
//                        @Override
//                        public int available() throws IOException {
//                            return (int) dataLen;
//                        }
//                    };
//                    fis.skip(startFrom);
//
//                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
//                    res.addHeader("Content-Length", "" + dataLen);
//                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
//                    res.addHeader("ETag", etag);
//                }
//            } else {
//                if (etag.equals(header.get("if-none-match")))
//                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
//                else {
//                    res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
//                    res.addHeader("Content-Length", "" + fileLen);
//                    res.addHeader("ETag", etag);
//                }
//            }
//        } catch (IOException ioe) {
//            res = createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
//        }
//
//        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
    
//    @Override
//    public Response serve(IHTTPSession session) {
//        String uri = session.getUri();
//        Map<String, String> header = session.getHeaders();
//        return super.serve(session);
//    }
    
    // Get MIME type from file name extension, if possible
    private String getMimeTypeForFile(String uri) {
        int dot = uri.lastIndexOf('.');
        String mime = null;
        if (dot >= 0) {
            mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
        }
        return mime == null ? MIME_DEFAULT_BINARY : mime;
    }
    
}
/* Sapporoid2013 Add End Tateishi */