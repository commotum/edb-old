/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class extensions$ensure_sv_attrid
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"require-attrid");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"attribute");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object db2, Object a) {
        Object attr;
        Object attrid = ((IFn)const__0.getRawRoot()).invoke(db2, a);
        Object object = db2;
        db2 = null;
        Object object2 = attr = ((IFn)const__1.getRawRoot()).invoke(object, attrid);
        attr = null;
        if (Util.equiv((long)36L, (Object)((Attribute)object2).cardinality)) {
            Object object3 = a;
            a = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__4.getRawRoot()).invoke((Object)"cardinality-many attrs not supported: ", object3));
        }
        Object var2_2 = null;
        return attrid;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extensions$ensure_sv_attrid.invokeStatic(object3, object4);
    }
}

