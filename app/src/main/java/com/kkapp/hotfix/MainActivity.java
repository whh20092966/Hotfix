package com.kkapp.hotfix;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.kkapp.hotfix.test.Fix;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTest(View view){
        Fix fil = new Fix();

        String dexPath = getCacheDir().getAbsolutePath() + "/out.dex";
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath,  getCacheDir().getAbsolutePath(), null, this.getClassLoader());

        //还是原来的类
        /*try {
            Class<?> clazz = dexClassLoader.loadClass("com.kkapp.hotfix.test.A");
            Constructor<?> constructor = clazz.getConstructor(null);
            Object obj = constructor.newInstance();
            Method m = clazz.getDeclaredMethod("getI");

            Log.d("HotFix", "A call getI(): " + String.valueOf(m.invoke(obj)));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.d("HotFix", "loadClass ERROR: " + e.getMessage());
        }*/


        //如果引用的另外的dex 会报错
        try {
            Class<?> clazz = dexClassLoader.loadClass("com.kkapp.hotfix.test.C");
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object obj = constructor.newInstance("hello word");
           // Field field = clazz.getField("s");

            Log.d("HotFix", "C call getI(): " + obj.toString());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.d("HotFix", "loadClass ERROR: " + e.getMessage());
        }

        Toast.makeText(this,  fil.c(), Toast.LENGTH_SHORT).show();
    }
}