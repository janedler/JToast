package com.janedler.V1;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author janedler
 * @ClassName: com.janedler.V1.JToastManager
 * @Description: CFPToast管理类
 * @date 2016/3/9 12:50
 */
public class JToastManager {

    private static final LinkedBlockingDeque<JToast.ToastEntity> mQueue = new LinkedBlockingDeque(128);

    private static class CFPToastManagerHolder {
        private static JToastManager instance = new JToastManager();
    }

    public static JToastManager getInstance() {
        return JToastManager.CFPToastManagerHolder.instance;
    }

    private JToastManager() {
    }

    public void enqueueToast(Context context, final JToast.ToastEntity entity, int duration) {
        if (entity == null) {
            return;
        }
        mQueue.offer(entity);
        Log.e("Thread", "mQueue size " + mQueue.size());
        //检查并通知CFPToastService开始工作
        JToastService.getInstance().notifiyAndCheckToastRuning();
    }

    public LinkedBlockingDeque<JToast.ToastEntity> getQueue() {
        Log.e("Thread", "mQueue size >> " + mQueue.size());
        return mQueue;
    }


    public void cleanQueue() {
        if (mQueue == null || mQueue.isEmpty()) {
            return;
        }
        mQueue.clear();
    }


}