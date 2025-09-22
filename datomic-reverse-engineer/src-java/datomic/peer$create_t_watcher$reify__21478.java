/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import java.util.Comparator;

public final class peer$create_t_watcher$reify__21478
implements Comparator,
IObj {
    final IPersistentMap __meta;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public peer$create_t_watcher$reify__21478(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public peer$create_t_watcher$reify__21478() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new peer$create_t_watcher$reify__21478(iPersistentMap);
    }

    public int compare(Object o1, Object o2) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = o1;
        o1 = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = o2;
        o2 = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        return Util.compare((Object)object2, (Object)object4);
    }
}

