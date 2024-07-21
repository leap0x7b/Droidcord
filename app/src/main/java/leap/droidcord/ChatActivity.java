package leap.droidcord;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        setTitle(s.selectedChannel.toString());

        s.messagesView = (ListView) findViewById(R.id.messages);
        mMsgComposer = (EditText) findViewById(R.id.msg_composer);
        mMsgSend = (Button) findViewById(R.id.msg_send);

        mMsgComposer.setHint(getResources().getString(
                R.string.msg_composer_hint, s.selectedChannel.toString()));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Handler handler = new Handler(Looper.getMainLooper());

        showProgress(true);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                new HTTPThread(s, HTTPThread.FETCH_MESSAGES).run();
                s.messagesAdapter = new MessageListAdapter(context, s,
                        s.messages);

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showProgress(false);
                        s.messagesView.setAdapter(s.messagesAdapter);
                    }
                });
            }
        });

        mMsgSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    s.sendMessage = mMsgComposer.getText().toString();
                    s.sendReference = 0;
                    s.sendPing = false;
                    new HTTPThread(s, HTTPThread.SEND_MESSAGE).start();
                    mMsgComposer.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                    s.error(e.toString());
                }
            }
        });
    }

    private void showProgress(final boolean show) {
        this.setProgressBarVisibility(show);
        this.setProgressBarIndeterminate(show);
    }
}
