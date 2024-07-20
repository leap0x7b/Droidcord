package leap.droidcord;

import java.util.Vector;

import leap.droidcord.Permissions.Overwrite;
import cc.nnproject.json.*;

public class Channel extends Snowflake {
	public String name;
	public long lastMessageID;
	public boolean unread;
	public Vector<Overwrite> overwrites;

	public Channel(JSONObject data) {
		super(Long.parseLong(data.getString("id")));
		name = data.getString("name");

		/*if (data.has("permission_overwrites")) {
			overwrites = new Vector<Overwrite>();
			JSONArray arr = data.getArray("permission_overwrites");

			for (int i = 0; i < arr.size(); i++) {
				for (int a = 0; a < arr.size(); a++) {
					JSONObject p = arr.getObject(a);
					overwrites.addElement(new Overwrite(p));
				}
			}
		}*/
		try {
			lastMessageID = Long.parseLong(data.getString("last_message_id"));
		} catch (Exception e) {
			lastMessageID = 0L;
		}
	}

	public String toString() {
		return "#" + name;
	}

	public static Channel getByID(State s, long id) {
		if (s.guilds != null) {
			for (int g = 0; g < s.guilds.size(); g++) {
				Guild guild = (Guild) s.guilds.elementAt(g);
				if (guild.channels == null)
					continue;

				for (int c = 0; c < guild.channels.size(); c++) {
					Channel ch = (Channel) guild.channels.elementAt(c);
					if (id == ch.id)
						return ch;
				}
			}
		}
		return null;
	}

	public long computePermissionOverwrites(Guild g, GuildMember member,
			long basePermissions) {
		// Administrator overrides any potential permission overwrites.
		if ((basePermissions & Permissions.ADMINISTRATOR) != 0) {
			return Permissions.ALL;
		}

		// Find @everyone overwrite and apply it.
		Overwrite everyoneOverwrite = Overwrite.findBySnowflake(overwrites, g.everyoneRole());
		if (everyoneOverwrite != null) {
			basePermissions &= ~everyoneOverwrite.deny;
			basePermissions |= everyoneOverwrite.allow;
		}

		// Apply role specific overwrites.
		long allow = 0, deny = 0;
		for (Role role : member.roles) {
			Overwrite roleOverwrite = Overwrite.findBySnowflake(overwrites, role);
			if (roleOverwrite != null) {
				allow |= roleOverwrite.allow;
				deny &= ~roleOverwrite.deny;
			}
		}

		basePermissions &= ~deny;
		basePermissions |= allow;

		// Apply a member specific overwrite, if it exists.
		Overwrite memberOverwrite = Overwrite.findBySnowflake(overwrites, member.user);
		if (memberOverwrite != null) {
			basePermissions &= ~memberOverwrite.deny;
			basePermissions |= memberOverwrite.allow;
		}

		return basePermissions;
	}

	public boolean hasPermission(Guild g, GuildMember member, long permissions) {
		if (this.overwrites != null) {
			return (computePermissionOverwrites(g, member,
				g.computeBasePermissions(member)) & permissions) != 0;
		}
		return true;
	}

	public static Vector<Channel> parseChannels(State s, Guild g, JSONArray arr) {
		Vector<Channel> result = new Vector<Channel>();

		for (int i = 0; i < arr.size(); i++) {
			for (int a = 0; a < arr.size(); a++) {
				JSONObject ch = arr.getObject(a);
				if (ch.getInt("position", i) != i)
					continue;

				int type = ch.getInt("type", 0);
				if (type != 0 && type != 5)
					continue;

				Channel channel = new Channel(ch);
				/*GuildMember me;
				try {
					me = new GuildMember(s, g, JSON.getObject(s.http.get("/guilds/"
							+ g.id + "/members/" + s.myUserId)));
					if (channel.hasPermission(g, me, Permissions.VIEW_CHANNEL))*/
						result.addElement(channel);
				/*} catch (Exception e) {
					//s.error(e.toString());
					e.printStackTrace();
				}*/
			}
		}
		return result;
	}
}