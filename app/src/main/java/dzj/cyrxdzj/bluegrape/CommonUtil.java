package dzj.cyrxdzj.bluegrape;

import android.os.Environment;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CommonUtil {
    public boolean is_video(String wallpaper_id)
    {
        File fobj=new File(Environment.getDataDirectory()+"/data/dzj.cyrxdzj.bluegrape/files/"+wallpaper_id+"/video_wallpaper");
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
}
