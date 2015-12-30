package de.michael_knape.webwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

/*
 * Created by Michael Knape on 29.12.2015.
 */
public class WidgetProvider extends AppWidgetProvider{

    public final static String EXTRA_URL = "WEB_PAGE_EXTRA";
    public static final String MY_WIDGET_IDS = "myWidgetIds";

    final String UPDATE_WIDGET_ACTION = "UPDATE_WIDGET_ACTION";
    final String START_BROWSER_ACTION = "START_BROWSER_ACTION";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // add Update Button click functionality
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_linear_horizontal);
        remoteViews.setOnClickPendingIntent(R.id.widgetUpdateButton, onClickUpdatePendingIntent(context, UPDATE_WIDGET_ACTION, appWidgetIds));

        int[] emptyIntArray = {};
        remoteViews.setOnClickPendingIntent(R.id.widget_image, onClickUpdatePendingIntent(context, START_BROWSER_ACTION, emptyIntArray));

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private PendingIntent onClickUpdatePendingIntent(Context context, String actionName, int[] appWidgetIds) {
        Intent onClickIntent = new Intent(context, WidgetProvider.class);
        onClickIntent.setAction(actionName);
        if (appWidgetIds.length > 0) {
            onClickIntent.putExtra(MY_WIDGET_IDS, appWidgetIds);
        }
        return PendingIntent.getBroadcast(context, 0, onClickIntent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)) {
            Log.d(this.toString(), AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED);

        } else if (intent.getAction().equals(UPDATE_WIDGET_ACTION)) {
            SharedPreferences prefs = context.getSharedPreferences(EXTRA_URL, Context.MODE_PRIVATE);
            if (intent.hasExtra(MY_WIDGET_IDS)) {
                callUpdateWebShotService(
                        context,
                        AppWidgetManager.getInstance(context),
                        prefs.getString(EXTRA_URL, ""),
                        intent.getIntArrayExtra(MY_WIDGET_IDS)
                );
            }
        } else if (intent.getAction().equals(START_BROWSER_ACTION)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            SharedPreferences prefs = context.getSharedPreferences(EXTRA_URL, Context.MODE_PRIVATE);
            browserIntent.setData(Uri.parse(prefs.getString(EXTRA_URL, "")));
            context.startActivity(browserIntent);
        }

        super.onReceive(context, intent);
    }

    private void callUpdateWebShotService(Context context, AppWidgetManager appWidgetManager, String webPageUrl, int[] appWidgetIds) {
        if (appWidgetIds.length < 1) {
            return;
        }

        // We attach the current Widget IDs to the alarm Intent to ensure its
        // broadcast is correctly routed to onUpdate() when our AppWidgetProvider
        // next receives it.
        Intent iWidget = new Intent(context, AppWidgetProvider.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, iWidget, 0);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .setExact(AlarmManager.RTC, System.currentTimeMillis() + 30000, pi);

        Intent iService = new Intent(context, WebShotService.class);
        iService.putExtra(EXTRA_URL, webPageUrl);
        iService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds);
        context.startService(iService);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        SharedPreferences prefs = context.getSharedPreferences(EXTRA_URL, Context.MODE_PRIVATE);
        int[] appWidgetIds = {appWidgetId};
        callUpdateWebShotService(
                context,
                appWidgetManager,
                prefs.getString(EXTRA_URL, ""),
                appWidgetIds
        );
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }
}
