/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * TiDev Titanium Mobile
 * Copyright TiDev, Inc. 04/07/2022-Present
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 *
 */
package com.inzori.stripe;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.util.TiActivityResultHandler;
import org.appcelerator.titanium.util.TiActivitySupport;
import org.appcelerator.kroll.common.Log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.stripe.android.identity.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.activity.ComponentActivity;
import android.app.Activity;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.stripe.android.PaymentConfiguration;

@Kroll.module(name="Stripe", id="com.inzori.stripe")
public class StripeModule extends KrollModule implements TiActivityResultHandler
{

	// Standard Debugging variables
	private static final String LCAT = "StripeModule";
	private String ephemeralKeySecret = "";
	private String verificationSessionId = "";
	private static IdentityVerificationSheet identityVerificationSheet;
	StripeModule self;
	private KrollFunction callback;

	// You can define constants with @Kroll.constant, for example:
	// @Kroll.constant public static final String EXTERNAL_NAME = value;
	public StripeModule()
	{
		super();
		self = this;
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.w(LCAT, "inside onAppCreate 1");
		// put module init code that needs to run when the application is created
	}

	// Methods for ID verification
	@Kroll.method
	public void startVerification(KrollDict options)
	{
		// listen to broadcast event from StripeActivity
		LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).registerReceiver(myReceiver, new IntentFilter("stripe:verification_result"));

		verificationSessionId = options.containsKey("verificationSessionId") ? (String) options.get("verificationSessionId") : "";
		ephemeralKeySecret = options.containsKey("ephemeralKeySecret") ? (String) options.get("ephemeralKeySecret") : "";
		String logoUrl = options.containsKey("logoUrl") ? (String) options.get("logoUrl") : "";
		String logoUrlAsset = logoUrl;

		//KrollFunction onComplete = (KrollFunction) options.get("onComplete");

		Intent intent = new Intent(TiApplication.getInstance().getApplicationContext(), StripeActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("verificationSessionId", verificationSessionId);
		intent.putExtra("ephemeralKeySecret", ephemeralKeySecret);
		intent.putExtra("logoUrl", logoUrlAsset);

		TiApplication.getInstance().getApplicationContext().startActivity(intent);
	}

	BroadcastReceiver myReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Handle the event here
			Boolean success = intent.getBooleanExtra("success", true);
			String type = intent.getStringExtra("type");
			String status = intent.getStringExtra("status");
			String message = intent.getStringExtra("message");
			Log.w(LCAT, "Received event with success: " + success);

			// fire event back to Ti app
			KrollDict props = new KrollDict();
			props.put("success", success);
			props.put("type", type);
			props.put("status", status);
			props.put("message", message);
			self.fireEvent("stripe:verification_result", props);

			LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).unregisterReceiver(myReceiver);
		}
	};

	// Methods for PAYMENTS
	@Kroll.method
	public void initializePayments(KrollDict params) {
		String publishableKey = params.getString("publishableKey");
		PaymentConfiguration.init(TiApplication.getInstance().getApplicationContext(), publishableKey);

		KrollDict event = new KrollDict();

		event.put("success", true);

		KrollFunction onComplete = (KrollFunction) params.get("onComplete");
		onComplete.callAsync(getKrollObject(), event);
	}
	@Kroll.method
	public void showPaymentSheet(KrollDict params) {
		Log.w(LCAT, "showPaymentSheet()");

		callback = (KrollFunction) params.get("onComplete");
		String merchantDisplayName = params.getString("merchantDisplayName");
		String customerId = params.getString("customerId");
		String customerEphemeralKeySecret = params.getString("customerEphemeralKeySecret");
		String paymentIntentClientSecret = params.getString("paymentIntentClientSecret");
		String country = params.getString("country");
		Boolean isSandbox = params.containsKey("isSandbox") ? (Boolean) params.get("isSandbox") : true;

		if (callback == null || merchantDisplayName == null || customerId == null ||
				customerEphemeralKeySecret == null || paymentIntentClientSecret == null) {
			Log.e(
					LCAT,
					"Missing required parameters: callback, customerId, customerEphemeralKeySecret or paymentIntentClientSecret"
			);
			return;
		}

		params.remove("callback");
		Intent intent = new Intent(TiApplication.getInstance().getApplicationContext(), TiStripeHostActivity.class);
		//intent.putExtra("params", params);
		intent.putExtra("merchantDisplayName", merchantDisplayName);
		intent.putExtra("customerId", customerId);
		intent.putExtra("customerEphemeralKeySecret", customerEphemeralKeySecret);
		intent.putExtra("paymentIntentClientSecret", paymentIntentClientSecret);
		intent.putExtra("isSandbox", isSandbox);
		intent.putExtra("country", country);

		TiActivitySupport support = (TiActivitySupport) TiApplication.getInstance().getCurrentActivity();
		support.launchActivityForResult(intent, support.getUniqueResultCode(), this);
	}

	@Override
	public void onResult(Activity activity, int requestCode, int resultCode, Intent intent) {
		boolean success = intent.getBooleanExtra("success", false);
		boolean cancel = intent.getBooleanExtra("cancel", false);
		String error = intent.getStringExtra("error");

		KrollDict event = new KrollDict();

		event.put("success", success);
		event.put("cancel", cancel);
		event.put("error", error);

		callback.callAsync(getKrollObject(), event);
	}

	@Override
	public void onError(Activity activity, int requestCode, Exception e) {
		KrollDict event = new KrollDict();

		event.put("success", false);
		event.put("cancel", false);
		event.put("error", e.getMessage());

		callback.callAsync(getKrollObject(), event);
	}

	// My code starts here
	// specific for gPay ?
	@Kroll.method
	public void showWalletSheet(KrollDict options)
	{
		// listen to broadcast event from StripeGooglePayActivity
		LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).registerReceiver(myReceiverGooglePay, new IntentFilter("stripe:onGooglePay"));

		Log.w(LCAT, "showWalletSheet()" );
		Integer amount = options.containsKey("amount") ? (Integer) options.get("amount") : 180;
		String currency = options.containsKey("currency") ? (String) options.get("currency") : "usd";
		String country = options.containsKey("country") ? (String) options.get("country") : "US";
		String companyName = options.containsKey("companyName") ? (String) options.get("companyName") : "Fluid Market";
		String pk = options.containsKey("pk") ? (String) options.get("pk") : "";
		String clientSecret = options.containsKey("clientSecret") ? (String) options.get("clientSecret") : "";
		Boolean isSandbox = options.containsKey("isSandbox") ? (Boolean) options.get("isSandbox") : true;

		callback = (KrollFunction) options.get("onComplete");

		Intent intent = new Intent(TiApplication.getInstance().getApplicationContext(), StripeGooglePayActivity.class);
		intent.putExtra("amount", amount);
		intent.putExtra("currency", currency);
		intent.putExtra("country", country);
		intent.putExtra("companyName", companyName);
		intent.putExtra("pk", pk);
		intent.putExtra("clientSecret", clientSecret);
		intent.putExtra("isSandbox", isSandbox);

		//TiApplication.getInstance().getApplicationContext().startActivity(intent);
		TiActivitySupport support = (TiActivitySupport) TiApplication.getInstance().getCurrentActivity();
		support.launchActivityForResult(intent, support.getUniqueResultCode(), this);
	}	


	BroadcastReceiver myReceiverGooglePay = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Handle the event here
			Boolean success = intent.getBooleanExtra("success", false);
			String event = intent.getStringExtra("event");
			Boolean cancelled = intent.getBooleanExtra("cancelled", false);
			String message = intent.getStringExtra("message");
			Log.w(LCAT, "Received event with success: " + success);

			// fire event back to Ti app
			KrollDict props = new KrollDict();
			props.put("success", success);
			props.put("cancelled", cancelled);
			props.put("event", event);
			props.put("message", message);
			self.fireEvent("fluid:payment_status", props);

			LocalBroadcastManager.getInstance(TiApplication.getInstance().getApplicationContext()).unregisterReceiver(myReceiverGooglePay);
		}
	};	
}

