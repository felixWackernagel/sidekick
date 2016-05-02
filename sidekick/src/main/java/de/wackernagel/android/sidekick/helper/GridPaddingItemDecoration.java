package de.wackernagel.android.sidekick.helper;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridPaddingItemDecoration extends RecyclerView.ItemDecoration {
    private final int gridSpacingPx;
    private final int columnCount;
    private boolean mNeedLeftSpacing = false;

    public GridPaddingItemDecoration( int gridSpacing, int gridColumns ) {
        gridSpacingPx = gridSpacing;
        columnCount = gridColumns;
    }

    @Override
    public void getItemOffsets( Rect outRect, View view, RecyclerView parent, RecyclerView.State state ) {
        int frameWidth = (int) ( ( parent.getWidth() - (float) gridSpacingPx * ( columnCount - 1 ) ) / columnCount );
        int padding = parent.getWidth() / columnCount - frameWidth;
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();

        if (itemPosition < columnCount) {
            outRect.top = 0;
        } else {
            outRect.top = gridSpacingPx;
        }

        if (itemPosition % columnCount == 0) {
            outRect.left = 0;
            outRect.right = padding;
            mNeedLeftSpacing = true;
        } else if ((itemPosition + 1) % columnCount == 0) {
            mNeedLeftSpacing = false;
            outRect.right = 0;
            outRect.left = padding;
        } else if (mNeedLeftSpacing) {
            mNeedLeftSpacing = false;
            outRect.left = gridSpacingPx - padding;
            if ((itemPosition + 2) % columnCount == 0) {
                outRect.right = gridSpacingPx - padding;
            } else {
                outRect.right = gridSpacingPx / 2;
            }
        } else if ((itemPosition + 2) % columnCount == 0) {
            mNeedLeftSpacing = false;
            outRect.left = gridSpacingPx / 2;
            outRect.right = gridSpacingPx - padding;
        } else {
            mNeedLeftSpacing = false;
            outRect.left = gridSpacingPx / 2;
            outRect.right = gridSpacingPx / 2;
        }

        outRect.bottom = 0;
    }
}