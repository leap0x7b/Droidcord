package leap.droidcord.model;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class Presence {
    public enum ActiveStatus {
        ONLINE,
        OFFLINE,
        IDLE,
        DND
    }

    public enum Activity {
        PLAYING,
        STREAMING,
        LISTENING,
        WATCHING,
        CUSTOM_STATUS,
        COMPETING
    }

    public ActiveStatus activeStatus;
    public Activity currentActivity;
    public String status;

    public Presence(JSONObject data) {
        System.out.println(data.build());
        activeStatus = ActiveStatus.valueOf(data.getString("status").toUpperCase());

        if (data.has("game")) {
            currentActivity = Activity.values()[data.getObject("game").getInt("type")];
            status = getStatusFromActivity(data.getObject("game"));
        } else if (data.has("activities")) {
            currentActivity = getActivityTypeFromActivities(data.getArray("activities"));
            status = getStatusFromActivities(data.getArray("activities"));
        }
    }

    private static String getStatusFromActivities(JSONArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            JSONObject a = arr.getObject(i);
            if (a.getString("name") == "Custom Status")
                // prioritize custom statuses
                return getStatusFromActivity(a);
        }
        return getStatusFromActivity(arr.getObject(0));
    }

    private static Activity getActivityTypeFromActivities(JSONArray arr) {
        for (int i = 0; i < arr.size(); i++) {
            JSONObject a = arr.getObject(i);
            if (a.getString("name") == "Custom Status")
                // prioritize custom statuses
                return Activity.CUSTOM_STATUS;
        }
        return Activity.values()[arr.getObject(0).getInt("type")];
    }

    private static String getStatusFromActivity(JSONObject data) {
        if (!data.has("type"))
            return null;
        
        Activity type = Activity.values()[data.getInt("type")];
        switch (type) {
            case PLAYING:
            case LISTENING:
            case WATCHING:
            case COMPETING:
                return data.getString("name");
            case STREAMING:
                return data.getString("details");
            case CUSTOM_STATUS:
                return data.getString("state");
        }

        return null;
    }
}
