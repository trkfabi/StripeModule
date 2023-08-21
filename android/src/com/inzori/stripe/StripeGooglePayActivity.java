
package com.inzori.stripe;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.view.TiCompositeLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stripe.android.PaymentConfiguration;
import com.stripe.android.googlepaylauncher.GooglePayEnvironment;
import com.stripe.android.googlepaylauncher.GooglePayLauncher;

public class StripeGooglePayActivity extends AppCompatActivity
{
    private static final String LCAT = "StripeGooglePayActivity";

    protected TiCompositeLayout layout = null;
    
    public StripeGooglePayActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        layout = new TiCompositeLayout(this);
        setContentView(layout);

        Log.w(LCAT, "GooglePayActivitiy onCreate " );

        Intent intent = getIntent();
        Integer amount = intent.getIntExtra("amount", 0);
        String currency = intent.getStringExtra("currency");
        String country = intent.getStringExtra("country");
        String companyName = intent.getStringExtra("companyName");
        String clientSecret = intent.getStringExtra("clientSecret");
        String pk = intent.getStringExtra("pk");
        Boolean isSandbox = intent.getBooleanExtra("isSandbox", true);

        Log.w(LCAT, "GooglePayActivitiy onCreate pk: "+pk );

        PaymentConfiguration.init(this, pk);

        Log.w(LCAT, "GooglePayActivitiy onCreate googlePayLauncher" );

        final GooglePayLauncher googlePayLauncher = new GooglePayLauncher(
                this,
                new GooglePayLauncher.Config(
                        isSandbox? GooglePayEnvironment.Test: GooglePayEnvironment.Production,
                        country,
                        companyName
                ),
                this::onGooglePayReady,
                this::onGooglePayResult
        );

        Log.w(LCAT, "GooglePayActivitiy onCreate googlePayLauncher present" );
        googlePayLauncher.presentForPaymentIntent(clientSecret);

        Log.w(LCAT, "GooglePayActivitiy onCreate end" );

    }
    private void onGooglePayReady(Boolean isReady) {
        Log.w(LCAT, "GooglePayActivitiy onGooglePayReady isReady: " + isReady );
        // implemented below
        Intent eventIntent = new Intent();
        eventIntent.setAction("stripe:onGooglePay");
        eventIntent.putExtra("event", "onReady");
        eventIntent.putExtra("cancelled", false);
        eventIntent.putExtra("message", "On Google Pay ready");
        eventIntent.putExtra("success", isReady);
        LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).sendBroadcast(eventIntent);
        
    }

    private void onGooglePayResult(GooglePayLauncher.Result result) {
        Log.w(LCAT, "GooglePayActivitiy onGooglePayResult result: " + result );

        Intent eventIntent = new Intent();
        eventIntent.setAction("stripe:onGooglePay");

        if (result instanceof GooglePayLauncher.Result.Completed) {
            // Payment succeeded, show a receipt view
            eventIntent.putExtra("success", true);
            eventIntent.putExtra("event", "didCompleteWith");
            eventIntent.putExtra("cancelled", false);
            eventIntent.putExtra("message", "Payment succeded");
        } else if (result instanceof GooglePayLauncher.Result.Canceled) {
            // User canceled the operation
            eventIntent.putExtra("success", true);
            eventIntent.putExtra("event", "didCompleteWith");
            eventIntent.putExtra("cancelled", true);
            eventIntent.putExtra("message", "User cancelled payment");
        } else if (result instanceof GooglePayLauncher.Result.Failed) {
            // Operation failed; inspect `result.getError()` for more details
            eventIntent.putExtra("success", false);
            eventIntent.putExtra("event", "didCompleteWith");
            eventIntent.putExtra("cancelled", false);
            eventIntent.putExtra("message", ((GooglePayLauncher.Result.Failed) result).getError());
        }
        LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).sendBroadcast(eventIntent);
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