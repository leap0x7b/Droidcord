package leap.droidcord;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Window;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {

	private State s;
	ExpandableListView mGuildsView;
	ExpandableListAdapter mGuildsAdapter;
	ListView mDmsView;
	ListAdapter mDmsAdapter;
	List<String> guildNames;
	List<Guild> guildList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_PROGRESS);
		TabHost tabHost = getTabHost();
		mGuildsView = (ExpandableListView) findViewById(R.id.servers);
		s = new State(this);

		LayoutInflater.from(this).inflate(R.layout.activity_main,
				tabHost.getTabContentView(), true);
		mGuildsView = (ExpandableListView) findViewById(R.id.servers);

		tabHost.addTab(tabHost.newTabSpec("servers").setIndicator("Servers")
				.setContent(R.id.server_tab));
		tabHost.addTab(tabHost.newTabSpec("dm").setIndicator("Direct Messages")
				.setContent(R.id.dm_tab));
		tabHost.addTab(tabHost.newTabSpec("settings").setIndicator("Settings")
				.setContent(R.id.settings_tab));

		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this);
		if (TextUtils.isEmpty(sp.getString("token", null))) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			finish();
		} else {
			String api_url = sp.getString("api", null);
			String cdn_url = sp.getString("cdn", null);
			boolean use_gateway = sp.getBoolean("useGateway", false);
			String gateway_url = sp.getString("gateway", null);
			String token = sp.getString("token", null);
			int token_type = sp.getInt("tokenType", 0);

			try {
				s.useGateway = true;
				s.tokenType = token_type;
				s.login(api_url, "socket://146.59.80.3:8081", cdn_url, token);
				// FIXME: for some reason making this a thread doesn't fetch the guild list and would just error out with a null pointer exception
				//new HTTPThread(s, HTTPThread.FETCH_GUILDS).start();
				
				ExecutorService executor = Executors.newSingleThreadExecutor();
				final Handler handler = new Handler(Looper.getMainLooper());
				final Context context = this;

				showProgress(true);
				executor.execute(new Runnable() {
					@Override
					public void run() {
						new HTTPThread(s, HTTPThread.FETCH_GUILDS).run();
						new HTTPThread(s, HTTPThread.FETCH_DIRECT_MESSAGES).run();
						mGuildsAdapter = new GuildListAdapter(context, s, s.guilds);
						mDmsAdapter = new DMListAdapter(context, s, s.directMessages);

						handler.post(new Runnable() {
							@Override
							public void run() {
								showProgress(false);
								mGuildsView.setAdapter(mGuildsAdapter);
								// FIXME: WHY THE FUCK IS IT NULL I LOADED THE DM ADAPTER PROPERLY WHY THE FUCK DOES IT THINK ITS NULL THE FUCK DID I DO??????
								//mDmsView.setAdapter(mDmsAdapter);
							}
						});
					}
				});
			} catch (Exception e) {
				//s.error(e.toString());
				e.printStackTrace();
			}
		}
	}
	
	public void showProgress(final boolean show) {
		this.setProgressBarVisibility(show);
		this.setProgressBarIndeterminate(show);
	}
}
