/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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
import datomic.db$tx_data_for_latest_schema_level$fn__14133;

public final class db$tx_data_for_latest_schema_level
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"drop");
    public static final Var const__3 = RT.var((String)"datomic.db", (String)"bootstrap-txes");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"schema-level"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object db2) {
        IFn iFn = (IFn)const__0.getRawRoot();
        db$tx_data_for_latest_schema_level$fn__14133 db$tx_data_for_latest_schema_level$fn__14133 = new db$tx_data_for_latest_schema_level$fn__14133();
        IFn iFn2 = (IFn)const__1.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = db2;
        db2 = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return iFn.invoke((Object)db$tx_data_for_latest_schema_level$fn__14133, iFn2.invoke(object2, const__3.getRawRoot()));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$tx_data_for_latest_schema_level.invokeStatic(object2);
    }
}

