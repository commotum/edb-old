/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class coordination$lookup_endpoint$fn__11694
extends AFunction {
    Object k;
    Object cluster;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__6;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public coordination$lookup_endpoint$fn__11694(Object object, Object object2) {
        this.k = object;
        this.cluster = object2;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = coordination$lookup_endpoint$fn__11694.const__0;
            v1 = (IFn)coordination$lookup_endpoint$fn__11694.const__1.getRawRoot();
            v2 = this.cluster;
            this.cluster = null;
            v3 = v2;
            if (Util.classOf((Object)v2) == coordination$lookup_endpoint$fn__11694.__cached_class__0) ** GOTO lbl12
            if (!(v3 instanceof ClusteredStore)) {
                v3 = v3;
                coordination$lookup_endpoint$fn__11694.__cached_class__0 = Util.classOf((Object)v3);
lbl12:
                // 2 sources

                this.k = null;
                v4 = coordination$lookup_endpoint$fn__11694.const__2.getRawRoot().invoke(v3, this.k);
            } else {
                this.k = null;
                v4 = ((ClusteredStore)v3).get_ref(this.k);
            }
            v5 = temp__5457__auto__11696 = v1.invoke(v4);
            if (v5 != null && v5 != Boolean.FALSE) {
                v6 = temp__5457__auto__11696;
                temp__5457__auto__11696 = null;
                pret = v6;
                v7 = (IFn)coordination$lookup_endpoint$fn__11694.const__3.getRawRoot();
                v8 = (IFn)coordination$lookup_endpoint$fn__11694.const__4.getRawRoot();
                v9 = coordination$lookup_endpoint$fn__11694.__thunk__0__;
                v10 = pret;
                pret = null;
                v11 = v9.get(v10);
                if (v9 == v11) {
                    coordination$lookup_endpoint$fn__11694.__thunk__0__ = coordination$lookup_endpoint$fn__11694.__site__0__.fault(v10);
                    v11 = coordination$lookup_endpoint$fn__11694.__thunk__0__.get(v10);
                }
                v12 = v7.invoke(v8.invoke(v11));
            } else {
                v12 = null;
            }
            v0[1] = v12;
            var3_5 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v13 = new Object[2];
            v13[0] = coordination$lookup_endpoint$fn__11694.const__6;
            t__8983__auto__ = null;
            v13[1] = t__8983__auto__;
            var3_5 = RT.mapUniqueKeys((Object[])v13);
        }
        return var3_5;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__1 = RT.var((String)"clojure.core", (String)"deref");
        const__2 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__3 = RT.var((String)"datomic.coordination", (String)"heartbeat->endpoint");
        const__4 = RT.var((String)"clojure.core", (String)"read-string");
        const__6 = RT.keyword(null, (String)"threw");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

