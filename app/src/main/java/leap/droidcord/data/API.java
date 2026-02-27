package leap.droidcord.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Vector;

import leap.droidcord.State;
import leap.droidcord.model.Attachment;
import leap.droidcord.model.Channel;
import leap.droidcord.model.DirectMessage;
import leap.droidcord.model.Guild;
import leap.droidcord.model.HasIcon;
import leap.droidcord.model.Message;
import leap.droidcord.model.Role;
import leap.droidcord.model.Snowflake;

import android.graphics.Bitmap;
import android.util.Log;
import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class API {
    private static final String TAG = "API";
    private State s;

    public API(State s) {
        this.s = s;
    }

    private static final String BOUNDARY = "----WebKitFormBoundary7MA4YWykTrZu0gW";
    private static final String LINE_FEED = "\r\n";

    private byte[] createFormPart(String name, String filename) {
        StringBuffer r = new StringBuffer();

        r.append("--").append(BOUNDARY).append(LINE_FEED);
        r.append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"");
        if (filename != null) {
            r.append("; filename=\"").append(filename).append("\"");
        }
        r.append(LINE_FEED);
        if (filename != null) {
            r.append("Content-Type: application/octet-stream")
                    .append(LINE_FEED);
        }
        r.append(LINE_FEED);

        return r.toString().getBytes();
    }

    public void fetchGuilds() {
        try {
            final String response = s.http.get("/users/@me/guilds");
            final JSONArray data = JSON.getArray(response);

            s.guilds.clear();
            for (int i = 0; i < data.size(); i++)
                s.guilds.add(new Guild(s, data.getObject(i)));
        } catch (Exception e) {
            Log.e(TAG, "Error fetching guilds", e);
        }
    }

    public void fetchDirectMessages() {
        try {
            JSONArray channels = JSON.getArray(s.http.get("/users/@me/channels"));
            s.directMessages = new Vector<DirectMessage>();

            for (int i = 0; i < channels.size(); i++) {
                JSONObject ch = channels.getObject(i);
                int type = ch.getInt("type", 1);
                if (type != 1 && type != 3)
                    continue;

                s.directMessages.add(new DirectMessage(s, ch));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching direct messages", e);
        }
    }

    public void fetchChannels() {
        try {
            // Fetch role data (role colors) for this server if needed
            if (s.gatewayActive() && s.selectedGuild.roles == null) {
                String roleData = s.http.get("/guilds/"
                        + s.selectedGuild.id + "/roles?droidcord=1");
                JSONArray roleArr = JSON.getArray(roleData);

                s.selectedGuild.roles = new Vector<Role>();

                for (int i = roleArr.size() - 1; i >= 0; i--) {
                    for (int a = roleArr.size() - 1; a >= 0; a--) {
                        JSONObject data = roleArr.getObject(a);
                        if (data.getInt("position", i) != i)
                            continue;

                        int color = data.getInt("color");
                        if (color == 0)
                            continue;

                        s.selectedGuild.roles.add(new Role(data));
                    }
                }
            }

            s.selectedGuild.channels = Channel.parseChannels(s, s.selectedGuild,
                    JSON.getArray(s.http.get("/guilds/" + s.selectedGuild.id
                            + "/channels")));

            s.channels = s.selectedGuild.channels;
        } catch (Exception e) {
            Log.e(TAG, "Error fetching channels", e);
        }
    }

    public void fetchMessages(long before, long after) {
        try {
            s.messages.reset();

            Snowflake channel = s.isDM ? s.selectedDm : s.selectedChannel;

            StringBuffer url = new StringBuffer("/channels/" + channel.id
                    + "/messages?droidcord=1&limit=" + s.messageLoadCount);
            if (before != 0)
                url.append("&before=" + before);
            if (after != 0)
                url.append("&after=" + after);

            JSONArray messages = JSON.getArray(s.http.get(url.toString()));

            for (int i = messages.size() - 1; i >= 0; i--)
                s.messages.add(new Message(s, messages.getObject(i)));

            s.messages.cluster();
            s.sendMessage = null;
            s.typingUsers = new Vector<String>();
            s.typingUserIDs = new Vector<Long>();

            if (!s.isDM) {
                s.guildInformation.fetch();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching messages", e);
            s.error("Error fetching messages: " + e.getMessage());
        }
    }

    public void fetchAttachment(Attachment attachment) {
        try {
            if (s.cdn == null || s.cdn.length() == 0) {
                throw new Exception("CDN URL has not been defined. Attachments are not available.");
            }

            if (attachment.supported) {
                try {
                    Bitmap image = s.http.getImage(attachment.previewUrl);
                    s.attachments.set(attachment.previewUrl, image);
                } catch (Exception e) {
                    s.attachments.removeRequest(attachment.previewUrl);
                    Log.e(TAG, "Error fetching attachment", e);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error fetching attachment", e);
            s.error("Error fetching attachment: " + e.getMessage());
        }
    }

    public void fetchIcon(HasIcon target, int size) {
        try {
            if (s.cdn == null || s.cdn.length() == 0) {
                throw new Exception(
                        "CDN URL has not been defined. Server icons and user avatars are not available.");
            }

            String format = (s.useJpeg ? "jpg" : "png");
            String type = target.getIconType();
            long id = target.getIconID();
            String hash = target.getIconHash();

            Bitmap icon = s.http.getImage(s.cdn + type + id + "/" + hash + "."
                                          + format + "?size=" + size);

            s.icons.set(hash + String.valueOf(size), icon);
            target.iconLoaded(s);
        } catch (Exception e) {
            s.icons.removeRequest(target.getIconHash() + String.valueOf(size));
            Log.e(TAG, "Error fetching icon", e);
            s.error("Error fetching icon: " + e.getMessage());
        }
    }

    public void sendMessage() {
        try {
            Snowflake channel = s.isDM ? s.selectedDm : s.selectedChannel;

            JSONObject json = new JSONObject();
            JSONObject ref = null;
            JSONObject ping = null;

            json.put("content", s.sendMessage);
            json.put("flags", 0);
            json.put("mobile_network_type", "unknown");
            json.put("tts", false);

            // Reply
            if (s.sendReference != 0) {
                ref = new JSONObject();
                ref.put("channel_id", channel.id);
                if (!s.isDM)
                    ref.put("guild_id", s.selectedGuild.id);
                ref.put("message_id", s.sendReference);
                json.put("message_reference", ref);

                if (!s.sendPing && !s.isDM) {
                    ping = new JSONObject();
                    ping.put("replied_user", false);
                    json.put("allowed_mentions", ping);
                }
            }

            s.http.post("/channels/" + channel.id + "/messages", json);
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
            s.error("Error sending message: " + e.getMessage());
        }
    }

    public void sendAttachment(String path, String name) {
        HttpURLConnection httpConn = null;
        OutputStream os = null;

        try {
            Snowflake channel = s.isDM ? s.selectedDm : s.selectedChannel;
            httpConn = s.http.openConnection("/channels/" + channel.id
                    + "/upload");
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);

            os = httpConn.getOutputStream();

            os.write(createFormPart("token", null));
            os.write(s.http.token.getBytes());
            os.write(LINE_FEED.getBytes());

            os.write(createFormPart("content", null));
            os.write(LINE_FEED.getBytes());

            os.write(createFormPart("files", name));

            FileInputStream fileInputStream = new FileInputStream(
                    path);

            byte[] buffer = new byte[1024];
            int bytesRead = -1;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            os.write(LINE_FEED.getBytes());
            os.write(("--" + BOUNDARY + "--" + LINE_FEED).getBytes());
            os.flush();

            fileInputStream.close();

            s.http.sendRequest(httpConn);

            // TODO: Async
            fetchMessages(0, 0);
        } catch (Exception e) {
            s.error("Error while sending file: " + e.toString());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void editMessage(Message message, String content) {
        try {
            JSONObject newMessage = new JSONObject();
            newMessage.put("content", content);

            Snowflake channel = s.isDM ? s.selectedDm
                    : s.selectedChannel;
            String path = "/channels/" + channel.id + "/messages/"
                    + message.id + "/edit";
            s.http.post(path, newMessage);

            // Manually update message content if gateway disabled
            // (if enabled, new message content will come through gateway
            // event)
            if (!s.gatewayActive()) {
                message.content = content;
                message.rawContent = content;
                message.needUpdate = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error editing message", e);
            s.error("Error editing message: " + e.getMessage());
        }
    }

    public void deleteMessage(Message message) {
        try {
            Snowflake channel = s.isDM ? s.selectedDm
                    : s.selectedChannel;

            s.http.get("/channels/" + channel.id + "/messages/"
                    + message.id + "/delete");

            // Manually update message to be deleted if gateway disabled
            // (if enabled, deletion event will come through gateway)
            if (!s.gatewayActive()) {
                message.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting message", e);
            s.error("Error deleting message: " + e.getMessage());
        }
    }

    public void aFetchGuilds(final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                fetchGuilds();
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aFetchDirectMessages(final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                fetchDirectMessages();
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aFetchChannels(final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                fetchChannels();
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aFetchMessages(final long before, final long after,
                               final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                fetchMessages(before, after);
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aFetchAttachment(final Attachment attachment, final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                fetchAttachment(attachment);
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aFetchIcon(final HasIcon target, final int size,
                           final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                fetchIcon(target, size);
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aSendMessage(final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                sendMessage();
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aSendAttachment(final String path, final String name,
                                final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                sendAttachment(path, name);
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aEditMessage(final Message message, final String content,
                             final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                editMessage(message, content);
                if (callback != null)
                    callback.run();
			}
        });
    }

    public void aDeleteMessage(final Message message, final Runnable callback) {
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                deleteMessage(message);
                if (callback != null)
                    callback.run();
			}
        });
    }
}
