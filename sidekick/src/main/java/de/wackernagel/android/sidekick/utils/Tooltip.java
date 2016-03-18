package de.wackernagel.android.sidekick.utils;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 * Create a Toast near to the given view on a long click.
 * Overrides a previous set OnLongClickListener.
 */
public class Tooltip {

    private Tooltip() {
    }

    /**
     * Create a tooltip with the contentDescription text of the view.
     *
     * @param view which get the tooltip
     */
    private static void forView(@NonNull final View view) {
        forView(view, view.getContentDescription());
    }

    /**
     * Create a tooltip with the text from the given id.
     *
     * @param view which get the tooltip
     * @param tooltipTextId for tooltip text
     */
    public static void forView(@NonNull final View view, @StringRes int tooltipTextId) {
        forView(view, view.getResources().getString(tooltipTextId));
    }

    /**
     * Create a tooltip with the text.
     *
     * @param view which get the tooltip
     * @param tooltipText for tooltip text
     */
    public static void forView(@NonNull final View view, @NonNull final CharSequence tooltipText) {
        if( TextUtils.isEmpty( tooltipText ) ) {
            throw new IllegalArgumentException( "Tooltip.forView( ... ): text can't be empty" );
        }

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int[] screenPos = new int[2];
                final Rect displayFrame = new Rect();
                v.getLocationOnScreen(screenPos);
                v.getWindowVisibleDisplayFrame(displayFrame);

                final int width = v.getWidth();
                final int height = v.getHeight();
                final int middleY = screenPos[1] + height / 2;
                int referenceX = screenPos[0] + width / 2;
                if (ViewCompat.getLayoutDirection(v) == ViewCompat.LAYOUT_DIRECTION_LTR) {
                    final int screenWidth = v.getResources().getDisplayMetrics().widthPixels;
                    referenceX = screenWidth - referenceX; // mirror
                }
                Toast cheatSheet = Toast.makeText(v.getContext(), tooltipText, Toast.LENGTH_SHORT);
                if (middleY < displayFrame.height()) {
                    // Show along the top; follow action buttons
                    cheatSheet.setGravity(Gravity.TOP | GravityCompat.END, referenceX, height);
                } else {
                    // Show along the bottom center
                    cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, height);
                }
                cheatSheet.show();
                return true;
            }
        });
    }

    /**
     * @param view which has a tooltip
     */
    public static void removeFrom( @NonNull final View view ) {
        view.setOnLongClickListener( null );
    }
}
