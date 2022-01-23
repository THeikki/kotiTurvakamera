package com.example.kotiturvakamera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;


public class MainActivity extends AppCompatActivity {

    WebView web;
    Integer alertCountAtBeginning;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(getAlertCountAtBeginning).start();

        web = findViewById(R.id.webView);
        WebSettings webSettings = web.getSettings();
        webSettings.setJavaScriptEnabled(true);

        web.post(new Runnable() {
            public void run() {
                web.loadUrl("https://raspberry-webpage.herokuapp.com");
            }
        });
    }

    public class CheckNewAlerts extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    AlertServiceSingleton alert = new AlertServiceSingleton(getApplicationContext());
                    alert.getAlerts(new AlertServiceSingleton.VolleyResponseListener() {
                        @Override
                        public void onError(String message) {
                            System.out.println("Error");
                        }

                        @Override
                        public void onResponse(Integer alertCount) {

                            if (alertCountAtBeginning < alertCount) {
                                new Thread(sendNotification).start();
                                alertCountAtBeginning = alertCount;
                            } else if (alertCountAtBeginning > alertCount) {
                                alertCountAtBeginning = alertCount;
                            }
                        }
                    });

                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public Runnable sendNotification = new Runnable() {
        @Override
        public void run() {
            NotificationChannel channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getBaseContext(), "Notification");
            builder.setContentTitle("Hälytys");
            builder.setContentText("Liikettä havaittu");
            builder.setAutoCancel(true);
            builder.setSmallIcon(R.drawable.ic_baseline_notification);
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setCategory(NotificationCompat.CATEGORY_ALARM);

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
            managerCompat.notify(1, builder.build());
        }
    };



    public Runnable getAlertCountAtBeginning = new Runnable() {
        @Override
        public void run() {
            AlertServiceSingleton alert = new AlertServiceSingleton(MainActivity.this);
            alert.getAlerts(new AlertServiceSingleton.VolleyResponseListener() {
                @Override
                public void onError(String message) {
                    System.out.println("Error");
                }
                @Override
                public void onResponse(Integer alertCount) {
                    System.out.println("Hälytyksiä aluksi: " + alertCount);
                    alertCountAtBeginning = alertCount;
                }
            });

            CheckNewAlerts checkNewAlerts = new CheckNewAlerts();
            checkNewAlerts.start();

        }
    };
}