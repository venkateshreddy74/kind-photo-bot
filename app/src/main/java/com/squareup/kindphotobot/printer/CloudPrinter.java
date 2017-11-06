package com.squareup.kindphotobot.printer;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.squareup.kindphotobot.BuildConfig;
import com.squareup.kindphotobot.oauth.OAuthHeaderInterceptor;
import com.squareup.kindphotobot.oauth.OAuthStore;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.squareup.kindphotobot.printer.CloudPrintService.CONNECTION_STATUS_ALL;

public class CloudPrinter {

  private static final String KEY_PRINTER_ID = "printerId";
  private static final String KEY_PRINTER_NAME = "printerName";
  // Change this to see what Google Cloud Print has to say.
  @SuppressWarnings("PointlessBooleanExpression") //
  private static final boolean LOG_HTTP = BuildConfig.DEBUG && false;

  public static CloudPrinter create(OAuthStore oAuthStore, Context context) {
    Interceptor authorizationInterceptor = new OAuthHeaderInterceptor(oAuthStore);
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (LOG_HTTP) {
      HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
      loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
      builder.addInterceptor(loggingInterceptor);
    }
    OkHttpClient client = builder.addInterceptor(authorizationInterceptor).build();
    Retrofit retrofit = new Retrofit.Builder().baseUrl("https://www.google.com/cloudprint/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build();
    CloudPrintService cloudPrintService = retrofit.create(CloudPrintService.class);
    return new CloudPrinter(cloudPrintService, context);
  }

  private final CloudPrintService cloudPrintService;
  private final SharedPreferences preferences;
  private final Gson gson;

  private CloudPrinter(CloudPrintService cloudPrintService, Context context) {
    this.cloudPrintService = cloudPrintService;
    Context appContext = context.getApplicationContext();
    preferences = appContext.getSharedPreferences("print", Context.MODE_PRIVATE);
    gson = new Gson();
  }

  public void search(Callback<CloudPrintService.SearchResponse> callback) {
    cloudPrintService.search(CONNECTION_STATUS_ALL).enqueue(callback);
  }

  public void selectPrinter(CloudPrintService.Printer printer) {
    preferences.edit()
        .putString(KEY_PRINTER_ID, printer.id)
        .putString(KEY_PRINTER_NAME, printer.description)
        .apply();
  }

  @Nullable public String getSelectedPrinterName() {
    return preferences.getString(KEY_PRINTER_NAME, null);
  }

  public void clearPrinter() {
    preferences.edit().remove(KEY_PRINTER_ID).remove(KEY_PRINTER_NAME).apply();
  }

  public void cloudPrint(byte[] jpegBytes) throws IOException {
    String printerId = preferences.getString(KEY_PRINTER_ID, null);
    if (printerId == null) {
      throw new UnsupportedOperationException("Please call isCloudPrintSetUp()");
    }

    // Not great, but got the job done. Tailored to work with the Selphy CP1300.
    String ticketString = "{\n"
        + "  \"version\": \"1.0\",\n"
        + "  \"print\": {\n"
        + "    \"color\": {\"type\": \"STANDARD_COLOR\"},\n"
        + "    \"copies\": {\"copies\": 1},\n"
        + "    \"media_size\": {\"height_microns\": 86000, \"width_microns\": 54000, \"is_continuous_feed\": false},\n"
        + "    \"dpi\": {\"horizontal_dpi\": 300, \"vertical_dpi\": 300}\n"
        + "  }\n"
        + "}";

    RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), jpegBytes);

    MultipartBody.Part content =
        MultipartBody.Part.createFormData("content", "picture.jpeg", requestFile);

    RequestBody printerIdBody = RequestBody.create(MultipartBody.FORM, printerId);
    RequestBody titleBody = RequestBody.create(MultipartBody.FORM, "My print title");
    RequestBody ticketBody = RequestBody.create(MultipartBody.FORM, ticketString);
    RequestBody contentTypeBody = RequestBody.create(MultipartBody.FORM, "image/jpeg");
    Response<CloudPrintService.SubmitResponse> printResponse =
        cloudPrintService.submit(printerIdBody, titleBody, ticketBody, content, contentTypeBody)
            .execute();

    if (!printResponse.isSuccessful()) {
      throw new IOException("Print submit not successful");
    }
    CloudPrintService.SubmitResponse submitResponse = printResponse.body();

    if (!submitResponse.success) {
      throw new IOException("Server error: " + submitResponse.message);
    }
  }

  public boolean isCloudPrintSetUp() {
    String printerId = preferences.getString(KEY_PRINTER_ID, null);
    return printerId != null;
  }
}
