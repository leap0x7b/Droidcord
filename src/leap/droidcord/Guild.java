package leap.droidcord;

import cc.nnproject.json.*;

import java.util.*;

public class Guild extends Snowflake implements HasIcon {
	public String name;
	public long ownerId;
	public User me;
	public long myPermissions;
	public Vector<Channel> channels;
	public Vector<Role> roles;
	public String iconHash;

	public Guild(State s, JSONObject data) {
		super(Long.parseLong(data.getString("id")));
		iconHash = data.getString("icon", null);

		if (data.has("owner_id")) {
			ownerId = Long.parseLong(data.getString("owner_id"));
		}

		if (data.has("name")) {
			name = data.getString("name");
		} else if (data.has("properties")) {
			name = data.getObject("properties").getString("name");
		} else {
			name = "(unknown)";
		}

		if (data.has("channels")) {
			channels = Channel.parseChannels(s, this, data.getArray("channels"));
		}
		
		if (data.has("permissions")) {
			myPermissions = Long.parseLong(data.getString("permissions"));
		}
		
		if (data.has("roles")) {
			roles = Role.parseRoles(data.getArray("roles"));
		}
	}

    public Role everyoneRole() {
        for (Role role : roles) {
            if (role.id == this.id) {
                return role;
            }
        }
        return null;
    }
	
    public long computeBasePermissions(GuildMember member) {
        if (member.user.id == ownerId) {
            return Permissions.ALL;
        }

        long perms = everyoneRole().permissions;

        for (Role role : member.roles) {
            perms |= role.permissions;
        }

        if ((perms & Permissions.ADMINISTRATOR) != 0) {
            return Permissions.ALL;
        }

        return perms;
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