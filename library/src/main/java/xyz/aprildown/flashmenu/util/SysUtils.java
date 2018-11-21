package xyz.aprildown.flashmenu.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SysUtils {
    // A device reporting strictly more total memory in megabytes cannot be considered 'low-end'.
    private static final int ANDROID_LOW_MEMORY_DEVICE_THRESHOLD_MB = 512;
    private static final int ANDROID_O_LOW_MEMORY_DEVICE_THRESHOLD_MB = 1024;

    private static final String TAG = "SysUtils";

    private static Boolean sLowEndDevice;

    /**
     * Return the amount of physical memory on this device in kilobytes.
     *
     * @return Amount of physical memory in kilobytes, or 0 if there was
     * an error trying to access the information.
     */
    private static int detectAmountOfPhysicalMemoryKB() {
        // Extract total memory RAM size by parsing /proc/meminfo, note that
        // this is exactly what the implementation of sysconf(_SC_PHYS_PAGES)
        // does. However, it can't be called because this method must be
        // usable before any native code is loaded.

        // An alternative is to use ActivityManager.getMemoryInfo(), but this
        // requires a valid ActivityManager handle, which can only come from
        // a valid Context object, which itself cannot be retrieved
        // during early startup, where this method is called. And making it
        // an explicit parameter here makes all call paths _much_ more
        // complicated.

        Pattern pattern = Pattern.compile("^MemTotal:\\s+([0-9]+) kB$");
        // Synchronously reading files in /proc in the UI thread is safe.
        StrictMode.ThreadPolicy oldPolicy = StrictMode.allowThreadDiskReads();
        try {
            try (FileReader fileReader = new FileReader("/proc/meminfo")) {
                try (BufferedReader reader = new BufferedReader(fileReader)) {
                    String line;
                    for (; ; ) {
                        line = reader.readLine();
                        if (line == null) {
                            Log.w(TAG, "/proc/meminfo lacks a MemTotal entry?");
                            break;
                        }
                        Matcher m = pattern.matcher(line);
                        if (!m.find()) continue;

                        int totalMemoryKB = Integer.parseInt(m.group(1));
                        // Sanity check.
                        if (totalMemoryKB <= 1024) {
                            Log.w(TAG, "Invalid /proc/meminfo total size in kB: " + m.group(1));
                            break;
                        }

                        return totalMemoryKB;
                    }

                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Cannot get total physical size from /proc/meminfo", e);
        } finally {
            StrictMode.setThreadPolicy(oldPolicy);
        }

        return 0;
    }

    /**
     * @return Whether or not this device should be considered a low end device.
     */
    public static boolean isLowEndDevice() {
        if (sLowEndDevice == null) {
            sLowEndDevice = detectLowEndDevice();
        }
        return sLowEndDevice;
    }

    private static boolean detectLowEndDevice() {
//        assert CommandLine.isInitialized();
//        if (CommandLine.getInstance().hasSwitch(BaseSwitches.ENABLE_LOW_END_DEVICE_MODE)) {
//            return true;
//        }
//        if (CommandLine.getInstance().hasSwitch(BaseSwitches.DISABLE_LOW_END_DEVICE_MODE)) {
//            return false;
//        }

        Integer sAmountOfPhysicalMemoryKB = detectAmountOfPhysicalMemoryKB();
        boolean isLowEnd;
        if (sAmountOfPhysicalMemoryKB <= 0) {
            isLowEnd = false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            isLowEnd = sAmountOfPhysicalMemoryKB / 1024 <= ANDROID_O_LOW_MEMORY_DEVICE_THRESHOLD_MB;
        } else {
            isLowEnd = sAmountOfPhysicalMemoryKB / 1024 <= ANDROID_LOW_MEMORY_DEVICE_THRESHOLD_MB;
        }

        // For evaluation purposes check whether our computation agrees with Android API value.
        Context appContext = ContextUtils.getApplicationContext();
        boolean isLowRam = false;
        if (appContext != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            isLowRam = ((ActivityManager) ContextUtils.getApplicationContext().getSystemService(
                    Context.ACTIVITY_SERVICE))
                    .isLowRamDevice();
        }

        return isLowEnd;
    }
}
