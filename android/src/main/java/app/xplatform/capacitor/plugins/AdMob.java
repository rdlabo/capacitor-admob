package app.xplatform.capacitor.plugins;

import android.Manifest;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;


@NativePlugin(
    permissions = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET
    }
)
public class AdMob extends Plugin {

    private PluginCall call;

    private ViewGroup mViewGroup;


    private RelativeLayout mAdViewLayout;


    private AdView mAdView;


    private InterstitialAd mInterstitialAd;


    private RewardedAd mRewardedVideoAd;


    // Initialize AdMob with appId
    @PluginMethod()
    public void initialize(PluginCall call) {
        /* Sample AdMob App ID: ca-app-pub-3940256099942544~3347511713 */
        String appId = call.getString("appId", "ca-app-pub-3940256099942544~3347511713");

        try {
            MobileAds.initialize(this.getContext(), appId);

            mViewGroup = (ViewGroup) ((ViewGroup) getBridge().getActivity()
                    .findViewById(android.R.id.content)).getChildAt(0);

            call.success();

        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Show a banner Ad
    @PluginMethod()
    public void showBanner(PluginCall call) {
        /* Dedicated test ad unit ID for Android banners: ca-app-pub-3940256099942544/6300978111*/
        String adId = call.getString("adId", "ca-app-pub-3940256099942544/6300978111");
        String adSize = call.getString("adSize", "SMART_BANNER");
        String adPosition = call.getString("position", "BOTTOM_CENTER");

        try {

            if (mAdView == null) {
                mAdView = new AdView(getContext());
                mAdView.setAdUnitId(adId);


                Log.d(getLogTag(), "Ad ID: " + adId);


                switch (adSize) {
                    /*case "SMART_BANNER":
                        mAdView.setAdSize(AdSize.SMART_BANNER);
                        break;*/
                    case "BANNER":
                        mAdView.setAdSize(AdSize.BANNER);
                        break;
                    case "FLUID":
                        mAdView.setAdSize(AdSize.FLUID);
                        break;
                    case "FULL_BANNER":
                        mAdView.setAdSize(AdSize.FULL_BANNER);
                        break;
                    case "LARGE_BANNER":
                        mAdView.setAdSize(AdSize.LARGE_BANNER);
                        break;
                    case "LEADERBOARD":
                        mAdView.setAdSize(AdSize.LEADERBOARD);
                        break;
                    case "MEDIUM_RECTANGLE":
                        mAdView.setAdSize(AdSize.MEDIUM_RECTANGLE);
                        break;
                    default:
                        mAdView.setAdSize(AdSize.SMART_BANNER);
                        break;
                }
            }


            // Setup AdView Layout

            mAdViewLayout = new RelativeLayout(getContext());
            mAdViewLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
            mAdViewLayout.setVerticalGravity(Gravity.BOTTOM);

            final CoordinatorLayout.LayoutParams mAdViewLayoutParams = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );


            switch (adPosition) {
                case "TOP_CENTER":
                    mAdViewLayoutParams.gravity = Gravity.TOP;
                    break;
                case "CENTER":
                    mAdViewLayoutParams.gravity = Gravity.CENTER;
                    break;
                case "BOTTOM_CENTER":
                    mAdViewLayoutParams.gravity = Gravity.BOTTOM | Gravity.CENTER;
                    break;
                default:
                    mAdViewLayoutParams.gravity = Gravity.BOTTOM;
                    break;
            }


            // Set Bottom margin for TabBar
            boolean hasTabBar = call.getBoolean("hasTabBar", false);
            if (hasTabBar) {
                float density = getContext().getResources().getDisplayMetrics().density;
                float tabBarHeight = call.getInt("tabBarHeight", 56);
                int margin = (int) (tabBarHeight * density);
                mAdViewLayoutParams.setMargins(0, 0, 0, margin);
            }


            // Remove child from AdViewLayout
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if (mAdView.getParent() != null) {
                    ((ViewGroup) mAdView.getParent()).removeView(mAdView);
                }
                mAdViewLayout.setLayoutParams(mAdViewLayoutParams);
                // Add AdView into AdViewLayout
                mAdViewLayout.addView(mAdView);
                }
            });


            // Run AdMob In Main UI Thread
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                mAdView.loadAd(new AdRequest.Builder().build());

                mAdView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        notifyListeners("onAdLoaded", new JSObject().put("value", true));
                        super.onAdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(int i) {
                        notifyListeners("onAdFailedToLoad", new JSObject().put("errorCode", i));
                        super.onAdFailedToLoad(i);
                    }

                    @Override
                    public void onAdOpened() {
                        notifyListeners("onAdOpened", new JSObject().put("value", true));
                        super.onAdOpened();
                    }

                    @Override
                    public void onAdClosed() {
                        notifyListeners("onAdClosed", new JSObject().put("value", true));
                        super.onAdClosed();
                    }
                });

                // Add AdViewLayout top of the WebView
                mViewGroup.addView(mAdViewLayout);
                }
            });

            call.success(new JSObject().put("value", true));

        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Hide the banner, remove it from screen, but can show it later
    @PluginMethod()
    public void hideBanner(PluginCall call) {
        try {
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAdViewLayout != null) {
                        mAdViewLayout.setVisibility(View.GONE);
                        mAdView.pause();
                    }
                }
            });

            call.success(new JSObject().put("value", true));

        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Resume the banner, show it after hide
    @PluginMethod()
    public void resumeBanner(PluginCall call) {
        try {
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if (mAdViewLayout != null && mAdView != null) {
                    mAdViewLayout.setVisibility(View.VISIBLE);
                    mAdView.resume();
                    Log.d(getLogTag(), "Banner AD Resumed");
                }
                }
            });

            call.success(new JSObject().put("value", true));

        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Destroy the banner, remove it from screen.
    @PluginMethod()
    public void removeBanner(PluginCall call) {
        try {
            if (mAdView != null) {
                getBridge().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAdView != null) {
                            mViewGroup.removeView(mAdViewLayout);
                            mAdViewLayout.removeView(mAdView);
                            mAdView.destroy();
                            mAdView = null;
                            Log.d(getLogTag(), "Banner AD Removed");
                        }
                    }
                });
            }

            call.success(new JSObject().put("value", true));

        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Prepare interstitial Ad
    @PluginMethod()
    public void prepareInterstitial(final PluginCall call) {
        this.call = call;
        /* dedicated test ad unit ID for Android interstitials:
            ca-app-pub-3940256099942544/1033173712
        */
        String adId = call.getString("adId", "ca-app-pub-3940256099942544/1033173712");


        try {

            mInterstitialAd = new InterstitialAd(getContext());
            mInterstitialAd.setAdUnitId(adId);


            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());

                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        notifyListeners("onInterstitialAdLoaded", new JSObject().put("value", true));
                        call.success(new JSObject().put("value", true));
                        super.onAdLoaded();

                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Code to be executed when an ad request fails.
                        notifyListeners("onInterstitialAdFailedToLoad", new JSObject().put("errorCode", errorCode));
                        super.onAdFailedToLoad(errorCode);
                    }

                    @Override
                    public void onAdOpened() {
                        // Code to be executed when the ad is displayed.
                        notifyListeners("onInterstitialAdOpened", new JSObject().put("value", true));
                        super.onAdOpened();
                    }

                    @Override
                    public void onAdLeftApplication() {
                        // Code to be executed when the user has left the app.
                        notifyListeners("onInterstitialAdLeftApplication", new JSObject().put("value", true));
                        super.onAdLeftApplication();
                    }

                    @Override
                    public void onAdClosed() {
                        // Code to be executed when when the interstitial ad is closed.
                        notifyListeners("onInterstitialAdClosed", new JSObject().put("value", true));
                        super.onAdClosed();
                    }
                });

                }
            });

        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Show interstitial Ad
    @PluginMethod()
    public void showInterstitial(final PluginCall call) {
        try {
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                    getBridge().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mInterstitialAd.show();
                        }
                    });
                    call.success(new JSObject().put("value", true));
                } else {
                    call.error("The interstitial wasn't loaded yet.");
                }
                }
            });
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Prepare a RewardVideoAd
    @PluginMethod()
    public void prepareRewardVideoAd(final PluginCall call) {
        this.call = call;
        /* dedicated test ad unit ID for Android rewarded video:
            ca-app-pub-3940256099942544/5224354917
        */
        final String adId = call.getString("adId", "ca-app-pub-3940256099942544/5224354917");

        try {
            //mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(getContext());
            mRewardedVideoAd = new RewardedAd(getContext(), adId);

            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                //mRewardedVideoAd.loadAd(adId, new AdRequest.Builder().build());

                mRewardedVideoAd.loadAd(new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                    @Override
                    public void onRewardedAdLoaded() {
                        // Ad successfully loaded.
                        call.success(new JSObject().put("value", true));
                        notifyListeners("onRewardedVideoAdLoaded", new JSObject().put("value", true));
                    }

                    @Override
                    public void onRewardedAdFailedToLoad(int errorCode) {
                        // Ad failed to load.
                        notifyListeners("onRewardedVideoAdFailedToLoad", new JSObject().put("value", true));
                    }
                });
                }
            });


        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }

    }

    // Show a RewardVideoAd
    @PluginMethod()
    public void showRewardVideoAd(final PluginCall call) {
        try {
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                if (mRewardedVideoAd != null && mRewardedVideoAd.isLoaded()) {
                    getBridge().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        // mRewardedVideoAd.show();
                        mRewardedVideoAd.show(getBridge().getActivity(), new RewardedAdCallback() {
                            @Override
                            public void onRewardedAdOpened() {
                                // Ad opened.
                                notifyListeners("onRewardedVideoAdOpened", new JSObject().put("value", true));
                            }

                            @Override
                            public void onRewardedAdClosed() {
                                // Ad closed.
                                notifyListeners("onRewardedVideoAdClosed", new JSObject().put("value", true));
                            }

                            @Override
                            public void onUserEarnedReward(@NonNull RewardItem reward) {
                                // User earned reward.
                                notifyListeners("onRewarded", new JSObject().put("value", reward));
                            }

                            @Override
                            public void onRewardedAdFailedToShow(int errorCode) {
                                // Ad failed to display.
                                notifyListeners("onRewardedVideoAdFailedToShow", new JSObject().put("value", errorCode));
                            }
                        });
                        }
                    });
                    call.success(new JSObject().put("value", true));
                } else {
                    call.error("The RewardedVideoAd wasn't loaded yet.");
                }
                }
            });

        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }
    }


    // Pause a RewardVideoAd
    @Deprecated
    @PluginMethod()
    public void pauseRewardedVideo(PluginCall call) {
        /*try {
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd.pause(getContext());
                }
            });
            call.success(new JSObject().put("value", true));
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }*/
        call.error("This method no longer support by AdMob SDK");
    }


    // Resume a RewardVideoAd
    @Deprecated
    @PluginMethod()
    public void resumeRewardedVideo(PluginCall call) {
        /*try {
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd.resume(getContext());
                }
            });
            call.success(new JSObject().put("value", true));
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }*/
        call.error("This method no longer support by AdMob SDK");
    }


    // Destroy a RewardVideoAd
    @Deprecated
    @PluginMethod()
    public void stopRewardedVideo(PluginCall call) {
        /*try {
            getBridge().getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mRewardedVideoAd.destroy(getContext());
                }
            });
            call.success(new JSObject().put("value", true));
        } catch (Exception ex) {
            call.error(ex.getLocalizedMessage(), ex);
        }*/
        call.error("This method no longer support by AdMob SDK");
    }
}
