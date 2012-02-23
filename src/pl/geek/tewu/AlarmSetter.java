package pl.geek.tewu;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

// FIXME:
// (DONE) volume relative to media channel
// (DONE) selectable alarm sound
// (DONE) alarm volume preview
// (DONE) Incrising volume
//
// alarm notification icon not appearing on reboot-set alert
// Sound Alarm when low on battery
// continuously show time till next alarm in main activity
// testowanie budzika powoduje jego dismiss a to wywo³uje jego w³¹czenie jednoczeœnie nie zaznaczaj¹c
// checkboxa "Budzik w³¹czony"

public class AlarmSetter {
    private static AlarmSetter soleInstance;
    
    private Context context;
    private SharedPreferences prefs;
    

    public static AlarmSetter getInstance(Context context) {
        if (soleInstance == null) soleInstance = new AlarmSetter();
        soleInstance.setContext(context);
        return soleInstance;
    }
    
    private AlarmSetter() {}
    
    private void setContext(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public void setAlarm() {
        setAlarm(true);
    }
    
    public void setAlarm(boolean showMessage) {
        Calendar alarm_time = parseAlarmTime(prefs.getString("preferences_alarm_time","08:00"));
        scheduleAlarm(alarm_time.getTimeInMillis());
        
        setStatusBarIcon(true);
        if (showMessage) Toast.makeText(context,getTimeRemainingMessage(alarm_time),Toast.LENGTH_LONG).show();
    }
    
    public void cancelAlarm() {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(getAlarmPendingIntent());
        setStatusBarIcon(false);
    }
    
    public void setSnooze(int snooze_time_minutes) {
        final long snooze_time = 60000 * snooze_time_minutes;
        scheduleAlarm(System.currentTimeMillis() + snooze_time);
        Toast.makeText(context,"Snoozed",Toast.LENGTH_SHORT).show();
    }
    
    private void scheduleAlarm(long triggerAtTime) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP,triggerAtTime,getAlarmPendingIntent());
    }
    
    private PendingIntent getAlarmPendingIntent() {
        Intent intent = new Intent(context,AlarmActivity.class);
        return PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
    }
    
    private void setStatusBarIcon(boolean enabled) {
        final Intent alarmChanged = new Intent("android.intent.action.ALARM_CHANGED");
        alarmChanged.putExtra("alarmSet",enabled);
        context.sendBroadcast(alarmChanged);
    }
    
    private Calendar parseAlarmTime(String time_str) {
        String[] splitted = time_str.split(":");
        Calendar alarm_time = GregorianCalendar.getInstance();
        alarm_time.set(Calendar.HOUR_OF_DAY,Integer.parseInt(splitted[0]));
        alarm_time.set(Calendar.MINUTE,Integer.parseInt(splitted[1]));
        alarm_time.set(Calendar.SECOND,0);
        
        if (alarm_time.before(GregorianCalendar.getInstance()))
            alarm_time.roll(Calendar.DATE,true);
        
        Log.d("prase milis delta","" + (alarm_time.getTimeInMillis() - System.currentTimeMillis()));
        return alarm_time;
    }
    
    private long milisFromNow(Calendar calendar) {
        return calendar.getTimeInMillis() - System.currentTimeMillis();
    }
    
    private String getTimeRemainingMessage(Calendar alarm_time) {
        final int milis_in_hour = 3600000;
        final int milis_in_minute = 60000;
        final int milis_in_sec = 1000;
        double milis = milisFromNow(alarm_time);
        
        int hours = (int)(milis / milis_in_hour);
        milis -= hours * milis_in_hour;
        int minutes = (int)(milis / milis_in_minute);
        milis -= minutes * milis_in_minute;
        int seconds = (int)(milis / milis_in_sec);
        
        if (hours != 0) return String.format("Alarm zostanie uruchomiony za %s godzin %s minut",hours,minutes);
        if (minutes != 0) return String.format("Alarm zostanie uruchomiony za %s minut",minutes);
        return String.format("Alarm zostanie uruchomiony za %s sekund",seconds);
    }
}
