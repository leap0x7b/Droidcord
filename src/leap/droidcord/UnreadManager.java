package leap.droidcord;

import java.util.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import cc.nnproject.json.*;

public class UnreadManager {
	private Hashtable<String, String> channels;
	private State s;
	private Context c;
	public boolean autoSave;

	public UnreadManager(State s, Context c) {
		this.s = s;
		this.c = c;
		channels = new Hashtable<String, String>();
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
				long key = Long.parseLong(elem.getString(0),
						Character.MAX_RADIX);
				long value = Long.parseLong(elem.getString(1),
						Character.MAX_RADIX);
				String keyStr = String.valueOf(key);
				String valStr = String.valueOf(value);
				channels.put(keyStr, valStr);
			}
		} catch (Exception e) {
			s.error(e.toString());
		}
	}

	public void save() {
		JSONArray json = new JSONArray();

		// Convert hashtable to JSON array of key/value pairs
		for (Enumeration<String> e = channels.keys(); e.hasMoreElements();) {
			JSONArray elem = new JSONArray();
			String key = (String) e.nextElement();
			String value = (String) channels.get(key);

			// Convert decimal string -> long -> base-36 string
			elem.add(Long.toString(Long.parseLong(key), Character.MAX_RADIX));
			elem.add(Long.toString(Long.parseLong(value), Character.MAX_RADIX));
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

	private void put(String channelID, String lastReadTime) {
		if (lastReadTime == null || lastReadTime.equals("0"))
			return;
		channels.put(channelID, lastReadTime);
		if (autoSave)
			save();
	}

	public boolean hasUnreads(long channelID, long lastMessageID) {
		long lastMessageTime = lastMessageID >> 22;

		String lastReadTime = (String) channels.get(channelID);
		if (lastReadTime == null) {
			put(String.valueOf(channelID), String.valueOf(lastMessageTime));
			return false;
		}

		return Long.parseLong(lastReadTime) < lastMessageTime;
	}

	public boolean hasUnreads(Channel ch) {
		return hasUnreads(ch.id, ch.lastMessageID);
	}

	public boolean hasUnreads(DirectMessage dm) {
		return hasUnreads(dm.id, dm.lastMessageID);
	}

	public void markRead(Long channelID, long lastMessageID) {
		long lastMessageTime = lastMessageID >> 22;
		String lastReadTime = (String) channels.get(channelID);

		if (lastReadTime == null
				|| Long.parseLong(lastReadTime) < lastMessageTime) {
			put(String.valueOf(channelID), String.valueOf(lastMessageTime));
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