/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.index$add_avet_indexes$sort_and_merge__15566$fn__15570$fn__15571$fn__15572;

public final class index$add_avet_indexes$sort_and_merge__15566$fn__15570$fn__15571
extends AFunction {
    Object root_map;
    Object olookup;
    Object as_of_t;
    Object cstore;
    Object k;
    Object garbage;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"merge-one-index");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"iter-seq");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"avet-cmp");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"common-write-handlers");
    public static final Keyword const__4 = RT.keyword(null, (String)"avet");
    public static final Var const__5 = RT.var((String)"datomic.index", (String)"avet-cmpi");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__7 = 0L;

    public index$add_avet_indexes$sort_and_merge__15566$fn__15570$fn__15571(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this.root_map = object;
        this.olookup = object2;
        this.as_of_t = object3;
        this.cstore = object4;
        this.k = object5;
        this.garbage = object6;
        this.db = object7;
    }

    public Object invoke(Object avet_sorted_datoms) {
        Object object = avet_sorted_datoms;
        avet_sorted_datoms = null;
        index$add_avet_indexes$sort_and_merge__15566$fn__15570$fn__15571 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, this_.cstore, this_.olookup, ((IFn)this_.k).invoke(this_.root_map), ((IFn)const__1.getRawRoot()).invoke(object), this_.garbage, (Object)new index$add_avet_indexes$sort_and_merge__15566$fn__15570$fn__15571$fn__15572(), const__2.getRawRoot(), const__3.getRawRoot(), (Object)Boolean.FALSE, this_.as_of_t, (Object)const__4, null, null, const__5.getRawRoot(), ((IFn)const__6.getRawRoot()).invoke(const__7), ((IFn)const__6.getRawRoot()).invoke(const__7));
    }
}

