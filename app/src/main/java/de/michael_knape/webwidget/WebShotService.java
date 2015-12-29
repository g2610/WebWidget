package de.michael_knape.webwidget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Date;

public class WebShotService extends Service {
    private WebView webView;
    private WindowManager winManager;

    public int onStartCommand(Intent intent, int flags, int startId) {
        winManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        webView = new WebView(this);
        webView.setVerticalScrollBarEnabled(false);
        webView.setWebViewClient(client);

        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        PixelFormat.TRANSLUCENT);
        params.x = 0;
        params.y = 0;
        params.width = 0;
        params.height = 0;

        final FrameLayout frame = new FrameLayout(this);
        frame.addView(webView);
        winManager.addView(frame, params);

        // This is the important code :)
        //webView.setDrawingCacheEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress >= 100) {
                    final Point p = new Point();
                    winManager.getDefaultDisplay().getSize(p);

                    int x = p.x;
                    int y = p.y;

                    webView.measure(
                            MeasureSpec.makeMeasureSpec(x, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(y, MeasureSpec.EXACTLY));

                    webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());

                    webView.postDelayed(capture, 5000);

                    Toast.makeText(WebShotService.this, "WebWidget will Update! Progress is " + newProgress, Toast.LENGTH_SHORT).show();
                }
            }
        });
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://raspberrypi.wut5dwti0mvremiv.myfritz.net/tempNow.php");

        return START_STICKY;
    }

    private final WebViewClient client = new WebViewClient() {
        public void onPageFinished(WebView view, String url) {
        }
    };

    private final Runnable capture = new Runnable() {
        @Override
        public void run() {
            try {
                final Bitmap bmp = Bitmap.createBitmap(webView.getWidth(),
                        webView.getHeight(), Bitmap.Config.ARGB_8888);
                final Canvas c = new Canvas(bmp);
                webView.draw(c);

                updateWidgets(bmp);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(WebShotService.this, "WebWidget Update Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            stopSelf();
        }
    };

    private void updateWidgets(Bitmap bmp) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
        final int[] ids = widgetManager.getAppWidgetIds(
                new ComponentName(this, WidgetProvider.class));

        if (ids.length < 1) {
            return;
        }

        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_layout_linear_horizontal);
        views.setImageViewBitmap(R.id.widget_image, bmp);
        views.setTextViewText(R.id.lastUpdateTime, DateFormat.getInstance().format(new Date(System.currentTimeMillis())));
        widgetManager.updateAppWidget(ids, views);

        Toast.makeText(this, "WebWidget Updated", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
