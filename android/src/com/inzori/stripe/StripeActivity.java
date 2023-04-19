/**
 * TiDev Titanium Mobile
 * Copyright TiDev, Inc. 04/07/2022-Present. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package com.inzori.stripe;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiLifecycle;
import org.appcelerator.titanium.view.TiCompositeLayout;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Messenger;

import androidx.appcompat.app.AppCompatActivity;

import com.stripe.android.identity.*;
public class StripeActivity extends AppCompatActivity
{
    private static final String LCAT = "StripeActivity";

    protected TiCompositeLayout layout = null;
    private TiLifecycle.OnLifecycleEvent lifecycleListener = null;
    private IdentityVerificationSheet identityVerificationSheet;

    public StripeActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.w(LCAT,"stripeActivity onCreate override");

        layout = new TiCompositeLayout(this);
        setContentView(layout);

        Log.w(LCAT, "lets create identityVerificationSheet");
        try {
            identityVerificationSheet = IdentityVerificationSheet.Companion.create(
                    this,
                    new IdentityVerificationSheet.Configuration(Uri.parse("https://files.stripe.com/files/MDB8YWNjdF8xMDQ5RVE0NmdmQ0VhM1F2fGZfbGl2ZV9MbnRzRjA2Nk1MVnh0TjB0dkhwYnFSY2M00BC2xHgEr")),
                    verificationFlowResult -> {
                        // handle verificationResult
                        KrollDict eventData = new KrollDict();
                        Log.w(LCAT, verificationFlowResult.toString());
//				if (verificationFlowResult instanceof Completed) {
//					// The user has completed uploading their documents.
//					// Let them know that the verification is processing.
//
//					eventData.put("success",true);
//					eventData.put("type","status");
//					eventData.put("status","flowCompleted");
//					eventData.put("message","Verification Flow Completed!");
//					onComplete.callAsync(getKrollObject(), eventData);
//					Log.d(LCAT, "Verification Flow Completed!");
//				} else if (verificationFlowResult instanceof Canceled) {
//					// The user did not complete uploading their documents.
//					// You should allow them to try again.
//					eventData.put("success",true);
//					eventData.put("type","status");
//					eventData.put("status","flowCanceled");
//					eventData.put("message","Verification Flow Canceled!");
//					onComplete.callAsync(getKrollObject(), eventData);
//					Log.d(LCAT, "Verification Flow Canceled!");
//				} else  if (verificationFlowResult instanceof Failed) {
//					// If the flow fails, you should display the localized error
//					// message to your user using throwable.getLocalizedMessage()
//					eventData.put("success",false);
//					eventData.put("type","status");
//					eventData.put("status","flowFailed");
//					eventData.put("message","Verification Flow Failed!");
//					onComplete.callAsync(getKrollObject(), eventData);
//					Log.d(LCAT, "Verification Flow Failed!");
//				}
                    }
            );
        } catch (Exception e) {
            Log.e(LCAT,"exc1 : " +  e.getLocalizedMessage());
        }

    }

    public IdentityVerificationSheet getSheet() {
        return identityVerificationSheet;
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
        Log.w(LCAT, "StripeActivity onStart");
        if (lifecycleListener != null) {
            lifecycleListener.onStart(this);
        }
        Intent intent = getIntent();
        String verificationSessionId = intent.getStringExtra("verificationSessionId");
        String ephemeralKeySecret = intent.getStringExtra("ephemeralKeySecret");
        Log.w(LCAT, "ephemeralKeySecret: " + ephemeralKeySecret + " verificationSessionId: "+verificationSessionId);
        try{
            identityVerificationSheet.present(
                    verificationSessionId,
                    ephemeralKeySecret
            );
        } catch (Exception e) {
            Log.e(LCAT, "exc launcher : " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.w(LCAT, "StripeActivity onResume");
        TiApplication.getInstance().setCurrentActivity(this, this);
        if (lifecycleListener != null) {
            lifecycleListener.onResume(this);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.w(LCAT, "StripeActivity onPause");
        TiApplication.getInstance().setCurrentActivity(this, null);
        if (lifecycleListener != null) {
            lifecycleListener.onPause(this);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.w(LCAT, "StripeActivity onDestroy");
        if (lifecycleListener != null) {
            lifecycleListener.onDestroy(this);
        }
    }


    public void setOnLifecycleEventListener(TiLifecycle.OnLifecycleEvent listener)
    {
        lifecycleListener = listener;
    }
}