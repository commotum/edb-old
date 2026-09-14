/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LO
 *  clojure.lang.IFn$OLO
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$prefetch_identity$fn__13992
extends AFunction {
    Object v;
    Object tx_stat_registers;
    Object db;
    Object e;
    Object a;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"tempid?");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"find-avet");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"long-add!");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"res-pf-ms"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public db$prefetch_identity$fn__13992(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.v = object;
        this.tx_stat_registers = object2;
        this.db = object3;
        this.e = object4;
        this.a = object5;
    }

    public Object invoke() {
        Object object;
        Object object2 = ((IFn.LO)const__0.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)this.e)));
        if (object2 != null && object2 != Boolean.FALSE) {
            long start__13414__auto__13994 = System.nanoTime();
            Object ret__13415__auto__13995 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.db, this.a, this.v));
            IFn.OLO oLO = (IFn.OLO)const__3.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object3 = this.tx_stat_registers;
            Object object4 = iLookupThunk.get(object3);
            if (iLookupThunk == object4) {
                __thunk__0__ = __site__0__.fault(object3);
                object4 = __thunk__0__.get(object3);
            }
            oLO.invokePrim(object4, System.nanoTime() - start__13414__auto__13994);
            object = ret__13415__auto__13995;
            Object var3_2 = null;
        } else {
            object = null;
        }
        return object;
    }
}

