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

public final class db$can_immediately_toggle_storage_has_avet_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"has-background-indexing?");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"has-values?");

    public static Object invokeStatic(Object db2, Object aid) {
        Object object;
        Object and__5236__auto__13161;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = and__5236__auto__13161 = ((IFn)const__1.getRawRoot()).invoke(db2);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = db2;
            db2 = null;
            Object object4 = aid;
            aid = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3, object4);
        } else {
            object = and__5236__auto__13161;
            Object var2_2 = null;
        }
        return iFn.invoke(object);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$can_immediately_toggle_storage_has_avet_QMARK_.invokeStatic(object3, object4);
    }
}

