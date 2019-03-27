package com.example.lyyz7.xposedcatchv2;

import android.app.Activity;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookMethod implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        //获得Sharedpreference保存的数据
        final XSharedPreferences sPre = new XSharedPreferences(this.getClass().getPackage().getName(), "sPref");

        XposedHelpers.findAndHookMethod(TelephonyManager.class.getName(), loadPackageParam.classLoader, "getDeviceId", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                //具体的hook操作就这一行，设置getDeviceId（）的返回
                param.setResult(sPre.getString("imei", null));
                //打印log
                XposedBridge.log("hook ---getDeviceId***after " + param.getResult());
            }
        });

    }
}
