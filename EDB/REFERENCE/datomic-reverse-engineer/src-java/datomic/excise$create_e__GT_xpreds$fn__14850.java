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
import datomic.excise$create_e__GT_xpreds$fn__14850$fn__14854;

public final class excise$create_e__GT_xpreds$fn__14850
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.excise", (String)"target");
    public static final Var const__1 = RT.var((String)"datomic.excise", (String)"e-target?");
    public static final Var const__2 = RT.var((String)"datomic.excise", (String)"pred-and-extent");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"reduce");

    public excise$create_e__GT_xpreds$fn__14850(Object object) {
        this.db = object;
    }

    public Object invoke(Object m, Object spec) {
        Object object;
        Object e;
        Object object2 = e = ((IFn)const__0.getRawRoot()).invoke(this_.db, spec);
        e = null;
        Object object3 = ((IFn)const__1.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = spec;
            spec = null;
            Object vec__14851 = ((IFn)const__2.getRawRoot()).invoke(this_.db, object4);
            Object epred = RT.nth((Object)vec__14851, (int)RT.intCast((long)0L), null);
            Object object5 = vec__14851;
            vec__14851 = null;
            Object es = RT.nth((Object)object5, (int)RT.intCast((long)1L), null);
            Object object6 = epred;
            epred = null;
            Object object7 = m;
            m = null;
            Object object8 = es;
            es = null;
            excise$create_e__GT_xpreds$fn__14850 this_ = null;
            object = ((IFn)const__6.getRawRoot()).invoke((Object)new excise$create_e__GT_xpreds$fn__14850$fn__14854(object6), object7, object8);
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}

