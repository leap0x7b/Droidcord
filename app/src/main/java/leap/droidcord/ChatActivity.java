package leap.droidcord;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import leap.droidcord.ui.MessageListAdapter;

public class ChatActivity extends Activity {
    private State s;
    private Context context;
    private EditText mMsgComposer;
    private Button mMsgSend;
    private boolean mLoadingOlder;
    private boolean mNoMoreHistory;

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

        s.api.aFetchMessages(0, 0, () -> {
            s.messagesAdapter = new MessageListAdapter(context, s, s.messages);
            s.runOnUiThread(() -> {
                s.messagesView.setAdapter(s.messagesAdapter);
                showProgress(false);
            });
        });

        s.messagesView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView v, int i) {
            }

            @Override
            public void onScroll(AbsListView v, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem != 0 || totalItemCount == 0
                        || mLoadingOlder || mNoMoreHistory)
                    return;
                loadOlderMessages();
            }
        });

        mMsgSend.setOnClickListener((View v) -> {
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
        });
    }

    private void loadOlderMessages() {
        mLoadingOlder = true;
        showProgress(true);

        final int prevCount = s.messages.size();
        final long before = s.messages.get(0).id;

        s.api.aFetchMessagesBefore(before, () -> {
            final int added = s.messages.size() - prevCount;
            s.runOnUiThread(() -> {
                if (added > 0) {
                    final ListView view = s.messagesView;
                    final int first = view.getFirstVisiblePosition();
                    final View topChild = view.getChildAt(0);
                    final int top = topChild != null ? topChild.getTop() : 0;
                    s.messagesAdapter.notifyDataSetChanged();
                    view.setSelectionFromTop(first + added, top);
                }
                if (added < Math.max(1, s.messageLoadCount))
                    mNoMoreHistory = true;
                showProgress(false);
                mLoadingOlder = false;
            });
        });
    }

    private void showProgress(final boolean show) {
        this.setProgressBarVisibility(show);
        this.setProgressBarIndeterminate(show);
    }
}
