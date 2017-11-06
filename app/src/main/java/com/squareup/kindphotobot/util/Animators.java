package com.squareup.kindphotobot.util;

import android.animation.Animator;

public final class Animators {

  public interface AnimatorEndListener {
    void onAnimationEnd(Animator animator);
  }

  public static Animator.AnimatorListener onAnimationEnd(AnimatorEndListener listener) {
    return new Animator.AnimatorListener() {
      @Override public void onAnimationStart(Animator animator) {

      }

      @Override public void onAnimationEnd(Animator animator) {
        listener.onAnimationEnd(animator);
      }

      @Override public void onAnimationCancel(Animator animator) {

      }

      @Override public void onAnimationRepeat(Animator animator) {

      }
    };
  }

  private Animators() {
    throw new AssertionError();
  }
}
