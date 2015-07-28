package robo2014.sapporoid;

import java.util.HashMap;

public class RobotController {
    static public final String KEY_ARM = "arm_";
    static public final String KEY_LEG = "leg_";
    
//    static public final String ACT_ARM_DEFAULT = "arm_Default";
//    static public final String ACT_ARM_BRING_LEFT = "arm_bring_left";
//    static public final String ACT_ARM_BRING_BOTH = "arm_bring_both";
//    static public final String ACT_ARM_BRING_RIGHT = "arm_bring_right";
//    static public final String ACT_ARM_PUT_BACK = "arm_put_back";
//    static public final String ACT_LEG_DEFAULT = "leg_Default";
//    static public final String ACT_ARM_BRING_WALK1 = "arm_bring_walk1";
//    static public final String ACT_ARM_BRING_WALK2 = "arm_bring_walk2";
//    static public final String ACT_ARM_BRING_WALK3 = "arm_bring_walk3";
//    static public final String ACT_LEG_WALK1 = "leg_Walk1";
//    static public final String ACT_LEG_WALK2 = "leg_Walk2";
//    static public final String ACT_ARM_ONEFOOT = "arm_onefoot";
//    static public final String ACT_LEG_ONEFOOT = "leg_onefoot";
//    static public final String ACT_ARM_GET_LEFT = "arm_get_left";
//    static public final String ACT_ARM_GET_LEFT2 = "arm_get_left2";
//    static public final String ACT_ARM_GET_RIGHT = "arm_get_right";
//    static public final String ACT_ARM_GET_RIGHT2 = "arm_get_right2";
//    static public final String ACT_ARM_TURN_RIGHT1 = "arm_turn_right1";
//    static public final String ACT_ARM_TURN_RIGHT2 = "arm_turn_right2";
//    static public final String ACT_ARM_TURN_LEFT1 = "arm_turn_left1";
//    static public final String ACT_LEG_GET_WALK_LEFT = "leg_get_Walk_left";
//    static public final String ACT_LEG_GET_WALK_BOTH = "leg_get_Walk_both";
//    static public final String ACT_LEG_GET_WALK_BOTH2 = "leg_get_Walk_both2";

//2013
//    static public final String ACT_ARM_DEFAULT = "arm_Default";
//    static public final String ACT_LEG_DEFAULT = "leg_Default";
//    static public final String ACT_LEG_WALK3 = "leg_Walk3";
//    static public final String ACT_LEG_WALK4 = "leg_Walk4";
//    static public final String ACT_LEG_TURN_LEFT = "leg_turn_left";
//    static public final String ACT_LEG_TURN_RIGHT = "leg_turn_right";
//    static public final String ACT_LEG_WALK_LEFT = "leg_Walk_left";
//    static public final String ACT_LEG_WALK_RIGHT = "leg_Walk_right";
//    static public final String ACT_ARM_LEFT_PREGET = "arm_Left_PreGet";
//    static public final String ACT_LEG_GET = "leg_Get";
//    static public final String ACT_ARM_LEFT_GET = "arm_Left_Get";
//    static public final String ACT_LEG_SQUAT = "leg_squat";
//    static public final String ACT_ARM_LEFT_BRING = "arm_Left_Bring";
//    static public final String ACT_LEG_BRING = "leg_Bring";
//    static public final String ACT_ARM_PUT = "arm_put";
//    static public final String ACT_LEG_PUT = "leg_put";
//    static public final String ACT_LEG_BACK = "leg_Back1";
//    static public final String ACT_ARM_WAKEUP_FRONT = "arm_WakeUpUp";
//    static public final String ACT_LEG_WAKEUP_FRONT = "leg_WakeUpUp";
//    static public final String ACT_ARM_WAKEUP_BACK = "arm_WakeUpDown";
//    static public final String ACT_LEG_WAKEUP_BACK = "leg_WakeUpDown";
    
    private Robo mRobo = null;

    public RobotController( Robo robo ){
        mRobo = robo;
    }
    
    public void stopRobot(){
        mRobo.forceStop();
    }

    public void MoveStop(){
        mRobo.MoveStop();
    }
    
    public int actionNext( int act ){
        return mRobo.setMoveAction(act);
    }
    
    public int actionForce( int act ){
        return mRobo.setMoveAction(act);
    }

    public boolean isActionDone(){
        boolean ret = mRobo.isActionDone();
        return ret; 
    }
    
    public void setMotionEdit( String str ){
    	mRobo.setMotionEdit(str);
    }
}
