package leap.droidcord;

import java.util.Hashtable;
import java.util.Vector;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class NameColorCache {
    public boolean activeRequest;
    private State s;
    private Hashtable<String, Integer> colors;
    private Vector<String> keys;

    public NameColorCache(State s) {
        this.s = s;
        colors = new Hashtable<String, Integer>();
        keys = new Vector<String>();
    }

    private void fetch() {
        JSONObject reqData = new JSONObject();
        reqData.put("guild_id", s.selectedGuild.id);

        JSONArray requestIds = new JSONArray();

        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.elementAt(i);
            long userId = msg.author.id;
            if (requestIds.indexOf(userId) == -1
                    && !s.nameColorCache.has(msg.author)) {
                requestIds.add(userId);
            }
        }

        reqData.put("user_ids", requestIds);

        JSONObject msg = new JSONObject();
        msg.put("op", 8);
        msg.put("d", reqData);
        s.gateway.send(msg);
    }

    public int get(long userId) {
        if (!s.useNameColors)
            return 0;

        // name colors are not applicable in non-guild contexts
        if (s.isDM || s.selectedGuild == null)
            return 0;

        String key = String.valueOf(userId) + String.valueOf(s.selectedGuild.id);

        Integer result = (Integer) colors.get(key);
        if (result != null)
            return result.intValue();

        // name colors cannot be fetched without gateway (technically can but
        // isn't practical)
        if (!s.gatewayActive())
            return 0;

        if (!activeRequest) {
            activeRequest = true;
            fetch();
        }
        return 0;
    }

    public int get(User user) {
        return get(user.id);
    }

    public void set(String key, int color) {
        if (!colors.containsKey(key) && colors.size() >= 50) {
            String firstHash = (String) keys.elementAt(0);
            colors.remove(firstHash);
            keys.removeElementAt(0);
        }
        colors.put(key, Integer.valueOf(color));
        keys.addElement(key);

        // s.channelView.repaint();
    }

    public boolean has(User user) {
        if (s.isDM || s.selectedGuild == null)
            return false;
        String key = String.valueOf(user.id) + String.valueOf(s.selectedGuild.id);
        return colors.containsKey(key);
    }
}