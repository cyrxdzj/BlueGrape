package dzj.cyrxdzj.bluegrape;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class EditWallpaper extends AppCompatActivity {

    private String wallpaper_id;
    public static final int CHOOSE_IMAGE = 2;
    private EditText wallpaper_name_editor;
    private SeekBar alpha_seekbar;
    private Spinner fill_method_spinner,position_spinner;
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
    public void write_file(String path,byte[] content) throws IOException
    {
        try {
            FileOutputStream writer = new FileOutputStream(new File(path));
            writer.write(content, 0, content.length);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
                .setTitle(getString(R.string.confirm_delete))
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
        setContentView(R.layout.activity_edit_wallpaper);
        Intent intent=getIntent();
        wallpaper_id=intent.getStringExtra("wallpaper_id");
        Log.d("EditWallpaper",wallpaper_id);
        try {
            //初始化配置
            String config_str = read_file("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/config.json");
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
            //初始化组件
            refresh_image();
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
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void refresh_image()
    {
        ImageView wallpaper_view=(ImageView)findViewById(R.id.wallpaper_image);
        wallpaper_view.setImageResource(R.drawable.default_image);
        wallpaper_view.setImageURI(Uri.parse("file:///storage/emulated/0/BlueGrape/"+wallpaper_id+"/image.png"));
    }
    public void choose_image(View view)
    {
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/");
        startActivityForResult(intent,CHOOSE_IMAGE);
    }
    private String getImagePath(Uri uri,String selection){
        String path = null;
        //通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri,null,selection,null,null);
        if(cursor!= null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imagePath=null;
        switch (requestCode){
            case CHOOSE_IMAGE:
                if(resultCode == RESULT_OK){
                    //判断手机系统版本号
                    if(Build.VERSION.SDK_INT>=19){
                        Uri uri = data.getData();
                        if(DocumentsContract.isDocumentUri(this,uri)){
                            //如果是document类型的Uri，则通过document id处理
                            String docId = DocumentsContract.getDocumentId(uri);
                            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                                String id = docId.split(":")[1];  //解析出数字格式的id
                                String selection = MediaStore.Images.Media._ID+"="+id;
                                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
                            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public downloads"),Long.valueOf(docId));
                                imagePath = getImagePath(contentUri,null);
                            }
                        }else if("content".equalsIgnoreCase(uri.getScheme())){
                            //如果是file类型的Uri，直接获取图片路径即可
                            imagePath = getImagePath(uri,null);
                        }else if("file".equalsIgnoreCase(uri.getScheme())){
                            //如果是file类型的Uri，直接获取图片路径即可
                            imagePath = uri.getPath();
                        }
                        Log.d("EditWallpaper",imagePath);
                        Bitmap image= BitmapFactory.decodeFile(imagePath);
                        try {
                            FileOutputStream writer=new FileOutputStream(new File("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/image.png"));
                            image.compress(Bitmap.CompressFormat.PNG,100,writer);
                            writer.flush();
                            writer.close();
                            refresh_image();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    public void save(View view)
    {
        try {
            String save_str = "{\n"+
                    "\t\"name\":\""+ URLEncoder.encode(wallpaper_name_editor.getText().toString(),"UTF-8")+"\",\n"+
                    "\t\"alpha\":"+String.valueOf(alpha_seekbar.getProgress())+",\n"+
                    "\t\"fill_method\":\""+(fill_method_spinner.getSelectedItem().toString()=="左右填充"?"left-right":"top-bottom")+"\",\n"+
                    "\t\"position\":\""+(position_spinner.getSelectedItem().toString()=="左/上位置"?"left-top":"right-bottom")+"\"\n}";
            Log.d("EditWallpaper",save_str);
            write_file("/storage/emulated/0/BlueGrape/"+wallpaper_id+"/config.json",save_str);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        show_info_dialog(getString(R.string.save_successfully),getString(R.string.save_successfully));
    }
    public void button_delete(View view)
    {
        show_delete_question_dialog();
    }
    public void delete()
    {
        Log.d("EditWallpaper","Delete");
        File file_obj=new File("/storage/emulated/0/BlueGrape/"+wallpaper_id);
        delete_dir(file_obj);
        finish();
        //file_obj.delete();
    }
    private void delete_dir(File file) {
        File[] list = file.listFiles();
        if (list != null) {
            for (File temp : list) {
                delete_dir(temp);
            }
        }
        file.delete();
    }
}