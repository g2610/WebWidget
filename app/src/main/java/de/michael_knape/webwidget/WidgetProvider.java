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
import android.widget.RemoteViews;
import android.widget.Toast;

/*
 * Created by Michael Knape on 29.12.2015.
 */
public class WidgetProvider extends AppWidgetProvider {

    public final static String SHARED_PREF_WEB_URL_NAME = "WebWidgetManager_web_url_prefs";
    public final static String EXTRA_URL = "WEB_PAGE_EXTRA";

    final String UPDATE_WIDGET_ACTION = "UPDATE_WIDGET_ACTION";
    final String START_BROWSER_ACTION = "START_BROWSER_ACTION";

    private long updateTimeStamp = 0;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // add Update Button click functionality
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_linear_horizontal);
        remoteViews.setOnClickPendingIntent(R.id.widgetUpdateButton, onClickUpdatePendingIntent(context, UPDATE_WIDGET_ACTION, appWidgetIds));

        remoteViews.setOnClickPendingIntent(R.id.widget_image, onClickUpdatePendingIntent(context, START_BROWSER_ACTION, appWidgetIds));

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        if (appWidgetIds.length > 0) {
            callUpdateWebShotService(
                    context,
                    appWidgetManager,
                    getSharedPreferencesUrl(context, appWidgetIds.length != 1 ? appWidgetIds[0] : AppWidgetManager.INVALID_APPWIDGET_ID),
                    appWidgetIds[0]);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private PendingIntent onClickUpdatePendingIntent(Context context, String actionName, int[] appWidgetIds) {
        Intent onClickIntent = new Intent(context, WidgetProvider.class);
        onClickIntent.setAction(actionName);
        // always use first app widget id. Multiple are not supported yet
        if (appWidgetIds.length > 0) {
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[0]);
        }
        return PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)) {
            // Check weather the app widget id is given at Intent or not
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                Toast.makeText(context, "Cannot get app widget id of action " + intent.getAction(), Toast.LENGTH_SHORT).show();
            } else {
                callUpdateWebShotService(
                        context,
                        AppWidgetManager.getInstance(context),
                        getSharedPreferencesUrl(context, appWidgetId),
                        appWidgetId
                );
            }
        } else if (intent.getAction().equals(UPDATE_WIDGET_ACTION)) {
            // Check weather the app widget id is given at Intent or not
            if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                Toast.makeText(context, "Cannot get app widget id of action " + intent.getAction(), Toast.LENGTH_SHORT).show();
            } else {
                callUpdateWebShotService(
                        context,
                        AppWidgetManager.getInstance(context),
                        getSharedPreferencesUrl(context, appWidgetId),
                        appWidgetId
                );
            }
        } else if (intent.getAction().equals(START_BROWSER_ACTION)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            browserIntent.setData(Uri.parse(getSharedPreferencesUrl(context, appWidgetId)));
            context.startActivity(browserIntent);
        }

        super.onReceive(context, intent);
    }

    private void callUpdateWebShotService(Context context, AppWidgetManager appWidgetManager, String webPageUrl, int appWidgetId) {

        long currentTime = System.currentTimeMillis();
        // allow Update only avery 3 seconds to avoid endless loop
        if (currentTime - 15000 > updateTimeStamp) {
            // We attach the current Widget IDs to the alarm Intent to ensure its
            // broadcast is correctly routed to onUpdate() when our AppWidgetProvider
            // next receives it.
            Intent iWidget = new Intent(context, AppWidgetProvider.class)
                    .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetId);
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, iWidget, 0);

            ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                    .setExact(AlarmManager.RTC, System.currentTimeMillis() + 30000, pi);

            Intent iService = new Intent(context, WebShotService.class);
            iService.putExtra(EXTRA_URL, webPageUrl);
            iService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            context.startService(iService);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        callUpdateWebShotService(
                context,
                appWidgetManager,
                getSharedPreferencesUrl(context, appWidgetId),
                appWidgetId
        );
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    public static String getSharedPreferencesUrl(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_WEB_URL_NAME, Context.MODE_PRIVATE);
        return prefs.getString(EXTRA_URL + "_" + appWidgetId, "");
    }

    public static void setSharedPreferencesUrl(Context context, String url, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_WEB_URL_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(WidgetProvider.EXTRA_URL + "_" + appWidgetId, url);
        edit.commit();
    }
}
