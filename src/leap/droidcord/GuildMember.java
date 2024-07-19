package leap.droidcord;

import java.util.Vector;

import cc.nnproject.json.JSONObject;

public class GuildMember implements HasIcon {
	User user;
	String username;
	String name;
	String nickname;
	Vector<Role> roles;
	long permissions;
	String iconHash;

	// For placeholder icon
	int iconColor;
	String initials;

	public GuildMember(State s, Guild g, JSONObject data) {
		user = new User(s, data.getObject("user"));
		username = data.getString("username", null);
		name = data.getString("global_name", username);
		if (data.has("nick")) {
			nickname = data.getString("nick", null);
		}
		if (data.has("roles")) {
			roles = Role.parseGuildMemberRoles(g, data.getArray("roles"));
		}
		
		if (s.iconType == State.ICON_TYPE_NONE)
			return;

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

		iconColor = Util.hsvToRgb((int) user.id % 360, 192, 224);
	}

	public Long getIconID() {
		return user.id;
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
