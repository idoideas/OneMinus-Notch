package com.idoideas.sometimesettle;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    private AdView mAdView;
    private BillingClient mBillingClient;
    public static SharedPreferences prefs;
    public static String username;
    private Switch settleSwitch;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = this.getSharedPreferences(
                "com.idoideas.sometimessettle", Context.MODE_PRIVATE);
        if(prefs.getBoolean("not-signed", true)){
            startActivity(new Intent(this, WelcomeActivity.class));
        }
        if(prefs.getString("username", null)!=null){
            username = prefs.getString("username", null);
        }
        DPIGetter.resetDPI((WindowManager) getSystemService(WINDOW_SERVICE));
        MobileAds.initialize(this, getString(R.string.app_id));

        mBillingClient = BillingClient.newBuilder(this).setListener(new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {

            }
        }).build();
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                }
            }
            @Override
            public void onBillingServiceDisconnected() {

            }
        });

        Button donate = findViewById(R.id.donate);
        Button changeDPI = findViewById(R.id.changedpi);
        Button checkBalance = findViewById(R.id.gotosite);
        settleSwitch = findViewById(R.id.settle_switch);
        final TextView never_settle = findViewById(R.id.never_settle_text);
        final TextView sometimes_settle = findViewById(R.id.sometimes_settle_text);

        boolean isItRunning = isMyServiceRunning(OverlayService.class);
        settleSwitch.setChecked(!isItRunning);

        settleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    if(!startNotch()){
                        compoundButton.setChecked(true);
                    } else {
                        never_settle.setVisibility(View.GONE);
                        sometimes_settle.setVisibility(View.VISIBLE);
                    }
                } else {
                    stop();
                    if(username!=null){
                        NetworkUtils.addNumberToServer(getApplicationContext(), username);
                    }
                    never_settle.setVisibility(View.VISIBLE);
                    sometimes_settle.setVisibility(View.GONE);
                }
            }
        });

        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startInAppPurchasesDialog();
            }
        });

        changeDPI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DPIGetter.startDPIChangeDialog(MainActivity.this);
            }
        });

        checkBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.wewillneversettle.com"));
                startActivity(browserIntent);
            }
        });


        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device_id))
                .addTestDevice("B53868878BCBC97153D1E113A9B453A6")
                .build();
        mAdView.loadAd(adRequest);
    }

    public boolean startNotch(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (Settings.canDrawOverlays(getApplicationContext())){
                start();
                return true;
            } else {
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Just a second.");
                alertDialog.setMessage("Since you're using Android 6.0 and above, you'll need to allow the app to draw over other apps for the notch to work across every screen.");
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Go To Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        checkDrawOverlayPermission();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
                return false;
            }
        } else {
            start();
            return true;
        }
    }

    public void start(){
        if(!isMyServiceRunning(OverlayService.class)){
            startService(new Intent(this, OverlayService.class));
        }
    }

    public void stop(){
        if(isMyServiceRunning(OverlayService.class)){
            stopService(new Intent(this, OverlayService.class));
            OverlayService.removeView();
            showFullScreenAd();
        }
    }

    /** code to post/handler request for permission */
    public final static int REQUEST_CODE = 3000;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkDrawOverlayPermission() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e){
            Toast.makeText(getApplicationContext(), "Couldn't find overlay premission activity.\nPlease permit drawing over apps manually through the device settings.", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                // continue here - permission was granted
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    public void showFullScreenAd(){
        final InterstitialAd mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));

        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device_id))
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        });
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startInAppPurchasesDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Donation");
        builder.setMessage("How much would you like to donate?");

        builder.setPositiveButton("1 Dollar - A bubble gum", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startInAppPurchase("1_dollar_donation");
            }
        });
        builder.setNegativeButton("3 Dollars - A cup of coffee", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startInAppPurchase("3_dollar_donation");
            }
        });
        builder.setNeutralButton("5 Dollars - A sandwich", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startInAppPurchase("5_dollar_donation");
            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void startInAppPurchase(String sku){
        mBillingClient.consumeAsync("", new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(int responseCode, String purchaseToken) {

            }
        });
        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setSku(sku)
                .setType(BillingClient.SkuType.INAPP)
                .build();
        mBillingClient.launchBillingFlow(MainActivity.this, flowParams);
        Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@BillingClient.BillingResponse int responseCode, String outToken) {
                if (responseCode == BillingClient.BillingResponse.OK) {
                    Toast.makeText(getApplicationContext(), "Thank you so much for your donation!", Toast.LENGTH_LONG).show();
                }
            }};
        if (purchasesResult!=null){
            if(purchasesResult.getPurchasesList()!=null){
                if(purchasesResult.getPurchasesList().size()>0){
                    for (int i = 0; i<purchasesResult.getPurchasesList().size(); i++){
                        mBillingClient.consumeAsync(purchasesResult.getPurchasesList().get(i).getPurchaseToken(), listener);
                    }
                }
            }
        }
    }
}
