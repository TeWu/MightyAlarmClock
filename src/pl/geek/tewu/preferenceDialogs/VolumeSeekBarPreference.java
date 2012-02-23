package pl.geek.tewu.preferenceDialogs;

/*
 * The following code was written by Matthew Wiggins and is released under the APACHE 2.0 license
 * http://www.apache.org/licenses/LICENSE-2.0
 */

import pl.geek.tewu.AlarmPlayer;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class VolumeSeekBarPreference extends SeekBarPreference {
    
    protected AlarmPlayer alarmPlayer;
    

    public VolumeSeekBarPreference(Context context, AttributeSet attrs) {
        super(context,attrs);
    }
    
    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        super.onProgressChanged(seek,value,fromTouch);
        
        if (alarmPlayer == null) {
            alarmPlayer = AlarmPlayer.getInstance(mContext);
            alarmPlayer.prepare(false);
        }
        
        if (alarmPlayer.isPlaying()) {
            alarmPlayer.pause();
            alarmPlayer.seekTo(0);
        }
        alarmPlayer.setVolume(value / 100f);
        alarmPlayer.play();
    }
    
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (alarmPlayer != null) {
            alarmPlayer.release();
            alarmPlayer = null;
        }
        super.onDismiss(dialog);
    }
}
