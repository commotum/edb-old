/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class query$pull_fv$fn__19501$fn__19503
extends AFunction {
    Object pattern;
    Object srcs;
    Object srcmap;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__1 = RT.var((String)"datomic.pull", (String)"pull");

    public query$pull_fv$fn__19501$fn__19503(Object object, Object object2, Object object3, Object object4) {
        this.pattern = object;
        this.srcs = object2;
        this.srcmap = object3;
        this.db = object4;
    }

    public Object invoke(Object p1__19497_SHARP_) {
        Object object = p1__19497_SHARP_;
        p1__19497_SHARP_ = null;
        query$pull_fv$fn__19501$fn__19503 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(RT.get((Object)this_.srcmap, (Object)this_.db, (Object)((IFn)const__0.getRawRoot()).invoke(this_.srcs)), RT.get((Object)this_.srcmap, (Object)this_.pattern, (Object)this_.pattern), (Object)Tuple.create((Object)object)));
    }
}

