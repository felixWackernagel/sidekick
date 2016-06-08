# Sidekick

Sidekick is a android library project with utils, helpers and micro frameworks.

This project is in the early stages of development but grows continuously to support your own app.

## Installation

```gradle
dependencies {
    ...
    compile 'de.wackernagel.android:sidekick:1.3.3'
}
```

## Docs

Check-out the [Wiki](../../wiki) pages for further examples and descriptions.

## Utils

* DeviceUtils
* NetworkUtils
* ColorFilterUtils
* TintUtils
* NetworkUtils
* TooltipUtils
* TypefaceUtils
* PreferenceUtils

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

```xml
<de.wackernagel.android.sidekick.widgets.CircularRevealView
    android:id="@+id/circularRevealView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:color="@android:color/white"/>
```
```java
final CircularRevealView circularRevealView = (CircularRevealView) findViewById( R.id.circularRevealView );
circularRevealView.enterReveal( new int[]{ centerWidth, centerHeight } );
```
