/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class extensions$missing_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__3 = RT.keyword(null, (String)"aevt");

    public static Object invokeStatic(Object db2, Object e, Object attr) {
        Object object = db2;
        db2 = null;
        Object object2 = attr;
        attr = null;
        Object object3 = e;
        e = null;
        return Util.identical((Object)((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(object, (Object)const__3, (Object)Tuple.create((Object)object2, (Object)object3))), null) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return extensions$missing_QMARK_.invokeStatic(object4, object5, object6);
    }
}

