/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.treewalk$create_ids__GT_nodes$fn__19733;

public final class treewalk$create_ids__GT_nodes
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.treewalk", (String)"create-ids->nodes");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__3 = (AFn)Symbol.intern(null, (String)"lookup");

    public static Object invokeStatic(Object lookup, Object allow_missing_QMARK_) {
        Object object = lookup;
        if (object == null || object == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__1.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__2.getRawRoot()).invoke((Object)const__3))));
        }
        Object object2 = allow_missing_QMARK_;
        allow_missing_QMARK_ = null;
        Object object3 = lookup;
        lookup = null;
        return new treewalk$create_ids__GT_nodes$fn__19733(object2, object3);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return treewalk$create_ids__GT_nodes.invokeStatic(object3, object4);
    }

    public static Object invokeStatic(Object lookup) {
        Object object = lookup;
        lookup = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)Boolean.FALSE);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return treewalk$create_ids__GT_nodes.invokeStatic(object2);
    }
}

