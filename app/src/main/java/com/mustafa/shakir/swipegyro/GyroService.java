package com.mustafa.shakir.swipegyro;

import java.io.DataOutputStream;
import java.io.IOException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataOutputStream;

public class GyroService extends Service implements SensorEventListener {
    Process m_process = null;
    DataOutputStream m_dataOut = null;

    public final String TAG = GyroService.class.getSimpleName();
    private SensorManager sensorManager;
    private Sensor sensor;
    private long lastUpdate = 0,lastUpdate2=System.currentTimeMillis();
    private float lastX = 0, lastY = 0, lastZ = 0, minX = 0,maxX=0,minY = 0,maxY=0;
    private static final int SHAKE_THRESHOLD = 600;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getCalib(float x,float y,float z){
        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > 10) {
            if(x>maxX) {
                maxX = x;
            }
            if(x<minX){
                minX = x;
            }
            if ((curTime - lastUpdate) > 10) {
                if (y > maxY) {
                    maxY = y;
                }
                if (y < minY) {
                    minY = y;
                }
            }
            lastUpdate = curTime;
        }
        Log.i(TAG, "MAX:"+maxX+" MIN:"+minX);
        }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == sensor.TYPE_LINEAR_ACCELERATION) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            /*
            switch (mDisplay.getRotation()) {
            case Surface.ROTATION_0:
                mSensorX = event.values[0];
                mSensorY = event.values[1];
                break;
            case Surface.ROTATION_90:
                mSensorX = -event.values[1];
                mSensorY = event.values[0];
                break;
            case Surface.ROTATION_180:
                mSensorX = -event.values[0];
                mSensorY = -event.values[1];
                break;
            case Surface.ROTATION_270:
                mSensorX = event.values[1];
                mSensorY = -event.values[0];
         }
             */
            long curTime = System.currentTimeMillis();

            if((curTime-lastUpdate2)<10001){
                getCalib(x,y,z);
            }
            if((curTime-lastUpdate2)>10001) {
                int wid = getScreenWidth();
                int hi = getScreenHeight();
                if (x > maxX+0.1) {
                    swipe(wid / 2, hi / 2, wid * 9 / 15, hi / 2, 50);
                }
                if (x < minX-0.1) {
                    swipe(wid / 2, hi / 2, wid * 6 / 15, hi / 2, 50);
                }
                if (y > maxY+0.1) {
                    swipe(wid / 2, hi / 2, wid/2, hi *9/15, 50);
                }
                if (y < minY-0.1) {
                    swipe(wid / 2, hi / 2, wid /2, hi*6/15, 50);
                }
            /*if(y-lastY<0.1 && z-lastZ<0.1) {
                swipe(wid/2,hi/2,wid*(3/4),hi/2,100);
            }*/
            /*long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 10) {

                //long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                //float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;
                if (x>maxX) {
                    //Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    //vib.vibrate(200);
                    swipe(wid / 2, hi / 2, wid * 5 / 8, hi / 2, 100);
                    maxX = x;
                }

                lastX = x;
                lastY = y;
                lastZ = z;

            }
            */
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        askForRoot();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    /*
    private void drag(float fromX, float toX, float fromY, float toY, int stepCount){
        runSwipeCommand((int)fromX, (int)toX, (int)fromY, (int)toY, stepCount);
    }
*/
    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    private void askForRoot() {
        try {
            m_process = Runtime.getRuntime().exec("su");
            m_dataOut = new DataOutputStream(m_process.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void swipe(int x1, int y1, int x2, int y2, int duration) {
        try {
            Thread.sleep(0);
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String cmd = "/system/bin/input swipe " + x1 + " " + y1 + " " + x2 + " " + y2 + " " + duration + "\n";
            os.writeBytes(cmd);
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.waitFor();
            Log.e(TAG, "Command executed: " + cmd);
        } catch (IOException e) {
            Log.e(TAG, "Runtime problems\n");
            e.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /*
    private void runSwipeCommand(final int fromX, final int toX, final int fromY, final int toY, final int duration){
        Thread t1 = new Thread(new Runnable(){
            public void run(){
                try {
                    if(m_process != null && m_dataOut != null){
                        String cmd = "/system/bin/input swipe "+fromX+" "+fromY+" "+toX+" "+toY+" "+ duration+"\n";
                        m_dataOut.writeBytes(cmd);
                        m_dataOut.writeBytes("exit\n");
                        m_dataOut.flush();
                        m_dataOut.close();
                        m_process.waitFor();
                        Log.e(TAG, "Command executed: " + cmd);
                    }
                } catch (IOException e) { e.printStackTrace();} catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }
    */

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
