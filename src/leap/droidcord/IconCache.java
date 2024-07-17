package leap.droidcord;

import java.util.*;
import android.graphics.Bitmap;

public class IconCache {
	private State s;
	private Hashtable<String, Bitmap> icons;
	private Vector<String> iconHashes;
	private Vector<String> activeRequests;

	public IconCache(State s) {
		this.s = s;
		icons = new Hashtable<String, Bitmap>();
		iconHashes = new Vector<String>();
		activeRequests = new Vector<String>();
	}

	public Bitmap get(HasIcon target) {
		if (s.iconType == State.ICON_TYPE_NONE)
			return null;

		// Don't show menu icons (DirectMessages and Guild) if menu icons disabled
		if (!s.showMenuIcons && !(target instanceof User))
			return null;

		String hash = target.getIconHash();
		if (hash == null)
			return null;

		Bitmap result = (Bitmap) icons.get(hash);
		if (result != null)
			return result;

		if (!activeRequests.contains(hash)) {
			activeRequests.addElement(hash);
			HTTPThread http = new HTTPThread(s, HTTPThread.FETCH_ICON);
			http.iconTarget = target; http.start();
		}
		return null;
	}

	public void removeRequest(String hash) {
		int index = activeRequests.indexOf(hash);
		if (index != -1)
			activeRequests.removeElementAt(index);
	}

	public void set(String hash, Bitmap icon) {
		removeRequest(hash);

		if (!icons.containsKey(hash) && icons.size() >= 100) {
			String firstHash = (String) iconHashes.elementAt(0);
			icons.remove(firstHash);
			iconHashes.removeElementAt(0);
		}
		icons.put(hash, icon);
		iconHashes.addElement(hash);
	}
}