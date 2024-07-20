package leap.droidcord;

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
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
		Collections.reverse(this.messages);

		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				48 + 0.5f, metrics);
	}

	public Vector<Message> getData() {
		return this.messages;
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
		avatar.setTag(s.cdn + type + id + "/" + hash + "." + format + "?size="
				+ iconSize);
		LoadImage loadImage = new LoadImage(avatar);
		loadImage.call();

		View msg = (View) convertView.findViewById(R.id.message);
		TextView author = (TextView) convertView.findViewById(R.id.msg_author);
		TextView timestamp = (TextView) convertView
				.findViewById(R.id.msg_timestamp);
		TextView content = (TextView) convertView
				.findViewById(R.id.msg_content);
		
		author.setText(message.author.name);
		timestamp.setText(message.timestamp);
		content.setText(message.content);
		/*if (!message.showAuthor) {
			View metadata = convertView.findViewById(R.id.msg_metadata);
			metadata.setVisibility(View.GONE);
			avatar.getLayoutParams().height = 0;
		}*/
		
		View reply = (View) convertView.findViewById(R.id.msg_reply);
		TextView replyAuthor = (TextView) convertView.findViewById(R.id.reply_author);
		TextView replyContent = (TextView) convertView
				.findViewById(R.id.reply_content);

		View status = (View) convertView.findViewById(R.id.status);
		TextView statusText = (TextView) convertView
				.findViewById(R.id.status_text);
		TextView statusTimestamp = (TextView) convertView
				.findViewById(R.id.status_timestamp);
		
		if (message.isStatus) {
			msg.setVisibility(View.GONE);
			status.setVisibility(View.VISIBLE);

			SpannableStringBuilder sb = new SpannableStringBuilder(message.author.name + " " + message.content);
			sb.setSpan(new StyleSpan(Typeface.BOLD), 0, message.author.name.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			statusText.setText(sb);
			statusTimestamp.setText(message.timestamp);
		} 

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