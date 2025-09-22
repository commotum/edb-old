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
import datomic.db.Db;
import datomic.db.IndexSet;

public final class index$aevt_attrs_datoms$fn__15545
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"attr-datoms");
    public static final Var const__1 = RT.var((String)"datomic.iter", (String)"merge-iters");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"aevt-cmp");

    public index$aevt_attrs_datoms$fn__15545(Object object) {
        this.db = object;
    }

    public Object invoke(Object attrid) {
        Object object;
        Object temp__5457__auto__15548;
        Object object2;
        Object mid_aevt_datoms;
        Object object3;
        Object temp__5457__auto__15547;
        Object main_aevt_datoms = ((IFn)const__0.getRawRoot()).invoke(this.db, ((IndexSet)((Db)this.db).index).aevt, attrid);
        Object object4 = temp__5457__auto__15547 = ((IndexSet)((Db)this.db).mid_index).aevt;
        if (object4 != null && object4 != Boolean.FALSE) {
            Object mid_aevt;
            Object object5 = temp__5457__auto__15547;
            temp__5457__auto__15547 = null;
            Object object6 = mid_aevt = object5;
            mid_aevt = null;
            object3 = ((IFn)const__0.getRawRoot()).invoke(this.db, object6, attrid);
        } else {
            object3 = null;
        }
        Object object7 = mid_aevt_datoms = object3;
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = main_aevt_datoms;
            main_aevt_datoms = null;
            Object object9 = mid_aevt_datoms;
            mid_aevt_datoms = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object8, object9);
        } else {
            object2 = main_aevt_datoms;
            main_aevt_datoms = null;
        }
        Object aevt_datoms = object2;
        Object object10 = temp__5457__auto__15548 = ((IndexSet)((Db)this.db).history).aevt;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object hist_aevt;
            Object object11 = temp__5457__auto__15548;
            temp__5457__auto__15548 = null;
            Object object12 = hist_aevt = object11;
            hist_aevt = null;
            Object object13 = attrid;
            attrid = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.db, object12, object13);
        } else {
            object = null;
        }
        Object hist_aevt_datoms = object;
        Object object14 = aevt_datoms;
        aevt_datoms = null;
        Object object15 = hist_aevt_datoms;
        hist_aevt_datoms = null;
        return Tuple.create((Object)object14, object15);
    }
}

