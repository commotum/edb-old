/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Comparator;

public final class integrity$eavt_dquark_seq$reify__22343
implements Comparator,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"aevt-cmp");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"datom"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"datom"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public integrity$eavt_dquark_seq$reify__22343(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public integrity$eavt_dquark_seq$reify__22343() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new integrity$eavt_dquark_seq$reify__22343(iPersistentMap);
    }

    public int compare(Object x, Object y) {
        Comparator comparator = (Comparator)const__0.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = x;
        x = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = y;
        y = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        integrity$eavt_dquark_seq$reify__22343 this_ = null;
        return comparator.compare(object2, object4);
    }
}

