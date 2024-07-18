package leap.droidcord;

import cc.nnproject.json.*;
import java.util.*;

public class Guild implements HasIcon {
	public long id;
	public String name;
	public Vector<Channel> channels;
	public String iconHash;
	public Vector<Role> roles;

	public Guild(State s, JSONObject data) {
		id = Long.parseLong(data.getString("id"));
		iconHash = data.getString("icon", null);

		if (data.has("name")) {
			name = data.getString("name");
		} else if (data.has("properties")) {
			name = data.getObject("properties").getString("name");
		} else {
			name = "(unknown)";
		}

		if (data.has("channels")) {
			channels = Channel.parseChannels(data.getArray("channels"));
		}
	}

	public String toString(State s) {
		return name;
	}

	public Long getIconID() {
		return id;
	}

	public String getIconHash() {
		return iconHash;
	}

	public String getIconType() {
		return "/icons/";
	}

	public void iconLoaded(State s) {
		//if (s.guildSelector != null) s.guildSelector.update(id);
	}

	public void largeIconLoaded(State s) {
	}
}