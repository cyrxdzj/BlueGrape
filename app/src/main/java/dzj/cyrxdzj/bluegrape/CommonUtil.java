package dzj.cyrxdzj.bluegrape;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CommonUtil {
    public boolean is_image(String wallpaper_id)
    {
        return !(is_video(wallpaper_id)||is_html(wallpaper_id));
    }
    public boolean is_video(String wallpaper_id)
    {
        File fobj=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/"+wallpaper_id+"/video_wallpaper");
        return fobj.exists();
    }
    public boolean is_html(String wallpaper_id)
    {
        File fobj=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/"+wallpaper_id+"/html_wallpaper");
        return fobj.exists();
    }
    public String read_file(String path) throws IOException {
        FileReader reader=new FileReader(new File(path));
        char[] temp=new char[500];
        reader.read(temp);
        reader.close();
        return new String(temp);
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
}
