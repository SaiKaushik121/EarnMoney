package com.example.moneymaker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

public class MainActivity extends AppCompatActivity {

    private RewardedAd rewardedAd;
    private Button rewardedAdButton;
    private Button checkoutButton;
    private TextView pointsTextView;
    private int points = 100000000;
    private AdView bannerAdView;
    private static final int YOUR_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        points = sharedPreferences.getInt("points", 0);


        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this, initializationStatus -> {
        });

        // Load the rewarded ad
        loadRewardedAd();

        // Find the button and set the click listener
        rewardedAdButton = findViewById(R.id.rewardedAdButton);
        rewardedAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardedAd();
            }
        });


        // Find the checkout button and set the click listener
        checkoutButton = findViewById(R.id.checkoutButton);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (points >= 10000) {
                    showPayoutForm();
                } else {
                    Toast.makeText(MainActivity.this, "Insufficient points for checkout.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Find the points TextView
        pointsTextView = findViewById(R.id.pointsTextView);
        updatePointsTextView();

        // Find the banner ad view and load the ad
        bannerAdView = findViewById(R.id.bannerAdView);
        loadBannerAd();
    }

    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

/*Originnal - ca-app-pub-5909072142022222/9307698479
        Test - ca-app-pub-3940256099942544/5224354917*/

        RewardedAd.load(this, "ca-app-pub-5909072142022222/9307698479", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                rewardedAd = ad;
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        loadRewardedAd(); // Load the next rewarded ad
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                rewardedAd = null;
                Toast.makeText(MainActivity.this, "Failed to load rewarded ad.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showRewardedAd() {
        if (rewardedAd != null) {
            rewardedAd.show(this, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(RewardItem rewardItem) {
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    // Give points to the user based on rewardAmount and rewardType
                    points += 10; // Reward the user with 10 points
                    updatePointsTextView();
                    Toast.makeText(MainActivity.this, "You earned " + rewardAmount + " " + rewardType, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(MainActivity.this, "Rewarded ad is not ready yet. Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAdView.loadAd(adRequest);
    }

    private void updatePointsTextView()
    {
        pointsTextView.setText("Points: " + points);
        // Save the points value in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("points", points);
        editor.apply();

        // Check if the points reached the payout threshold
        if (points >= 10000) {
            checkoutButton.setEnabled(true); // Enable the checkout button
        } else {
            checkoutButton.setEnabled(false); // Disable the checkout button
        }
    }

    private void showPayoutForm() {
        // Open the payout form activity
        Intent intent = new Intent(MainActivity.this, PayoutFormActivity.class);
        intent.putExtra("points", points);
        startActivityForResult(intent, YOUR_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == YOUR_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int deductedPoints = data.getIntExtra("deductedPoints", 0);
            points -= deductedPoints;
            updatePointsTextView();
        }
    }

    @Override
    protected void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
        }
        super.onDestroy();
    }

}
