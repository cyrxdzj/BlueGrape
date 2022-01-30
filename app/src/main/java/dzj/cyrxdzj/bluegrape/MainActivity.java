package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    public void write_file(String path,String content) throws IOException
    {
        FileWriter writer=new FileWriter(new File(path));
        writer.write(content);
        writer.close();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //新建BlueGrape文件夹
        File folder=new File("/storage/emulated/0/BlueGrape");
        if(!folder.exists())
        {
            folder.mkdir();
        }
        File file=new File("/storage/emulated/0/BlueGrape/current_wallpaper.json");
        if(!file.exists())
        {
            try {
                file.createNewFile();
                write_file("/storage/emulated/0/BlueGrape/current_wallpaper.json","[]");
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        //TEST CODE BEGIN
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        //TEST CODE END
        startService(new Intent(MainActivity.this, AppListener.class));
        startService(new Intent(MainActivity.this, WallpaperService.class));
    }
    public void open_my_wallpaper(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,MyWallpaper.class);
        MainActivity.this.startActivity(intent);
    }
    public void open_current_wallpaper(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,CurrentWallpaper.class);
        MainActivity.this.startActivity(intent);
    }
    public void open_about_this_software(View view)
    {
        Intent intent=new Intent();
        intent.setClass(MainActivity.this,AboutThisSoftware.class);
        MainActivity.this.startActivity(intent);
    }
}