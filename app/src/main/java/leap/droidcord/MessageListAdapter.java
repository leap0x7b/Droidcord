package leap.droidcord;

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

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageListAdapter extends BaseAdapter {

    private Context context;
    private State s;
    private Vector<Message> messages;
    // serve nothing other than preventing calculating the pixel size every time
    // the item is shown on-screen
    private int iconSize;
    private int replyIconSize;

    public MessageListAdapter(Context context, State s, Vector<Message> messages) {
        this.context = context;
        this.s = s;
        this.messages = messages;
        Collections.reverse(this.messages);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                48 + 0.5f, metrics);
        replyIconSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16 + 0.5f, metrics);
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
        ViewHolder viewHolder;
        Message message = (Message) getItem(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.message, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.author.setText(message.author.name);
        viewHolder.timestamp.setText(message.timestamp);
        viewHolder.content.setText(message.content);

        if (!message.showAuthor && message.recipient == null) {
            viewHolder.metadata.setVisibility(View.GONE);
            viewHolder.avatar.getLayoutParams().height = 0;
        } else {
            viewHolder.metadata.setVisibility(View.VISIBLE);
            viewHolder.avatar.getLayoutParams().height = iconSize;
        }

        viewHolder.avatar.setImageDrawable(context.getResources().getDrawable(
                R.drawable.ic_launcher));

        String format = (s.useJpeg ? "jpg" : "png");
        String type = message.author.getIconType();
        long id = message.author.getIconID();
        String hash = message.author.getIconHash();
        viewHolder.avatar.setTag(s.cdn + type + id + "/" + hash + "." + format
                + "?size=" + iconSize);
        LoadImage loadImage = new LoadImage(viewHolder.avatar);
        loadImage.call();

        if (message.recipient != null) {
            viewHolder.reply.setVisibility(View.VISIBLE);
            viewHolder.replyAuthor.setText(message.recipient.name);
            viewHolder.replyContent.setText(message.refContent);

            viewHolder.replyAvatar.setImageDrawable(context.getResources()
                    .getDrawable(R.drawable.ic_launcher));

            String recipientType = message.recipient.getIconType();
            long recipientId = message.recipient.getIconID();
            String recipientHash = message.recipient.getIconHash();
            viewHolder.replyAvatar.setTag(s.cdn + recipientType + recipientId
                    + "/" + recipientHash + "." + format + "?size="
                    + replyIconSize);
            LoadImage recipientLoadImage = new LoadImage(viewHolder.replyAvatar);
            recipientLoadImage.call();
        } else {
            viewHolder.reply.setVisibility(View.GONE);
        }

        if (message.isStatus) {
            viewHolder.msg.setVisibility(View.GONE);
            viewHolder.status.setVisibility(View.VISIBLE);

            SpannableStringBuilder sb = new SpannableStringBuilder(
                    message.author.name + " " + message.content);
            sb.setSpan(new StyleSpan(Typeface.BOLD), 0,
                    message.author.name.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            viewHolder.statusText.setText(sb);
            viewHolder.statusTimestamp.setText(message.timestamp);
        } else {
            viewHolder.msg.setVisibility(View.VISIBLE);
            viewHolder.status.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private static class ViewHolder {
        View msg;
        TextView author;
        TextView timestamp;
        TextView content;
        ImageView avatar;
        View metadata;
        View reply;
        TextView replyAuthor;
        TextView replyContent;
        ImageView replyAvatar;
        View status;
        TextView statusText;
        TextView statusTimestamp;

        public ViewHolder(View view) {
            msg = view.findViewById(R.id.message);
            author = (TextView) view.findViewById(R.id.msg_author);
            timestamp = (TextView) view.findViewById(R.id.msg_timestamp);
            content = (TextView) view.findViewById(R.id.msg_content);
            avatar = (ImageView) view.findViewById(R.id.msg_avatar);
            metadata = view.findViewById(R.id.msg_metadata);
            reply = view.findViewById(R.id.msg_reply);
            replyAuthor = (TextView) view.findViewById(R.id.reply_author);
            replyContent = (TextView) view.findViewById(R.id.reply_content);
            replyAvatar = (ImageView) view.findViewById(R.id.reply_avatar);
            status = view.findViewById(R.id.status);
            statusText = (TextView) view.findViewById(R.id.status_text);
            statusTimestamp = (TextView) view
                    .findViewById(R.id.status_timestamp);
        }
    }

    private class LoadImage implements Callable<Void> {

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