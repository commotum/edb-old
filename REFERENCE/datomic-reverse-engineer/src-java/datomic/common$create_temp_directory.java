/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class common$create_temp_directory
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.java.io", (String)"file");

    public static Object invokeStatic(Object dir) {
        Boolean bl = ((File)((IFn)const__0.getRawRoot()).invoke(dir)).mkdirs() ? Boolean.TRUE : Boolean.FALSE;
        Object object = dir;
        dir = null;
        File f = File.createTempFile(((DateFormat)new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss-")).format(new Date()), "", (File)((IFn)const__0.getRawRoot()).invoke(object));
        boolean or__5238__auto__9173 = f.delete();
        if (!or__5238__auto__9173) {
            throw (Throwable)new IOException();
        }
        boolean or__5238__auto__9174 = f.mkdir();
        if (!or__5238__auto__9174) {
            throw (Throwable)new IOException();
        }
        Object var1_1 = null;
        return f;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$create_temp_directory.invokeStatic(object2);
    }
}

