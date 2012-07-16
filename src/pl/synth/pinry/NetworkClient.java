package pl.synth.pinry;

import android.content.Context;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class NetworkClient {
    private static final String TAG = "NetworkClient";
    private Context context;
    private static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;
    private String baseUrl;

    private static HttpClient getHttpClient() {
        HttpClient client = new DefaultHttpClient();
        final HttpParams params = client.getParams();
        HttpConnectionParams.setConnectionTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
        ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);

        return client;
    }

    public NetworkClient(String url, Context context) {
        this.context = context;
        this.baseUrl = url;
    }

    public ArrayList<Pin> getPinsSince(String lastKnownUpdate) {
        ArrayList<Pin> returnList = new ArrayList<Pin>();
        HttpClient client = new DefaultHttpClient();
        String url = baseUrl;
        url += "/api/pin/";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("format", "json"));
        if (lastKnownUpdate != null && lastKnownUpdate.length() > 0) {
            params.add(new BasicNameValuePair("published__gt", lastKnownUpdate));
        }
        String paramString = URLEncodedUtils.format(params, "utf-8");
        url += "?" + paramString;

        HttpGet request = new HttpGet(url);
        JSONObject json;
        try {
            HttpResponse httpResponse = client.execute(request);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.e(TAG, "Unexpected status code. Expected 200, got " + statusCode);
                return returnList;
            }

            String responseString = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
            json = new JSONObject(responseString);
        } catch (IOException e) {
            return returnList;
        } catch (JSONException e) {
            Log.e(TAG, "Could not parse the JSON response: " + e.getMessage());
            return returnList;
        }

        /* every proper API response contains the meta object, so we can check for its existence and decide whether
           the server returned correct response.
          */
        if (!json.has("meta")) {
            Log.e(TAG, "Server returned unexpected response. Bailing out.");
            return returnList;
        }

        try {
            int totalCount = json.getJSONObject("meta").getInt("total_count");
            JSONArray objects = json.getJSONArray("objects");

            for (int i = 0; i < totalCount; i++) {
                JSONObject object = objects.getJSONObject(i);
                String imagePath = object.getString("image");
                String localPath;
                int pinId = object.getInt("id");
                try {
                    localPath = fetchImage(imagePath, pinId);
                } catch (IOException e) {
                    Log.e(TAG, "fetchAndProcessImage failed: " + e.getMessage());
                    continue;
                }

                int id = object.getInt("id");
                String description = object.getString("description");
                String sourceUrl = object.getString("url");
                String publishedDate = object.getString("published");

                Pin pin = new Pin(this.context, id, sourceUrl, localPath, description, publishedDate);

                returnList.add(pin);
            }
        } catch (JSONException e) {
            return returnList;
        }

        return returnList;
    }

    private String fetchImage(String imagePath, int pinId) throws IOException {
        HttpClient client = getHttpClient();

        String remoteFileName = Tools.last(imagePath.split("/"));
        File path = context.getExternalFilesDir(null);

        String[] tokens = remoteFileName.split("\\.(?=[^\\.]+$)");
        File localFile = new File(path, tokens[0] + "_" + pinId + "." + tokens[1]);

        if (localFile.exists()) {
            return localFile.getAbsolutePath();
        }

        String imageUrl = baseUrl + imagePath;
        HttpGet request = new HttpGet(imageUrl);
        OutputStream stream = null;
        try {
            HttpResponse response = client.execute(request);
            stream = new FileOutputStream(localFile);
            response.getEntity().writeTo(stream);
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        return localFile.getAbsolutePath();
    }
}
