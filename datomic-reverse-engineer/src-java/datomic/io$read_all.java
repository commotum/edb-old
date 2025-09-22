/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.io.PushbackReader;
import java.io.Reader;

public final class io$read_all
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*read-eval*");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"transient");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"read");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"persistent!");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"conj!");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public static Object invokeStatic(Object src) {
        Object object;
        Object object2 = src;
        src = null;
        Object src2 = object2;
        try {
            Object object3;
            PushbackReader reader2 = new PushbackReader((Reader)src2);
            ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)Boolean.FALSE));
            try {
                Object tv = ((IFn)const__3.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
                while (true) {
                    Object form;
                    if (Util.identical((Object)tv, (Object)(form = ((IFn)const__4.getRawRoot()).invoke((Object)reader2, (Object)Boolean.FALSE, tv)))) break;
                    Object object4 = tv;
                    tv = null;
                    Object object5 = form;
                    form = null;
                    tv = ((IFn)const__7.getRawRoot()).invoke(object4, object5);
                }
                Object object6 = tv;
                tv = null;
                object3 = ((IFn)const__6.getRawRoot()).invoke(object6);
            }
            finally {
                ((IFn)const__8.getRawRoot()).invoke();
            }
            object = object3;
        }
        finally {
            Object object7 = src2;
            src2 = null;
            ((Reader)object7).close();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return io$read_all.invokeStatic(object2);
    }
}

