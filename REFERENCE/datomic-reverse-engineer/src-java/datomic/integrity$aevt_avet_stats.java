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
import datomic.integrity$aevt_avet_stats$fn__22481;
import datomic.integrity$aevt_avet_stats$fn__22483;
import datomic.integrity$aevt_avet_stats$fn__22485;

public final class integrity$aevt_avet_stats
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.tools", (String)"ever-nohistory-attrs");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"attribute-seq");

    public static Object invokeStatic(Object db2) {
        Object nohists = ((IFn)const__0.getRawRoot()).invoke(db2);
        integrity$aevt_avet_stats$fn__22481 integrity$aevt_avet_stats$fn__22481 = new integrity$aevt_avet_stats$fn__22481(db2);
        Object object = nohists;
        nohists = null;
        Object object2 = db2;
        db2 = null;
        return ((IFn)const__1.getRawRoot()).invoke((Object)integrity$aevt_avet_stats$fn__22481, ((IFn)const__2.getRawRoot()).invoke((Object)new integrity$aevt_avet_stats$fn__22483(object), ((IFn)const__3.getRawRoot()).invoke((Object)new integrity$aevt_avet_stats$fn__22485(), ((IFn)const__4.getRawRoot()).invoke(object2))));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$aevt_avet_stats.invokeStatic(object2);
    }
}

