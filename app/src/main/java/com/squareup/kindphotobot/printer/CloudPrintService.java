package com.squareup.kindphotobot.printer;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CloudPrintService {

  String CONNECTION_STATUS_ALL = "ALL";

  @FormUrlEncoded //
  @POST("search") //
  Call<SearchResponse> search(@Field("connection_status") String connectionStatus);

  @Multipart //
  @POST("submit") //
  Call<SubmitResponse> submit(@Part("printerid") RequestBody printerId,
      @Part("title") RequestBody title, @Part("ticket") RequestBody ticket,
      @Part MultipartBody.Part content, @Part("contentType") RequestBody contentType);

  class SearchResponse {
    public boolean success;
    public String message;
    public List<Printer> printers;
  }

  class SubmitResponse {
    public boolean success;
    public String message;
  }

  class Printer {
    String id;
    String name;
    String displayName;
    String description;
    String ownerId;
    String ownerName;
  }
}
