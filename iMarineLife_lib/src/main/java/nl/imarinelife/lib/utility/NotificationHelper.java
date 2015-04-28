package nl.imarinelife.lib.utility;

import nl.imarinelife.lib.MainActivity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
 
public class NotificationHelper {
    private int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    private PendingIntent mContentIntent;
 
    /**
     * Put the notification into the status bar
     */
    public void createNotification(String title,String info) {
    	NotificationCompat.Builder builder =
    		    new NotificationCompat.Builder(MainActivity.me)
    		    .setSmallIcon(android.R.drawable.stat_sys_download)
    		    .setContentTitle(title)
    		    .setContentText(info);
    	builder.setAutoCancel(true);

    	
        //you have to set a PendingIntent on a notification to tell the system what you want it to do when the notification is selected
        //I don't want to use this here so I'm just creating a blank one
        Intent notificationIntent = new Intent();
        mContentIntent = PendingIntent.getActivity(MainActivity.me, 0, notificationIntent, 0);
        builder.setContentIntent(mContentIntent);
        
        //get the notification manager
        mNotificationManager = (NotificationManager) MainActivity.me.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
 
    /**
     * Receives progress updates from the background task and updates the status bar notification appropriately
     */
    public void progressUpdate(String title, String info) {
    	NotificationCompat.Builder builder =
    		    new NotificationCompat.Builder(MainActivity.me)
    		    .setSmallIcon(android.R.drawable.stat_sys_download)
    		    .setContentTitle(title)
    		    .setContentText(info);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
 
    /**
     * called when the background task is complete, this removes the notification from the status bar.
     * We could also use this to add a new 'task complete' notification
     */
    public void completed()    {
        //remove the notification from the status bar
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

}
