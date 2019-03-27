package com.example.lyyz7.xposedcatchv2;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


public class XposedCheckActivity extends AppCompatActivity {

    private Button XposedCheck;
    private TextView XposedRes;
    private TextView XposedFrame;
    private TextView rootRes;

    public void checkXposed(){
        try {
            Object localObject = ClassLoader.getSystemClassLoader().loadClass("de.robv.android.xposed.XposedHelpers").newInstance();
            // 如果加载类失败 则表示当前环境没有xposed
            if (localObject != null) {
                XposedRes.setText("检测到Xposed");
                XposedRes.setTextColor(Color.rgb(233,22,22));
                xposedCatch(localObject, "fieldCache");
                xposedCatch(localObject, "methodCache");
                xposedCatch(localObject, "constructorCache");
            }
            return;
        }
        catch (Throwable localThrowable) {
            XposedRes.setText("未检测到Xposed");
            XposedRes.setTextColor(Color.rgb(101,212,90));
            XposedFrame.setText("未安装Xposed框架");
            XposedFrame.setTextColor(Color.rgb(101,212,90));
        }
    }

    private void xposedCatch(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            Set keySet = ((HashMap<String, String>)(field.get(obj))).keySet();
            if(keySet == null) {
                return;
            }
            if(keySet.isEmpty()) {
                return;
            }
            Iterator v1 = keySet.iterator();
            // 排除无关紧要的类
            StringBuffer frame = new StringBuffer();
            while(v1.hasNext()) {
                Object now = v1.next();
                if(now  == null) {
                    continue;
                }
                else if(((String)now).length() <= 0) {
                    continue;
                }
                else if(((String)now).toLowerCase().startsWith("android.support")) {
                    continue;
                }
                else if(((String)now).toLowerCase().startsWith("javax.")) {
                    continue;
                }
                else if(((String)now).toLowerCase().startsWith("android.webkit")) {
                    continue;
                }
                else if(((String)now).toLowerCase().startsWith("java.util")) {
                    continue;
                }
                else if(((String)now).toLowerCase().startsWith("android.widget")) {
                    continue;
                }
                else if(((String)now).toLowerCase().startsWith("sun.")) {
                    continue;
                } else if(((String)now).toLowerCase().contains("layout")){
                    continue;
                } else {
                    frame.append((String) now + '\n');
                }
            }
            if(frame.length() == 0){
                XposedFrame.setText("没有被安装的框架");
            } else {
                XposedFrame.setText(frame);
                XposedFrame.setTextColor(Color.rgb(233,22,22));
            }
        }
        catch(Throwable v0) {
            v0.printStackTrace();
        }
    }

    public static boolean checkSuperuserApk(){
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e) { }
        return false;
    }

    public static boolean checkRootPathSU() {
        File file = null;
        final String kSuSearchPaths[]={"/system/bin/","/system/xbin/","/system/sbin/","/sbin/","/vendor/bin/"};
        try{
            for(int i=0;i<kSuSearchPaths.length;i++) {
                file = new File(kSuSearchPaths[i]+"su");
                if(file != null && file.exists()){
                    return true;
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_check);

        XposedCheck = (Button) findViewById(R.id.btn_CheckXposed);
        XposedRes = (TextView) findViewById(R.id.text_XposedRes);
        XposedFrame = (TextView) findViewById(R.id.text_XposedFrame);
        rootRes = (TextView) findViewById(R.id.text_root);

        if(checkSuperuserApk() || checkRootPathSU()){
            rootRes.setText("Root状态： 已Root");
            rootRes.setTextColor(Color.rgb(233,22,22));
        }

        XposedCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkXposed();
            }
        });

    }
}
