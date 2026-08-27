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

public final class query$ref_val
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-kw");
    public static final Var const__1 = RT.var((String)"datomic.query", (String)"emap");

    public static Object invokeStatic(Object db2, Object ref_QMARK_, Object v) {
        Object object;
        Object object2 = ref_QMARK_;
        ref_QMARK_ = null;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object or__5238__auto__19144;
            Object object3 = or__5238__auto__19144 = ((IFn)const__0.getRawRoot()).invoke(db2, v);
            if (object3 != null && object3 != Boolean.FALSE) {
                object = or__5238__auto__19144;
                or__5238__auto__19144 = null;
            } else {
                Object object4 = db2;
                db2 = null;
                Object object5 = v;
                v = null;
                object = ((IFn)const__1.getRawRoot()).invoke(object4, object5);
            }
        } else {
            object = v;
            Object var2_2 = null;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return query$ref_val.invokeStatic(object4, object5, object6);
    }
}

