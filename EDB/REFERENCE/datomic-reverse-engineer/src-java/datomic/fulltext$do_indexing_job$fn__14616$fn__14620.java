/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;

public final class fulltext$do_indexing_job$fn__14616$fn__14620
extends AFunction {
    Object job;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"delete-file-recursively");
    public static final Keyword const__3 = RT.keyword(null, (String)"threw");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"path"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public fulltext$do_indexing_job$fn__14616$fn__14620(Object object) {
        this.job = object;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            IFn iFn = (IFn)const__1.getRawRoot();
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object = this.job;
            this.job = null;
            Object object2 = iLookupThunk.get(object);
            if (iLookupThunk == object2) {
                __thunk__0__ = __site__0__.fault(object);
                object2 = __thunk__0__.get(object);
            }
            objectArray[1] = iFn.invoke(object2);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__3;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

