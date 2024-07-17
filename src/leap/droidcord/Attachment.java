package leap.droidcord;

import cc.nnproject.json.*;
import android.app.*;
import android.content.res.Resources;

public class Attachment {
	public String url;
	public String previewUrl;
	public String name;
	public int size;
	public boolean supported;
	public boolean isText;

	private static final String[] nonTextFormats = { ".zip", ".rar", ".7z",
			".exe", ".jar", ".apk", ".sis", ".sisx", ".bin", ".mp3", ".wav",
			".ogg", ".m4a", ".amr", ".flac", ".mid", ".mmf", ".mp4", ".3gp" };

	public Attachment(State s, JSONObject data) {
		String proxyUrl = data.getString("proxy_url");

		url = s.cdn
				+ proxyUrl.substring("https://media.discordapp.net".length());

		name = data.getString("filename", "Unnamed file");
		size = data.getInt("size", 0);

		// Attachments that aren't images or videos are unsupported
		// (cannot be previewed but can be viewed as text or downloaded)
		if (!data.has("width")) {
			supported = false;

			// Can be viewed as text if it's not one of the blacklisted file
			// extensions
			isText = (Util.indexOfAny(name.toLowerCase(), nonTextFormats, 0) == -1);

			return;
		}

		supported = true;
		int imageWidth = data.getInt("width");
		int imageHeight = data.getInt("height");

		int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
		int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

		int[] size = Util.resizeFit(imageWidth, imageHeight, screenWidth,
				screenHeight);

		// Preview url is not using our own proxy, because media.discordapp.net
		// works over http
		previewUrl = "http://" + proxyUrl.substring("https://".length())
				+ "format=" + (s.useJpeg ? "jpeg" : "png") + "&width="
				+ size[0] + "&height=" + size[1];
	}
}