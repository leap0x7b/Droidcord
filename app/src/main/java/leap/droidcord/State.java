package leap.droidcord;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import leap.droidcord.data.API;
import leap.droidcord.data.Attachments;
import leap.droidcord.data.GuildInformation;
import leap.droidcord.data.Icons;
import leap.droidcord.data.Messages;
import leap.droidcord.data.Presences;
import leap.droidcord.model.Channel;
import leap.droidcord.model.DirectMessage;
import leap.droidcord.model.Guild;
import leap.droidcord.ui.GuildListAdapter;
import leap.droidcord.ui.DMListAdapter;
import leap.droidcord.ui.MessageListAdapter;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONObject;

public class State {
    public static final int ICON_TYPE_NONE = 0;
    public static final int ICON_TYPE_SQUARE = 1;
    public static final int ICON_TYPE_CIRCLE = 2;
    public static final int ICON_TYPE_CIRCLE_HQ = 3;

    public static final int TOKEN_TYPE_HEADER = 0;
    public static final int TOKEN_TYPE_JSON = 1;
    public static final int TOKEN_TYPE_QUERY = 2;

    public boolean use12hTime;
    public boolean useGateway;
    public int messageLoadCount;
    public boolean useJpeg;
    public int attachmentSize;
    public int iconType;
    public boolean autoReConnect;
    public boolean showMenuIcons;
    public int tokenType;
    public boolean useNameColors;
    public boolean showRefMessage;

    public Context c;

    public HTTP http;
    public API api;
    public GatewayThread gateway;
    public String cdn;

    public long myUserId;
    public boolean isLiteProxy;

    public Icons icons;
    public Attachments attachments;
    public GuildInformation guildInformation;
    public Presences presences;
    public UnreadManager unreads;

    public Vector<Guild> guilds;
    public Guild selectedGuild;
    public Vector<Guild> subscribedGuilds;

    public GuildListAdapter guildsAdapter;
    public ExpandableListView guildsView;

    public Vector<Channel> channels;
    public Channel selectedChannel;
    public boolean channelIsOpen;

    public Messages messages;
    public Vector<String> typingUsers;
    public Vector<Long> typingUserIDs;

    public MessageListAdapter messagesAdapter;
    public ListView messagesView;

    // Parameters for message/reply sending
    public String sendMessage;
    public long sendReference; // ID of the message the user is replying to
    public boolean sendPing;

    public boolean isDM;
    public Vector<DirectMessage> directMessages;
    public DirectMessage selectedDm;

    public DMListAdapter dmsAdapter;
    public ListView dmsView;

    public ExecutorService executor;
    public Handler handler;

    public State(Context c) {
        this.executor = Executors.newFixedThreadPool(10);
        this.handler = new Handler(Looper.getMainLooper());
        this.api = new API(this);
        this.subscribedGuilds = new Vector<Guild>();
        this.icons = new Icons(this);
        this.attachments = new Attachments(this);
        this.guildInformation = new GuildInformation(this);
        this.presences = new Presences(this);
        this.unreads = new UnreadManager(this, c);
        this.guilds = new Vector<Guild>();
        this.channels = new Vector<Channel>();
        this.directMessages = new Vector<DirectMessage>();
        this.messages = new Messages();
    }

    public void login(String api, String gateway, String cdn, String token) {
        this.cdn = cdn;
        this.http = new HTTP(this, api, token);

        try {
            JSONObject me = JSON.getObject(this.http.get("/users/@me"));
            this.myUserId = Long.parseLong(me.getString("id"));
        } catch (Exception e) {
            this.error("Error getting user ID: " + e.getMessage());
            e.printStackTrace();
        }

        if (useGateway) {
            this.gateway = new GatewayThread(this, gateway, token);
            this.gateway.start();
        }
    }

    public void error(String message) {
        try {
            Toast toast = Toast.makeText(c, "Error: " + message, Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e) {
            System.err.println("Error: " + message);
        }
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

    public void platformRequest(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        c.startActivity(browserIntent);
    }

    public void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
    }
}