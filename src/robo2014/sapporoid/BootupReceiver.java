package robo2014.sapporoid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootupReceiver extends BroadcastReceiver{
    private static final String ACT_BOOT = "android.intent.action.BOOT_COMPLETED";
    
    @Override
    public void onReceive(Context arg0, Intent arg1) {
        String action = arg1.getAction();
        if( action.equals(ACT_BOOT) ){
            Log.d("SAPPOROID", "Start Sapporoid.");
            Intent intent = new Intent( arg0, SapporoidActivity.class );
            arg0.startActivity(intent);
        }
    }
}
