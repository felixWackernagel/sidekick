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
        textView.setText(
                "Device:"
                + "\n\twidth=" + DeviceUtils.getWidth(this) + "px"
                + "\n\theight=" + DeviceUtils.getHeight(this) + "px"
                + "\n\thas tablet size? " + DeviceUtils.isTablet(this)
                + "\n\tis landscape mode? " + DeviceUtils.isLandscape(this)
                + "\nInternet:"
                + "\n\tonline? " + NetworkUtils.isOnline(this)
                + "\n\tmobile? " + NetworkUtils.isOnlineMobile(this)
                + "\n\twifi? " + NetworkUtils.isOnlineWifi(this));
    }
}
