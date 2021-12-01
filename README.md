# Hotfix

此项目是 基于Andfix https://github.com/alibaba/AndFix 实现

#### 改动的地方
采用了 mmcpy 实现ArtMethod 字段的替换

``` c
static void replaceMethod(JNIEnv* env, jclass clazz, jobject src,
                          jobject dest) {
    if (artMethodSize < 1){
        LOGD("Init artMehtod size first");
        return;
    }

    if (isArt) {
        //art_replaceMethod(env, src, dest);
        char * smeth = (char *) env->FromReflectedMethod(src);
        char * dmeth = (char *) env->FromReflectedMethod(dest);

        memcpy(smeth, dmeth, artMethodSize);
    } else {
        //dalvik_replaceMethod(env, src, dest);
        LOGD("No support dalvik ");
    }
}
```

### artMethodSize 的主算
采用两个连续的静态方法，使用两个方法地址相减得到 ArtMethod 结构体的大小。

``` c
static void initArtMethodSize(JNIEnv* env,  jclass clazz, jobject fun1, jobject fun2){
    char * meth1 = (char *) env->FromReflectedMethod(fun1);
    char * meth2 = (char *) env->FromReflectedMethod(fun2);

    artMethodSize = (meth2 - meth1);
    LOGD("artMethodSize size %i", artMethodSize);
}

```

### 注意
这种做法只能适配到Android 8,  Android 9就不行了，Android 调用方法时，增加了类的检测。而Andfix 的做就是新增了一个类。

Android 9 会报如下错误

failed to verify: void com.kkapp.hotfix.MainActivity.onTest(android.view.View): [0x5] 'this' argument 'Precise Reference: com.kkapp.hotfix.test.Fix' not instance of 'Reference: com.kkapp.hotfix.test.Fix_CF'