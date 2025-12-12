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
import java.io.PrintWriter;
import java.io.StringWriter;

public final class slf4j$exception_string
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object t) {
        StringWriter s = new StringWriter();
        PrintWriter p = new PrintWriter(s);
        Object object = t;
        t = null;
        PrintWriter printWriter = p;
        p = null;
        ((Throwable)object).printStackTrace(printWriter);
        StringWriter stringWriter = s;
        s = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)stringWriter);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return slf4j$exception_string.invokeStatic(object2);
    }
}

