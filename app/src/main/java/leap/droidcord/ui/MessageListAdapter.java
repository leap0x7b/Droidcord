package leap.droidcord.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import leap.droidcord.R;
import leap.droidcord.State;
import leap.droidcord.data.Messages;
import leap.droidcord.model.Attachment;
import leap.droidcord.model.Message;

public class MessageListAdapter extends BaseAdapter {

    private Context context;
    private State s;
    private Messages messages;
    private Drawable defaultAvatar;
    // serve nothing other than preventing calculating the pixel size every time
    // the item is shown on-screen
    private int iconSize;
    private int replyIconSize;

    public MessageListAdapter(Context context, State s, Messages messages) {
        this.context = context;
        this.s = s;
        this.messages = messages;
        this.defaultAvatar = context.getResources().getDrawable(R.drawable.ic_launcher);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
        replyIconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, metrics);
    }

    public Messages getData() {
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
        return messages.get(position).id;
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

        if (message.isStatus) {
            viewHolder.msg.setVisibility(View.GONE);
            viewHolder.status.setVisibility(View.VISIBLE);

            // TODO: integrate GuildInformation with status messages
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

            s.icons.load(viewHolder.avatar, defaultAvatar, message.author, iconSize);
            s.guildInformation.load(viewHolder.author, message.author);
            viewHolder.timestamp.setText(message.timestamp);

            if (TextUtils.isEmpty(message.content))
                viewHolder.content.setVisibility(View.GONE);
            else
                viewHolder.content.setText(message.content);

            if (message.attachments != null && message.attachments.size() > 0) {
                viewHolder.attachments.removeAllViews();
                viewHolder.attachments.setVisibility(View.VISIBLE);
                for (Attachment attachment : message.attachments) {
                    if (attachment.supported) {
                        final ImageView image = new ImageView(context);
                        final LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

                        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                        int bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, metrics);
                        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomMargin);

                        image.setLayoutParams(lp);
                        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        image.setAdjustViewBounds(true);

                        s.attachments.load(image, defaultAvatar, message, attachment);
                        viewHolder.attachments.addView(image);
                    }
                }
            } else {
                viewHolder.attachments.setVisibility(View.GONE);
            }

            if (!message.showAuthor && message.recipient == null) {
                viewHolder.metadata.setVisibility(View.GONE);
                viewHolder.avatar.getLayoutParams().height = 0;
            } else {
                viewHolder.metadata.setVisibility(View.VISIBLE);
                viewHolder.avatar.getLayoutParams().height = iconSize;
            }

            if (message.recipient != null) {
                viewHolder.reply.setVisibility(View.VISIBLE);
                s.icons.load(viewHolder.replyAvatar, defaultAvatar, message.recipient, replyIconSize);
                s.guildInformation.load(viewHolder.replyAuthor, message.recipient);
                viewHolder.replyContent.setText(message.refContent);
            } else {
                viewHolder.reply.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private static class ViewHolder {
        View msg;
        View metadata;
        TextView author;
        TextView timestamp;
        TextView content;
        ImageView avatar;
        LinearLayout attachments;

        View reply;
        TextView replyAuthor;
        TextView replyContent;
        ImageView replyAvatar;

        View status;
        TextView statusText;
        TextView statusTimestamp;

        public ViewHolder(View view) {
            msg = view.findViewById(R.id.message);
            metadata = view.findViewById(R.id.msg_metadata);
            author = (TextView) view.findViewById(R.id.msg_author);
            timestamp = (TextView) view.findViewById(R.id.msg_timestamp);
            content = (TextView) view.findViewById(R.id.msg_content);
            avatar = (ImageView) view.findViewById(R.id.msg_avatar);
            attachments = (LinearLayout) view.findViewById(R.id.msg_attachments);

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
}