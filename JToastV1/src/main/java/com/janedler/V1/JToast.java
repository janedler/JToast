package com.janedler.V1;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * @author janedler
 * @ClassName: com.janedler.V1.JToast
 * @Description: 自定义Toast
 * @date 2016/3/8 18:11
 * <p/>
 * 暂时只提供了Toast的基本用法 还不能支持定制 以后再进行扩展
 * <p/>
 * JToast.makeText(mContext,"hello world",JToast.LENGTH_SHORT).show();
 */
public class JToast {
    private static final String TAG = "JToast";
    private static final boolean isShowTag = true;

    public static final int LENGTH_SHORT = 2000;
    public static final int LENGTH_LONG = 3500;

    private Context mContext;
    private int mDuration;
    private JToast.ToastEntity mEntity;
    private View mView;

    public JToast(Context context) {
        this.mContext = context;
        mEntity = new JToast.ToastEntity();
        mEntity.mGravity = Gravity.BOTTOM;
        mEntity.mY = (int) mContext.getResources().getDimension(R.dimen.cfp_toast_y_offset);
    }

    /**
     * Show the view for the specified duration.
     */
    public void show() {
        if (mView == null) {
            return;
        }
        JToast.ToastEntity entity = mEntity;
        entity.mNextView = mView;
        entity.mDuration = mDuration;
        JToastManager.getInstance().enqueueToast(mContext, entity, mDuration);
    }

    /**
     * Set the view to show.
     *
     * @see #getView
     */
    public void setView(View view) {
        mView = view;
    }

    /**
     * Return the view.
     *
     * @see #setView
     */
    public View getView() {
        return mView;
    }

    /**
     * Set how long to show the view for.
     *
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public void setDuration(int duration) {
        mDuration = duration;
    }

    /**
     * Return the duration.
     *
     * @see #setDuration
     */
    public int getDuration() {
        return mDuration;
    }

    /**
     * Set the margins of the view.
     *
     * @param horizontalMargin The horizontal margin, in percentage of the
     *                         container width, between the container's edges and the
     *                         notification
     * @param verticalMargin   The vertical margin, in percentage of the
     *                         container height, between the container's edges and the
     *                         notification
     */
    public void setMargin(float horizontalMargin, float verticalMargin) {
        mEntity.mHorizontalMargin = horizontalMargin;
        mEntity.mVerticalMargin = verticalMargin;
    }

    /**
     * Return the horizontal margin.
     */
    public float getHorizontalMargin() {
        return mEntity.mHorizontalMargin;
    }

    /**
     * Return the vertical margin.
     */
    public float getVerticalMargin() {
        return mEntity.mVerticalMargin;
    }

    /**
     * Set the location at which the notification should appear on the screen.
     *
     * @see Gravity
     * @see #getGravity
     */
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mEntity.mGravity = gravity;
        mEntity.mX = xOffset;
        mEntity.mY = yOffset;
    }

    /**
     * Get the location at which the notification should appear on the screen.
     *
     * @see Gravity
     * @see #getGravity
     */
    public int getGravity() {
        return mEntity.mGravity;
    }

    /**
     * Return the X offset in pixels to apply to the gravity's location.
     */
    public int getXOffset() {
        return mEntity.mX;
    }

    /**
     * Return the Y offset in pixels to apply to the gravity's location.
     */
    public int getYOffset() {
        return mEntity.mY;
    }

    /**
     * Gets the LayoutParams for the Toast window.
     *
     * @hide
     */
    public WindowManager.LayoutParams getWindowParams() {
        return mEntity.mParams;
    }


    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     *
     * @param resId The new text for the Toast.
     */
    public void setText(int resId) {
        setText(mContext.getText(resId));
    }

    /**
     * Update the text in a Toast that was previously created using one of the makeText() methods.
     *
     * @param s The new text for the Toast.
     */
    public void setText(CharSequence s) {
        if (mView == null) {
            return;
        }
        TextView tv = (TextView) mView.findViewById(R.id.message);
        if (tv == null) {
            return;
        }
        tv.setText(s);
    }


    /**
     * 创建CFPToast
     *
     * @param context  上下文 如果传人null CFPToast就会默认使用applicationContext
     * @param text     CFPToast文字显示
     * @param duration 时间间隔
     * @return
     */
    public static JToast makeText(Context context, CharSequence text, int duration) {
        JToast result = new JToast(context);
        View view = LayoutInflater.from(context).inflate(R.layout.ui_toast_layout, null);
        TextView tv = (TextView) view.findViewById(R.id.message);
        tv.setText(text);
        result.mView = view;
        result.mDuration = duration;
        return result;
    }

    /**
     * 创建CFPToast
     *
     * @param context  上下文 如果传人null CFPToast就会默认使用applicationContext
     * @param resId    CFPToast文字资源id
     * @param duration 时间间隔
     * @return
     */
    public static JToast makeText(Context context, int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    public static class ToastEntity {

        final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();
        final Handler mHandler = new Handler();
        int mGravity;
        int mX;
        int mY = -1;
        float mHorizontalMargin;
        float mVerticalMargin;
        View mView;
        View mNextView;
        int mDuration;
        WindowManager mWM;


        final Runnable mShow = new Runnable() {
            @Override
            public void run() {
                handleShow();
            }
        };

        final Runnable mHide = new Runnable() {
            @Override
            public void run() {
                handleHide();
                mNextView = null;
            }
        };

        ToastEntity() {
            final WindowManager.LayoutParams params = mParams;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = R.style.CFPToastAnimation;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                params.type = WindowManager.LayoutParams.TYPE_TOAST;
            } else {
                params.type = WindowManager.LayoutParams.TYPE_PHONE;
            }

//            params.type = WindowManager.LayoutParams.TYPE_TOAST;
//            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            params.setTitle("Toast");
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }

        /**
         * schedule handleShow into the right thread
         */
        public void show() {
            if (isShowTag) Log.v(TAG, "SHOW: " + this);
            mHandler.post(mShow);
        }

        /**
         * schedule handleHide into the right thread
         */
        public void hide() {
            if (isShowTag) Log.v(TAG, "HIDE: " + this);
            mHandler.post(mHide);
        }

        public void handleShow() {
            if (isShowTag)
                Log.v(TAG, "HANDLE SHOW: " + this + " mView=" + mView + " mNextView=" + mNextView);
            if (mView != mNextView) {
                // remove the old view if necessary
                handleHide();
                mView = mNextView;
                Context context = mView.getContext().getApplicationContext();
                if (context == null) {
                    context = mView.getContext();
                }
                mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                mParams.gravity = mGravity;
                mParams.x = mX;
                mParams.y = mY;
                mParams.verticalMargin = mVerticalMargin;
                mParams.horizontalMargin = mHorizontalMargin;
                if (mView.getParent() != null) {
                    if (isShowTag) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }
                if (isShowTag) Log.v(TAG, "ADD! " + mView + " in " + this);
                mWM.addView(mView, mParams);
            }
        }

        public void handleHide() {
            if (isShowTag) Log.v(TAG, "HANDLE HIDE: " + this + " mView=" + mView);
            if (mView != null) {
                if (mView.getParent() != null) {
                    if (isShowTag) Log.v(TAG, "REMOVE! " + mView + " in " + this);
                    mWM.removeView(mView);
                }
                mView = null;
            }
        }


    }
}
