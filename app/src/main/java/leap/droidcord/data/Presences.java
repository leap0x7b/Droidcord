package leap.droidcord.data;

import java.util.Hashtable;
import java.lang.ref.WeakReference;
import java.text.StringCharacterIterator;
import java.util.Vector;

import leap.droidcord.State;
import leap.droidcord.model.DirectMessage;
import leap.droidcord.model.Presence;
import leap.droidcord.model.User;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import cc.nnproject.json.JSONArray;
import cc.nnproject.json.JSONObject;

public class Presences {
    private State s;

    private Hashtable<String, Presence> presences;
    private Hashtable<String, Vector<WeakReference<TextView>>> views;
    private Vector<String> keys;

    public Presences(State s) {
        this.s = s;
        presences = new Hashtable<String, Presence>();
        views = new Hashtable<String, Vector<WeakReference<TextView>>>();
        keys = new Vector<String>();
    }

    public Presence get(long userId) {
        // presences cannot be fetched without gateway (technically can but
        // isn't practical)
        if (!s.gatewayActive())
            return null;

        return presences.get(String.valueOf(userId));
    }

    public Presence get(User user) {
        // this shit is held together with duct tape, pain, and suffering
        return get(user instanceof DirectMessage ? user.getIconID() : user.id);
    }

    public void set(final String key, final Presence presence) {
        if (!presences.containsKey(key) && presences.size() >= 100) {
            String firstHash = (String) keys.get(0);
            presences.remove(firstHash);
            keys.remove(0);
        }

        presences.put(key, presence);
        keys.add(key);

        if (!views.containsKey(key))
            return;

        s.runOnUiThread(() -> {
            final Vector<WeakReference<TextView>> references = views.get(key);
            //views.remove(key);

            if (references == null || references.size() == 0)
                return;

            for (WeakReference<TextView> ref : references) {
                TextView textView = ref.get();
                if (textView != null) {
                    if (presence.status != null) {
                        textView.setVisibility(View.VISIBLE);
                        switch (presence.currentActivity) {
                            case PLAYING:
                                textView.setText("Playing " + presence.status);
                                break;
                            case STREAMING:
                                textView.setText("Streaming " + presence.status);
                                break;
                            case LISTENING:
                                textView.setText("Listening to " + presence.status);
                                break;
                            case WATCHING:
                                textView.setText("Watching " + presence.status);
                                break;
                            case CUSTOM_STATUS:
                                textView.setText(presence.status);
                                break;
                            case COMPETING:
                                textView.setText("Competing in " + presence.status);
                                break;
                        }
                    } else {
                        textView.setVisibility(View.GONE);
                    }
                }
            }

            s.dmsAdapter.notifyDataSetChanged();
            s.dmsView.invalidate();
            references.clear();
        });
    }

    public boolean has(User user) {
        return presences.containsKey(String.valueOf(user instanceof DirectMessage ? user.getIconID() : user.id));
    }

    public void load(TextView textView, User user) {
        String key = String.valueOf(user instanceof DirectMessage ? user.getIconID() : user.id);
        if (has(user)) {
            Presence presence = presences.get(key);
            if (presence.status != null) {
                textView.setVisibility(View.VISIBLE);
                switch (presence.currentActivity) {
                    case PLAYING:
                        textView.setText("Playing " + presence.status);
                        break;
                    case STREAMING:
                        textView.setText("Streaming " + presence.status);
                        break;
                    case LISTENING:
                        textView.setText("Listening to " + presence.status);
                        break;
                    case WATCHING:
                        textView.setText("Watching " + presence.status);
                        break;
                    case CUSTOM_STATUS:
                        textView.setText(presence.status);
                        break;
                    case COMPETING:
                        textView.setText("Competing in " + presence.status);
                        break;
                }
            } else {
                textView.setVisibility(View.GONE);
            }
        } else {
            textView.setVisibility(View.GONE);
        }

        if (!views.containsKey(key))
            views.put(key, new Vector<WeakReference<TextView>>());

        final Vector<WeakReference<TextView>> references = views.get(key);
        references.add(new WeakReference<TextView>(textView));
    }
}