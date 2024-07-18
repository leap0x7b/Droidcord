package leap.droidcord;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONArray;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TabHost;
import android.text.TextUtils;
import android.view.LayoutInflater;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {

	private State s;
	ExpandableListView mGuildsView;
	ExpandableListAdapter mGuildsAdapter;
	List<String> guildNames;
	List<Guild> guildList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabHost tabHost = getTabHost();
		s = new State(this);

		LayoutInflater.from(this).inflate(R.layout.activity_main,
				tabHost.getTabContentView(), true);

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
				s.useGateway = use_gateway;
				s.tokenType = token_type;
				s.login(api_url, gateway_url, cdn_url, token);
				// new HTTPThread(s, HTTPThread.FETCH_GUILDS).start();
				new HTTPThread(s, HTTPThread.FETCH_GUILDS).run();
				// new HTTPThread(s, HTTPThread.FETCH_CHANNELS).start();
				System.out.println(s.guilds);
			} catch (Exception e) {
				System.out.println(e.toString());
			}
			mGuildsView = (ExpandableListView) findViewById(R.id.servers);
			mGuildsAdapter = new GuildListAdapter(this, s, s.guilds);
			mGuildsView.setAdapter(mGuildsAdapter);
		}
	}
}
