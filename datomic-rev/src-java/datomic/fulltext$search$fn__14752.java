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

public final class fulltext$search$fn__14752
extends AFunction {
    Object attrid;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.fulltext", (String)"fulltext-index-reader");

    public fulltext$search$fn__14752(Object object, Object object2) {
        this.attrid = object;
        this.db = object2;
    }

    public Object invoke(Object p1__14751_SHARP_) {
        Object object = p1__14751_SHARP_;
        p1__14751_SHARP_ = null;
        fulltext$search$fn__14752 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, object, this_.attrid);
    }
}

