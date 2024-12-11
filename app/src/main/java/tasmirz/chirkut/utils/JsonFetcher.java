package tasmirz.chirkut.utils;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonFetcher extends AsyncTask<String, Void, String> {

    private JsonFetcherCallback callback;

    public JsonFetcher(JsonFetcherCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... urls) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urls[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        if (callback != null) {
            callback.onJsonFetched(result);
        }
    }

    public interface JsonFetcherCallback {
        void onJsonFetched(String json);
    }
}