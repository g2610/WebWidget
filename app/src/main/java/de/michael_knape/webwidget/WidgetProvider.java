package de.michael_knape.webwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/*
 * Created by Michael Knape on 29.12.2015.
 */
public class WidgetProvider extends AppWidgetProvider{

    final String UPDATE_WIDGAT_ACTION = "UPDATE WIDGET ACTION";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        callUpdateWebShotService(context, appWidgetManager);

        // add Update Button click functionality
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout_linear_horizontal);
        remoteViews.setOnClickPendingIntent(R.id.widgetUpdateButton, onClickUpdatePendingIntent(context, UPDATE_WIDGAT_ACTION));

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private PendingIntent onClickUpdatePendingIntent(Context context, String actionName) {
        Intent onClickIntent = new Intent(context, WidgetProvider.class);
        onClickIntent.setAction(actionName);

        return PendingIntent.getBroadcast(context, 0, onClickIntent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(UPDATE_WIDGAT_ACTION)) {
            callUpdateWebShotService(context, AppWidgetManager.getInstance(context));
        }

        super.onReceive(context, intent);
    }

    private void callUpdateWebShotService(Context context, AppWidgetManager appWidgetManager) {
        // We can't trust the appWidgetIds param here, as we're using
        // ACTION_APPWIDGET_UPDATE to trigger our own updates, and
        // Widgets might've been removed/added since the alarm was last set.
        final int[] currentIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, WidgetProvider.class));

        if (currentIds.length < 1) {
            return;
        }

        // We attach the current Widget IDs to the alarm Intent to ensure its
        // broadcast is correctly routed to onUpdate() when our AppWidgetProvider
        // next receives it.
        Intent iWidget = new Intent(context, AppWidgetProvider.class)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, currentIds);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, iWidget, 0);

        ((AlarmManager) context.getSystemService(Context.ALARM_SERVICE))
                .setExact(AlarmManager.RTC, System.currentTimeMillis() + 30000, pi);

        Intent iService = new Intent(context, WebShotService.class);
        context.startService(iService);
    }


}
