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
package datomic;

import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class integrity$progress_dot_fn$fn__22035
extends RestFn {
    Object n;
    Object c;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mod");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"inc");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"flush");

    public integrity$progress_dot_fn$fn__22035(Object object, Object object2) {
        this.n = object;
        this.c = object2;
    }

    public Object doInvoke(Object _) {
        Object object;
        if (Numbers.isZero((Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this_.c, const__3.getRawRoot()), this_.n))) {
            ((IFn)const__4.getRawRoot()).invoke((Object)".");
            integrity$progress_dot_fn$fn__22035 this_ = null;
            object = ((IFn)const__5.getRawRoot()).invoke();
        } else {
            object = null;
        }
        return object;
    }

    public int getRequiredArity() {
        return 0;
    }
}

