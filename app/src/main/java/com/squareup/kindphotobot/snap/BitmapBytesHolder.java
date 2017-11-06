package com.squareup.kindphotobot.snap;

import static com.squareup.kindphotobot.util.Preconditions.assertMainThread;

public class BitmapBytesHolder {

  private byte[] bitmapBytes;

  public void hold(byte[] bitmapBytes) {
    assertMainThread();
    this.bitmapBytes = bitmapBytes;
  }

  public byte[] release() {
    assertMainThread();
    byte[] bitmapBytes = this.bitmapBytes;
    this.bitmapBytes = null;
    return bitmapBytes;
  }
}
