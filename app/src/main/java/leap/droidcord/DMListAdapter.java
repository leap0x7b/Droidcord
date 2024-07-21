package leap.droidcord;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DMListAdapter extends BaseAdapter {

    private Context context;
    private State s;
    private Vector<DirectMessage> dms;
    // serve nothing other than preventing calculating the pixel size every time
    // the item is shown on-screen
    private int iconSize;

    public DMListAdapter(Context context, State s, Vector<DirectMessage> dms) {
        this.context = context;
        this.s = s;
        this.dms = dms;

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                48 + 0.5f, metrics);
    }

    @Override
    public Object getItem(int position) {
        return dms.get(position);
    }

    @Override
    public int getCount() {
        return dms.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DirectMessage dm = (DirectMessage) getItem(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.dm_list_item, null);
        }

        final ImageView imageView = (ImageView) convertView
                .findViewById(R.id.dm_item_icon);
        imageView.setImageDrawable(context.getResources().getDrawable(
                R.drawable.ic_launcher));

        String format = (s.useJpeg ? "jpg" : "png");
        String type = dm.getIconType();
        long id = dm.getIconID();
        String hash = dm.getIconHash();
        imageView.setTag(s.cdn + type + id + "/" + hash + "." + format
                + "?size=" + iconSize);
        LoadImage loadImage = new LoadImage(imageView);
        loadImage.call();

        TextView textView = (TextView) convertView
                .findViewById(R.id.dm_item_name);
        textView.setText(dm.name);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public class LoadImage implements Callable<Void> {

        private String url;
        private ImageView imageView;

        LoadImage(ImageView imageView) {
            this.imageView = imageView;
            this.url = imageView.getTag().toString();
        }

        @Override
        public Void call() {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            final Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Bitmap icon = s.http.getImage(url);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!imageView.getTag().toString().equals(url)) {
                                    return;
                                }
                                if (icon != null) {
                                    imageView.setImageBitmap(icon);
                                } else {
                                    imageView.setImageDrawable(context
                                            .getResources().getDrawable(
                                                    R.drawable.ic_launcher));
                                }
                            }
                        });
                    } catch (IOException e) {
                    }
                }
            });
            return null;
        }

    }
}