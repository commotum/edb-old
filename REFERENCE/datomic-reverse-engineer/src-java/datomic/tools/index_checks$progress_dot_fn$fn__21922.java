/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class index_checks$progress_dot_fn$fn__21922
extends RestFn {
    Object n;
    Object c;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mod");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"inc");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"*out*");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"*err*");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"flush");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public index_checks$progress_dot_fn$fn__21922(Object object, Object object2) {
        this.n = object;
        this.c = object2;
    }

    public Object doInvoke(Object _) {
        Object object;
        if (Numbers.isZero((Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.c, const__3.getRawRoot()), this.n))) {
            Object object2;
            ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke((Object)const__6, const__7.get()));
            try {
                ((IFn)const__8.getRawRoot()).invoke((Object)".");
                object2 = ((IFn)const__9.getRawRoot()).invoke();
            }
            finally {
                ((IFn)const__10.getRawRoot()).invoke();
            }
            object = object2;
        } else {
            object = null;
        }
        return object;
    }

    public int getRequiredArity() {
        return 0;
    }
}

