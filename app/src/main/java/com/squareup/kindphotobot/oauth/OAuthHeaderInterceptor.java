package com.squareup.kindphotobot.oauth;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class OAuthHeaderInterceptor implements Interceptor {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private final OAuthStore oAuthStore;

  public OAuthHeaderInterceptor(OAuthStore oAuthStore) {
    this.oAuthStore = oAuthStore;
  }

  @Override public Response intercept(Chain chain) throws IOException {
    String oauthToken = oAuthStore.getOAuthToken();
    Request request;
    if (oauthToken != null) {
      request = chain.request()
          .newBuilder()
          .addHeader(AUTHORIZATION_HEADER, "OAuth " + oauthToken)
          .build();
    } else {
      request = chain.request();
    }
    return chain.proceed(request);
  }
}
