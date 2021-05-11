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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

import it.pgp.basicsingletouch.utils.TLSSocketFactoryCompat;

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
    }

    public void clearCanvas(View unused) {
        path2.reset();
        view.invalidate();
    }

    public void testTls12NoCert(View unused) {
        final String s = ((EditText)findViewById(R.id.tlsRemoteHost)).getText().toString();
        new Thread(()->{
            try {
                TLSSocketFactoryCompat f = new TLSSocketFactoryCompat("");
                Socket clientSocket = f.createSocket(s, 11111);
                InputStream i = clientSocket.getInputStream();
                OutputStream o = clientSocket.getOutputStream();
                o.write(new byte[]{0x1F});
                clientSocket.close();
                runOnUiThread(()->Toast.makeText(this, "TLS connection test OK for host:"+s, Toast.LENGTH_SHORT).show());
            }
            catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(()->Toast.makeText(this, "TLS connection test failed for host:"+s, Toast.LENGTH_SHORT).show());
            }
        }).start();
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
                    break;
                case MotionEvent.ACTION_UP:
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
                    x2 = event.getX();
                    y2 = event.getY();
                    dx = x2-x1;
                    dy = y2-y1;
                    if(clickDuration < MAX_CLICK_DURATION && dx < MAX_CLICK_DISTANCE && dy < MAX_CLICK_DISTANCE) {
                        Toast.makeText(getApplicationContext(), "clicked", Toast.LENGTH_SHORT).show();
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