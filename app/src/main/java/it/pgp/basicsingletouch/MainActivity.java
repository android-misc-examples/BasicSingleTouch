package it.pgp.basicsingletouch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import it.pgp.basicsingletouch.utils.RemoteTrackpad;

/**
 * Web source:
 * https://www.android-examples.com/android-simple-draw-canvas-finger-example-tutorial/
 */

public class MainActivity extends Activity {

    RelativeLayout relativeLayout;
    Paint paint;
    View view;
    Path path2;
    Bitmap bitmap;
    Canvas canvas;
    RemoteTrackpad remoteTrackpad;

    EditText tlsRemoteHost;
    Button connect_btn, left_click_btn, right_click_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relativeLayout = findViewById(R.id.relativelayout1);
        view = new SketchSheetView(MainActivity.this);
        paint = new Paint();
        path2 = new Path();
        relativeLayout.addView(view, new LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));
        paint.setDither(true);
        paint.setColor(Color.parseColor("#000000"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(6);
        remoteTrackpad = new RemoteTrackpad(this);

        tlsRemoteHost = findViewById(R.id.tlsRemoteHost);
        connect_btn = findViewById(R.id.connect_btn);
        left_click_btn = findViewById(R.id.left_click_btn);
        right_click_btn = findViewById(R.id.right_click_btn);

        toggleWidgetsInner(false);
    }

    public void clearCanvas(View unused) {
        path2.reset();
        view.invalidate();
    }

    public void toggleWidgetsInner(boolean connected) {
        tlsRemoteHost.setEnabled(!connected);
        connect_btn.setEnabled(!connected);
        left_click_btn.setEnabled(connected);
        right_click_btn.setEnabled(connected);
    }

    public void toggleWidgets(boolean connected, String host, boolean error) {
        toggleWidgetsInner(connected);
        String msg = error?"Unable to connect to ":(connected?"Connected to ":"Disconnected from ");
        Toast.makeText(this, msg+host, Toast.LENGTH_SHORT).show();
    }

    public void buttonHandler(View v) {
        switch(v.getId()) {
            case R.id.connect_btn:
            {
                String s = tlsRemoteHost.getText().toString();
                new Thread(()-> remoteTrackpad.connect(s)).start();
            }
                break;
            case R.id.left_click_btn:
                remoteTrackpad.left_click();
                break;
            case R.id.right_click_btn:
                remoteTrackpad.right_click();
                break;
        }
    }

    public static class DrawingClass {

        Path DrawingClassPath;
        Paint DrawingClassPaint;

        public Path getPath() {
            return DrawingClassPath;
        }

        public void setPath(Path path) {
            this.DrawingClassPath = path;
        }


        public Paint getPaint() {
            return DrawingClassPaint;
        }

        public void setPaint(Paint paint) {
            this.DrawingClassPaint = paint;
        }
    }

    class SketchSheetView extends View {

        private final ArrayList<DrawingClass> drawingClassArrayList = new ArrayList<>();

        public SketchSheetView(Context context) {
            super(context);
            bitmap = Bitmap.createBitmap(820, 480, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            this.setBackgroundColor(Color.WHITE);
        }

        long startClickTime;
        float x1,y1,x2,y2,dx,dy;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            DrawingClass pathWithPaint = new DrawingClass();
            canvas.drawPath(path2, paint);

            int MAX_CLICK_DURATION = 400;
            int MAX_CLICK_DISTANCE = 5;


            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
//                    long clickDuration1 = Calendar.getInstance().getTimeInMillis() - startClickTime;


                    startClickTime = Calendar.getInstance().getTimeInMillis();
                    x1 = event.getX();
                    y1 = event.getY();

                    path2.moveTo(x1, y1);
                    path2.lineTo(x1, y1);

                    remoteTrackpad.move_cursor((int)x1,(int)y1,true); // TODO
                    remoteTrackpad.motion_started();
                    break;
                case MotionEvent.ACTION_UP:
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    x2 = event.getX();
                    y2 = event.getY();
                    dx = x2-x1;
                    dy = y2-y1;
                    if(clickDuration < MAX_CLICK_DURATION && dx < MAX_CLICK_DISTANCE && dy < MAX_CLICK_DISTANCE) {
                        Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
                        remoteTrackpad.left_click();
                    }

                    // always clear canvas
                    clearCanvas(null);
                    break;
                case MotionEvent.ACTION_MOVE:
//                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    x2 = event.getX();
                    y2 = event.getY();
                    dx = x2-x1;
                    dy = y2-y1;

                    path2.lineTo(x2, y2);
                    pathWithPaint.setPath(path2);
                    pathWithPaint.setPaint(paint);
                    drawingClassArrayList.add(pathWithPaint);
                    remoteTrackpad.move_cursor((int)x2,(int)y2,false); // TODO
                    break;
            }
            invalidate();

            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (drawingClassArrayList.size() > 0) {
                canvas.drawPath(
                        drawingClassArrayList.get(drawingClassArrayList.size() - 1).getPath(),
                        drawingClassArrayList.get(drawingClassArrayList.size() - 1).getPaint());
            }
        }
    }

}