package com.fersifex.fishcards;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity  {

    private static final int NOTIFY_ID = 1337;

    private Button startRunningButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startRunningButton = (Button) findViewById(R.id.start_running_button);
        startRunningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                raisePermanentNotification();
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void raisePermanentNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setOngoing(true).setDefaults(Notification.DEFAULT_ALL)
                .setContentTitle("Add word").setContentText("Text goes here")
                .setSmallIcon(android.R.drawable.stat_sys_warning);

        Intent outbound = new Intent(this, ScreenshotActivity.class);
        outbound.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, outbound, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        NotificationManager mgr=
                (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        mgr.notify(NOTIFY_ID, builder.build());
    }
}
