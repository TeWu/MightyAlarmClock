package pl.geek.tewu.preferenceDialogs;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimePreference extends DialogPreference {
    protected int hour;
    protected int minute;
    protected boolean is24HourFormat;
    protected TextView timeDisplay;
    protected TimePicker picker;
    

    public int getHour() {
        return hour;
    }
    
    public void setHour(int hour) {
        this.hour = hour;
    }
    
    public int getMinute() {
        return minute;
    }
    
    public void setMinute(int minute) {
        this.minute = minute;
    }
    
    public boolean is24HourFormat() {
        return is24HourFormat;
    }
    
    public void set24HourFormat(boolean is24HourFormat) {
        this.is24HourFormat = is24HourFormat;
    }
    
    public TimePreference(Context ctxt) {
        this(ctxt,null);
    }
    
    public TimePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt,attrs,0);
    }
    
    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt,attrs,defStyle);
        
        is24HourFormat = DateFormat.is24HourFormat(ctxt);
        setPositiveButtonText("Ustaw");
        setNegativeButtonText("Anuluj");
    }
    
    @Override
    public String toString() {
        if (is24HourFormat) {
            return ((hour < 10) ? "0" : "")
                    + Integer.toString(hour)
                    + ":" + ((minute < 10) ? "0" : "")
                    + Integer.toString(minute);
        }
        else {
            int myHour = hour % 12;
            return ((myHour == 0) ? "12" : ((myHour < 10) ? "0" : "") + Integer.toString(myHour))
                    + ":" + ((minute < 10) ? "0" : "")
                    + Integer.toString(minute)
                    + ((hour >= 12) ? " PM" : " AM");
        }
    }
    
    @Override
    protected View onCreateDialogView() {
        return picker = new TimePicker(getContext().getApplicationContext());
    }
    
    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setIs24HourView(is24HourFormat);
        picker.setCurrentHour(hour);
        picker.setCurrentMinute(minute);
    }
    
    @Override
    public void onBindView(View view) {
        View widgetLayout;
        int childCounter = 0;
        do {
            widgetLayout = ((ViewGroup)view).getChildAt(childCounter);
            childCounter++ ;
        } while (widgetLayout.getId() != android.R.id.widget_frame);
        ((ViewGroup)widgetLayout).removeAllViews();
        timeDisplay = new TextView(widgetLayout.getContext());
        timeDisplay.setText(toString());
        ((ViewGroup)widgetLayout).addView(timeDisplay);
        super.onBindView(view);
    }
    
    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        
        if (positiveResult) {
            picker.clearFocus();
            hour = picker.getCurrentHour();
            minute = picker.getCurrentMinute();
            
            String time = String.valueOf(hour) + ":" + String.valueOf(minute);
            
            if (callChangeListener(time)) {
                persistString(time);
                timeDisplay.setText(toString());
            }
        }
    }
    
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }
    
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;
        
        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            }
            else {
                time = getPersistedString(defaultValue.toString());
            }
        }
        else {
            if (defaultValue == null) {
                time = "00:00";
            }
            else {
                time = defaultValue.toString();
            }
            if (shouldPersist()) {
                persistString(time);
            }
        }
        
        String[] timeParts = time.split(":");
        hour = Integer.parseInt(timeParts[0]);
        minute = Integer.parseInt(timeParts[1]);;
    }
}
