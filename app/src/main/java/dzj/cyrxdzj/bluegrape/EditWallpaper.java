package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class EditWallpaper extends AppCompatActivity {

    private String wallpaper_id;
    public String read_file(String path) throws IOException {
        File file_obj = new File(path);
        FileInputStream reader=new FileInputStream(file_obj);
        byte[] temp=new byte[500];
        reader.read(temp);
        return new String(temp,"UTF-8");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_wallpaper);
        Intent intent=getIntent();
        wallpaper_id=intent.getStringExtra("wallpaper_id");
        Log.d("EditWallpaper",wallpaper_id);
        //初始化配置
        String config_str= null;
        try {
            config_str = read_file("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/config.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] fill_method_list={"左右填充","上下填充"},position_list={"左/上位置","右/下位置"};
        Log.d("EditWallpaper",config_str);
        try {
            JSONObject config=new JSONObject(config_str);
            if(config.getString("fill_method")=="top-bottom")
            {
                fill_method_list[0]="上下填充";
                fill_method_list[1]="左右填充";
            }
            if(config.getString("position")=="right-bottom")
            {
                position_list[0]="右/下位置";
                position_list[1]="左/上位置";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //初始化组件
        ArrayAdapter<String> fill_method_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,fill_method_list);
        fill_method_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<String> position_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,position_list);
        position_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner fill_method_spinner=(Spinner)findViewById(R.id.fill_method);
        fill_method_spinner.setAdapter(fill_method_adapter);
        Spinner position_spinner=(Spinner)findViewById(R.id.position);
        position_spinner.setAdapter(position_adapter);
    }
}