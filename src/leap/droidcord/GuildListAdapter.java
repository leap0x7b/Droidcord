package leap.droidcord;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cc.nnproject.json.JSON;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GuildListAdapter extends BaseExpandableListAdapter {

	private Context context;
	private State s;
	private Vector<Guild> guilds;

	public GuildListAdapter(Context context, State s, Vector<Guild> guilds) {
		this.context = context;
		this.s = s;
		this.guilds = guilds;
	}

	@Override
	public Object getChild(int position, int childPosition) {
		try {
			return Channel.parseChannels(
					JSON.getArray(s.http.get("/guilds/"
							+ guilds.get(position).id + "/channels")))
					.get(childPosition);
		} catch (Exception e) {
			return null;
		}
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
			LayoutInflater layoutInflater = (LayoutInflater) this.context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.channel_list_item,
					null);
		}
		if (channel != null) {
			TextView textView = (TextView) convertView
					.findViewById(R.id.channel_item_name);
			textView.setText(channel.toString());
		}
		return convertView;
	}

	@Override
	public int getChildrenCount(int position) {
		try {
			return Channel.parseChannels(
					JSON.getArray(s.http.get("/guilds/"
							+ guilds.get(position).id + "/channels")))
					.size();
		} catch (Exception e) {
			return 0;
		}
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
			LayoutInflater layoutInflater = (LayoutInflater) this.context
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
		imageView.setTag(s.cdn + type + id + "/" + hash + "." + format + "?size=48");
		LoadImage loadImage = new LoadImage(imageView);
		loadImage.call();

		TextView textView = (TextView) convertView
				.findViewById(R.id.guild_item_name);
		textView.setText(guild.name);

		return convertView;
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
						    		imageView.setImageDrawable(context.getResources().getDrawable(
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

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int position, int childPosition) {
		return true;
	}
}