/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class common$endpoint_QMARK_
extends AFunction {
    public static final AFn const__3 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"port"), RT.keyword(null, (String)"host")});
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"keys");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return Util.equiv((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)PersistentHashSet.EMPTY, ((IFn)const__5.getRawRoot()).invoke(object))) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$endpoint_QMARK_.invokeStatic(object2);
    }
}

