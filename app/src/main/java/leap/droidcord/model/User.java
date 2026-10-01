package leap.droidcord.model;

import cc.nnproject.json.JSONObject;

import leap.droidcord.State;
import leap.droidcord.Util;

public class User extends Snowflake implements HasIcon {
    public String name;
    public String username;
    public String iconHash;

    // For placeholder icon
    public int iconColor;
    public String initials;

    public User(State s, JSONObject data) {
        super(Long.parseLong(data.getString("id")));

        username = data.getString("username", "(unknown)");
        name = data.getString("global_name", username);
        if (name == null)
            name = "(unknown)";

        iconHash = data.getString("avatar", null);

        StringBuffer initialsBuf = new StringBuffer();
        initialsBuf.append(name.charAt(0));
        if (name.length() > 1) {
            for (int i = 1; i < name.length(); i++) {
                char last = name.charAt(i - 1);
                char curr = name.charAt(i);

                if (last == ' '
                        || (Character.isLowerCase(last) && Character
                        .isUpperCase(curr))) {
                    initialsBuf.append(curr);
                    break; // max 2 chars
                }
            }
        }
        initials = initialsBuf.toString();

        iconColor = Util.hsvToRgb((int) id % 360, 192, 224);
    }

    public Long getIconID() {
        return id;
    }

    public String getIconHash() {
        return iconHash;
    }

    public String getIconType() {
        return "/avatars/";
    }

    public void iconLoaded(State s) {
    }

    public void largeIconLoaded(State s) {
        iconLoaded(s);
    }
}
