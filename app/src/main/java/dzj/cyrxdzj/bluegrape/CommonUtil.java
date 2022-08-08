package dzj.cyrxdzj.bluegrape;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CommonUtil {
    public static final int CHOOSE_IMAGE=2;
    public static final int CHOOSE_VIDEO=3;
    public boolean is_image(String wallpaper_id)
    {
        return !(is_video(wallpaper_id)||is_html(wallpaper_id));
    }
    public boolean is_video(String wallpaper_id)
    {
        File fobj=new File(get_storage_path()+wallpaper_id+"/video_wallpaper");
        return fobj.exists();
    }
    public boolean is_html(String wallpaper_id)
    {
        File fobj=new File(get_storage_path()+wallpaper_id+"/html_wallpaper");
        return fobj.exists();
    }
    public String read_file(String path) throws IOException {
        FileReader reader=new FileReader(new File(path));
        char[] temp=new char[1024*1024*2];
        reader.read(temp);
        reader.close();
        return new String(temp);
    }
    public String read_file(int res_id, Context context) throws IOException {
        InputStream is=context.getResources().openRawResource(res_id);
        byte[] buffer=new byte[is.available()];
        is.read(buffer);
        return new String(buffer,"UTF-8");
    }
    public void write_file(String path,String content) throws IOException
    {
        FileWriter writer=new FileWriter(new File(path));
        writer.write(content);
        writer.close();
    }
    public void ask_permission(AppCompatActivity context)
    {
        try {
            String[] PERMISSIONS_STORAGE = {
                    "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE" };
            int permission = ActivityCompat.checkSelfPermission(context,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(context,PERMISSIONS_STORAGE,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void delete_dir(File file) {
        File[] list = file.listFiles();
        if (list != null) {
            for (File temp : list) {
                delete_dir(temp);
            }
        }
        file.delete();
    }
    public void show_info_dialog(String title,String content,android.content.Context context)
    {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    public String format_time(Date time)
    {
        SimpleDateFormat time_formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        time_formatter.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        return time_formatter.format(time);
    }
    public String get_storage_path()
    {
        return Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/";
    }
    public void refresh_settings(Context context) throws IOException, JSONException {
        JSONObject settings_object = new JSONObject(read_file(get_storage_path()+"settings.json"));
        JSONArray settings_template = new JSONArray(read_file(R.raw.settings,context));
        for(int i=0;i<settings_template.length();i++) {
            JSONObject settings_part = settings_template.getJSONObject(i);
            JSONArray settings_detail_array = settings_part.getJSONArray("settings");
            for (int j = 0; j < settings_detail_array.length(); j++) {
                JSONObject settings_detail = settings_detail_array.getJSONObject(j);
                String id = settings_detail.getString("id");
                if (settings_detail.getString("type").equals("bool")) {
                    Settings.bool_settings.put(id, settings_object.getBoolean(id));
                } else if (settings_detail.getString("type").equals("string")) {
                    Settings.string_settings.put(id, settings_object.getString(id));
                } else if (settings_detail.getString("type").equals("int")) {
                    Settings.int_settings.put(id, settings_object.getInt(id));
                }
            }
        }
        LogUtils.dTag("CommonUtils","Settings json content:\n"+settings_object.toString());
    }
    public int get_screen_width(Context context)
    {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
    public int get_screen_height(Context context)
    {
        int status_bar_height=0;
        int status_bar_r_id=context.getResources().getIdentifier("status_bar_height","dimen","android");
        if(status_bar_r_id>0)
        {
            status_bar_height=context.getResources().getDimensionPixelSize(status_bar_r_id);
        }
        return context.getResources().getDisplayMetrics().heightPixels-status_bar_height;
    }
}
