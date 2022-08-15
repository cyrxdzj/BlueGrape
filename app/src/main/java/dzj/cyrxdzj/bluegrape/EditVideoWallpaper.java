package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.VideoView;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EditVideoWallpaper extends AppCompatActivity {

    private String wallpaper_id,wallpaper_path;
    private EditText wallpaper_name_editor;
    private SeekBar alpha_seekbar;
    private Spinner fill_method_spinner,position_spinner;
    private CommonUtil util=new CommonUtil();
    public void show_delete_question_dialog()
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage(getString(R.string.confirm_delete))
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete();
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video_wallpaper);
        util.ask_permission(this);
        Intent intent=getIntent();
        wallpaper_id=intent.getStringExtra("wallpaper_id");
        LogUtils.dTag("EditVideoWallpaper","This wallpaper will be edited: "+wallpaper_id);
        try {
            //初始化配置
            String config_str = util.read_file(util.get_storage_path()+wallpaper_id+"/config.json");
            String[] fill_method_list={"左右填充","上下填充"},position_list={"左/上位置","右/下位置"};
            JSONObject config=new JSONObject(config_str);
            if(config.getString("fill_method").equals("top-bottom"))
            {
                fill_method_list[0]="上下填充";
                fill_method_list[1]="左右填充";
            }
            if(config.getString("position").equals("right-bottom"))
            {
                position_list[0]="右/下位置";
                position_list[1]="左/上位置";
            }
            wallpaper_path=config.getString("wallpaper_path");
            //初始化组件
            refresh_video();
            wallpaper_name_editor=(EditText)findViewById(R.id.wallpaper_name);
            wallpaper_name_editor.setText(URLDecoder.decode(config.getString("name"),"UTF-8"));
            alpha_seekbar=(SeekBar)findViewById(R.id.alpha);
            alpha_seekbar.setProgress(config.getInt("alpha"));
            ArrayAdapter<String> fill_method_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,fill_method_list);
            fill_method_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ArrayAdapter<String> position_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,position_list);
            position_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            fill_method_spinner=(Spinner)findViewById(R.id.fill_method);
            fill_method_spinner.setAdapter(fill_method_adapter);
            position_spinner=(Spinner)findViewById(R.id.position);
            position_spinner.setAdapter(position_adapter);
        } catch (Exception e) {
            util.show_info_dialog("",getString(R.string.load_failed),this);
            e.printStackTrace();
        }
    }
    private void refresh_video()
    {
        VideoView wallpaper_view=(VideoView)findViewById(R.id.wallpaper_video);
        //wallpaper_view.start();
        wallpaper_view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.setLooping(true);
                mp.setVolume(0f,0f);
            }
        });
        if(wallpaper_path.equals("none"))
        {
            wallpaper_view.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/raw/default_video"));
        }
        else
        {
            //wallpaper_view.setVideoPath(wallpaper_path);
            wallpaper_view.setVideoURI(Uri.parse("file://"+wallpaper_path));
        }
    }
    public void choose_video(View view)
    {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        startActivityForResult(intent, CommonUtil.CHOOSE_VIDEO);
    }
    private String getVideoPath(Uri uri)
    {
        LogUtils.dTag("EditVideoWallpaper", String.valueOf(uri));
        String v_path=null;
        try {
            Cursor cursor = getContentResolver().query(uri, null, null,
                    null, null);
            if(cursor!=null)
            {
                cursor.moveToFirst();
                v_path = cursor.getString(1);
                cursor.close();
            }
            else
            {
                v_path=String.valueOf(uri).replace("file://","");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return v_path;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CommonUtil.CHOOSE_VIDEO&&resultCode==RESULT_OK)
        {
            //String path=getVideoPath(data.getData());
            String path=util.get_path_from_uri(data.getData());
            LogUtils.dTag("EditVideoWallpaper","Video path is: "+wallpaper_path);
            if(path==null)
            {
                util.show_info_dialog("",getString(R.string.choose_video_failed),this);
                return;
            }
            wallpaper_path=path;
            refresh_video();
        }
    }
    public void save(View view)
    {
        try {
            save_without_dialog();
            util.show_info_dialog("",getString(R.string.save_successfully),this);
        }
        catch (Exception ex) {
            util.show_info_dialog("",getString(R.string.save_failed),this);
        }
    }
    public void save_without_dialog() throws IOException {
        String save_str = "{\n"+
                "\t\"name\":\""+ URLEncoder.encode(wallpaper_name_editor.getText().toString(),"UTF-8")+"\",\n"+
                "\t\"wallpaper_path\":\""+wallpaper_path+"\",\n"+
                "\t\"alpha\":"+String.valueOf(alpha_seekbar.getProgress())+",\n"+
                "\t\"fill_method\":\""+(fill_method_spinner.getSelectedItem().toString()=="左右填充"?"left-right":"top-bottom")+"\",\n"+
                "\t\"position\":\""+(position_spinner.getSelectedItem().toString()=="左/上位置"?"left-top":"right-bottom")+"\"\n}";
        LogUtils.dTag("EditVideoWallpaper","Config content:\n"+save_str);
        util.write_file(util.get_storage_path()+wallpaper_id+"/config.json",save_str);
    }
    public void apply(View view)
    {
        try {
            save_without_dialog();
        }
        catch (Exception ex) {
            util.show_info_dialog("",getString(R.string.save_failed),this);
        }
        Intent intent=new Intent();
        intent.setClass(this,ApplyWallpaper.class);
        intent.putExtra("wallpaper_id",wallpaper_id);
        intent.putExtra("wallpaper_name",wallpaper_name_editor.getText().toString());
        this.startActivity(intent);
    }
    public void button_delete(View view)
    {
        show_delete_question_dialog();
    }
    public void delete()
    {
        LogUtils.dTag("EditVideoWallpaper","This wallpaper will be deleted");
        File file_obj=new File(util.get_storage_path()+wallpaper_id);
        util.delete_dir(file_obj);
        finish();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            try {
                save_without_dialog();
            }
            catch (Exception ex) {
                util.show_info_dialog("",getString(R.string.save_failed),this);
            }
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}