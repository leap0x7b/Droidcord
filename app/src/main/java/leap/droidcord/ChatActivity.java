package leap.droidcord;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import leap.droidcord.ui.MessageListAdapter;

public class ChatActivity extends Activity {
    int page;
    long before;
    long after;
    private State s;
    private Context context;
    private EditText mMsgComposer;
    private Button mMsgSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.activity_chat);

        s = MainActivity.s;
        context = this;
        s.channelIsOpen = true;

        s.messagesView = (ListView) findViewById(R.id.messages);
        mMsgComposer = (EditText) findViewById(R.id.msg_composer);
        mMsgSend = (Button) findViewById(R.id.msg_send);

        if (s.isDM) {
            setTitle("@" + s.selectedDm.toString());
            mMsgComposer.setHint(getResources().getString(
                    R.string.msg_composer_hint, "@" + s.selectedDm.toString()));
        } else {
            setTitle(s.selectedChannel.toString());
            mMsgComposer.setHint(getResources().getString(
                    R.string.msg_composer_hint, s.selectedChannel.toString()));
        }

        showProgress(true);

        s.api.aFetchMessages(0, 0, new Runnable() {
			@Override
			public void run() {
                s.messagesAdapter = new MessageListAdapter(context, s, s.messages);
                s.runOnUiThread(new Runnable() {
				    @Override
				    public void run() {
                        s.messagesView.setAdapter(s.messagesAdapter);
                        showProgress(false);
				    }
                });
			}
        });

        mMsgSend.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
                try {
                    s.sendMessage = mMsgComposer.getText().toString();
                    s.sendReference = 0;
                    s.sendPing = false;
                    s.api.aSendMessage(null);
                    mMsgComposer.setText("");
                } catch (Exception e) {
                    s.error("Error sending mesage: " + e.getMessage());
                    e.printStackTrace();
                }
			}
        });
    }

    private void showProgress(final boolean show) {
        this.setProgressBarVisibility(show);
        this.setProgressBarIndeterminate(show);
    }
}
