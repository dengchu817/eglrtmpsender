package com.example.dengchu.eglrtmpsender.Rtmp;

/**
 * Created by dengchu on 2018/6/7.
 */

public interface ConnectionListener {
    void onOpenConnectionResult(int result);

    void onWriteError(int errno);

    void onCloseConnectionResult(int result);

    class RESWriteErrorRunable implements Runnable {
        ConnectionListener connectionListener;
        int errno;

        public RESWriteErrorRunable(ConnectionListener connectionListener, int errno) {
            this.connectionListener = connectionListener;
            this.errno = errno;
        }

        @Override
        public void run() {
            if (connectionListener != null) {
                connectionListener.onWriteError(errno);
            }
        }
    }
}
