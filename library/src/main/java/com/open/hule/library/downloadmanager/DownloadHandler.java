package com.open.hule.library.downloadmanager;

import android.app.DownloadManager;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;

import com.open.hule.library.utils.UpdateManager;

import java.lang.ref.WeakReference;

/**
 * @author hule
 * @date 2019/7/15 16:43
 * description:下载监听handler
 */
public class DownloadHandler extends Handler {

    private final WeakReference<UpdateManager> wrfUpdateManager;

    public DownloadHandler(UpdateManager updateManager) {
        wrfUpdateManager = new WeakReference<>(updateManager);
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case DownloadManager.STATUS_PAUSED:
                // 暂停
                break;
            case DownloadManager.STATUS_PENDING:
                // 开始
                break;
            case DownloadManager.STATUS_RUNNING:
                // 下载中
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().setProgress(msg.arg1);
                }
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().setProgress(100);
                    wrfUpdateManager.get().unregisterContentObserver();
                }
                //下载成功,做200ms安装延迟
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (wrfUpdateManager.get() != null) {
                            wrfUpdateManager.get().dismissDialog();
                            wrfUpdateManager.get().installApp(wrfUpdateManager.get().getDownloadFile());
                        }
                    }
                }, 200);
                break;
            case DownloadManager.STATUS_FAILED:
                // 下载失败，清除本次的下载任务
                if (wrfUpdateManager.get() != null) {
                    wrfUpdateManager.get().clearCurrentTask();
                    wrfUpdateManager.get().unregisterContentObserver();
                    wrfUpdateManager.get().showFail();
                }
                break;
            default:
                break;
        }
    }
}
