package leap.droidcord.model;

import cc.nnproject.json.JSONObject;

import leap.droidcord.State;

public class DirectMessage extends User {
    public long lastMessageID;
    public long iconID; // for groups, group ID. for users, recipient ID (not DM channel ID)
    public boolean isGroup;

    public DirectMessage(State s, JSONObject data) {
        super(s, data);
        isGroup = data.getInt("type") == 3;

        if (isGroup) {
            name = data.getString("name");
            iconID = id;
            iconHash = data.getString("icon", null);
        } else {
            try {
                JSONObject recipient = data.getArray("recipients").getObject(0);

                name = recipient.getString("global_name", null);
                if (name == null)
                    name = recipient.getString("username");
                else
                    username = recipient.getString("username", null);

                iconID = Long.parseLong(recipient.getString("id"));
                iconHash = recipient.getString("avatar");
            } catch (Exception e) {
            }
        }
        if (name == null)
            name = "(unknown)";

        String msgIdStr = data.getString("last_message_id");
        if (msgIdStr != null)
            lastMessageID = Long.parseLong(msgIdStr);
        else
            lastMessageID = id;
    }

    public static DirectMessage getById(State s, long id) {
        if (s.directMessages == null)
            return null;

        for (int c = 0; c < s.directMessages.size(); c++) {
            DirectMessage ch = (DirectMessage) s.directMessages.elementAt(c);
            if (id == ch.id)
                return ch;
        }
        return null;
    }

    public String toString() {
        return name;
    }

    public Long getIconID() {
        return iconID;
    }

    public String getIconHash() {
        return iconHash;
    }

    public String getIconType() {
        return isGroup ? "/channel-icons/" : "/avatars/";
    }

    public void iconLoaded(State s) {
        // if (s.dmSelector != null) s.dmSelector.update(id);
    }

    public void largeIconLoaded(State s) {
    }
}