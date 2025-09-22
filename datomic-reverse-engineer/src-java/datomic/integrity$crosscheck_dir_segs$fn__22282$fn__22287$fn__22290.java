/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;

public final class integrity$crosscheck_dir_segs$fn__22282$fn__22287$fn__22290
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"dir-t"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"seg-t"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public Object invoke(Object p1__22276_SHARP_) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = p1__22276_SHARP_;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object3 = p1__22276_SHARP_;
        p1__22276_SHARP_ = null;
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__1__ = __site__1__.fault(object3);
            object4 = __thunk__1__.get(object3);
        }
        integrity$crosscheck_dir_segs$fn__22282$fn__22287$fn__22290 this_ = null;
        return Util.equiv((Object)object2, (Object)object4) ? Boolean.TRUE : Boolean.FALSE;
    }
}

