package leap.droidcord;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.nnproject.json.JSON;

public class GuildListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private State s;
    private Vector<Guild> guilds;
    // serve nothing other than preventing calculating the pixel size every time
    // the item is shown on-screen
    private int iconSize;

    public GuildListAdapter(Context context, State s, Vector<Guild> guilds) {
        this.context = context;
        this.s = s;
        this.guilds = guilds;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                48 + 0.5f, metrics);
    }

    @Override
    public Object getChild(int position, int childPosition) {
        if (guilds.get(position).channels == null) {
            try {
                /*if (guilds.get(position).roles == null) {
					guilds.get(position).roles = Role.parseRoles(JSON
							.getArray(s.http.get("/guilds/"
									+ guilds.get(position).id + "/roles")));
				}*/
                guilds.get(position).channels = Channel.parseChannels(
                        s,
                        guilds.get(position),
                        JSON.getArray(s.http.get("/guilds/"
                                + guilds.get(position).id + "/channels")));
            } catch (Exception e) {
                return null;
            }
        }
        return guilds.get(position).channels.get(childPosition);
    }

    @Override
    public long getChildId(int position, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int position, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final Channel channel = (Channel) getChild(position, childPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.channel_list_item,
                    null);
        }

        TextView textView = (TextView) convertView
                .findViewById(R.id.channel_item_name);
        textView.setText(channel.toString());

        return convertView;
    }

    @Override
    public int getChildrenCount(int position) {
        if (guilds.get(position).channels == null) {
            try {
				/*if (guilds.get(position).roles == null) {
					guilds.get(position).roles = Role.parseRoles(JSON
							.getArray(s.http.get("/guilds/"
									+ guilds.get(position).id + "/roles")));
				}*/
                guilds.get(position).channels = Channel.parseChannels(
                        s,
                        guilds.get(position),
                        JSON.getArray(s.http.get("/guilds/"
                                + guilds.get(position).id + "/channels")));
            } catch (Exception e) {
                return 0;
            }
        }
        return guilds.get(position).channels.size();
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
        final Guild guild = (Guild) getGroup(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater
                    .inflate(R.layout.guild_list_item, null);
        }

        final ImageView imageView = (ImageView) convertView
                .findViewById(R.id.guild_item_icon);
        imageView.setImageDrawable(context.getResources().getDrawable(
                R.drawable.ic_launcher));

        String format = (s.useJpeg ? "jpg" : "png");
        String type = guild.getIconType();
        long id = guild.getIconID();
        String hash = guild.getIconHash();
        imageView.setTag(s.cdn + type + id + "/" + hash + "." + format
                + "?size=" + iconSize);
        LoadImage loadImage = new LoadImage(imageView);
        loadImage.call();

        TextView textView = (TextView) convertView
                .findViewById(R.id.guild_item_name);
        textView.setText(guild.name);

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

    public class LoadImage implements Callable<Void> {

        private String url;
        private ImageView imageView;

        LoadImage(ImageView imageView) {
            this.imageView = imageView;
            this.url = imageView.getTag().toString();
        }

        @Override
        public Void call() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            final Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Bitmap icon = s.http.getImage(url);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!imageView.getTag().toString().equals(url)) {
                                    return;
                                }
                                if (icon != null) {
                                    imageView.setImageBitmap(icon);
                                } else {
                                    imageView.setImageDrawable(context
                                            .getResources().getDrawable(
                                                    R.drawable.ic_launcher));
                                }
                            }
                        });
                    } catch (IOException e) {
                    }
                }
            });
            return null;
        }

    }
}