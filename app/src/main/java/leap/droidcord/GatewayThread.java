package leap.droidcord;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Vector;

import leap.droidcord.model.Channel;
import leap.droidcord.model.DirectMessage;
import leap.droidcord.model.Guild;
import leap.droidcord.model.Message;
import leap.droidcord.model.Role;

import android.util.Log;
import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class GatewayThread extends Thread {
    public String gateway;
    public int port;
    public String token;
    volatile boolean stop;
    volatile String stopMessage;
    private leap.droidcord.State s;
    private HeartbeatThread hbThread;

    private Socket sc;
    private InputStream is;
    private OutputStream os;

    public GatewayThread(leap.droidcord.State s, String gateway, String token) {
        this.s = s;
        String[] split = gateway.replaceAll("socket:", "").replaceAll("/", "").split(":");
        this.gateway = split[0];
        this.port = Integer.parseInt(split[1]);
        this.token = token;

        s.subscribedGuilds = new Vector<Guild>();
    }

    private void disconnect() {
        if (hbThread != null)
            hbThread.stop = true;
        try {
            if (is != null)
                is.close();
            if (os != null)
                os.close();
            if (sc != null)
                sc.close();
        } catch (Exception ee) {
        }
    }

    public void disconnected(String message) {
        disconnect();
        //if (s.autoReConnect) {
        s.gateway = new GatewayThread(s, gateway + ":" + port, token);
        s.gateway.start();
        /*} else {
			//s.disp.setCurrent(new GatewayAlert(s, message), s.disp.getCurrent());
		}*/
    }

    public void send(JSONObject msg) {
        try {
            os.write((msg.build() + "\n").getBytes());
            os.flush();
        } catch (Exception e) {
        }
    }

    public void run() {
        try {
            sc = new Socket(gateway, port);
            sc.setKeepAlive(true);

            is = sc.getInputStream();
            os = sc.getOutputStream();

            StringBuffer sb = new StringBuffer();
            String msgStr;

            while (true) {
                // Get message
                while (true) {
                    if (stop) {
                        if (stopMessage != null)
                            disconnected(stopMessage);
                        else
                            disconnect();
                        return;
                    }

                    int ch = is.read();
                    if (ch == '\n' || ch == -1) {
                        if (sb.length() > 0) {
                            // This message has been fully received, start processing it
                            msgStr = new String(sb.toString().getBytes(), "UTF-8");
                            sb = new StringBuffer();
                            break;
                        }
                    } else {
                        sb.append((char) ch);
                    }
                }

                // Process message
                JSONObject message = JSON.getObject(msgStr);
                String op = message.getString("t", "");

                // Save message sequence number (used for heartbeats)
                int seq = message.getInt("s", -1);
                if (hbThread != null && seq > hbThread.lastReceived) {
                    hbThread.lastReceived = seq;
                }

                if ("GATEWAY_HELLO".equals(op)) {
                    handleGatewayHello();
                } else if ("GATEWAY_DISCONNECT".equals(op)) {
                    handleGatewayDisconnect(message);
                    return;
                    // TODO: change these from J2ME_ to DROIDCORD_ after a PR in the
                    //       gateway proxy for Droidcord compatibility parity with
                    //       the API proxy get merged.
                } else if ("J2ME_MESSAGE_CREATE".equals(op)) {
                    handleGatewayMessageCreate(message);
                } else if ("MESSAGE_DELETE".equals(op)) {
                    handleGatewayMessageDelete(message);
                } else if ("J2ME_MESSAGE_UPDATE".equals(op)) {
                    handleGatewayMessageUpdate(message);
                } else if ("TYPING_START".equals(op)) {
                    handleGatewayTypingStart(message);
                } else if ("GUILD_MEMBERS_CHUNK".equals(op)) {
                    handleGatewayGuildMembersChunk(message);
                } else if ("J2ME_READY".equals(op)) {
                    handleGatewayReady(message);
                } else if (message.getInt("op", 0) == 10) {
                    handleGatewayIdentify(message);
                }
            }
        } catch (Exception e) {
            disconnected(e.toString());
        }
    }

    private void handleGatewayHello() {
        JSONArray events = new JSONArray();
        // TODO: change these from J2ME_ to DROIDCORD_ after a PR in the
        //       gateway proxy for Droidcord compatibility parity with
        //       the API proxy get merged.
        events.add("J2ME_MESSAGE_CREATE");
        events.add("MESSAGE_DELETE");
        events.add("J2ME_MESSAGE_UPDATE");
        events.add("TYPING_START");
        events.add("GUILD_MEMBERS_CHUNK");
        events.add("J2ME_READY");

        JSONObject data = new JSONObject();
        data.put("supported_events", events);
        data.put("url", "wss://gateway.discord.gg/?v=9&encoding=json");

        JSONObject message = new JSONObject();
        message.put("op", -1);
        message.put("t", "GATEWAY_CONNECT");
        message.put("d", data);
        send(message);
    }

    private void handleGatewayDisconnect(JSONObject message) {
        String reason = message.getObject("d").getString("message");
        disconnected(reason);
    }

    private void handleGatewayMessageCreate(JSONObject message) {
        JSONObject msgData = message.getObject("d");
        long msgId = Long.parseLong(msgData.getString("id"));
        long chId = Long.parseLong(msgData.getString("channel_id"));

        // Mark this channel as unread if it's not the currently opened channel
        if (!s.channelIsOpen
                || (s.isDM && !(chId == s.selectedDm.id))
                || (!s.isDM && !(chId == s.selectedChannel.id))) {
            Channel ch = Channel.getByID(s, chId);
            if (ch != null) {
                ch.lastMessageID = msgId;
                s.updateUnreadIndicators(false, chId);
                return;
            }
            DirectMessage dm = DirectMessage.getById(s, chId);
            if (dm != null) {
                dm.lastMessageID = msgId;
                s.updateUnreadIndicators(true, chId);
            }
            return;
        }

        s.messages.add(new Message(s, msgData));

        // Remove this user's typing indicator
        if (s.isDM) {
            if (s.typingUsers.size() >= 1) {
                s.typingUsers.remove(0);
                s.typingUserIDs.remove(0);
            }
        } else {
            Long authorID = Long.parseLong(msgData.getObject("author").getString("id"));

            for (int i = 0; i < s.typingUsers.size(); i++) {
                if (s.typingUserIDs.get(i).equals(authorID)) {
                    s.typingUsers.remove(i);
                    s.typingUserIDs.remove(i);
                }
            }
        }

        s.runOnUiThread(new Runnable() {
		    @Override
			public void run() {
                s.messages.cluster();
                s.messagesAdapter.notifyDataSetChanged();
                s.messagesView.setSelection(s.messagesAdapter.getCount() - 1);
                s.messagesView.invalidate();
			}
        });
    }

    private void handleGatewayMessageDelete(JSONObject message) {
        JSONObject msgData = message.getObject("d");

        Long channel = Long.parseLong(msgData.getString("channel_id", "0"));
        Long selected = s.isDM ? s.selectedDm.id : s.selectedChannel.id;
        if (!channel.equals(selected))
            return;

        long messageId = Long.parseLong(msgData.getString("id"));

        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.get(i);
            if (!(msg.id == messageId))
                continue;

            msg.delete();
            break;
        }
    }

    private void handleGatewayMessageUpdate(JSONObject message) {
        JSONObject msgData = message.getObject("d");

        // Check if content was changed (other parts of the message can change too,
        // but currently we can only update the content)
        String newContent = msgData.getString("content", null);
        if (newContent == null)
            return;

        Long channel = Long.parseLong(msgData.getString("channel_id", "0"));
        long selected = s.isDM ? s.selectedDm.id : s.selectedChannel.id;
        if (!channel.equals(selected))
            return;

        long messageId = Long.parseLong(msgData.getString("id"));

        for (int i = 0; i < s.messages.size(); i++) {
            Message msg = (Message) s.messages.get(i);
            if (!(msg.id == messageId))
                continue;

            msg.content = newContent;
            msg.needUpdate = true;

            break;
        }
    }

    private void handleGatewayTypingStart(JSONObject message) {
        JSONObject msgData = message.getObject("d");
        long channel = Long.parseLong(msgData.getString("channel_id"));

        // Check that the opened channel (if there is any) is the one where the typing
        // event happened
        if (s.isDM) {
            if (!(channel == s.selectedDm.id))
                return;
        } else {
            if (!(channel == s.selectedChannel.id))
                return;
        }

        if (s.isDM) {
            // Typing events not supported in group DMs (typing event contains guild member
            // info if it happened in a server, but not user info; in a group DM, there's no
            // easy way to know who started typing)
            if (s.selectedDm.isGroup)
                return;

            // If we are in a one person DM, then we know the typing user is the other
            // participant
            // If we already have a typing indicator, don't create a dupe
            if (s.typingUsers.size() >= 1)
                return;

            s.typingUsers.add(s.selectedDm.name);
            s.typingUserIDs.add((long) 0);

            // Remove the name from the typing list after 10 seconds
            StopTypingThread stopThread = new StopTypingThread(s, 0);
            stopThread.start();
        } else {
            try {
                // Get this user's name and add it to the typing users list
                JSONObject userObj = msgData.getObject("member").getObject("user");

                String author = userObj.getString("global_name", null);
                if (author == null) {
                    author = userObj.getString("username", "(no name)");
                }

                // If this user is already in the list, don't add them again
                long id = Long.parseLong(userObj.getString("id"));
                if (s.typingUserIDs.indexOf(id) != -1)
                    return;

                s.typingUsers.add(author);
                s.typingUserIDs.add(id);

                StopTypingThread stopThread = new StopTypingThread(s, id);
                stopThread.start();
            } catch (Exception e) {
            }
        }
    }

    private void handleGatewayGuildMembersChunk(JSONObject message) {
        if (s.selectedGuild == null)
            return;

        JSONObject data = message.getObject("d");
        JSONArray members = data.getArray("members");

        String guildId = data.getString("guild_id");
        JSONArray notFound = data.getArray("not_found");
        for (int i = 0; i < notFound.size(); i++) {
            String id = notFound.getString(i);
            s.guildInformation.set(id + guildId, 0, null);
        }

        for (int i = 0; i < members.size(); i++) {
            int resultColor = 0;

            JSONObject member = members.getObject(i);
            Log.w("GatewayThread", "Member: " + member.toString());
            String id = member.getObject("user").getString("id");

            String nickname = member.getString("nick", null);
            if (nickname == null)
                nickname = member.getString("display_name", null);

            JSONArray memberRoles = member.getArray("roles");

            for (int r = 0; r < s.selectedGuild.roles.size(); r++) {
                Role role = (Role) s.selectedGuild.roles.get(r);
                Long roleId = role.id;
                if (memberRoles.indexOf(roleId.toString()) == -1 || role.color == 0)
                    continue;
                resultColor = role.color;
                break;
            }

            if (resultColor == 0)
                resultColor = 0xFFFFFFFF;

            s.guildInformation.set(id + guildId, resultColor, nickname);
        }

        s.guildInformation.activeRequest = false;
    }

    private void handleGatewayReady(JSONObject message) {
        if (s.myUserId == 0)
            s.myUserId = Long.parseLong(message.getObject("d").getString("id"));
    }

    private void handleGatewayIdentify(JSONObject message) {
        int heartbeatInterval = message.getObject("d").getInt("heartbeat_interval");
        hbThread = new HeartbeatThread(s, os, heartbeatInterval);
        hbThread.start();

        // Identify
        JSONObject idProps = new JSONObject();
        idProps.put("os", "Linux");
        idProps.put("browser", "Firefox");
        idProps.put("device", "");

        JSONObject idData = new JSONObject();
        idData.put("token", token.trim());
        idData.put("capabilities", 30717);
        idData.put("properties", idProps);

        JSONObject idMsg = new JSONObject();
        idMsg.put("op", 2);
        idMsg.put("d", idData);

        try {
            try {
                os.write((idMsg.toString() + "\n").getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                os.write((idMsg.toString() + "\n").getBytes());
            }
            os.flush();
        } catch (Exception e) {
            Log.e("GatewayThread", "Error sending identify message: " + e.toString());
        }
    }
}
