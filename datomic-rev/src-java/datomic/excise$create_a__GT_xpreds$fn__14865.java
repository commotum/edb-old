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

public final class excise$create_a__GT_xpreds$fn__14865
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.excise", (String)"target");
    public static final Var const__1 = RT.var((String)"datomic.excise", (String)"a-target?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"update");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__4 = RT.var((String)"datomic.excise", (String)"pred");

    public excise$create_a__GT_xpreds$fn__14865(Object object) {
        this.db = object;
    }

    public Object invoke(Object m, Object spec) {
        Object object;
        Object a = ((IFn)const__0.getRawRoot()).invoke(this_.db, spec);
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(a);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = m;
            m = null;
            Object object4 = a;
            a = null;
            Object object5 = spec;
            spec = null;
            excise$create_a__GT_xpreds$fn__14865 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object3, object4, const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(this_.db, object5));
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}

