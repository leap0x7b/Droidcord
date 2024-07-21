package leap.droidcord;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Enumeration;
import java.util.Hashtable;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;

public class UnreadManager {
    public boolean autoSave;
    private Hashtable<Long, Long> channels;
    private State s;
    private Context c;

    public UnreadManager(State s, Context c) {
        this.s = s;
        this.c = c;
        channels = new Hashtable<Long, Long>();
        autoSave = true;

        // Load last read message IDs from RMS (convert JSON to hashtable)
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(c);
            JSONArray json = JSON.getArray(new String(sp
                    .getString("unread", "")));

            for (int i = 0; i < json.size(); i++) {
                JSONArray elem = json.getArray(i);
                // Convert base-36 string -> long -> decimal string
                long key = elem.getLong(0);
                long value = elem.getLong(1);
                channels.put(key, value);
            }
        } catch (Exception e) {
            s.error(e.toString());
        }
    }

    public void save() {
        JSONArray json = new JSONArray();

        // Convert hashtable to JSON array of key/value pairs
        for (Enumeration<Long> e = channels.keys(); e.hasMoreElements(); ) {
            JSONArray elem = new JSONArray();
            long key = e.nextElement();
            long value = channels.get(key);

            // Convert decimal string -> long -> base-36 string
            elem.add(key);
            elem.add(value);
            json.add(elem);
        }

        // Write stringified JSON to RMS
        try {
            SharedPreferences sp = PreferenceManager
                    .getDefaultSharedPreferences(c);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("unread", json.build());
            editor.commit();
        } catch (Exception e) {
            s.error(e.toString());
        }
    }

    private void put(long channelID, long lastReadTime) {
        if (lastReadTime == 0)
            return;
        channels.put(channelID, lastReadTime);
        if (autoSave)
            save();
    }

    public boolean hasUnreads(long channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;
        long lastReadTime = channels.get(channelID);
        if (lastReadTime == 0) {
            put(channelID, lastMessageTime);
            return false;
        }

        return lastReadTime < lastMessageTime;
    }

    public boolean hasUnreads(Channel ch) {
        return hasUnreads(ch.id, ch.lastMessageID);
    }

    public boolean hasUnreads(DirectMessage dm) {
        return hasUnreads(dm.id, dm.lastMessageID);
    }

    public void markRead(Long channelID, long lastMessageID) {
        long lastMessageTime = lastMessageID >> 22;
        long lastReadTime = channels.get(channelID);

        if (lastReadTime == 0 || lastReadTime < lastMessageTime) {
            put(channelID, lastMessageTime);
        }
    }

    public void markRead(Channel ch) {
        markRead(ch.id, ch.lastMessageID);
    }

    public void markRead(DirectMessage dm) {
        markRead(dm.id, dm.lastMessageID);
    }

    public void markRead(Guild g) {
        if (g == null || g.channels == null)
            return;

        autoSave = false;
        for (int i = 0; i < g.channels.size(); i++) {
            Channel ch = (Channel) g.channels.elementAt(i);
            markRead(ch);
        }
        autoSave = true;
        save();
    }

    public void markDMsRead() {
        /*if (s.dmSelector == null) return;

        autoSave = false;
        for (int i = 0; i < s.dmSelector.lastDMs.size(); i++) {
            DMChannel dmCh = (DMChannel) s.dmSelector.lastDMs.elementAt(i);
            markRead(dmCh);
        }
        autoSave = true;
        save();*/
    }
}