package com.kkapp.hotfix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Array;
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

    public void onLoadDex(View view){
        String rootPath = "/data/data/com.kkapp.hotfix/cache";
        String dexPath = rootPath + "/out.dex";
        DexClassLoader dexClassLoader = new DexClassLoader(dexPath,  rootPath, null, this.getClassLoader());

        //还是原来的类
        try {
            Class<?> clazz = dexClassLoader.loadClass("com.kkapp.hotfix.test.A");
            Constructor<?> constructor = clazz.getConstructor(null);
            Object obj = constructor.newInstance();
            Method m = clazz.getDeclaredMethod("getI");

            Log.d("HotFix", "A call getI(): " + String.valueOf(m.invoke(obj)));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.d("HotFix", "loadClass ERROR: " + e.getMessage());
        }

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

        //如果不使用dexClassLoader load dex 能否加载到那个类？
        try {
            Class<?> clazz = Class.forName("com.kkapp.hotfix.test.C");
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object obj = constructor.newInstance("hello word");
            // Field field = clazz.getField("s");

            Log.d("HotFix", "C call getI(): " + obj.toString());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.d("HotFix", "loadClass ERROR: " + KkLog.getStackTrace(e));
        }
    }

    public void onInjectDex(View view){
        String rootPath = "/data/data/com.kkapp.hotfix/cache";
        String dexPath = rootPath + "/out.dex";

        injectDex(this, dexPath);

        //注入dex, 使用默认的class load 是否能加载到 C
        try {
            Class<?> clazz = Class.forName("com.kkapp.hotfix.test.C");
            Constructor<?> constructor = clazz.getConstructor(String.class);
            Object obj = constructor.newInstance("hello word");
            // Field field = clazz.getField("s");

            Log.d("HotFix", "C call getI(): " + obj.toString());
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            Log.d("HotFix", "loadClass ERROR: " + KkLog.getStackTrace(e));
        }
    }

    private static void injectDex(Context context, String patch) {

        try {
            ClassLoader classLoader = context.getClassLoader();
            Field pathList = getField(classLoader, "pathList");
            Object pathListObject = pathList.get(classLoader);

            Field dexElementsField = getField(pathListObject, "dexElements");
            //1.先记录插入patch前 dexElements的长度
            int oldLength = ((Object[]) dexElementsField.get(pathListObject)).length;

            //2.插入patch.dex
            Method method = getMethod(classLoader, "addDexPath", String.class);
            method.invoke(classLoader, patch);

            Object[] newDexElements = (Object[]) dexElementsField.get(pathListObject);
            //3.读取插入patch后 dexElements的长度
            int newLength = newDexElements.length;
            //4.前后交换，并重新反射赋值dexElements
            Object[] resultElements = (Object[]) Array.newInstance(newDexElements.getClass().getComponentType(),
                    newLength);
            System.arraycopy(newDexElements, 0, resultElements, newLength - oldLength, oldLength);
            System.arraycopy(newDexElements, oldLength, resultElements, 0, newLength - oldLength);
            dexElementsField.set(pathListObject, resultElements);

            for (Object newElement : resultElements) {
                Log.d("alvin",newElement.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.d("HotFix", "installPatch=" + e.toString());
        }
    }

    private static Field getField(Object instance, String name) throws NoSuchFieldException {
        for (Class<?> cls = instance.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Field declaredField = cls.getDeclaredField(name);
                //如果反射获取的类 方法 属性不是public 需要设置权限
                declaredField.setAccessible(true);
                return declaredField;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        throw new NoSuchFieldException("Field: " + name + " not found in " + instance.getClass());
    }


    public static Method getMethod(Object instance, String name, Class<?>... parameterTypes) throws NoSuchFieldException {
        for (Class<?> cls = instance.getClass(); cls != null; cls = cls.getSuperclass()) {
            try {
                Method methodMethod = cls.getDeclaredMethod(name, parameterTypes);
                //如果反射获取的类 方法 属性不是public 需要设置权限
                methodMethod.setAccessible(true);
                return methodMethod;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        throw new NoSuchFieldException("Field: " + name + " not found in " + instance.getClass());
    }
}