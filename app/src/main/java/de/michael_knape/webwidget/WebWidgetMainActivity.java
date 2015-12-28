package de.michael_knape.webwidget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

public class WebWidgetMainActivity extends ActionBarActivity {

    Button loadbtn;
    Button widgetbtn;
    EditText urlEdittext;
    WebView activityWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_widget_main);

        loadbtn = (Button) findViewById(R.id.loadButton);
        widgetbtn = (Button) findViewById(R.id.widgetSaveButton);
        urlEdittext = (EditText) findViewById(R.id.urlEditText);
        activityWebView = (WebView) findViewById(R.id.activityWebView);
        activityWebView.getSettings().setJavaScriptEnabled(true);

        activityWebView.setBackgroundColor(Color.TRANSPARENT);
        activityWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        activityWebView.setWebViewClient(new WebViewClient(){
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

        widgetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent onUpdateWebViewIntent = new Intent(WebWidgetMainActivity.this, WidgetProvider.class);
                onUpdateWebViewIntent.setAction(AppWidgetManager.EXTRA_CUSTOM_EXTRAS);
                onUpdateWebViewIntent.putExtra("webViewUrl", urlEdittext.getText().toString());
                sendBroadcast(onUpdateWebViewIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_web_widget_main, menu);
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
}
