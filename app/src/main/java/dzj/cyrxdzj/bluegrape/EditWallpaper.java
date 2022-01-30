package dzj.cyrxdzj.bluegrape;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageReader;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;

public class EditWallpaper extends AppCompatActivity {

    private String wallpaper_id;
    public static final int CHOOSE_IMAGE = 2;
    public String read_file(String path) throws IOException {
        FileReader reader=new FileReader(new File(path));
        char[] temp=new char[500];
        reader.read(temp);
        reader.close();
        return new String(temp);
    }
    public int read_file(String path,byte[] res) {
        FileInputStream reader=null;
        int file_len=0;
        try {
            reader = new FileInputStream(new File(path));
            byte buffer[] = new byte[1024];
            int len = 0;
            int cur=0;
            while ((len = reader.read(buffer,0,buffer.length))>0) {
                for(int i=0;i<len;i++)
                {
                    res[cur+i]=buffer[i];
                }
                file_len+=len;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader!=null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file_len;
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
            EditText wallpaper_name_editor=(EditText)findViewById(R.id.wallpaper_name);
            wallpaper_name_editor.setText(URLDecoder.decode(config.getString("name"),"UTF-8"));
            SeekBar alpha_seekbar=(SeekBar)findViewById(R.id.alpha);
            alpha_seekbar.setProgress(config.getInt("alpha"));
            ArrayAdapter<String> fill_method_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,fill_method_list);
            fill_method_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            ArrayAdapter<String> position_adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,position_list);
            position_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            Spinner fill_method_spinner=(Spinner)findViewById(R.id.fill_method);
            fill_method_spinner.setAdapter(fill_method_adapter);
            Spinner position_spinner=(Spinner)findViewById(R.id.position);
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
}