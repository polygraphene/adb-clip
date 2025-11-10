/*
Build command:

# framework.jar must contain hidden APIs.
# (aosp source)/out/soong/.intermediates/frameworks/base/framework/android_common/repackaged-jarjar/turbine/framework.jar

mkdir -p out
javac -source 1.8 -target 1.8 -cp framework.jar Clip.java -d out
$ANDROID_SDK/build-tools/36.0.0/d8 out/Clip.class --output out/clip.jar

Deploy:
adb push out/clip.jar /data/local/tmp

Run command (in adb shell):
ANDROID_ROOT=/system ANDROID_DATA=/data CLASSPATH=/data/local/tmp/clip.jar app_process /system/bin Clip
*/

import android.app.ActivityThread;
import android.app.Application;
import android.app.LoadedApk;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.IClipboard;
import android.os.Looper;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;

public class Clip {
    public static void main(String[] args) {
        (new Clip()).run(args);
    }

    private void run(String[] args) {
        try {
            boolean DEBUG = false;

            if (Process.myUid() == 0) {
                // root user can not call getPrimaryClip
                int r1 = Process.setUid(2000);
                int r2 = Process.setGid(2000);

                if (DEBUG) System.out.println("setuid: " + r1 + " " + r2);
            }
            if (DEBUG) System.out.println("uid: " + Process.myUid());
            Looper.prepareMainLooper();

            ActivityThread actThread = ActivityThread.systemMain();

            Context systemContext = actThread.getSystemContext();
            Context appContext;
            if (DEBUG) System.out.println("system: " + systemContext.getOpPackageName());

            if (DEBUG) System.out.println("Current uid=" + Process.myUid());
            if (Process.myUid() == 2000) {
                // System Context's opPackageName is "android". It is bad for app ops permission check.
                // We need "com.android.shell".

                LoadedApk pi = actThread.getPackageInfo("com.android.shell", systemContext.getResources().getCompatibilityInfo(),
                        Context.CONTEXT_REGISTER_PACKAGE, 0);

                appContext = pi.makeApplication(false, null);
                // appContext.getOpPackageName() is now "com.android.shell"
            } else {
                // uid 1000 can use system context. Other uid is not supported (fail).
                appContext = systemContext;
            }
            if (DEBUG) System.out.println("app: " + appContext.getOpPackageName());

            ClipboardManager cm = (ClipboardManager) appContext.getSystemService(Context.CLIPBOARD_SERVICE);

            if (args.length == 0) {
                ClipData data = cm.getPrimaryClip();
                if (DEBUG) System.out.println("data=" + data);
                if(data == null || data.getItemCount() == 0) {
                    // Empty
                }else{
                    System.out.println("" + data.getItemAt(0).getText().toString());
                }
            } else {
                 ClipData data = ClipData.newPlainText("" /* Label */, args[0]);
                 cm.setPrimaryClip(data);
            }

            // Unstable API. Works on Android 15 and 16, but maybe does not work on other versions.
            //
            // IClipboard clipboardManager = (IClipboard)IClipboard.Stub.asInterface(ServiceManager.getService(Context.CLIPBOARD_SERVICE));

            // if (args.length == 0) {
            //     // No argument = get operation
            //     //ClipData.Item item = clipboardManager.getPrimaryClip("com.android.shell", null, 0, 0).getItemAt(0);
            //     ClipData.Item item = clipboardManager.getPrimaryClip("com.android.shell", null, 0, 0).getItemAt(0);
            //     String pasteData = item.getText().toString();
            //     System.out.println(pasteData);
            // } else {
            //     ClipData data = ClipData.newPlainText("" /* Label */, args[0]);
            //     clipboardManager.setPrimaryClip(data, "com.android.shell", null, 0, 0);
            // }
        }catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        // ART runtime sleeps 800ms (400ms * 2) when shutdown app_process.
        // It causes annoying delay of command execution.
        // Call exit to bypass it.
        System.exit(0);
    }
}
