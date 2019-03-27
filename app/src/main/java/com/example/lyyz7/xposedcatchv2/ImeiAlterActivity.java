package com.example.lyyz7.xposedcatchv2;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ImeiAlterActivity extends AppCompatActivity{

    private Button IMEICheck;
    private Button IMEIAlter;
    private TextView imei1;
    private TextView imei2;
    private TextView mac;
    private EditText alterImei;

    final int MY_PERMIEAD_CONTACTS = 1;
    private TelephonyManager Phone;

    private void saveData(){
        try {
            //sp键值对保存，文本框里数据
            SharedPreferences sh = this.getSharedPreferences("sPref", Context.MODE_WORLD_READABLE);
            SharedPreferences.Editor sPre = sh.edit();
            sPre.putString("imei", alterImei.getText().toString());
            sPre.apply();
            Toast.makeText(ImeiAlterActivity.this, "数据推送成功，重启应用生效", Toast.LENGTH_SHORT).show();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String formatImei(String imei) {
        int dxml = imei.length();
        if (dxml != 14 && dxml != 16) {
            return imei;
        }
        String imeiRes = "";
        if (dxml == 14) {
            imeiRes =  imei + getimei15(imei);
        }
        if (dxml == 16) {
            imeiRes =  imei + getimei15(imei.substring(0,14));
        }
        return imeiRes;
    }

    private static String getimei15(String imei){
        if (imei.length() == 14) {
            char[] imeiChar=imei.toCharArray();
            int resultInt=0;
            for (int i = 0; i < imeiChar.length; i++) {
                int a=Integer.parseInt(String.valueOf(imeiChar[i]));
                i++;
                final int temp=Integer.parseInt(String.valueOf(imeiChar[i]))*2;
                final int b=temp<10?temp:temp-9;
                resultInt+=a+b;
            }
            resultInt%=10;
            resultInt=resultInt==0?0:10-resultInt;
            return resultInt + "";
        }else{
            return "";
        }
    }

    private boolean IMEICheck(String imei) {
        if(imei.length() != 15)
            return false;
        for(int i = 0; i < imei.length(); i++){
            if(!Character.isDigit(imei.charAt(i)))
                return false;
        }
        try {
            int sum1 = 0, sum2 = 0, temp = 0, total = 0, lastNum = 0;
            for (int i = 0; i < imei.length(); i++) {
                if ((i % 2) == 0) {//奇数位
                    sum1 = sum1 + (imei.charAt(i) - '0');
                } else {//偶数位
                    temp = (imei.charAt(i) - '0') * 2;
                    if (temp < 10) {
                        sum2 = sum2 + temp;
                    } else {
                        sum2 = sum2 + 1 + temp - 10;
                    }
                }
            }
            total = sum1 + sum2;
            //获取个位数
            if ((total % 10) == 0) {
                lastNum = 0;
            } else {
                lastNum = total % 10;
            }
            //校验
            if ((10 - lastNum) != (imei.charAt(imei.length() - 1) - '0'))
                return false;
            else
                return true;
        } catch (Throwable e){
            return false;
        }
    }

    private static String meidValidChar(String meid) {
        if (meid.length() == 14) {
            String myStr[] = { "a", "b", "c", "d", "e", "f" };
            int sum = 0;
            for (int i = 0; i < meid.length(); i++) {
                String param = meid.substring(i, i + 1);
                for (int j = 0; j < myStr.length; j++) {
                    if (param.equalsIgnoreCase(myStr[j])) {
                        param = "1" + String.valueOf(j);
                    }
                }
                if (i % 2 == 0) {
                    sum = sum + Integer.parseInt(param);
                } else {
                    sum = sum + 2 * Integer.parseInt(param) % 16;
                    sum = sum + 2 * Integer.parseInt(param) / 16;
                }
            }
            if (sum % 16 == 0) {
                return "0";
            } else {
                int result = 16 - sum % 16;
                if (result > 9) {
                    result += 65 - 10;
                }
                return result + "";
            }
        } else {
            return "<null>";
        }
    }

    /**
     * Android  6.0 之前（不包括6.0）
     * 必须的权限  <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
     * @param context
     * @return
     */
    private static String getMacDefault(Context context) {
        String mac = "02:00:00:00:00:00";
        if (context == null) {
            return mac;
        }
        WifiManager wifi = (WifiManager) context.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * Android 6.0（包括） - Android 7.0（不包括）
     * @return
     */
    private static String getMacFromFile() {
        String WifiAddress = "02:00:00:00:00:00";
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return WifiAddress;
    }

    /**
     * 遍历循环所有的网络接口，找到接口是 wlan0
     * 必须的权限 <uses-permission android:name="android.permission.INTERNET" />
     * @return
     */
    private static String getMacFromHardware() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }
                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }
                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "02:00:00:00:00:00";
    }

    private static String getMacAddress(Context context) {
        String mac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mac = getMacDefault(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mac = getMacFromFile();
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            mac = getMacFromHardware();
        }
        return mac;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei_alter);

        IMEICheck = (Button)findViewById(R.id.btn_CheckIMEI);
        IMEIAlter = (Button)findViewById(R.id.btn_IMEIAlter);
        imei1 = (TextView)findViewById(R.id.text_imei1);
        imei2 = (TextView)findViewById(R.id.text_imei2);
        mac = (TextView) findViewById(R.id.text_mac);
        alterImei = (EditText)findViewById(R.id.edit_IMEI);
        Phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        IMEICheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //获取权限状态
                    int permissionCheck = ContextCompat.checkSelfPermission(ImeiAlterActivity.this, Manifest.permission.READ_PHONE_STATE);
                    //判断权限是否开启
                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ImeiAlterActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMIEAD_CONTACTS);
                    } else {
                        //设置显示文本的数据
                        String imei = (Phone.getDeviceId() == null) ? "<null>" : Phone.getDeviceId();
                        String valid = "";
                        if(Phone.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM)
                            valid = (IMEICheck(formatImei(imei))) ? "有效" : "无效";
                        else if(Phone.getPhoneType() == TelephonyManager.PHONE_TYPE_CDMA)
                            valid = meidValidChar(imei);

                        String macRes = getMacAddress(ImeiAlterActivity.this);
                        if(Phone.getPhoneType() == TelephonyManager.PHONE_TYPE_GSM)
                            imei1.setText("IMEI: \n" + imei + "(GSM" + valid +  ")");
                        else
                            imei1.setText("MEID: \n" + imei + "(CDMA 校验位" + valid +  ")");
                        imei2.setText("IMSI: \n" + Phone.getSubscriberId());
                        mac.setText("MAC: \n" + macRes);
                    }
                } catch (Throwable e){
                    e.printStackTrace();
                }
            }
        });

        IMEIAlter.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                saveData();
            }
        });
    }
}
