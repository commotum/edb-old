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
import datomic.Datom;

public final class extensions$get_else
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.extensions", (String)"ensure-sv-attrid");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__4 = RT.keyword(null, (String)"aevt");

    public static Object invokeStatic(Object db2, Object e, Object attr, Object v) {
        Object object;
        Object temp__5455__auto__18014;
        if (Util.identical((Object)v, null)) {
            throw (Throwable)new IllegalArgumentException("nil default value not supported");
        }
        Object object2 = attr;
        attr = null;
        Object attrid = ((IFn)const__1.getRawRoot()).invoke(db2, object2);
        Object object3 = db2;
        db2 = null;
        Object object4 = attrid;
        attrid = null;
        Object object5 = e;
        e = null;
        Object object6 = temp__5455__auto__18014 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object3, (Object)const__4, (Object)Tuple.create((Object)object4, (Object)object5)));
        if (object6 != null && object6 != Boolean.FALSE) {
            Object d;
            Object object7 = temp__5455__auto__18014;
            temp__5455__auto__18014 = null;
            Object object8 = d = object7;
            d = null;
            object = ((Datom)object8).v();
        } else {
            object = v;
            Object var3_3 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return extensions$get_else.invokeStatic(object5, object6, object7, object8);
    }
}

