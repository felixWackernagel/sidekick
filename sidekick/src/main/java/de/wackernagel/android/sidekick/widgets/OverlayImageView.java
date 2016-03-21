/*
 * Copyright 2014 Chris Banes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.wackernagel.android.sidekick.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.FloatRange;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

public class OverlayImageView extends ImageView {

    private int maskAlpha = 51;
    private int maskColor = Color.BLACK;
    private final Paint maskPaint = new Paint();

    public OverlayImageView(Context context) {
        this(context, null);
    }

    public OverlayImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverlayImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setMaskColorResource( @ColorRes int colorRes ) {
        setMaskColor(ContextCompat.getColor(getContext(), colorRes));
    }

    public void setMaskColor(@ColorInt int color) {
        if (this.maskColor != color) {
            this.maskColor = color;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setMaskAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
        int scrimAlpha  = (int) (255 * alpha);
        if( this.maskAlpha != scrimAlpha ) {
            this.maskAlpha = scrimAlpha;
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        maskPaint.setColor( ColorUtils.setAlphaComponent( maskColor, maskAlpha ) );
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), maskPaint);
    }

}