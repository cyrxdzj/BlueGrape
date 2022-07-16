package dzj.cyrxdzj.bluegrape;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blankj.utilcode.util.LogUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EditHtmlWallpaper extends AppCompatActivity {

    private String wallpaper_id;
    private EditText wallpaper_name_editor;
    private SeekBar alpha_seekbar;
    private TextView wallpaper_raw_name;
    private ImageView wallpaper_preview_view;
    private CommonUtil util=new CommonUtil();
    public void show_info_dialog(String title,String content)
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
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
        setContentView(R.layout.activity_edit_html_wallpaper);
        util.ask_permission(this);
        Intent intent = getIntent();
        wallpaper_id = intent.getStringExtra("wallpaper_id");
        LogUtils.dTag("EditHtmlWallpaper", "This wallpaper will be edited: " + wallpaper_id);
        try {
            //初始化配置
            String config_str = util.read_file(util.get_storage_path() + wallpaper_id + "/config.json");
            JSONObject config = new JSONObject(config_str);
            wallpaper_name_editor = (EditText) findViewById(R.id.wallpaper_name);
            wallpaper_name_editor.setText(URLDecoder.decode(config.getString("name"), "UTF-8"));
            alpha_seekbar=(SeekBar)findViewById(R.id.alpha);
            alpha_seekbar.setProgress(config.getInt("alpha"));
            String raw_config_str = util.read_file(util.get_storage_path() + wallpaper_id + "/src/config.json");
            JSONObject raw_config = new JSONObject(raw_config_str);
            wallpaper_raw_name=(TextView)findViewById(R.id.raw_name);
            wallpaper_raw_name.setText(raw_config.getString("raw_name"));
            wallpaper_preview_view=(ImageView)findViewById(R.id.preview_image);
            wallpaper_preview_view.setImageURI(Uri.fromFile(new File(util.get_storage_path()+wallpaper_id+"/src/preview.png")));
        } catch (Exception e) {
            util.show_info_dialog("", getString(R.string.load_failed),this);
            e.printStackTrace();
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
                "\t\"alpha\":"+String.valueOf(alpha_seekbar.getProgress())+"\n}";
        LogUtils.dTag("EditHtmlWallpaper","Config content:\n"+save_str);
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
        LogUtils.dTag("EditHtmlWallpaper","This wallpaper will be deleted");
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