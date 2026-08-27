/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$crosscheck_indexes$fn__22207$fn__22212$fn__22213
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"mod");
    public static final Object const__2 = 100000L;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"flush");

    public Object invoke(Object n) {
        Object object;
        Object object2 = n;
        n = null;
        if (Numbers.isZero((Object)((IFn)const__1.getRawRoot()).invoke(object2, const__2))) {
            ((IFn)const__3.getRawRoot()).invoke((Object)".");
            integrity$crosscheck_indexes$fn__22207$fn__22212$fn__22213 this_ = null;
            object = ((IFn)const__4.getRawRoot()).invoke();
        } else {
            object = null;
        }
        return object;
    }
}

