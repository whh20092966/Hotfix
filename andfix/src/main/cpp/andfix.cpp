#include <jni.h>
#include <stdio.h>
#include <cassert>
#include <stdlib.h>
#include <cstring>
#include "common.h"

#define JNIREG_CLASS "com/alipay/euler/andfix/AndFix"

//dalvik
extern jboolean dalvik_setup(JNIEnv* env, int apilevel);
extern void dalvik_replaceMethod(JNIEnv* env, jobject src, jobject dest);
extern void dalvik_setFieldFlag(JNIEnv* env, jobject field);
//art
extern jboolean art_setup(JNIEnv* env, int apilevel);
extern void art_replaceMethod(JNIEnv* env, jobject src, jobject dest);
extern void art_setFieldFlag(JNIEnv* env, jobject field);

static bool isArt;

static int artMethodSize = 0;

static jboolean setup(JNIEnv* env, jclass clazz, jboolean isart,
                      jint apilevel) {
    isArt = isart;
    LOGD("vm is: %s , apilevel is: %i", (isArt ? "art" : "dalvik"),
         (int )apilevel);
    if (isArt) {
        return art_setup(env, (int) apilevel);
    } else {
        return dalvik_setup(env, (int) apilevel);
    }
}

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

static void setFieldFlag(JNIEnv* env, jclass clazz, jobject field) {
    if (isArt) {
        art_setFieldFlag(env, field);
    } else {
        dalvik_setFieldFlag(env, field);
    }
}

static void initArtMethodSize(JNIEnv* env,  jclass clazz, jobject fun1, jobject fun2){
    char * meth1 = (char *) env->FromReflectedMethod(fun1);
    char * meth2 = (char *) env->FromReflectedMethod(fun2);

    artMethodSize = (meth2 - meth1);
    LOGD("artMethodSize size %i", artMethodSize);
}

/*
 * JNI registration.
 */
static JNINativeMethod gMethods[] = {
/* name, signature, funcPtr */
        { "setup", "(ZI)Z", (void*) setup },
        { "replaceMethod","(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V",(void*) replaceMethod },
        { "setFieldFlag","(Ljava/lang/reflect/Field;)V", (void*) setFieldFlag },
        {"initArtMethodSize", "(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V", (void*) initArtMethodSize},
        };

/*
 * Register several native methods for one class.
 */
static int registerNativeMethods(JNIEnv* env, const char* className,
                                 JNINativeMethod* gMethods, int numMethods) {
    jclass clazz;
    clazz = env->FindClass(className);
    if (clazz == NULL) {
        return JNI_FALSE;
    }
    if (env->RegisterNatives(clazz, gMethods, numMethods) < 0) {
        return JNI_FALSE;
    }

    return JNI_TRUE;
}

/*
 * Register native methods for all classes we know about.
 */
static int registerNatives(JNIEnv* env) {
    if (!registerNativeMethods(env, JNIREG_CLASS, gMethods,
                               sizeof(gMethods) / sizeof(gMethods[0])))
        return JNI_FALSE;

    return JNI_TRUE;
}

/*
 * Set some test stuff up.
 *
 * Returns the JNI version on success, -1 on failure.
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env = NULL;
    jint result = -1;

    if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        return -1;
    }
    assert(env != NULL);

    if (!registerNatives(env)) { //注册
        return -1;
    }
    /* success -- return valid version number */
    result = JNI_VERSION_1_4;

    return result;
}