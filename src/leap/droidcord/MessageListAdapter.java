package leap.droidcord;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListAdapter extends BaseAdapter {

	private Context context;
	private State s;
	private Vector<Message> messages;
	// serve nothing other than preventing calculating the pixel size every time
	// the item is shown on-screen
	private int iconSize;

	public MessageListAdapter(Context context, State s, Vector<Message> messages) {
		this.context = context;
		this.s = s;
		this.messages = messages;

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				48 + 0.5f, metrics);
	}

	@Override
	public Object getItem(int position) {
		return messages.get(position);
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Message message = (Message) getItem(position);
		if (convertView == null) {
			LayoutInflater layoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = layoutInflater.inflate(R.layout.message, null);
		}

		final ImageView avatar = (ImageView) convertView
				.findViewById(R.id.msg_avatar);
		avatar.setImageDrawable(context.getResources().getDrawable(
				R.drawable.ic_launcher));

		String format = (s.useJpeg ? "jpg" : "png");
		String type = message.author.getIconType();
		long id = message.author.getIconID();
		String hash = message.author.getIconHash();
		avatar.setTag(s.cdn + type + id + "/" + hash + "." + format
				+ "?size=" + iconSize);
		LoadImage loadImage = new LoadImage(avatar);
		loadImage.call();

		TextView author = (TextView) convertView.findViewById(R.id.msg_author);
		TextView timestamp = (TextView) convertView.findViewById(R.id.msg_timestamp);
		TextView content = (TextView) convertView.findViewById(R.id.msg_content);
		author.setText(message.author.name);
		timestamp.setText(message.timestamp);
		content.setText(message.content);

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

	@Override
	public boolean hasStableIds() {
		return false;
	}
}