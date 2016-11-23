package com.janedler.V1;

import android.util.Log;

import java.util.concurrent.LinkedBlockingDeque;


/**
 * @author janedler
 * @ClassName: com.janedler.V1.JToastService
 * @Description: CFPToast服务类  里面维护CFPToast队列
 * @date 2016/3/9 12:51
 */

public class JToastService {
    private Thread mDispatchThread;

    private boolean isWhiling = false; //判断是否在循环Queue里面的数据

    private static class JToastServiceHolder {
        private static JToastService instance = new JToastService();
    }

    public static JToastService getInstance() {
        return JToastService.JToastServiceHolder.instance;
    }

    private JToastService() {
        Log.e("TAG", "initial CFPToastService");
    }

    private Thread createToastQueueThread() {
        Thread dispatchThread = new JToastService.DispatchToastQueueThread();
        dispatchThread.setPriority(Thread.MAX_PRIORITY);
        return dispatchThread;
    }

    private class DispatchToastQueueThread extends Thread {

        @Override
        public void run() {
            dispatchQueueToast();
        }

        /**
         * 对queue里面的数据进行分发处理
         */
        private synchronized void dispatchQueueToast() {
            LinkedBlockingDeque<JToast.ToastEntity> queue = JToastManager.getInstance().getQueue();
            Log.e("Thread", "mQueue size " + queue.size());
            try {
                JToast.ToastEntity entity = null;
                //如果queue里面的数据已处理完毕 线程就退出
                while (queue != null && !queue.isEmpty() && (entity = queue.poll()) != null) {
                    isWhiling = true;
                    Log.e("Thread", "CFPToastService is running");
                    entity.show();
                    Thread.sleep(entity.mDuration);
                    entity.hide();
                }
                isWhiling = false;
                Log.e("TAG", "Thread is completed");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    /**
     * 检查并通知CFPToastService开始工作
     */
    public synchronized void notifiyAndCheckToastRuning() {
        LinkedBlockingDeque<JToast.ToastEntity> queue = JToastManager.getInstance().getQueue();
        if (mDispatchThread == null || !isWhiling && queue != null && !queue.isEmpty()) {
            mDispatchThread = createToastQueueThread();
            mDispatchThread.start();
        }
    }
}
