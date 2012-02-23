package pl.geek.tewu;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AlarmActivity extends Activity {
    
    protected boolean fake = false;
    
    private TextView timeout_message;
    private SharedPreferences prefs;
    private AlarmPlayer alarmPlayer;
    private PowerManager.WakeLock wakeLock;
    private boolean dismissed = false;
    private boolean snoozed = false;
    

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFullscreen = prefs.getBoolean("preferences_alarm_fullscreen",false);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        if (isFullscreen) getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.alert);
        String label = prefs.getString("preferences_alarm_label","");
        Button snooze_button = (Button)findViewById(R.id.snooze_button);
        snooze_button.setText(label + "\n\n" + snooze_button.getText());
        
        timeout_message = (TextView)findViewById(R.id.alarm_timeout_message);
        alarmPlayer = AlarmPlayer.getInstance(this);
        alarmPlayer.prepare();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        dismissed = false;
        snoozed = false;
        timeout_message.setVisibility(View.GONE);
        
        alarmPlayer.play();
        
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,"AlarmActivity");
        wakeLock.acquire();
        
        final int alarm_time_minutes = prefs.getInt("preferences_alarm_autosilence_time",10);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(60000 * alarm_time_minutes);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!isFinishing()) {
                    Log.d("MightyAlarmClock finishing thread","timeout action executed");
                    synchronized (alarmPlayer) {
                        if (alarmPlayer.isPlaying()) alarmPlayer.stop();
                    }
                    timeout_message.post(new Runnable() {
                        @Override
                        public void run() {
                            timeout_message.setText("Budzik wyciszony automatycznie po " + alarm_time_minutes + " minutach!!");
                            timeout_message.setVisibility(View.VISIBLE);
                            if (wakeLock.isHeld()) wakeLock.release();
                        }
                    });
                }
            }
        }).start();
        
        boolean incrementalVolume = prefs.getBoolean("preferences_alarm_incremental_volume",false);
        if (incrementalVolume) {
            alarmPlayer.setVolume(0);
            final int increment_speed = prefs.getInt("preferences_alarm_incremental_volume_speed",25);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final float volume_step = increment_speed / 100f;
                    final float max_volume = prefs.getInt("preferences_alarm_volume",1) / 100f;
                    float v = 0.1f;
                    
                    while (v < max_volume) {
                        synchronized (alarmPlayer) {
                            if (!isFinishing() && alarmPlayer.isPlaying()) {
                                Log.d("worker thread","volume up");
                                alarmPlayer.setVolume(v);
                            }
                        }
                        v += volume_step;
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    synchronized (alarmPlayer) {
                        if (!isFinishing() && alarmPlayer.isPlaying())
                            alarmPlayer.setVolume(max_volume);
                    }
                }
            }).start();
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (wakeLock.isHeld()) wakeLock.release();
        synchronized (alarmPlayer) {
            alarmPlayer.release();
        }
        
        if (!dismissed && !snoozed && !fake) { // je¿eli ani dismissed ani snoozed nie jest true (ani nie jest to tylko testowanie alarmu) to coœ ubi³o nasz¹ activity = failsafe snooze
            AlarmSetter alarmSetter = AlarmSetter.getInstance(this);
            alarmSetter.setSnooze(1000 * 10);
            Toast.makeText(this,"FailsSafe Snooze",Toast.LENGTH_SHORT).show();
            Log.d("AlarmActivity","FailsSafe Snooze");
        }
        
        if (!isFinishing()) finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!fake) System.exit(0); // hack to prevent task killer from killing scheduled alarm in AlarmManager
    }
    
    protected void snooze() {
        if (!fake) {
            final int snooze_time_minutes = prefs.getInt("preferences_alarm_snooze_time",8);
            AlarmSetter alarmSetter = AlarmSetter.getInstance(this);
            alarmSetter.setSnooze(snooze_time_minutes);
        }
        
        snoozed = true;
        finish();//startActivity(new Intent(this,SnoozedActivity.class));
    }
    
    protected void dismiss() {
        if (!fake) {
            AlarmSetter alarmSetter = AlarmSetter.getInstance(this);
            alarmSetter.setAlarm(false);
        }
        Toast.makeText(this,"Dismissed",Toast.LENGTH_SHORT).show();
        
        dismissed = true;
        finish();
    }
    
    public void onSnoozeClicked(View v) {
        snooze();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                dismiss();
                break;
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_POWER:
                snooze();
                break;
        }
        return true;
    }
}
