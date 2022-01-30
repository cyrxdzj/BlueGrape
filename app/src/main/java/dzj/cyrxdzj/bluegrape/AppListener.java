package dzj.cyrxdzj.bluegrape;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class AppListener extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("acc","run");
        String packageName = event.getPackageName().toString();
        int eventType = event.getEventType();
        Log.d("accessibility", "packageName = " + packageName + " eventType = " + eventType);
    }

    @Override
    public void onInterrupt() {

    }
}