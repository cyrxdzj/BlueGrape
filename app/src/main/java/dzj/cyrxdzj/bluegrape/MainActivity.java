package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //新建BlueGrape文件夹
        File folder=new File("/storage/emulated/0/BlueGrape");
        if(!folder.exists())
        {
            folder.mkdir();
            File file=new File("/storage/emulated/0/BlueGrape/current_wallpaper");
            if(!file.exists())
            {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        }
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