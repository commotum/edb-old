/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Database;

public final class excise$pred_and_extent$datoms__14833$fn__14837
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__1 = RT.keyword(null, (String)"vaet");

    public excise$pred_and_extent$datoms__14833$fn__14837(Object object) {
        this.db = object;
    }

    public Object invoke(Object p1__14814_SHARP_) {
        Object object = p1__14814_SHARP_;
        p1__14814_SHARP_ = null;
        excise$pred_and_extent$datoms__14833$fn__14837 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)((Database)this_.db).history(), (Object)const__1, (Object)Tuple.create((Object)object));
    }
}

