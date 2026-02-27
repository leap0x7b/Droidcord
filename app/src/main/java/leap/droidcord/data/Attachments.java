package leap.droidcord.data;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.Hashtable;

import leap.droidcord.State;
import leap.droidcord.model.Attachment;
import leap.droidcord.model.Message;

import java.lang.ref.WeakReference;
import java.util.Vector;

public class Attachments {
    private State s;
    private Hashtable<String, Bitmap> attachments;
    private Hashtable<String, Vector<WeakReference<ImageView>>> imageViews;

    private Vector<String> attachmentUrls;
    private Vector<String> activeRequests;

    public Attachments(State s) {
        this.s = s;
        attachments = new Hashtable<String, Bitmap>();
        imageViews = new Hashtable<String, Vector<WeakReference<ImageView>>>();
        attachmentUrls = new Vector<String>();
        activeRequests = new Vector<String>();
    }

    public Bitmap get(Message message, Attachment attachment) {
        String url = attachment.previewUrl;
        if (url == null)
            return null;

        Bitmap result = attachments.get(url);
        if (result != null)
            return result;

        if (!activeRequests.contains(url)) {
            activeRequests.add(url);
            s.api.aFetchAttachment(attachment, null);
        }

        return null;
    }

    public void removeRequest(String url) {
        int index = activeRequests.indexOf(url);
        if (index != -1)
            activeRequests.remove(index);
    }

    public void load(ImageView image, Drawable initial, Message message, Attachment attachment) {
        image.setTag(null);
        final Bitmap maybeBitmap = get(message, attachment);
        if (maybeBitmap != null) {
            image.setImageBitmap(maybeBitmap);
            return;
        }

        image.setImageDrawable(initial);
        if (attachment.previewUrl == null)
            return;

        if (!imageViews.containsKey(attachment.previewUrl)) {
            imageViews.put(attachment.previewUrl, new Vector<WeakReference<ImageView>>());
        }

        Vector<WeakReference<ImageView>> views = imageViews.get(attachment.previewUrl);
        views.add(new WeakReference<ImageView>(image));

        image.setTag(attachment.previewUrl);

        if (!activeRequests.contains(attachment.previewUrl)) {
            activeRequests.add(attachment.previewUrl);
            s.api.aFetchAttachment(attachment, null);
        }
    }

    public void set(final String url, final Bitmap image) {
        removeRequest(url);

        if (!attachments.containsKey(url) && attachments.size() >= 25) { // Cache size limit
            String firstUrl = attachmentUrls.get(0);
            attachments.remove(firstUrl);
            attachmentUrls.remove(0);
        }

        attachments.put(url, image);
        attachmentUrls.add(url);

        if (!imageViews.containsKey(url))
            return;

        s.runOnUiThread(new Runnable() {
			@Override
			public void run() {
                final Vector<WeakReference<ImageView>> references = imageViews.get(url);
                imageViews.remove(url);
                for (int i = 0; i < references.size(); i++) {
                    WeakReference<ImageView> ref = references.get(i);
                    if (ref == null)
                        continue;

                    ImageView imageView = ref.get();
                    if (imageView != null && imageView.getTag() != null && imageView.getTag().equals(url)) {
                        imageView.setImageBitmap(image);
                        imageView.invalidate();
                    }
                }
			}
        });
    }
}
