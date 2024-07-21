package leap.droidcord;

import java.util.Vector;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class Role extends Snowflake {
    public int color;
    public long permissions;
    public int position; // only used for parseGuildMemberRoles, other than that, its completely unnecessary

    public Role(JSONObject data) {
        super(Long.parseLong(data.getString("id")));
        color = data.getInt("color");
        permissions = Long.parseLong(data.getString("permissions"));
        position = data.getInt("position");
    }

    public static Vector<Role> parseRoles(JSONArray arr) {
        Vector<Role> result = new Vector<Role>();

        for (int i = arr.size() - 1; i >= 0; i--) {
            for (int a = arr.size() - 1; a >= 0; a--) {
                JSONObject data = arr.getObject(i);
                if (data.getInt("position", i) != i)
                    continue;

                result.addElement(new Role(data));
            }
        }

        return result;
    }

    public static Vector<Role> parseGuildMemberRoles(Guild g, JSONArray arr) {
        Vector<Role> result = new Vector<Role>();

        for (int i = arr.size() - 1; i >= 0; i--) {
            for (Role role : g.roles) {
                if (role.id == Long.parseLong(arr.getString(i))) {
                    if (role.position != i)
                        continue;

                    result.addElement(role);
                }
            }
        }

        return result;
    }
}
