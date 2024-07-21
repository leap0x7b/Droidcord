package leap.droidcord;

import cc.nnproject.json.JSONObject;

public class Embed {
    public String title;
    public String description;

    public Embed(JSONObject data) {
        title = data.getString("title", null);
        description = data.getString("description", null);
    }
}