/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import datomic.db$composites_prefetcher$fn__13947;
import java.util.HashSet;

public final class db$composites_prefetcher
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"constituents"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"comp-pf-ms"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public static Object invokeStatic(Object dispatcher, Object tx_stat_registers, Object db2) {
        HashSet needed_eas = new HashSet();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = db2;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        Object constituents = object2;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = tx_stat_registers;
        tx_stat_registers = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        Object adder = object4;
        Object object5 = dispatcher;
        dispatcher = null;
        Object object6 = constituents;
        constituents = null;
        Object object7 = db2;
        db2 = null;
        Object object8 = adder;
        adder = null;
        HashSet hashSet = needed_eas;
        needed_eas = null;
        return new db$composites_prefetcher$fn__13947(object5, object6, object7, object8, hashSet);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$composites_prefetcher.invokeStatic(object4, object5, object6);
    }
}

