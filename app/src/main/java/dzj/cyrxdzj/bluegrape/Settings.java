package dzj.cyrxdzj.bluegrape;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Settings extends AppCompatActivity {
    private Map<String, Switch> bool_settings_map=new HashMap<String,Switch>();
    private Map<String, EditText> string_settings_map=new HashMap<String,EditText>();
    private Map<String, EditText> int_settings_map=new HashMap<String,EditText>();
    private CommonUtil util=new CommonUtil();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        LinearLayout layout=(LinearLayout) findViewById(R.id.settings_layout);
        try {
            JSONObject settings_object=new JSONObject(util.read_file(util.get_storage_path()+"settings.json"));
            JSONArray settings_template=new JSONArray(util.read_file(R.raw.settings,this));
            for(int i=0;i<settings_template.length();i++)
            {
                JSONObject settings_part=settings_template.getJSONObject(i);
                TextView description_view=new TextView(this);
                description_view.setText(settings_part.getString("description"));
                description_view.setTextSize(24);
                description_view.setTextColor(Color.BLACK);
                LinearLayout.LayoutParams title_layout_params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                title_layout_params.setMargins(8,16,8,0);
                LinearLayout.LayoutParams common_layout_params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                common_layout_params.setMargins(8,8,8,0);
                description_view.setLayoutParams(title_layout_params);
                layout.addView(description_view);
                JSONArray settings_detail_array=settings_part.getJSONArray("settings");
                for(int j=0;j<settings_detail_array.length();j++)
                {
                    JSONObject settings_detail=settings_detail_array.getJSONObject(j);
                    if(settings_detail.getString("type").equals("bool"))
                    {
                        Switch bool_settings_switch=new Switch(this);
                        bool_settings_map.put(settings_detail.getString("id"),bool_settings_switch);
                        bool_settings_switch.setText(settings_detail.getString("title"));
                        try {
                            bool_settings_switch.setChecked(settings_object.getBoolean(settings_detail.getString("id")));
                        } catch (JSONException e) {
                            bool_settings_switch.setChecked(settings_detail.getBoolean("default"));
                        }
                        bool_settings_switch.setLayoutParams(common_layout_params);
                        layout.addView(bool_settings_switch);
                    }
                    else if(settings_detail.getString("type").equals("string"))
                    {
                        EditText string_settings_edit=new EditText(this);
                        TextView string_settings_text=new TextView(this);
                        string_settings_edit.setInputType(InputType.TYPE_CLASS_TEXT);
                        try {
                            string_settings_edit.setText(settings_object.getString(settings_detail.getString("id")));
                        } catch (JSONException e) {
                            string_settings_edit.setText(settings_detail.getString("default"));
                        }
                        string_settings_map.put(settings_detail.getString("id"),string_settings_edit);
                        string_settings_text.setText(settings_detail.getString("title"));
                        string_settings_edit.setLayoutParams(common_layout_params);
                        layout.addView(string_settings_text);
                        layout.addView(string_settings_edit);
                    }
                    else if(settings_detail.getString("type").equals("int"))
                    {
                        EditText int_settings_edit=new EditText(this);
                        TextView int_settings_text=new TextView(this);
                        int_settings_edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                        int_settings_edit.setLayoutParams(common_layout_params);
                        try {
                            int_settings_edit.setText(String.valueOf(settings_object.getInt(settings_detail.getString("id"))));
                        } catch (JSONException e) {
                            int_settings_edit.setText(String.valueOf(settings_detail.getInt("default")));
                        }
                        int_settings_map.put(settings_detail.getString("id"),int_settings_edit);
                        int_settings_text.setText(settings_detail.getString("title"));
                        layout.addView(int_settings_text);
                        layout.addView(int_settings_edit);
                    }
                    try
                    {
                        TextView description_text_view=new TextView(this);
                        description_text_view.setText(settings_detail.getString("description"));
                        description_text_view.setLayoutParams(common_layout_params);
                        layout.addView(description_text_view);
                    }
                    catch (JSONException ignored){}
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}