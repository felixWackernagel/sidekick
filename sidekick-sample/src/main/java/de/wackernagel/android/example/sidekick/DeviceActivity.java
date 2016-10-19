package de.wackernagel.android.example.sidekick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import de.wackernagel.android.sidekick.utils.DeviceUtils;
import de.wackernagel.android.sidekick.utils.NetworkUtils;

public class DeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setDeviceInfo(( TextView ) findViewById(R.id.text));
    }

    private void setDeviceInfo(final TextView textView) {
        textView.setText( getString( R.string.device_states,
                DeviceUtils.getWidth(this), DeviceUtils.getHeight(this),
                DeviceUtils.isTablet(this), DeviceUtils.isLandscape(this),
                NetworkUtils.isOnline(this), NetworkUtils.isOnlineMobile(this),
                NetworkUtils.isOnlineWifi(this) ) );
    }
}
