
package com.inzori.stripe;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import android.view.Window;
import android.view.WindowManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.googlepaylauncher.GooglePayEnvironment;
import com.stripe.android.googlepaylauncher.GooglePayLauncher;

public class StripeGooglePayActivity extends ComponentActivity
{
    private static final String LCAT = "StripeGooglePayActivity";
    private GooglePayLauncher googlePayLauncher;
    private String clientSecret;

    public StripeGooglePayActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Log.w(LCAT, "GooglePayActivity onCreate " );
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle("");

        Intent intent = getIntent();
        Integer amount = intent.getIntExtra("amount", 0);
        String currency = intent.getStringExtra("currency");
        String country = intent.getStringExtra("country");
        String companyName = intent.getStringExtra("companyName");
        clientSecret = intent.getStringExtra("clientSecret");
        String pk = intent.getStringExtra("pk");
        Boolean isSandbox = intent.getBooleanExtra("isSandbox", true);

        PaymentConfiguration.init(this, pk);

        googlePayLauncher = new GooglePayLauncher(
                this,
                new GooglePayLauncher.Config(
                        isSandbox? GooglePayEnvironment.Test: GooglePayEnvironment.Production,
                        country,
                        companyName
                ),
                this::onGooglePayReady,
                this::onGooglePayResult
        );

    }
    private void onGooglePayReady(Boolean isReady) {
        Log.w(LCAT, "GooglePayActivity onGooglePayReady isReady: " + isReady );
        // fire event
//        Intent eventIntent = new Intent();
//        eventIntent.setAction("stripe:onGooglePay");
//        eventIntent.putExtra("event", "onReady");
//        eventIntent.putExtra("cancelled", false);
//        eventIntent.putExtra("message", "On Google Pay ready");
//        eventIntent.putExtra("success", isReady);
//        LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).sendBroadcast(eventIntent);
        if( isReady) {
            googlePayLauncher.presentForPaymentIntent(clientSecret);
        } else {
            Intent eventIntent = new Intent();
            eventIntent.putExtra("success", false);
            eventIntent.putExtra("cancel", false);
            eventIntent.putExtra("error", "GPay is not available");
            setResult(RESULT_CANCELED, eventIntent);
        }
    }

    private void onGooglePayResult(GooglePayLauncher.Result result) {
        Log.w(LCAT, "GooglePayActivity onGooglePayResult result: " + result );

        Intent eventIntent = new Intent();
        //eventIntent.setAction("stripe:onGooglePay");

        if (result instanceof GooglePayLauncher.Result.Completed) {
            // Payment succeeded, show a receipt view
            eventIntent.putExtra("success", true);
            //eventIntent.putExtra("event", "didCompleteWith");
            eventIntent.putExtra("cancel", false);
            //eventIntent.putExtra("message", "Payment succeeded");
            eventIntent.putExtra("error", "");
            setResult(RESULT_OK, eventIntent);
        } else if (result instanceof GooglePayLauncher.Result.Canceled) {
            // User canceled the operation
            eventIntent.putExtra("success", false);
            //eventIntent.putExtra("event", "didCompleteWith");
            eventIntent.putExtra("cancel", true);
            //eventIntent.putExtra("message", "User cancelled payment");
            eventIntent.putExtra("error", "");
            setResult(RESULT_CANCELED, eventIntent);
        } else if (result instanceof GooglePayLauncher.Result.Failed) {
            // Operation failed; inspect `result.getError()` for more details
            eventIntent.putExtra("success", false);
            //eventIntent.putExtra("event", "didCompleteWith");
            eventIntent.putExtra("cancel", false);
            //eventIntent.putExtra("message", ((GooglePayLauncher.Result.Failed) result).getError());
            eventIntent.putExtra("error", ((GooglePayLauncher.Result.Failed) result).getError());
            setResult(RESULT_CANCELED, eventIntent);
        }
        //LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).sendBroadcast(eventIntent);
        finish();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}