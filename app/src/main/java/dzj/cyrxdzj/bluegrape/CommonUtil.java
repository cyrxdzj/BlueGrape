package dzj.cyrxdzj.bluegrape;

import android.os.Environment;

import java.io.File;

public class CommonUtil {
    public boolean is_video(String wallpaper_id)
    {
        File fobj=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/"+wallpaper_id+"/video_wallpaper");
        return fobj.exists();
    }
}
