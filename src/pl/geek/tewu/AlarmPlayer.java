package pl.geek.tewu;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.preference.PreferenceManager;

public class AlarmPlayer {
    
    private static AlarmPlayer soleInstance;
    
    private Context context;
    private SharedPreferences prefs;
    private MediaPlayer mp;
    

    public static AlarmPlayer getInstance(Context context) {
        if (soleInstance == null) soleInstance = new AlarmPlayer();
        soleInstance.setContext(context);
        return soleInstance;
    }
    
    private AlarmPlayer() {}
    
    private void setContext(Context context) {
        this.context = context;
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }
    
    public void prepare() {
        prepare(true);
    }
    
    public AlarmPlayer prepare(boolean looping) {
        if (mp != null) mp.release();
        
        Uri data_source = Uri.parse(prefs.getString("preferences_alarm_ringtone","content://settings/system/alarm_alert"));
        float volume = (prefs.getInt("preferences_alarm_volume",50) / 100.0f);
        mp = new MediaPlayer();
        
        mp.setAudioStreamType(AudioManager.STREAM_ALARM);
        try {
            mp.setDataSource(context,data_source);
            mp.prepare();
        }
        catch (Exception e) {
            mp = MediaPlayer.create(context,R.raw.alarm);
        }
        
        mp.setVolume(volume,volume);
        mp.setLooping(looping);
        return this;
    }
    
    public void play() {
        if (mp == null) throw new IllegalStateException();
        mp.start();
    }
    
    public void pause() {
        if (mp == null) throw new IllegalStateException();
        mp.pause();
    }
    
    public void stop() {
        if (mp == null) throw new IllegalStateException();
        mp.stop();
    }
    
    public void setVolume(float volume) {
        if (mp == null) throw new IllegalStateException();
        mp.setVolume(volume,volume);
    }
    
    public boolean isPlaying() {
        if (mp == null) throw new IllegalStateException();
        return mp.isPlaying();
    }
    
    public void seekTo(int msec) {
        if (mp == null) throw new IllegalStateException();
        mp.seekTo(msec);
    }
    
    public void release() {
        if (mp == null) throw new IllegalStateException();
        if (mp.isPlaying()) mp.stop();
        mp.release();
    }
}
