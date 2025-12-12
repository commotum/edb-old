/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import datomic.db$add_ensured_data$fn__14094;
import datomic.db.IDbImpl;
import java.util.ArrayList;

public final class db$add_ensured_data
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"long-add!");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"ensure-tx");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"considered-datoms"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db_before, Object data2, Object added, Object idmap, Object tx_stat_registers) {
        Object object = added;
        added = null;
        Object db_after = ((IDbImpl)db_before).addData(((IFn)const__0.getRawRoot()).invoke((Object)new db$add_ensured_data$fn__14094(db_before), data2), (ArrayList)object, tx_stat_registers);
        IFn.OLO oLO = (IFn.OLO)const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = tx_stat_registers;
        tx_stat_registers = null;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        oLO.invokePrim(object3, (long)RT.count((Object)data2));
        Object object4 = db_before;
        db_before = null;
        Object object5 = data2;
        data2 = null;
        Object object6 = idmap;
        idmap = null;
        ((IFn)const__4.getRawRoot()).invoke(object4, db_after, object5, object6);
        Object object7 = db_after;
        db_after = null;
        return object7;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4, Object object5) {
        Object object6 = object;
        object = null;
        Object object7 = object2;
        object2 = null;
        Object object8 = object3;
        object3 = null;
        Object object9 = object4;
        object4 = null;
        Object object10 = object5;
        object5 = null;
        return db$add_ensured_data.invokeStatic(object6, object7, object8, object9, object10);
    }
}

