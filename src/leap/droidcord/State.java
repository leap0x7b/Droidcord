package leap.droidcord;

import java.util.*;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class State {
	static final int ICON_TYPE_NONE = 0;
	static final int ICON_TYPE_SQUARE = 1;
	static final int ICON_TYPE_CIRCLE = 2;
	static final int ICON_TYPE_CIRCLE_HQ = 3;

	static final int TOKEN_TYPE_HEADER = 0;
	static final int TOKEN_TYPE_JSON = 1;
	static final int TOKEN_TYPE_QUERY = 2;

	boolean use12hTime;
	boolean useGateway;
	int messageLoadCount;
	boolean useJpeg;
	int attachmentSize;
	int iconType;
	boolean autoReConnect;
	boolean showMenuIcons;
	int tokenType;
	boolean useNameColors;
	boolean showRefMessage;

	Context c;

	HTTPThing http;
	GatewayThread gateway;
	String cdn;

	long myUserId;
	boolean isLiteProxy;

	IconCache iconCache;
	NameColorCache nameColorCache;
	UnreadManager unreads;

	Vector<Guild> guilds;
	Guild selectedGuild;
	// GuildSelector guildSelector;
	Vector<Guild> subscribedGuilds;

	Vector<Channel> channels;
	Channel selectedChannel;
	// ChannelSelector channelSelector;
	boolean channelIsOpen;

	Vector<Message> messages;
	Vector<String> typingUsers;
	Vector<Long> typingUserIDs;

	// Parameters for message/reply sending
	String sendMessage;
	String sendReference; // ID of the message the user is replying to
	boolean sendPing;

	boolean isDM;
	Vector<DirectMessage> directMessages;
	DirectMessage selectedDm;

	int sendHotkey;
	int replyHotkey;
	int copyHotkey;
	int refreshHotkey;
	int backHotkey;

	public State(Context c) {
		subscribedGuilds = new Vector<Guild>();
		iconCache = new IconCache(this);
		nameColorCache = new NameColorCache(this);
		unreads = new UnreadManager(this, c);
	}

	public void login(String api, String gateway, String cdn, String token) {
		this.cdn = cdn;
		http = new HTTPThing(this, api, token);

		if (useGateway) {
			this.gateway = new GatewayThread(this, gateway, token);
			this.gateway.start();
		}
	}

	/*private Alert createError(String message) {
		Alert error = new Alert("Error");
		error.setTimeout(Alert.FOREVER);
		error.setString(message);
		return error;
	}*/

	public void error(String message) {
		/*Toast toast = Toast.makeText(c, "Error: " + message, Toast.LENGTH_LONG);
		toast.show();*/
		System.out.println(message);
	}

	public boolean gatewayActive() {
		return gateway != null && gateway.isAlive();
	}

	public void updateUnreadIndicators(boolean isDM, long chId) {
		/*if (isDM) {
			if (dmSelector != null) dmSelector.update(chId);
		} else {
			if (channelSelector != null) channelSelector.update(chId);
			if (guildSelector != null) guildSelector.update();
		}*/
	}

	public void openGuildSelector(boolean reload) {
		/*try {
			if (reload || guildSelector == null || guilds == null) {
				new HTTPThread(this, HTTPThread.FETCH_GUILDS).start();
			} else {
				disp.setCurrent(guildSelector);
			}
		}
		catch (Exception e) {
			error(e.toString());
		}*/
	}

	public void openChannelSelector(boolean reload) {
		/*try {
			if (!reload && channelSelector != null && channels != null && channels == selectedGuild.channels) {
				disp.setCurrent(channelSelector);
			}
			else if (!reload && selectedGuild.channels != null) {
				channels = selectedGuild.channels;
				channelSelector = new ChannelSelector(this);
				disp.setCurrent(channelSelector);
			}
			else {
				new HTTPThread(this, HTTPThread.FETCH_CHANNELS).start();
			}
		}
		catch (Exception e) {
			error(e.toString());
		}*/
	}

	public void openDMSelector(boolean reload) {
		/*try {
			if (reload || dmSelector == null || dmChannels == null) {
				new HTTPThread(this, HTTPThread.FETCH_DM_CHANNELS).start();
			} else {
				disp.setCurrent(dmSelector);
			}
		}
		catch (Exception e) {
			error(e.toString());
		}*/
	}

	public void openChannelView(boolean reload) {
		/*try {
			if (oldUI) {
				if (reload || oldChannelView == null || messages == null) {
					new HTTPThread(this, HTTPThread.FETCH_MESSAGES).start();
				} else {
					disp.setCurrent(oldChannelView);
				}
			} else {
				if (reload || channelView == null || messages == null) {
					new HTTPThread(this, HTTPThread.FETCH_MESSAGES).start();
				} else {
					disp.setCurrent(channelView);
				}
			}
			if (isDM) {
				unreads.markRead(selectedDmChannel);
				updateUnreadIndicators(true, selectedDmChannel.id);
			} else {
				unreads.markRead(selectedChannel);
				updateUnreadIndicators(false, selectedChannel.id);
			}
		}
		catch (Exception e) {
			error(e.toString());
		}*/
	}

	public void openAttachmentView(boolean reload, Message msg) {
		/*try {
			if (reload || attachmentView == null || attachmentView.msg != msg) {
				attachmentView = new AttachmentView(this, msg);
			}
			disp.setCurrent(attachmentView);
		}
		catch (Exception e) {
			error(e.toString());
		}*/
	}

	public void platformRequest(String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		c.startActivity(browserIntent);
	}
}