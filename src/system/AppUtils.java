package system;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * @author powinandroid
 *         跟App相关的辅助类
 */
public class AppUtils {

    private AppUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @param versionFlag constants 有相关说明
     * @return 当前应用的版本名称或者版本号，默认返回格式: 版本名称 V版本号
     */
    public static String getVersionInfo(Context context, int versionFlag) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            switch (versionFlag) {
                case Constants.APPTUILS_VERSION_CODE:
                    return String.valueOf(packageInfo.versionCode);
                case Constants.APPTUILS_VERSION_NAME:
                    return packageInfo.versionName;
                default:
                    return packageInfo.versionName + " V" + packageInfo.versionCode;
            }

        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

}
