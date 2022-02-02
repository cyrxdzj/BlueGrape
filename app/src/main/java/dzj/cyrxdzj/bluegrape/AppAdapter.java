package dzj.cyrxdzj.bluegrape;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

public class AppAdapter extends BaseAdapter {
    public LinkedList<App> data;
    public Context context;
    public AppAdapter(LinkedList<App> data,Context context){this.data=data;this.context=context;}
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView= LayoutInflater.from(context).inflate(R.layout.app_item,parent,false);
        ImageView icon=convertView.findViewById(R.id.app_icon);
        TextView app_name=convertView.findViewById(R.id.app_name);
        TextView package_id=convertView.findViewById(R.id.package_id);
        TextView is_apply=convertView.findViewById(R.id.is_apply);
        Log.d("AppAdapter",package_id+" "+app_name);
        icon.setImageDrawable((Drawable)data.get(position).app_icon);
        app_name.setText(data.get(position).app_name);
        package_id.setText(data.get(position).package_id);
        if(data.get(position).is_apply)
        {
            is_apply.setText("已被应用");
            is_apply.setTextColor(Color.GREEN);
        }
        else
        {
            is_apply.setText("未被应用");
            is_apply.setTextColor(Color.BLUE);
        }
        return convertView;
    }
}
