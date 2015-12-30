package de.michael_knape.webwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class WebWidgetConfigurationActivity extends ActionBarActivity {

    Button loadbtn;
    Button savewidgetbtn;
    EditText urlEdittext;
    WebView activityWebView;

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED);
        setContentView(R.layout.activity_web_widget_config);

        loadbtn = (Button) findViewById(R.id.loadButton);
        savewidgetbtn = (Button) findViewById(R.id.saveConfigWidgetButton);
        urlEdittext = (EditText) findViewById(R.id.urlEditText);
        activityWebView = (WebView) findViewById(R.id.activityWebView);

        SharedPreferences prefs = getSharedPreferences(WidgetProvider.EXTRA_URL, Context.MODE_PRIVATE);
        String url = prefs.getString(WidgetProvider.EXTRA_URL, "");
        urlEdittext.setText(url);


        activityWebView.getSettings().setJavaScriptEnabled(true);
        activityWebView.setBackgroundColor(Color.TRANSPARENT);
        activityWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        activityWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                activityWebView.setBackgroundColor(Color.TRANSPARENT);
                activityWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            }
        });

        loadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activityWebView.loadUrl(urlEdittext.getText().toString());
            }
        });

        savewidgetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences(WidgetProvider.EXTRA_URL, Context.MODE_PRIVATE);
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString(WidgetProvider.EXTRA_URL, urlEdittext.getText().toString());
                edit.commit();

                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);

                //Toast.makeText(context, "WebWidgetConfigurationActivity.onClick(): " + String.valueOf(mAppWidgetId), Toast.LENGTH_LONG).show();
                finish();
            }
        });

        // intent for Configuration Widget
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }


}
