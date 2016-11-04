package de.wackernagel.android.sidekick.utils;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

/**
 * Show a toast below an anchor view except its go over the bottom edge. Then it shows the toast above the anchor.
 * Additional is the toast centered horizontal from anchor point except it goes over the left or right edges of the screen.
 * Then is the toast aligned on the left or right site of the anchor.
 * The toast is triggered by a long click so it overrides the OnLongClickListener of the given anchor view.
 */
public class TooltipUtils {

    private TooltipUtils() {
    }

    /**
     * Create a tooltip with the contentDescription text of the view.
     *
     * @param view which get the tooltip
     */
    public static void createFor(@NonNull final View view) {
        createFor(view, view.getContentDescription());
    }

    /**
     * Create a tooltip with the text from the given id.
     *
     * @param view which get the tooltip
     * @param tooltipTextId for tooltip text
     */
    public static void createFor(@NonNull final View view, @StringRes final int tooltipTextId) {
        createFor(view, view.getResources().getString(tooltipTextId));
    }

    /**
     * Create a tooltip with the text.
     *
     * @param view which get the tooltip
     * @param tooltipText for tooltip text
     */
    public static void createFor( @NonNull final View view, @NonNull final CharSequence tooltipText) {
        view.setOnLongClickListener( new ShowTooltipLongClickListener( tooltipText ) );
    }

    /**
     * @param view which has a tooltip
     */
    public static void removeFrom( @NonNull final View view ) {
        view.setOnLongClickListener( null );
    }

    private static class ShowTooltipLongClickListener implements View.OnLongClickListener {

        private final CharSequence tooltipText;

        ShowTooltipLongClickListener( final CharSequence tooltipText ) {
            this.tooltipText = tooltipText;
        }

        @SuppressLint( "RtlHardcoded" )
        @Override
        public boolean onLongClick( View tooltipAnchorView ) {
            final Toast tooltip = Toast.makeText(tooltipAnchorView.getContext(), tooltipText, Toast.LENGTH_SHORT);

            // measure final tooltip size
            final DisplayMetrics metrics = tooltipAnchorView.getResources().getDisplayMetrics();
            tooltip.getView().measure(
                    View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.UNSPECIFIED) );
            final int tooltipWidth = tooltip.getView().getMeasuredWidth();
            final int tooltipHeight = tooltip.getView().getMeasuredHeight();
            final int tooltipHalfWidth = tooltipWidth / 2;

            // collect anchor and screen coordinates
            final int[] anchorPosition = new int[2];
            tooltipAnchorView.getLocationOnScreen(anchorPosition);
            final int anchorWidth = tooltipAnchorView.getWidth();
            final int anchorHeight = tooltipAnchorView.getHeight();
            final int anchorX = anchorPosition[0];
            final int anchorY = anchorPosition[1];
            final int anchorCenterY = anchorY + anchorHeight / 2;
            final int anchorCenterX = anchorX + anchorWidth / 2;

            final Rect displayScreen = new Rect();
            tooltipAnchorView.getWindowVisibleDisplayFrame(displayScreen);

            // layout the tooltip
            int tooltipY;
            int tooltipX;

            if( anchorCenterY + tooltipHeight > displayScreen.height() ) {
                // above anchor
                tooltipY = anchorY - tooltipHeight - ( anchorHeight / 2 );
            } else {
                // below anchor
                tooltipY = anchorCenterY;
            }

            if( anchorCenterX - tooltipHalfWidth < 0 ) {
                // left align on anchor
                tooltipX = anchorX;
            } else if( anchorCenterX + tooltipHalfWidth > displayScreen.width() ) {
                // right align on anchor
                tooltipX = anchorX + anchorWidth - tooltipWidth;
            } else {
                // center horizontal
                tooltipX = anchorCenterX - tooltipHalfWidth;
            }

            tooltip.setGravity(Gravity.TOP | Gravity.LEFT, tooltipX, tooltipY );
            tooltip.show();

            return true;
        }

    }
}
