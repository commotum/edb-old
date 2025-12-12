/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$prefetch_redundancy_PLUS_uniqueness$fn__13978;

public final class db$prefetch_redundancy_PLUS_uniqueness
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__4 = RT.keyword(null, (String)"dedup-pf-ms");
    public static final Keyword const__5 = RT.keyword(null, (String)"ucheck-pf-ms");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"run!");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"nextT"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public static Object invokeStatic(Object dispatcher, Object tx_stat_registers, Object db2, Object datoms2) {
        Object object;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = db2;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object basis = object3;
        Object object4 = tx_stat_registers;
        tx_stat_registers = null;
        Object map__13977 = object4;
        Object object5 = ((IFn)const__1.getRawRoot()).invoke(map__13977);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__13977;
            map__13977 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object6)));
        } else {
            object = map__13977;
            map__13977 = null;
        }
        Object map__139772 = object;
        Object redundancy = RT.get((Object)map__139772, (Object)const__4);
        Object object7 = map__139772;
        map__139772 = null;
        Object uniqueness = RT.get((Object)object7, (Object)const__5);
        Object object8 = redundancy;
        redundancy = null;
        Object object9 = db2;
        db2 = null;
        Object object10 = uniqueness;
        uniqueness = null;
        Object object11 = basis;
        basis = null;
        Object object12 = dispatcher;
        dispatcher = null;
        Object object13 = datoms2;
        datoms2 = null;
        return ((IFn)const__6.getRawRoot()).invoke((Object)new db$prefetch_redundancy_PLUS_uniqueness$fn__13978(object8, object9, object10, object11, object12), object13);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$prefetch_redundancy_PLUS_uniqueness.invokeStatic(object5, object6, object7, object8);
    }
}

