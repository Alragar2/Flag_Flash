package alragar2.isi3.uv.flagflash

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.random.Random

object InterstitialAdManager {
    private var mInterstitialAd: InterstitialAd? = null
    private const val TAG = "InterstitialAdManager"
    private const val AD_UNIT_ID = "ca-app-pub-6281701644214410/1608017752" // Test ID

    fun loadAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(activity, AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun showAdWithProbability(activity: Activity, probability: Float = 0.4f) {
        if (Random.nextFloat() < probability) {
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(activity)
                mInterstitialAd = null // Reset after showing
                loadAd(activity) // Preload for next time
            } else {
                Log.d(TAG, "The interstitial ad wasn't ready yet.")
                loadAd(activity)
            }
        }
    }
}