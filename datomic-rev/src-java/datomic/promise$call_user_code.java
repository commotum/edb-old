/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.util.concurrent.Executor;

public final class promise$call_user_code
extends AFunction {
    public static Object invokeStatic(Object exec, Object listener) {
        Object var2_2;
        try {
            Object object = exec;
            exec = null;
            Object object2 = listener;
            listener = null;
            ((Executor)object).execute((Runnable)object2);
            var2_2 = null;
        }
        catch (Throwable t2) {
            Object v5;
            Object t2;
            Thread.UncaughtExceptionHandler temp__5455__auto__10383;
            Thread.UncaughtExceptionHandler uncaughtExceptionHandler = temp__5455__auto__10383 = Thread.getDefaultUncaughtExceptionHandler();
            if (uncaughtExceptionHandler != null && uncaughtExceptionHandler != Boolean.FALSE) {
                Thread.UncaughtExceptionHandler h;
                Thread.UncaughtExceptionHandler uncaughtExceptionHandler2 = temp__5455__auto__10383;
                temp__5455__auto__10383 = null;
                Thread.UncaughtExceptionHandler uncaughtExceptionHandler3 = h = uncaughtExceptionHandler2;
                h = null;
                t2 = null;
                uncaughtExceptionHandler3.uncaughtException(Thread.currentThread(), t2);
                v5 = null;
            } else {
                t2 = null;
                t2.printStackTrace();
                v5 = null;
            }
            var2_2 = v5;
        }
        return var2_2;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return promise$call_user_code.invokeStatic(object3, object4);
    }
}

