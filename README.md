# Sidekick

Sidekick is a android library project with utils, helpers and micro frameworks.

This project is in the early stages of development but grows continuously to support your own app.

## Utils

* DeviceUtils
* NetworkUtils

## Widgets

```xml
<de.wackernagel.android.sidekick.widgets.ForegroundRelativeLayout 
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:foreground="?attr/selectableItemBackground"
    android:foregroundGravity="fill">
    ...
</ de.wackernagel.android.sidekick.widgets.ForegroundRelativeLayout>
```

## Helper

```java
private final CameraHelper helper = new CameraHelper( "MyAppName" );
...
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...
    final Button startCamera = (Button) findViewById(R.id.button);
    startCamera.setEnabled(CameraHelper.hasCameraFeatures(this) && CameraHelper.isExternalStorageAccessible());
    startCamera.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            helper.startCameraActivity(MyActivity.this);
            }
    });
    ...
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    helper.handleCameraResult( this, requestCode, resultCode, data );
    helper.displayCapturedPhoto(imageView);
    imageView.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            return helper.deleteMedia( v.getContext() );
        }
    } );
}
```
