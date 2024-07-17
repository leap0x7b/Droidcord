package leap.droidcord;

import android.app.TabActivity;
import android.os.Bundle;
import android.widget.TabHost;
import android.view.LayoutInflater;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TabHost tabHost = getTabHost();

		LayoutInflater.from(this).inflate(R.layout.activity_main,
				tabHost.getTabContentView(), true);

		tabHost.addTab(tabHost
				.newTabSpec("servers")
				.setIndicator("Servers",
						getResources().getDrawable(R.drawable.ic_tab_channel))
				.setContent(R.id.server_tab));
		tabHost.addTab(tabHost.newTabSpec("dm").setIndicator("Direct Messages")
				.setContent(R.id.dm_tab));
		tabHost.addTab(tabHost.newTabSpec("settings").setIndicator("Settings")
				.setContent(R.id.settings_tab));
	}
}
