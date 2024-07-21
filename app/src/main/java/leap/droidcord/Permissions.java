package leap.droidcord;

import java.util.Vector;

import cc.nnproject.json.JSONObject;

public class Permissions {
    public static final long CREATE_INSTANT_INVITE = 0x0000000000000001L;
    public static final long KICK_MEMBERS = 0x0000000000000002L;
    public static final long BAN_MEMBERS = 0x0000000000000004L;
    public static final long ADMINISTRATOR = 0x0000000000000008L;
    public static final long MANAGE_CHANNELS = 0x0000000000000010L;
    public static final long MANAGE_GUILD = 0x0000000000000020L;
    public static final long ADD_REACTIONS = 0x0000000000000040L;
    public static final long VIEW_AUDIT_LOG = 0x0000000000000080L;
    public static final long PRIORITY_SPEAKER = 0x0000000000000100L;
    public static final long STREAM = 0x0000000000000200L;
    public static final long VIEW_CHANNEL = 0x0000000000000400L;
    public static final long SEND_MESSAGES = 0x0000000000000800L;
    public static final long SEND_TTS_MESSAGES = 0x0000000000001000L;
    public static final long MANAGE_MESSAGES = 0x0000000000002000L;
    public static final long EMBED_LINKS = 0x0000000000004000L;
    public static final long ATTACH_FILES = 0x0000000000008000L;
    public static final long READ_MESSAGE_HISTORY = 0x0000000000010000L;
    public static final long MENTION_EVERYONE = 0x0000000000020000L;
    public static final long USE_EXTERNAL_EMOJIS = 0x0000000000040000L;
    public static final long VIEW_GUILD_INSIGHTS = 0x0000000000080000L;
    public static final long CONNECT = 0x0000000000100000L;
    public static final long SPEAK = 0x0000000000200000L;
    public static final long MUTE_MEMBERS = 0x0000000000400000L;
    public static final long DEAFEN_MEMBERS = 0x0000000000800000L;
    public static final long MOVE_MEMBERS = 0x0000000001000000L;
    public static final long USE_VAD = 0x0000000002000000L;
    public static final long CHANGE_NICKNAME = 0x0000000004000000L;
    public static final long MANAGE_NICKNAMES = 0x0000000008000000L;
    public static final long MANAGE_ROLES = 0x0000000010000000L;
    public static final long MANAGE_WEBHOOKS = 0x0000000020000000L;
    public static final long MANAGE_GUILD_EXPRESSIONS = 0x0000000040000000L;
    public static final long USE_APPLICATION_COMMANDS = 0x0000000080000000L;
    public static final long REQUEST_TO_SPEAK = 0x0000000100000000L;
    public static final long MANAGE_EVENTS = 0x0000000200000000L;
    public static final long MANAGE_THREADS = 0x0000000400000000L;
    public static final long CREATE_PUBLIC_THREADS = 0x0000000800000000L;
    public static final long CREATE_PRIVATE_THREADS = 0x0000001000000000L;
    public static final long USE_EXTERNAL_STICKERS = 0x0000002000000000L;
    public static final long SEND_MESSAGES_IN_THREADS = 0x0000004000000000L;
    public static final long USE_EMBEDDED_ACTIVITIES = 0x0000008000000000L;
    public static final long MODERATE_MEMBERS = 0x0000010000000000L;
    public static final long VIEW_CREATOR_MONETIZATION_ANALYTICS = 0x0000020000000000L;
    public static final long USE_SOUNDBOARD = 0x0000040000000000L;
    public static final long CREATE_GUILD_EXPRESSIONS = 0x0000080000000000L;
    public static final long CREATE_EVENTS = 0x0000100000000000L;
    public static final long USE_EXTERNAL_SOUNDS = 0x0000200000000000L;
    public static final long SEND_VOICE_MESSAGES = 0x0000400000000000L;

    public static final long ALL = 0x00007FFFFFFFFFFFL;

    public Permissions() {
    }

    public static class Overwrite extends Snowflake {
        public long allow = 0;
        public long deny = 0;
        public boolean isMember = false;

        public Overwrite(JSONObject data) {
            super(Long.parseLong(data.getString("id")));
            allow = Long.parseLong(data.getString("allow"));
            deny = Long.parseLong(data.getString("deny"));
            isMember = data.getInt("type") == 1;
        }

        public static Overwrite findBySnowflake(Vector<Overwrite> overwrites, Snowflake snowflake) {
            for (Overwrite overwrite : overwrites) {
                if (overwrite.id == snowflake.id) {
                    return overwrite;
                }
            }
            return null;
        }

        public Overwrite findBySnowflake(Vector<Overwrite> overwrites, long snowflake) {
            for (Overwrite overwrite : overwrites) {
                if (overwrite.id == snowflake) {
                    return overwrite;
                }
            }
            return null;
        }
    }
}
