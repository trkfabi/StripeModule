
package com.inzori.stripe;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stripe.android.identity.IdentityVerificationSheet;


public class StripeIdentityActivity extends AppCompatActivity
{
    private static final String LCAT = "StripeIdentityActivity";

    private IdentityVerificationSheet identityVerificationSheet;

    public StripeIdentityActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.w(LCAT,"StripeIdentityActivity onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle("");

        Intent intent = getIntent();
        String logoUrl = intent.getStringExtra("logoUrl");
        String verificationSessionId = intent.getStringExtra("verificationSessionId");
        String ephemeralKeySecret = intent.getStringExtra("ephemeralKeySecret");

        Log.w(LCAT,"StripeIdentityActivity onCreate logo: " + logoUrl);

        identityVerificationSheet = IdentityVerificationSheet.Companion.create(
                this,
                new IdentityVerificationSheet.Configuration(Uri.parse(logoUrl)),
                verificationFlowResult -> {
                    // handle verificationResult
                    KrollDict eventData = new KrollDict();

                    Intent eventIntent = new Intent();
                    eventIntent.setAction("stripe:verification_result");

                    if (verificationFlowResult instanceof IdentityVerificationSheet.VerificationFlowResult.Completed) {
                        eventIntent.putExtra("success", true);
                        eventIntent.putExtra("type", "status");
                        eventIntent.putExtra("status", "flowCompleted");
                        eventIntent.putExtra("message", "Verification Flow Completed!");
                        Log.w(LCAT, "Verification Flow Completed!");
                    } else if (verificationFlowResult instanceof IdentityVerificationSheet.VerificationFlowResult.Canceled) {
                        eventIntent.putExtra("success", true);
                        eventIntent.putExtra("type", "status");
                        eventIntent.putExtra("status", "flowCanceled");
                        eventIntent.putExtra("message", "Verification Flow Canceled!");
                        Log.w(LCAT, "Verification Flow Canceled!");
                    } else  if (verificationFlowResult instanceof IdentityVerificationSheet.VerificationFlowResult.Failed) {
                        eventIntent.putExtra("success", false);
                        eventIntent.putExtra("type", "status");
                        eventIntent.putExtra("status", "flowFailed");
                        eventIntent.putExtra("message", "Verification Flow Failed!");
                        Log.w(LCAT, "Verification Flow Failed!");
                    }
                    LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).sendBroadcast(eventIntent);
                    finish();
                }
        );

        Log.w(LCAT,"StripeIdentityActivity onCreate will present");
        identityVerificationSheet.present(
                verificationSessionId,
                ephemeralKeySecret
        );
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