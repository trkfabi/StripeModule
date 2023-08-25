package com.inzori.stripe;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.activity.ComponentActivity;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import java.util.HashMap;
import org.appcelerator.kroll.common.Log;
import android.content.res.Configuration;

public class StripePaymentSheetActivity extends ComponentActivity {
    private PaymentSheet paymentSheet;
    private HashMap<?, ?> params;

    private static final String LCAT = "StripePaymentSheetActivity";

    public StripePaymentSheetActivity()
    {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w(LCAT, "StripePaymentSheetActivity onCreate " );
        super.onCreate(savedInstanceState);

        // Configure activity layout (in addition to the style)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTitle("");

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        Intent intent = getIntent();
        //params = (HashMap<?, ?>) intent.getSerializableExtra("params");

        // I changed this because the HashMap crashes
        String merchantDisplayName = intent.getStringExtra("merchantDisplayName");
        String customerId = intent.getStringExtra("customerId");
        String customerEphemeralKeySecret = intent.getStringExtra("customerEphemeralKeySecret");
        String paymentIntentClientSecret = intent.getStringExtra("paymentIntentClientSecret");
        String country = intent.getStringExtra("country");
        Boolean isSandbox = intent.getBooleanExtra("isSandbox", true);

        PaymentSheet.CustomerConfiguration customerConfig = new PaymentSheet.CustomerConfiguration(
                customerId,
                customerEphemeralKeySecret
        );

        PaymentSheet.GooglePayConfiguration googlePayConfiguration =
                new PaymentSheet.GooglePayConfiguration(
                        isSandbox ? PaymentSheet.GooglePayConfiguration.Environment.Test: PaymentSheet.GooglePayConfiguration.Environment.Production,
                        country
                );

        PaymentSheet.Configuration.Builder configurationBuilder = new PaymentSheet.Configuration.Builder(merchantDisplayName)
                .customer(customerConfig)
                .allowsDelayedPaymentMethods(true)
                .googlePay(googlePayConfiguration);

        // if (appearance != null) {
        //     configurationBuilder.appearance(mappedAppearance(appearance));
        // }

        PaymentSheet.Configuration configuration = configurationBuilder.build();

        paymentSheet.presentWithPaymentIntent(
                paymentIntentClientSecret,
                configuration
        );
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        Intent intent = new Intent();

        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            System.out.println("Canceled");
            intent.putExtra("cancel", true);
            setResult(RESULT_CANCELED, intent);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failedResult = (PaymentSheetResult.Failed) paymentSheetResult;
            System.out.println("Error: " + failedResult.getError());
            intent.putExtra("success", false);
            intent.putExtra("error", failedResult.getError().getMessage());
            setResult(RESULT_CANCELED, intent);
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            System.out.println("Completed");
            intent.putExtra("success", true);
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    private PaymentSheet.Appearance mappedAppearance(HashMap<?, ?> params) {
        PaymentSheet.Appearance appearance = new PaymentSheet.Appearance();
        HashMap<?, ?> colors = (HashMap<?, ?>) params.get("colors");
        HashMap<?, ?> font = (HashMap<?, ?>) params.get("font");

        if (colors != null) {
            // TODO: Map colors
        }
        if (font != null) {
            // TODO: Map font
        }
        return appearance;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart()
    {
        Log.w(LCAT, "StripePaymentSheetActivity onStart" );
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
