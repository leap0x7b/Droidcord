package leap.droidcord.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

public class Message extends LinearLayout implements OnClickListener {

    private ImageView mAvatar;
    private TextView mAuthor;
    private TextView mTimestamp;
    private TextView mContent;
    private Embed mEmbed;
    
    private ImageView mAttachmentImage;
    private VideoView mAttachmentVideo;

    private View mReply;
    private ImageView mReplyAvatar;
    private TextView mReplyAuthor;
    private TextView mReplyContent;
	
	public Message(Context context) {
		super(context);
	}

	public Message(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        /*mMsgListItem = findViewById(R.id.msg_list_item);
        mBodyTextView = (TextView) findViewById(R.id.text_view);
        mRightStatusIndicator = (ImageView) findViewById(R.id.right_status_indicator);*/
    }
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
