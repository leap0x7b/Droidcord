package leap.droidcord;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ChatActivity extends Activity {
	
	private State s;
	private Context context;
	int page;
	long before;
	long after;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_chat);

		s = MainActivity.s;
		s.messagesView = (ListView) findViewById(R.id.messages);
		context = this;
		s.channelIsOpen = true;
		setTitle(s.selectedChannel.toString());

		ExecutorService executor = Executors.newSingleThreadExecutor();
		final Handler handler = new Handler(Looper.getMainLooper());

		showProgress(true);
		executor.execute(new Runnable() {
			@Override
			public void run() {
				new HTTPThread(s, HTTPThread.FETCH_MESSAGES).run();
				s.messagesAdapter = new MessageListAdapter(context, s, s.messages);

				handler.post(new Runnable() {
					@Override
					public void run() {
						showProgress(false);
						s.messagesView.setAdapter(s.messagesAdapter);
					}
				});
			}
		});
	}
	
	private void showProgress(final boolean show) {
		this.setProgressBarVisibility(show);
		this.setProgressBarIndeterminate(show);
	}
}
