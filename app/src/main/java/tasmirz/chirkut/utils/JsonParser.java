package tasmirz.chirkut.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class JsonParser {

    public static List<ContentItem> parseJson(String json, String section) {
        List<ContentItem> contentItems = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(section);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                String type = item.getString("type");
                String text = item.getString("text");
                contentItems.add(new ContentItem(type, text));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contentItems;
    }

    public static class ContentItem {
        public String type;
        public String text;

        public ContentItem(String type, String text) {
            this.type = type;
            this.text = text;
        }
    }
}