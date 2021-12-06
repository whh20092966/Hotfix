package com.kkapp.hotfix;

import android.util.Log;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author zhanghongjie
 * @date 2019/6/3
 * @description 使用该自定义Log打印日志
 */
public class KkLog {

    private static final int LOG_I = 0;
    private static final int LOG_V = 1;
    private static final int LOG_D = 2;
    private static final int LOG_W = 3;
    private static final int LOG_E = 4;


    public static String getStackTrace(Throwable throwable) {

        String stackTrace = "";
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)){
            throwable.printStackTrace(pw);
            stackTrace = sw.toString();
        } catch (IOException e) {
            Log.e("KkLog", "get stack trace fail !");
        }

        return stackTrace;
    }


}
