package it.pgp.basicsingletouch.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import it.pgp.basicsingletouch.MainActivity;

public class RemoteTrackpad {
    enum CODES {
        CONNECT((byte)0x1E),
        START_MOVE((byte)0xF0),
        MOVE((byte)0xF1),
        LEFTCLICK((byte)0xF2),
        RIGHTCLICK((byte)0xF3);

        public byte i;

        CODES(byte i) {
            this.i = i;
        }
    }

    InputStream i;
    OutputStream o;
    NetworkQueue Q;

    final MainActivity mainActivity;

    public RemoteTrackpad(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void connect(String host) {
        try {
            TLSSocketFactoryCompat f = new TLSSocketFactoryCompat("");
            Socket clientSocket = f.createSocket(host, 11111);
            i = clientSocket.getInputStream();
            o = clientSocket.getOutputStream();
            o.write(CODES.CONNECT.i);
            Q = new NetworkQueue(i,o,mainActivity);
            mainActivity.runOnUiThread(()-> mainActivity.toggleWidgets(true, host, false));
            Q.start_thread();
        }
        catch(Exception e) {
            e.printStackTrace();
            mainActivity.runOnUiThread(()->mainActivity.toggleWidgets(false, host, true));
        }
    }

    public void motion_started() {
        if(Q != null) Q.move_started();
    }

    public void motion_ended() {
        if(Q != null) Q.move_ended();
    }

    public void left_click() {
        if(Q != null) Q.add(new byte[]{CODES.LEFTCLICK.i});
    }

    public void right_click() {
        if(Q != null) Q.add(new byte[]{CODES.RIGHTCLICK.i});
    }

    public void move_cursor(int x, int y, boolean start_move) {
        if(Q != null) {
            byte[] bb = new byte[5];
            bb[0] = start_move?CODES.START_MOVE.i : CODES.MOVE.i;
            System.arraycopy(Misc.castUnsignedNumberToBytes(x,2),0,bb,1,2);
            System.arraycopy(Misc.castUnsignedNumberToBytes(y,2),0,bb,3,2);
            Q.add(bb);
        }
    }
}
