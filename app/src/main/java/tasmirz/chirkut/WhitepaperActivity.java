package tasmirz.chirkut;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import tasmirz.chirkut.utils.JsonFetcher;
import tasmirz.chirkut.utils.JsonParser;
import java.util.List;

public class WhitepaperActivity extends AppCompatActivity implements JsonFetcher.JsonFetcherCallback {

    private LinearLayout contentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        contentLayout = findViewById(R.id.contentLayout);
    findViewById(R.id.back_bttn).setOnClickListener(v -> finish());

        new JsonFetcher(this).execute("https://gist.githubusercontent.com/tasmirz/17a7ca216c8ce1fabe40725e7d7c10ae/raw/d48829f3ab48cc1ace8ccec8c84aac135e05fcf0/parse.json");
    }

    @Override
    public void onJsonFetched(String json) {
        List<JsonParser.ContentItem> contentItems = JsonParser.parseJson(json, "whitepaper");
        displayContent(contentItems);
    }

    private void displayContent(List<JsonParser.ContentItem> contentItems) {
        for (JsonParser.ContentItem item : contentItems) {
            TextView textView = new TextView(this);
            textView.setText(item.text);
            if (item.type.equals("heading")) {
                textView.setTextSize(20);
                textView.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                textView.setTextSize(16);
            }
            contentLayout.addView(textView);
        }
    }
}