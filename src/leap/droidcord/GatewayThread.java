package leap.droidcord;

import java.io.*;
import java.net.*;
import java.util.Vector;

import cc.nnproject.json.*;

public class GatewayThread extends Thread {
	private leap.droidcord.State s;
	public String gateway;
	public int port;
	public String token;

	volatile boolean stop;
	volatile String stopMessage;

	private HeartbeatThread hbThread;

	private Socket sc;
	private InputStream is;
	private OutputStream os;

	public GatewayThread(leap.droidcord.State s, String gateway, String token) {
		this.s = s;
		String[] split = gateway.split(":");
		this.gateway = split[1].replaceAll("socket:", "").replaceAll("/", "");
		this.port = Integer.parseInt(split[2]);
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
		if (s.autoReConnect) {
			s.gateway = new GatewayThread(s, gateway, token);
			s.gateway.start();
		} else {
			//s.disp.setCurrent(new GatewayAlert(s, message), s.disp.getCurrent());
		}
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
                        if (stopMessage != null) disconnected(stopMessage);
                        else disconnect();
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

                if (op != null) {
                    if (op.equals("GATEWAY_HELLO")) {
                        // Connect to gateway
                        JSONArray events = new JSONArray();
                        events.add("MESSAGE_CREATE");
                        events.add("MESSAGE_DELETE");
                        events.add("MESSAGE_UPDATE");
                        events.add("TYPING_START");
                        events.add("GUILD_MEMBERS_CHUNK");

                        JSONObject connData = new JSONObject();
                        connData.put("supported_events", events);
                        connData.put("url", "wss://gateway.discord.gg/?v=9&encoding=json");

                        JSONObject connMsg = new JSONObject();
                        connMsg.put("op", -1);
                        connMsg.put("t", "GATEWAY_CONNECT");
                        connMsg.put("d", connData);
                        send(connMsg);

                        // Remove "Reconnecting" banner message if auto reconnected
                        /*if (s.channelView != null && "Reconnecting".equals(s.channelView.bannerText)) {
                            s.channelView.bannerText = null;
                            s.channelView.repaint();
                        }*/
                    }
                    else if (op.equals("GATEWAY_DISCONNECT")) {
                        String reason = message.getObject("d").getString("message");
                        disconnected(reason);
                        return;
                    }
                    else if (op.equals("MESSAGE_CREATE")) {
                        JSONObject msgData = message.getObject("d");
                        long msgId = Long.parseLong(msgData.getString("id"));
                        long chId = Long.parseLong(msgData.getString("channel_id"));

                        // Mark this channel as unread if it's not the currently opened channel
                        if (
                            !s.channelIsOpen
                            || (s.isDM && !(chId == s.selectedDm.id))
                            || (!s.isDM && !(chId == s.selectedChannel.id))
                        ) {
                            Channel ch = Channel.getByID(s, chId);
                            if (ch != null) {
                                ch.lastMessageID = msgId;
                                s.updateUnreadIndicators(false, chId);
                                continue;
                            }
                            DirectMessage dm = DirectMessage.getById(s, chId);
                            if (dm != null) {
                                dm.lastMessageID = msgId;
                                s.updateUnreadIndicators(true, chId);
                            }
                            continue;
                        }
                        
                        // If message was sent in the currently opened channel, update the channel view accordingly:

                        // If we're on the newest page, make the new message visible
                        // Add the new message to the message list
                        s.messages.insertElementAt(new Message(s, msgData), 0);

                        // Remove the oldest message in the message list so it doesn't break pagination
                        // Except for channels that have less messages than the full page capacity
                        if (s.messages.size() > s.messageLoadCount) {
                            s.messages.removeElementAt(s.messages.size() - 1);
                        }

                        // Remove this user's typing indicator
                        if (s.isDM) {
                            if (s.typingUsers.size() >= 1) {
                                s.typingUsers.removeElementAt(0);
                                s.typingUserIDs.removeElementAt(0);
                            }
                        } else {
                            String authorID = msgData.getObject("author").getString("id");
                            
                            for (int i = 0; i < s.typingUsers.size(); i++) {
                                if (s.typingUserIDs.elementAt(i).equals(authorID)) {
                                    s.typingUsers.removeElementAt(i);
                                    s.typingUserIDs.removeElementAt(i);
                                }
                            }
                        }

                        // Redraw the message list and mark it as read

                        /*if (page == 0) {
                            s.channelView.update(false, true);
                            s.unreads.autoSave = false;
                            s.unreads.markRead(chId, Long.parseLong(msgId));
                            s.unreads.autoSave = true;
                        } else {
                            // If user is not on the newest page of messages, ask them to refresh
                            // There is no easy way to do it any other way without breaking pagination
                            s.channelView.outdated = true;
                        }
                        s.channelView.repaint();*/
                    }
                    else if (op.equals("MESSAGE_DELETE")) {
                        JSONObject msgData = message.getObject("d");

                        String channel = msgData.getString("channel_id", "");
                        long selected = s.isDM ? s.selectedDm.id : s.selectedChannel.id;
                        if (!channel.equals(selected)) continue;

                        long messageId = Long.parseLong(msgData.getString("id"));

                        for (int i = 0; i < s.messages.size(); i++) {
                            Message msg = (Message) s.messages.elementAt(i);
                            if (!(msg.id == messageId)) continue;

                            msg.delete();

                            /*if (s.oldUI) {
                                s.oldChannelView.update();
                            } else {
                                s.channelView.update(false, true);
                                s.channelView.repaint();
                            }*/
                            break;
                        }
                    }
                    else if (op.equals("MESSAGE_UPDATE")) {
                        JSONObject msgData = message.getObject("d");

                        // Check if content was changed (other parts of the message can change too,
                        // but currently we can only update the content)
                        String newContent = msgData.getString("content", null);
                        if (newContent == null) continue;

                        String channel = msgData.getString("channel_id", "");
                        long selected = s.isDM ? s.selectedDm.id : s.selectedChannel.id;
                        if (!channel.equals(selected)) continue;

                        long messageId = Long.parseLong(msgData.getString("id"));

                        for (int i = 0; i < s.messages.size(); i++) {
                            Message msg = (Message) s.messages.elementAt(i);
                            if (!(msg.id == messageId)) continue;

                            msg.content = newContent;
                            msg.needUpdate = true;

                            /*if (s.oldUI) {
                                s.oldChannelView.update();
                            } else {
                                s.channelView.update(false, true);
                                s.channelView.repaint();
                            }*/
                            break;
                        }
                    }
                    else if (op.equals("TYPING_START")) {
                        JSONObject msgData = message.getObject("d");
                        long channel = Long.parseLong(msgData.getString("channel_id"));

                        // Check that the opened channel (if there is any) is the one where the typing event happened
                        if (s.isDM) {
                            if (!(channel == s.selectedDm.id)) continue;
                        } else {
                            if (!(channel == s.selectedChannel.id)) continue;
                        }

                        if (s.isDM) {
                            // Typing events not supported in group DMs (typing event contains guild member info if it happened in a server, but not user info; in a group DM, there's no easy way to know who started typing)
                            if (s.selectedDm.isGroup) continue;

                            // If we are in a one person DM, then we know the typing user is the other participant
                            // If we already have a typing indicator, don't create a dupe
                            if (s.typingUsers.size() >= 1) continue;

                            s.typingUsers.addElement(s.selectedDm.name);
                            s.typingUserIDs.addElement((long) 0);

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
                                if (s.typingUserIDs.indexOf(id) != -1) continue;

                                s.typingUsers.addElement(author);
                                s.typingUserIDs.addElement(id);

                                StopTypingThread stopThread = new StopTypingThread(s, id);
                                stopThread.start();
                            }
                            catch (Exception e) {}
                        }

                        // Redraw the message list
                        /*if (s.oldUI) {
                            s.oldChannelView.update();
                        } else {
                            s.channelView.repaint();
                        }*/
                    }
                    else if (op.equals("GUILD_MEMBERS_CHUNK")) {
                        //if (s.channelView == null || s.selectedGuild == null) continue;

                        JSONObject data = message.getObject("d");
                        JSONArray members = data.getArray("members");

                        /*if (s.disp.getCurrent() instanceof MentionForm) {
                            // Guild member request was for inserting a mention
                            ((MentionForm) s.disp.getCurrent()).searchCallback(members);
                        } else {*/
                            // Guild member request was for role data (name colors)
                            String guildId = data.getString("guild_id");
                            JSONArray notFound = data.getArray("not_found");

                            for (int i = 0; i < notFound.size(); i++) {
                                String id = notFound.getString(i);
                                s.nameColorCache.set(id + guildId, 0);
                            }

                            for (int i = 0; i < members.size(); i++) {
                                int resultColor = 0;

                                JSONObject member = members.getObject(i);
                                JSONArray memberRoles = member.getArray("roles");

                                for (int r = 0; r < s.selectedGuild.roles.size(); r++) {
                                    Role role = (Role) s.selectedGuild.roles.elementAt(r);
                                    if (memberRoles.indexOf(role.id) == -1) continue;

                                    resultColor = role.color;
                                    break;
                                }

                                String id = member.getObject("user").getString("id");
                                s.nameColorCache.set(id + guildId, resultColor);
                            }
                            s.nameColorCache.activeRequest = false;
                        //}
                    }
                }
                else if (message.getInt("op", 0) == 10) {
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
                        os.write((idMsg.build() + "\n").getBytes("UTF-8"));
                    }
                    catch (UnsupportedEncodingException e) {
                        os.write((idMsg.build() + "\n").getBytes());
                    }
                    os.flush();
                }
            }
        } catch (Exception e) {
			disconnected(e.toString());
		}
	}
}