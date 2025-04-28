package com.example.aplikasichefai;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;

public class SplashActivity extends AppCompatActivity {

    private View circleView;
    private TextView brandNameTextView;
    private ImageView logoImageView;
    private ConstraintLayout rootLayout;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        // Initialize views
        circleView = findViewById(R.id.circle_view);
        brandNameTextView = findViewById(R.id.brand_name_text);
        logoImageView = findViewById(R.id.logo_image);
        rootLayout = findViewById(R.id.root_layout);

        // Enable hardware acceleration for smoother animations
        rootLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        circleView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        brandNameTextView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        logoImageView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Set high quality rendering on views
        ViewCompat.setLayerType(circleView, ViewCompat.LAYER_TYPE_HARDWARE, null);

        // Initially hide the text and logo
        brandNameTextView.setAlpha(0f);
        logoImageView.setAlpha(0f);

        // Position the circle at the top of the screen with very small scale
        circleView.setScaleX(0.01f); // Even smaller initial size for more dramatic effect
        circleView.setScaleY(0.01f);

        // Move the circle to top of screen, keeping it centered horizontally
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) circleView.getLayoutParams();
        params.verticalBias = 0.05f; // Slightly below top for better visibility
        circleView.setLayoutParams(params);

        // Start animation sequence after a short delay
        handler.postDelayed(this::startAnimationSequence, 300); // Reduced delay for faster startup
    }

    private void startAnimationSequence() {
        // 1. Drop animation from top to center with gradual growth
        AnimatorSet dropAndGrowAnimation = createDropAndGrowAnimation();

        // 2. Enhanced bounce animation for the circle
        AnimatorSet enhancedBounceAnimation = createEnhancedBounceAnimation();

        // 3. Circle expansion animation
        AnimatorSet expansionAnimation = createExpansionAnimation();

        // 4. Text and logo fade-in animations
        AnimatorSet fadeInAnimation = createFadeInAnimation();

        // Chain animations with smoother transitions
        dropAndGrowAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Start next animation immediately without delay
                enhancedBounceAnimation.start();
            }
        });

        enhancedBounceAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Small pause before expansion for better visual effect
                handler.postDelayed(expansionAnimation::start, 50);
            }
        });

        expansionAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Change background to gradient after circle fills screen
                setGradientBackground();
                // Small delay for the background to settle
                handler.postDelayed(fadeInAnimation::start, 100);
            }
        });

        fadeInAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Animation completed, add a small delay before moving to main activity
                handler.postDelayed(() -> {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }, 600); // Reduced delay for faster app launch
            }
        });

        // Start the animation sequence
        dropAndGrowAnimation.start();
    }

    private AnimatorSet createDropAndGrowAnimation() {
        // Create an animation to move the circle from top to center
        ObjectAnimator moveVerticalBias = ObjectAnimator.ofFloat(circleView, "y",
                circleView.getY(), rootLayout.getHeight() / 2 - circleView.getHeight() / 2);

        // Create animations to grow the circle significantly as it falls
        ObjectAnimator growScaleX = ObjectAnimator.ofFloat(circleView, "scaleX", 0.01f, 0.45f);
        ObjectAnimator growScaleY = ObjectAnimator.ofFloat(circleView, "scaleY", 0.01f, 0.45f);

        // Set the duration - slightly faster for better responsiveness
        int dropDuration = 1800;
        moveVerticalBias.setDuration(dropDuration);
        growScaleX.setDuration(dropDuration);
        growScaleY.setDuration(dropDuration);

        // Use custom interpolator for more natural motion
        DecelerateInterpolator customDropInterpolator = new DecelerateInterpolator(2.5f);
        moveVerticalBias.setInterpolator(customDropInterpolator);

        // Different interpolator for growing effect
        DecelerateInterpolator growInterpolator = new DecelerateInterpolator(2.0f);
        growScaleX.setInterpolator(growInterpolator);
        growScaleY.setInterpolator(growInterpolator);

        // Combine the animations
        AnimatorSet dropAndGrowSet = new AnimatorSet();
        dropAndGrowSet.playTogether(moveVerticalBias, growScaleX, growScaleY);

        return dropAndGrowSet;
    }

    private AnimatorSet createEnhancedBounceAnimation() {
        // Smoother, more natural bounce sequence

        // First bounce bigger (0.45 to 1.15)
        ObjectAnimator scaleXBigger1 = ObjectAnimator.ofFloat(circleView, "scaleX", 0.45f, 1.15f);
        ObjectAnimator scaleYBigger1 = ObjectAnimator.ofFloat(circleView, "scaleY", 0.45f, 1.15f);

        // First bounce smaller (1.15 to 0.7)
        ObjectAnimator scaleXSmaller1 = ObjectAnimator.ofFloat(circleView, "scaleX", 1.15f, 0.7f);
        ObjectAnimator scaleYSmaller1 = ObjectAnimator.ofFloat(circleView, "scaleY", 1.15f, 0.7f);

        // Second bounce bigger (0.7 to 1.25)
        ObjectAnimator scaleXBigger2 = ObjectAnimator.ofFloat(circleView, "scaleX", 0.7f, 1.25f);
        ObjectAnimator scaleYBigger2 = ObjectAnimator.ofFloat(circleView, "scaleY", 0.7f, 1.25f);

        // Second bounce smaller (1.25 to 0.8)
        ObjectAnimator scaleXSmaller2 = ObjectAnimator.ofFloat(circleView, "scaleX", 1.25f, 0.8f);
        ObjectAnimator scaleYSmaller2 = ObjectAnimator.ofFloat(circleView, "scaleY", 1.25f, 0.8f);

        // Final bounce bigger (0.8 to 1.4)
        ObjectAnimator scaleXBigger3 = ObjectAnimator.ofFloat(circleView, "scaleX", 0.8f, 1.4f);
        ObjectAnimator scaleYBigger3 = ObjectAnimator.ofFloat(circleView, "scaleY", 0.8f, 1.4f);

        // Final settle (1.4 to 0.65)
        ObjectAnimator scaleXSettle = ObjectAnimator.ofFloat(circleView, "scaleX", 1.4f, 0.65f);
        ObjectAnimator scaleYSettle = ObjectAnimator.ofFloat(circleView, "scaleY", 1.4f, 0.65f);

        // Better timing for more fluid motion
        int expandDuration = 350;
        int contractDuration = 300;

        scaleXBigger1.setDuration(expandDuration);
        scaleYBigger1.setDuration(expandDuration);

        scaleXSmaller1.setDuration(contractDuration);
        scaleYSmaller1.setDuration(contractDuration);

        scaleXBigger2.setDuration(expandDuration);
        scaleYBigger2.setDuration(expandDuration);

        scaleXSmaller2.setDuration(contractDuration);
        scaleYSmaller2.setDuration(contractDuration);

        scaleXBigger3.setDuration(expandDuration);
        scaleYBigger3.setDuration(expandDuration);

        scaleXSettle.setDuration(contractDuration);
        scaleYSettle.setDuration(contractDuration);

        // Better interpolators
        AccelerateDecelerateInterpolator smoothInterpolator = new AccelerateDecelerateInterpolator();
        OvershootInterpolator overshootInterpolator = new OvershootInterpolator(2.5f);

        scaleXBigger1.setInterpolator(overshootInterpolator);
        scaleYBigger1.setInterpolator(overshootInterpolator);

        scaleXSmaller1.setInterpolator(smoothInterpolator);
        scaleYSmaller1.setInterpolator(smoothInterpolator);

        scaleXBigger2.setInterpolator(overshootInterpolator);
        scaleYBigger2.setInterpolator(overshootInterpolator);

        scaleXSmaller2.setInterpolator(smoothInterpolator);
        scaleYSmaller2.setInterpolator(smoothInterpolator);

        scaleXBigger3.setInterpolator(overshootInterpolator);
        scaleYBigger3.setInterpolator(overshootInterpolator);

        scaleXSettle.setInterpolator(smoothInterpolator);
        scaleYSettle.setInterpolator(smoothInterpolator);

        // Create animator sets for each bounce step
        AnimatorSet bounceBigger1Set = new AnimatorSet();
        bounceBigger1Set.playTogether(scaleXBigger1, scaleYBigger1);

        AnimatorSet bounceSmaller1Set = new AnimatorSet();
        bounceSmaller1Set.playTogether(scaleXSmaller1, scaleYSmaller1);

        AnimatorSet bounceBigger2Set = new AnimatorSet();
        bounceBigger2Set.playTogether(scaleXBigger2, scaleYBigger2);

        AnimatorSet bounceSmaller2Set = new AnimatorSet();
        bounceSmaller2Set.playTogether(scaleXSmaller2, scaleYSmaller2);

        AnimatorSet bounceBigger3Set = new AnimatorSet();
        bounceBigger3Set.playTogether(scaleXBigger3, scaleYBigger3);

        AnimatorSet settleSet = new AnimatorSet();
        settleSet.playTogether(scaleXSettle, scaleYSettle);

        // Play the sets in sequence
        AnimatorSet fullBounceSequence = new AnimatorSet();
        fullBounceSequence.playSequentially(
                bounceBigger1Set,
                bounceSmaller1Set,
                bounceBigger2Set,
                bounceSmaller2Set,
                bounceBigger3Set,
                settleSet
        );

        return fullBounceSequence;
    }

    private AnimatorSet createExpansionAnimation() {
        // Calculate how much to scale to cover the screen
        float scaleToFillScreen = 45f; // Increased for full coverage

        // Add subtle rotation during expansion for more dynamic feel
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(circleView, "rotation", 0f, 15f);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(circleView, "scaleX", 0.65f, scaleToFillScreen);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(circleView, "scaleY", 0.65f, scaleToFillScreen);

        // Better timing
        rotateAnimator.setDuration(1000);
        scaleXAnimator.setDuration(1000);
        scaleYAnimator.setDuration(1000);

        // Better interpolator
        DecelerateInterpolator interpolator = new DecelerateInterpolator(1.2f);
        rotateAnimator.setInterpolator(interpolator);
        scaleXAnimator.setInterpolator(interpolator);
        scaleYAnimator.setInterpolator(interpolator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(rotateAnimator, scaleXAnimator, scaleYAnimator);

        return animatorSet;
    }

    private AnimatorSet createFadeInAnimation() {
        // Add subtle translation for text and logo - they rise up slightly as they fade in
        ObjectAnimator textFadeAnimator = ObjectAnimator.ofFloat(brandNameTextView, "alpha", 0f, 1f);
        ObjectAnimator textRiseAnimator = ObjectAnimator.ofFloat(brandNameTextView, "translationY", 30f, 0f);

        ObjectAnimator logoFadeAnimator = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f);
        ObjectAnimator logoRiseAnimator = ObjectAnimator.ofFloat(logoImageView, "translationY", 30f, 0f);

        // Better timing
        textFadeAnimator.setDuration(600);
        textRiseAnimator.setDuration(600);

        logoFadeAnimator.setDuration(600);
        logoRiseAnimator.setDuration(600);
        logoFadeAnimator.setStartDelay(150); // Staggered entrance
        logoRiseAnimator.setStartDelay(150);

        // Better interpolator
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
        textFadeAnimator.setInterpolator(interpolator);
        textRiseAnimator.setInterpolator(interpolator);
        logoFadeAnimator.setInterpolator(interpolator);
        logoRiseAnimator.setInterpolator(interpolator);

        AnimatorSet textAnimSet = new AnimatorSet();
        textAnimSet.playTogether(textFadeAnimator, textRiseAnimator);

        AnimatorSet logoAnimSet = new AnimatorSet();
        logoAnimSet.playTogether(logoFadeAnimator, logoRiseAnimator);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(textAnimSet, logoAnimSet);

        return animatorSet;
    }

    private void setGradientBackground() {
        int skyBlue = getResources().getColor(R.color.sky_blue);
        int turquoise = getResources().getColor(R.color.turquoise);

        // Create a smoother gradient with better blending
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, // Diagonal gradient looks nicer
                new int[] {skyBlue, turquoise});

        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setCornerRadius(0f);

        // Animate the gradient alpha for smoother transition
        ValueAnimator fadeAnimator = ValueAnimator.ofInt(0, 255);
        fadeAnimator.setDuration(400);
        fadeAnimator.addUpdateListener(animation -> {
            int alpha = (int) animation.getAnimatedValue();
            gradientDrawable.setAlpha(alpha);
            rootLayout.setBackground(gradientDrawable);
            rootLayout.invalidate();
        });
        fadeAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Clean up any pending handlers
    }
}