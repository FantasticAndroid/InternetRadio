package com.android.internetradio.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.internetradio.R;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class CommonUtils {

    private static final String TAG = CommonUtils.class.getSimpleName();

    /**
     * Checks if is my service running.
     *
     * @return true, if is my service running
     */
    public static boolean isMyServiceRunning(@NonNull Context context,
                                             String serviceClassName) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {

            String serviceName = service.service.getClassName();
            if (serviceClassName.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static int generateRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    /**
     * To show Dialog with message
     *
     * @param activity
     * @param message
     */
    public static void showInfoDialog(final Activity activity,
                                      final String message) {

        try {
            if (activity != null && !activity.isFinishing()) {

                AlertDialog.Builder builder = null;
                if (activity.getParent() != null)
                    builder = new AlertDialog.Builder(activity.getParent());
                else
                    builder = new AlertDialog.Builder(activity);

                // builder.setTitle("Alert!");
                builder.setIcon(R.drawable.ic_radio_small);
                if (message != null)
                    builder.setMessage(message);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!activity.isFinishing()) {
                            dialog.dismiss();
                        }
                        Runtime.getRuntime().gc();
                    }
                });
                AlertDialog msg = builder.create();
                msg.setCancelable(false);
                msg.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will be used to read json from file
     *
     * @param context
     * @param fileName
     **/
    public static String readJSONFromAssetFile(@NonNull Context context, @NonNull String fileName) {
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
            ////Log.d("JSON", "Read JSON from file: " + json);
        } catch (Exception e) {
            Log.e("readJSON", "readJSON: " + e.getMessage());
        }
        return json;
    }

    public static Map<String, String> getIdenticalQueryParameters(String urlString) throws UnsupportedEncodingException, MalformedURLException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        urlString = urlString.replace(" ", "%20");
        URL url = new URL(urlString);
        String query = url.getQuery();
        String[] pairs = query.split("&");

        try {
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }
        } catch (Exception e) {
            Log.e(TAG, "getIdenticalQueryParameters() " + e.getMessage());
        }
        return query_pairs;
    }
}
