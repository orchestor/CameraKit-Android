package com.wonderkiln.camerakit;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.hardware.display.DisplayManagerCompat;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Display;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CameraView extends FrameLayout {

    public static final int FACING_BACK = Constants.FACING_BACK;
    public static final int FACING_FRONT = Constants.FACING_FRONT;

    @IntDef({FACING_BACK, FACING_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Facing {
    }

    public static final int FLASH_OFF = Constants.FLASH_OFF;
    public static final int FLASH_ON = Constants.FLASH_ON;
    public static final int FLASH_AUTO = Constants.FLASH_AUTO;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FLASH_OFF, FLASH_ON, FLASH_AUTO})
    public @interface Flash {
    }

    public static final int PICTURE_MODE_QUALITY = Constants.PICTURE_MODE_QUALITY;
    public static final int PICTURE_MODE_SPEED = Constants.PICTURE_MODE_SPEED;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PICTURE_MODE_QUALITY, PICTURE_MODE_SPEED})
    public @interface PictureMode {
    }

    private int mFacing;
    private int mDefaultFacing;

    private int mFlash;
    private int mDefaultFlash;

    private int mPictureMode;

    private boolean mAdjustViewBounds;

    private CameraListener mCameraListener;
    private DisplayOrientationDetector mDisplayOrientationDetector;

    private CameraViewImpl mCameraImpl;

    public CameraView(@NonNull Context context) {
        super(context, null);
    }

    public CameraView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        final PreviewImpl preview = new TextureViewPreview(context, this);
        mCameraImpl = new Camera2(context, mCameraListener, preview);

        setFacing(mFacing);
        setFlash(mFlash);

        mDisplayOrientationDetector = new DisplayOrientationDetector(context) {
            @Override
            public void onDisplayOrientationChanged(int displayOrientation) {
                mCameraImpl.setDisplayOrientation(displayOrientation);
            }
        };
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDisplayOrientationDetector.enable(
                ViewCompat.isAttachedToWindow(this)
                        ? DisplayManagerCompat.getInstance(getContext()).getDisplay(Display.DEFAULT_DISPLAY)
                        : null
        );
    }

    @Override
    protected void onDetachedFromWindow() {
        mDisplayOrientationDetector.disable();
        super.onDetachedFromWindow();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.facing = mFacing;
        state.flash = mFlash;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setFacing(ss.facing);
        setFlash(ss.flash);
    }

    public void start() {
        mCameraImpl.start();
    }

    public void stop() {
        mCameraImpl.stop();
    }

    public void setFacing(@Facing int facing) {
        this.mFacing = facing;
        mCameraImpl.setFacing(facing);
    }

    @Facing
    public int toggleFacing() {
        switch (mFacing) {
            case FACING_BACK:
                setFacing(FACING_FRONT);
                break;

            case FACING_FRONT:
                setFacing(FACING_BACK);
                break;
        }

        return mFacing;
    }

    public void setFlash(@Flash int flash) {
        this.mFlash = flash;
        mCameraImpl.setFlash(flash);
    }

    @Flash
    public int toggleFlash() {
        switch (mFlash) {
            case FLASH_OFF:
                setFlash(FLASH_ON);
                break;

            case FLASH_ON:
                setFlash(FLASH_AUTO);
                break;

            case FLASH_AUTO:
                setFlash(FLASH_OFF);
                break;
        }

        return mFlash;
    }

    public void setPictureMode(@PictureMode int pictureMode) {
        this.mPictureMode = pictureMode;
    }

    public void setCameraListener(CameraListener cameraListener) {
        this.mCameraListener = cameraListener;
        mCameraImpl.setCameraListener(cameraListener);
    }

    public void capturePicture() {
        switch (mPictureMode) {
            case PICTURE_MODE_QUALITY:
                mCameraImpl.capturePicture();
                break;
            case PICTURE_MODE_SPEED:
                mCameraImpl.captureStill();
                break;
        }
    }

    public void startRecordingVideo() {

    }

    public void stopRecordingVideo() {

    }

    protected static class SavedState extends BaseSavedState {

        @Facing
        private int facing;

        @Flash
        private int flash;

        @SuppressWarnings("WrongConstant")
        public SavedState(Parcel source, ClassLoader loader) {
            super(source);
            facing = source.readInt();
            flash = source.readInt();
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(facing);
            out.writeInt(flash);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                return new SavedState(in, loader);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }

        });

    }

}
