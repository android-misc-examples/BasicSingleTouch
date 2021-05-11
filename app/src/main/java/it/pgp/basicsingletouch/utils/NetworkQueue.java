package it.pgp.basicsingletouch.utils;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class NetworkQueue {

    Thread t;
    InputStream i;
    OutputStream o;
    final AtomicReference<ByteArrayOutputStream> baosr = new AtomicReference<>(new ByteArrayOutputStream());
    final Object data_available = new Object();
    final AtomicBoolean moving = new AtomicBoolean(true);

//    public static final int period_ns = 15000000;
    public static final int period_ns = 40000000;

    final Runnable r = () -> {
        try {
            for(;;) {
                LockSupport.parkNanos(period_ns);
                ByteArrayOutputStream current = baosr.get();
                if(current.size()>0) {
                    baosr.compareAndSet(current, new ByteArrayOutputStream());
                    o.write(current.toByteArray());
                }
                else if(!moving.get()) data_available.wait();
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            Log.e(getClass().getName(), "Remote host disconnected, please reconnect");
            // TODO update UI
            i = null;
            o = null;
        }
        t = null;
        baosr.set(new ByteArrayOutputStream());
        Log.i(getClass().getName(), "Flush thread ended");
    };

    public NetworkQueue(InputStream i, OutputStream o) { // TODO pass update_ui functional interface as param
        set_sock(i,o);
    }

    public void set_sock(InputStream i, OutputStream o) {
        this.i = i;
        this.o = o;
    }

    public void start_thread() {
        t = new Thread(r);
        t.start();
    }

    public void move_started() {
        moving.set(true);
        data_available.notify();
    }

    public void move_ended() {
        moving.set(false);
    }

    public void add(byte[] b) {
        try {
            baosr.get().write(b);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}