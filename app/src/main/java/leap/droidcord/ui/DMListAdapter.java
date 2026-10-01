package leap.droidcord.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

import leap.droidcord.R;
import leap.droidcord.State;
import leap.droidcord.model.DirectMessage;

public class DMListAdapter extends BaseAdapter {

    private Context context;
    private State s;
    private Vector<DirectMessage> dms;
    private Drawable defaultAvatar;
    private int iconSize;

    public DMListAdapter(Context context, State s, Vector<DirectMessage> dms) {
        this.context = context;
        this.s = s;
        this.dms = dms;
        this.defaultAvatar = context.getResources().getDrawable(R.drawable.ic_launcher);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        iconSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, metrics);
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
        ViewHolder viewHolder;
        final DirectMessage dm = (DirectMessage) getItem(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.dm_list_item, null);

            viewHolder = new ViewHolder();
            viewHolder.icon = (ImageView) convertView.findViewById(R.id.dm_item_icon);
            viewHolder.name = (TextView) convertView.findViewById(R.id.dm_item_name);
            viewHolder.status = (TextView) convertView.findViewById(R.id.dm_item_status);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        s.icons.load(viewHolder.icon, defaultAvatar, dm, iconSize);
        viewHolder.name.setText(dm.name);
        s.presences.load(viewHolder.status, dm);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView status;
    }
}