package pl.geek.tewu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAlarmEnabled = prefs.getBoolean("preferences_alarm_enabled",false);
        
        if (isAlarmEnabled) {
            AlarmSetter alarmSetter = AlarmSetter.getInstance(context);
            alarmSetter.setAlarm(false);
        }
    }
}
