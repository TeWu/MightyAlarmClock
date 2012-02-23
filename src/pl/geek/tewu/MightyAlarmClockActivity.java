package pl.geek.tewu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MightyAlarmClockActivity extends PreferenceActivity {
    
    private OnPreferenceChangeListener enabled_pref_changed_listener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            AlarmSetter alarmSetter = AlarmSetter.getInstance(MightyAlarmClockActivity.this);
            if ((Boolean)newValue) alarmSetter.setAlarm();
            else alarmSetter.cancelAlarm();
            return true;
        }
    };
    
    private final OnSharedPreferenceChangeListener prefsChangeHandler = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Context context = MightyAlarmClockActivity.this;
            if (key.equals("preferences_alarm_time")) {
                CheckBoxPreference enabled_pref = (CheckBoxPreference)findPreference("preferences_alarm_enabled");
                enabled_pref.setChecked(true);
                
                AlarmSetter alarmSetter = AlarmSetter.getInstance(context);
                alarmSetter.setAlarm();
            }
        }
    };
    
    private SharedPreferences prefs;
    

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        addPreferencesFromResource(R.xml.preferences);
        
        CheckBoxPreference enabled_pref = (CheckBoxPreference)findPreference("preferences_alarm_enabled");
        enabled_pref.setOnPreferenceChangeListener(enabled_pref_changed_listener);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(prefsChangeHandler);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        if (!isFinishing()) finish();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(prefsChangeHandler);
        System.exit(0); // hack to prevent task killer from killing scheduled alarm in AlarmManager
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemTestAlarm:
                startActivity(new Intent(this,FakeAlarmActivity.class));
                break;
        }
        return true;
    }
    
}
