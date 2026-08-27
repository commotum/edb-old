/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db$find_last_tx$fn__13597$fn__13599;
import datomic.db.IndexSet;
import datomic.iter.Iter;

public final class db$find_last_tx$fn__13597
extends AFunction {
    Object index;
    Object mid_index;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__4 = RT.var((String)"datomic.iter", (String)"merge-iters");
    public static final Var const__5 = RT.var((String)"datomic.db", (String)"eavt-cmp");
    public static final Var const__6 = RT.var((String)"datomic.btset", (String)"seek");
    public static final Var const__8 = RT.var((String)"datomic.iter", (String)"take-while");

    public db$find_last_tx$fn__13597(Object object, Object object2) {
        this.index = object;
        this.mid_index = object2;
    }

    public Object invoke(Object t) {
        Object object;
        Object G__13598;
        Object object2;
        Object object3;
        Object and__5236__auto__13604;
        Object object4 = t;
        t = null;
        long eid = ((IFn.LLL)const__0.getRawRoot()).invokePrim(3L, RT.uncheckedLongCast((Object)((Number)object4)));
        Object d = ((IFn.LLOLO)const__2.getRawRoot()).invokePrim(eid, 50L, null, eid);
        IFn iFn = (IFn)const__4.getRawRoot();
        Object object5 = const__5.getRawRoot();
        Object object6 = ((IFn)const__6.getRawRoot()).invoke(((IndexSet)this.index).eavt, d);
        IFn iFn2 = (IFn)const__6.getRawRoot();
        Object object7 = and__5236__auto__13604 = this.mid_index;
        if (object7 != null && object7 != Boolean.FALSE) {
            object3 = ((IndexSet)this.mid_index).eavt;
        } else {
            object3 = and__5236__auto__13604;
            and__5236__auto__13604 = null;
        }
        Object object8 = d;
        d = null;
        Object G__135982 = iFn.invoke(object5, object6, iFn2.invoke(object3, object8));
        if (Util.identical((Object)G__135982, null)) {
            object2 = null;
        } else {
            G__135982 = null;
            object2 = G__13598 = ((IFn)const__8.getRawRoot()).invoke((Object)new db$find_last_tx$fn__13597$fn__13599(eid), G__135982);
        }
        if (Util.identical(G__13598, null)) {
            object = null;
        } else {
            Object object9 = G__13598;
            G__13598 = null;
            object = ((Iter)object9).get();
        }
        return object;
    }
}

