package pl.geek.tewu;

import android.os.Bundle;

public class FakeAlarmActivity extends AlarmActivity {
    @Override
    protected void onCreate(Bundle state) {
        fake = true;
        super.onCreate(state);
    }
}
