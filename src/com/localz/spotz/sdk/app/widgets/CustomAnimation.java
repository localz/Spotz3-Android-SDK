package com.localz.spotz.sdk.app.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class CustomAnimation {

    @TargetApi(value = Build.VERSION_CODES.HONEYCOMB)
    public static AnimatorSet startWaveAnimation(final View view) {
        final AnimatorSet set;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.5f);
            scaleYAnim.setDuration(2000);
            scaleYAnim.setInterpolator(new DecelerateInterpolator());

            ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.5f);
            scaleXAnim.setDuration(2000);
            scaleXAnim.setInterpolator(new DecelerateInterpolator());

            ObjectAnimator alphaANim = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            alphaANim.setDuration(1600);
            alphaANim.setStartDelay(400);

            set = new AnimatorSet();
            set.playTogether(scaleYAnim, scaleXAnim, alphaANim);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setAlpha(1);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            set.start();
                        }
                    }, 1000);
                }
            });
            set.start();
            return set;
        } else {
            return null;
        }
    }

    @TargetApi(value = Build.VERSION_CODES.HONEYCOMB)
    public static void stopWaveAnimation(final AnimatorSet set) {
        if (set != null) {
            set.removeAllListeners();
            set.end();
//            set.cancel();
        }
    }
}
