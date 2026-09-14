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

public final class s3$get_bucket$fn__23286
extends AFunction {
    Object bucket_name;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"name"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public s3$get_bucket$fn__23286(Object object) {
        this.bucket_name = object;
    }

    public Object invoke(Object b) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = b;
        b = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        s3$get_bucket$fn__23286 this_ = null;
        return Util.equiv((Object)object2, (Object)this_.bucket_name) ? Boolean.TRUE : Boolean.FALSE;
    }
}

