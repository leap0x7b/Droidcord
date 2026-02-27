package leap.droidcord.data;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.Hashtable;

import leap.droidcord.State;
import leap.droidcord.model.HasIcon;

import java.lang.ref.WeakReference;
import java.util.Vector;

public class Icons {
    private State s;
    private Hashtable<String, Bitmap> icons;
    private Hashtable<String, Vector<WeakReference<ImageView>>> imageViews;

    private Vector<String> iconHashes;
    private Vector<String> activeRequests;

    public Icons(State s) {
        this.s = s;
        icons = new Hashtable<String, Bitmap>();
        imageViews = new Hashtable<String, Vector<WeakReference<ImageView>>>();
        iconHashes = new Vector<String>();
        activeRequests = new Vector<String>();
    }

    public Bitmap get(HasIcon target, int size) {
        if (s.iconType == State.ICON_TYPE_NONE)
            return null;

        String hash = target.getIconHash() + String.valueOf(size);
        if (hash == null)
            return null;

        Bitmap result = (Bitmap) icons.get(hash);
        if (result != null)
            return result;

        if (!activeRequests.contains(hash)) {
            activeRequests.add(hash);
            s.api.aFetchIcon(target, size, null);
        }

        return null;
    }

    public void removeRequest(String hash) {
        int index = activeRequests.indexOf(hash);
        if (index != -1)
            activeRequests.remove(index);
    }

    public void load(ImageView image, Drawable initial, HasIcon target, int size) {
        image.setTag(null);

        final Bitmap maybeBitmap = get(target, size);
        if (maybeBitmap != null) {
            image.setImageBitmap(maybeBitmap);
            return;
        }

        image.setImageDrawable(initial);

        if (target.getIconHash() == null)
            return;

        if (!imageViews.containsKey(target.getIconHash() + String.valueOf(size))) {
            imageViews.put(target.getIconHash() + String.valueOf(size),
                           new Vector<WeakReference<ImageView>>());
        }

        Vector<WeakReference<ImageView>> views = imageViews.get(target.getIconHash() + String.valueOf(size));
        views.add(new WeakReference<ImageView>(image));

        image.setTag(target.getIconHash() + String.valueOf(size));

        if (!activeRequests.contains(target.getIconHash() + String.valueOf(size))) {
            activeRequests.add(target.getIconHash() + String.valueOf(size));
            s.api.aFetchIcon(target, size, null);
        }
    }

    public void set(final String hash, final Bitmap icon) {
        removeRequest(hash);

        if (!icons.containsKey(hash) && icons.size() >= 100) {
            String firstHash = (String) iconHashes.get(0);
            icons.remove(firstHash);
            iconHashes.remove(0);
        }

        icons.put(hash, icon);
        iconHashes.add(hash);

        if (!imageViews.containsKey(hash))
            return;

        s.runOnUiThread(new Runnable() {
			@Override
			public void run() {
                final Vector<WeakReference<ImageView>> references = imageViews.get(hash);
                imageViews.remove(hash);

                for (int i = 0; i < references.size(); i++) {
                    WeakReference<ImageView> ref = references.get(i);
                    if (ref == null)
                        continue;

                    ImageView imageView = ref.get();
                    if (imageView != null && imageView.getTag() != null && imageView.getTag().equals(hash)) {
                        imageView.setImageBitmap(icon);
                        imageView.invalidate();
                    }
                }
		    }
        });
    }
}
