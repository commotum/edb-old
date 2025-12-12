/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$bootstrap_maybe_resolve
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"contains?");
    public static final AFn const__5 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword((String)"db", (String)"ident"), 10L});
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"resolve-id");

    public static Object invokeStatic(Object db2, Object a, Object v) {
        Object object;
        Object or__5238__auto__13614;
        Object object2;
        Object object3;
        Object and__5236__auto__13613;
        Object object4 = and__5236__auto__13613 = ((IFn)const__0.getRawRoot()).invoke(v);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = a;
            a = null;
            object3 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke((Object)const__5, object5));
        } else {
            object3 = and__5236__auto__13613;
            and__5236__auto__13613 = null;
        }
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object6 = db2;
            db2 = null;
            object2 = ((IFn)const__6.getRawRoot()).invoke(object6, v);
        } else {
            object2 = null;
        }
        Object object7 = or__5238__auto__13614 = object2;
        if (object7 != null && object7 != Boolean.FALSE) {
            object = or__5238__auto__13614;
            or__5238__auto__13614 = null;
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
        return db$bootstrap_maybe_resolve.invokeStatic(object4, object5, object6);
    }
}

