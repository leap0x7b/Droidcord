package leap.droidcord.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

import leap.droidcord.R;
import leap.droidcord.State;
import leap.droidcord.model.Channel;
import leap.droidcord.model.Guild;
import leap.droidcord.model.GuildMember;
import leap.droidcord.model.Role;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class GuildListAdapter extends BaseExpandableListAdapter {

    private Activity activity;
    private Context context;
    private State s;
    private Vector<Guild> guilds;
    private Drawable defaultAvatar;
    private int iconSize;

    public GuildListAdapter(Activity activity, Context context, State s, Vector<Guild> guilds) {
        this.activity = activity;
        this.context = context;
        this.s = s;
        this.guilds = guilds;
        this.defaultAvatar = context.getResources().getDrawable(R.drawable.ic_launcher);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
    }

    private Vector<Channel> getChannelsFor(final Guild guild) {
        if (guild.channels != null)
            return guild.channels;

        showProgress(true);
        s.executor.execute(new Runnable() {
			@Override
			public void run() {
                try {
                    if (guild.roles == null)
                        guild.roles = Role.parseRoles(JSON.getArray(s.http.get("/guilds/" + guild.id + "/roles?droidcord=1")));

                    if (guild.me == null)
                        guild.me = new GuildMember(s, guild, JSON.getObject(s.http.get("/guilds/" + guild.id + "/members/" + s.myUserId)));

                    guild.channels = Channel.parseChannels(s, guild, JSON.getArray(s.http.get("/guilds/" + guild.id + "/channels")));

                    s.runOnUiThread(new Runnable() {
						@Override
			            public void run() {
                            notifyDataSetChanged();
                            showProgress(false);
						}
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
			}
        });

        return new Vector<Channel>();
    }

    @Override
    public Object getChild(int position, int childPosition) {
        return getChannelsFor(guilds.get(position)).get(childPosition);
    }

    @Override
    public long getChildId(int position, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int position, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder viewHolder;
        final Channel channel = (Channel) getChild(position, childPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.channel_list_item, null);

            viewHolder = new ChildViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.channel_item_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ChildViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(channel.toString());

        return convertView;
    }

    @Override
    public int getChildrenCount(int position) {
        return getChannelsFor(guilds.get(position)).size();
    }

    @Override
    public Object getGroup(int position) {
        return guilds.get(position);
    }

    @Override
    public int getGroupCount() {
        return guilds.size();
    }

    @Override
    public long getGroupId(int position) {
        return position;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupViewHolder viewHolder;
        final Guild guild = (Guild) getGroup(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.guild_list_item, null);

            viewHolder = new GroupViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.guild_item_icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.guild_item_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder) convertView.getTag();
        }

        s.icons.load(viewHolder.icon, defaultAvatar, guild, iconSize);
        viewHolder.name.setText(guild.name);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int position, int childPosition) {
        return true;
    }

    private void showProgress(final boolean show) {
        ((Activity) context).setProgressBarVisibility(show);
        ((Activity) context).setProgressBarIndeterminate(show);
    }

    private static class ChildViewHolder {
        TextView name;
    }

    private static class GroupViewHolder {
        ImageView icon;
        TextView name;
    }
}
