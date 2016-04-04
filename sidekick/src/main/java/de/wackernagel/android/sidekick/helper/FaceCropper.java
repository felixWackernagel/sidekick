/*
 * Copyright (C) 2014 lafosca Studio, SL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.wackernagel.android.sidekick.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An utility that crops faces from bitmaps.
 * It support multiple faces (max 8 by default) and crop them all, fitted in the same image.
 */
public class FaceCropper {

    private static final String TAG = "FaceCropper";

    public static final int FACE_MARGIN_PX  = 0;
    public static final int EYE_DISTANCE_FACTOR_MARGIN  = 1;

    @IntDef({FACE_MARGIN_PX, EYE_DISTANCE_FACTOR_MARGIN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SizeMode {}

    private static final int MAX_FACES = 8;
    private static final int MIN_FACE_SIZE = 200;

    private int mFaceMinSize = MIN_FACE_SIZE;
    private int mFaceMarginPx = 100;
    private float mEyeDistanceFactorMargin = 2f;
    private int mMaxFaces = MAX_FACES;
    private @SizeMode int mSizeMode = EYE_DISTANCE_FACTOR_MARGIN;
    private boolean mDebug;
    private Paint mDebugPainter;
    private Paint mDebugAreaPainter;

    /**
     * Creates a FaceCropper with sizeMode EYE_DISTANCE_FACTOR_MARGIN and maximum 8 faces.
     */
    public FaceCropper() {
    }

    public FaceCropper(int faceMarginPx) {
        setFaceMarginPx(faceMarginPx);
    }

    public FaceCropper(float eyesDistanceFactorMargin) {
        setEyeDistanceFactorMargin(eyesDistanceFactorMargin);
    }

    /**
     * @return maximum number of faces to be recognized
     */
    public int getMaxFaces() {
        return mMaxFaces;
    }

    /**
     * Adjust the maximum number of faces to be recognized.
     * @param maxFaces maximum number of faces
     */
    public void setMaxFaces(int maxFaces) {
        this.mMaxFaces = maxFaces;
    }

    /**
     * @return minimum face size
     */
    public int getFaceMinSize() {
        return mFaceMinSize;
    }

    /**
     * Define the minimum face size. Eyes distance * 3 usually fits an entire face.
     * @param faceMinSize in pixels
     */
    public void setFaceMinSize(int faceMinSize) {
        mFaceMinSize = faceMinSize;
    }

    /**
     * @return face margin in pixels
     */
    public int getFaceMarginPx() {
        return mFaceMarginPx;
    }

    /**
     * Define the face margin for each size of a detected face.
     * @param faceMarginPx in pixels
     */
    public void setFaceMarginPx(int faceMarginPx) {
        mFaceMarginPx = faceMarginPx;
        mSizeMode = FACE_MARGIN_PX;
    }

    /**
     * @return the size mode for face margin. 0 for concrete faceMarginPx and 1 for EyeDistanceFactorMargin.
     */
    @SizeMode
    public int getSizeMode() {
        return mSizeMode;
    }

    /**
     * @return eye distance factor for face margin
     */
    public float getEyeDistanceFactorMargin() {
        return mEyeDistanceFactorMargin;
    }

    /**
     * Define a factor which is multiplied by the eye distance. The calculated result is added to the face size as margin.
     * @param eyeDistanceFactorMargin as a multiplier of the distance between the detected face eyes
     */
    public void setEyeDistanceFactorMargin(float eyeDistanceFactorMargin) {
        mEyeDistanceFactorMargin = eyeDistanceFactorMargin;
        mSizeMode = EYE_DISTANCE_FACTOR_MARGIN;
    }

    /**
     * @return true when debug is enabled otherwise false
     */
    public boolean isDebug() {
        return mDebug;
    }

    /**
     * Draws a red circle around detected faces.
     * @param debug for debug mode
     */
    public void setDebug(boolean debug) {
        mDebug = debug;
    }

    private CropResult cropFace(Bitmap original, boolean debug) {
        Bitmap fixedBitmap = forceEvenBitmapSize(original);
        fixedBitmap = forceConfig565(fixedBitmap);
        Bitmap mutableBitmap = fixedBitmap.copy(Bitmap.Config.RGB_565, true);

        if (fixedBitmap != mutableBitmap) {
            fixedBitmap.recycle();
        }

        FaceDetector faceDetector = new FaceDetector(
                mutableBitmap.getWidth(), mutableBitmap.getHeight(),
                mMaxFaces);

        FaceDetector.Face[] faces = new FaceDetector.Face[mMaxFaces];

        // The bitmap must be in 565 format (for now).
        int faceCount = faceDetector.findFaces(mutableBitmap, faces);

        if( debug ) {
            Log.d(TAG, faceCount + " faces found");

            if( mDebugPainter == null ) {
                mDebugPainter = new Paint();
                mDebugPainter.setColor(Color.RED);
                mDebugPainter.setAlpha(80);
            }
        }

        if (faceCount == 0) {
            return new CropResult(mutableBitmap);
        }

        int initX = mutableBitmap.getWidth();
        int initY = mutableBitmap.getHeight();
        int endX = 0;
        int endY = 0;

        PointF centerFace = new PointF();

        Canvas canvas = new Canvas(mutableBitmap);
        canvas.drawBitmap(mutableBitmap, new Matrix(), null);

        // Calculates minimum box to fit all detected faces
        for (int i = 0; i < faceCount; i++) {
            FaceDetector.Face face = faces[i];

            // Eyes distance * 3 usually fits an entire face
            int faceSize = (int) (face.eyesDistance() * 3);

            if (FACE_MARGIN_PX == mSizeMode) {
                faceSize += mFaceMarginPx * 2; // *2 for top and down/right and left effect
            } else if (EYE_DISTANCE_FACTOR_MARGIN == mSizeMode) {
                faceSize += face.eyesDistance() * mEyeDistanceFactorMargin;
            }

            faceSize = Math.max(faceSize, mFaceMinSize);

            face.getMidPoint(centerFace);

            if (debug) {
                canvas.drawPoint(centerFace.x, centerFace.y, mDebugPainter);
                canvas.drawCircle(centerFace.x, centerFace.y, face.eyesDistance() * 1.5f, mDebugPainter);
            }

            int tInitX = (int) (centerFace.x - faceSize / 2);
            int tInitY = (int) (centerFace.y - faceSize / 2);
            tInitX = Math.max(0, tInitX);
            tInitY = Math.max(0, tInitY);

            int tEndX = tInitX + faceSize;
            int tEndY = tInitY + faceSize;
            tEndX = Math.min(tEndX, mutableBitmap.getWidth());
            tEndY = Math.min(tEndY, mutableBitmap.getHeight());

            initX = Math.min(initX, tInitX);
            initY = Math.min(initY, tInitY);
            endX = Math.max(endX, tEndX);
            endY = Math.max(endY, tEndY);
        }

        int sizeX = endX - initX;
        int sizeY = endY - initY;

        if (sizeX + initX > mutableBitmap.getWidth()) {
            sizeX = mutableBitmap.getWidth() - initX;
        }
        if (sizeY + initY > mutableBitmap.getHeight()) {
            sizeY = mutableBitmap.getHeight() - initY;
        }

        Point init = new Point(initX, initY);
        Point end = new Point(initX + sizeX, initY + sizeY);

        return new CropResult(mutableBitmap, init, end);
    }

    /**
     * Get a non cropped image with red circles around detected faces and a green rectangle for the crop area.
     *
     * @param ctx to get the image
     * @param resDrawable which is cropped
     * @return cropped image in debug mode
     */
    public Bitmap getFullDebugImage( @NonNull final Context ctx, @DrawableRes final int resDrawable) {
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        return getFullDebugImage(BitmapFactory.decodeResource(ctx.getResources(), resDrawable, bitmapOptions));
    }

    /**
     * Get a non cropped image with red circles around detected faces and a green rectangle for the crop area.
     *
     * @param bitmap which is cropped
     * @return cropped image in debug mode
     */
    public Bitmap getFullDebugImage( @NonNull final Bitmap bitmap) {
        CropResult result = cropFace(bitmap, true);
        Canvas canvas = new Canvas(result.getBitmap());

        if( mDebugAreaPainter == null ) {
            mDebugAreaPainter = new Paint();
            mDebugAreaPainter.setColor(Color.GREEN);
            mDebugAreaPainter.setAlpha(80);
        }

        canvas.drawBitmap(result.getBitmap(), new Matrix(), null);
        canvas.drawRect(result.getInit().x,
                result.getInit().y,
                result.getEnd().x,
                result.getEnd().y,
                mDebugAreaPainter);

        return result.getBitmap();
    }

    /**
     * Get a cropped image with detected faces inside.
     *
     * @param ctx to get the image
     * @param resDrawable which is cropped
     * @return cropped image
     */
    public Bitmap getCroppedImage( @NonNull final Context ctx, @DrawableRes final int resDrawable ) {
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;

        return getCroppedImage(BitmapFactory.decodeResource(ctx.getResources(), resDrawable, bitmapOptions));
    }

    /**
     * Get a cropped image with detected faces inside.
     *
     * @param bitmap bitmap which is cropped
     * @return cropped image
     */
    public Bitmap getCroppedImage( @NonNull final Bitmap bitmap ) {
        CropResult result = cropFace(bitmap, mDebug);
        Bitmap croppedBitmap = Bitmap.createBitmap(result.getBitmap(),
                result.getInit().x,
                result.getInit().y,
                result.getEnd().x - result.getInit().x,
                result.getEnd().y - result.getInit().y);

        if (result.getBitmap() != croppedBitmap) {
            result.getBitmap().recycle();
        }

        return croppedBitmap;
    }

    private static class CropResult {
        Bitmap mBitmap;
        Point mInit;
        Point mEnd;

        public CropResult(Bitmap bitmap, Point init, Point end) {
            mBitmap = bitmap;
            mInit = init;
            mEnd = end;
        }

        public CropResult(Bitmap bitmap) {
            mBitmap = bitmap;
            mInit = new Point(0, 0);
            mEnd = new Point(bitmap.getWidth(), bitmap.getHeight());
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public Point getInit() {
            return mInit;
        }

        public Point getEnd() {
            return mEnd;
        }
    }

    /**************** Bitmap Utils ****************/

    private Bitmap forceEvenBitmapSize(Bitmap original) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width % 2 == 1) {
            width++;
        }
        if (height % 2 == 1) {
            height++;
        }

        Bitmap fixedBitmap = original;
        if (width != original.getWidth() || height != original.getHeight()) {
            fixedBitmap = Bitmap.createScaledBitmap(original, width, height, false);
        }

        if (fixedBitmap != original) {
            original.recycle();
        }

        return fixedBitmap;
    }

    private Bitmap forceConfig565(Bitmap original) {
        Bitmap convertedBitmap = original;
        if (original.getConfig() != Bitmap.Config.RGB_565) {
            convertedBitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(convertedBitmap);
            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            canvas.drawBitmap(original, 0, 0, paint);

            if (convertedBitmap != original) {
                original.recycle();
            }
        }

        return convertedBitmap;
    }
}
