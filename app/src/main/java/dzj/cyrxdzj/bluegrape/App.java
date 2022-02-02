package dzj.cyrxdzj.bluegrape;

import android.graphics.drawable.Drawable;

public class App {
    public Drawable app_icon;
    public String package_id,app_name;
    public boolean is_apply;
    public App(){}
    public App(Drawable app_icon,String package_id,String app_name,boolean is_apply){this.app_icon=app_icon;this.package_id=package_id;this.app_name=app_name;this.is_apply=is_apply;}
}
