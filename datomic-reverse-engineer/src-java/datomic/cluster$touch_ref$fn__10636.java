/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.Numbers
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
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class cluster$touch_ref$fn__10636
extends AFunction {
    Object rev;
    Object prev;
    Object k;
    Object cluster;
    private static Class __cached_class__0;
    public static final Keyword const__0;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Keyword const__7;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public cluster$touch_ref$fn__10636(Object object, Object object2, Object object3, Object object4) {
        this.rev = object;
        this.prev = object2;
        this.k = object3;
        this.cluster = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        try {
            v0 = new Object[2];
            v0[0] = cluster$touch_ref$fn__10636.const__0;
            v1 = (IFn)cluster$touch_ref$fn__10636.const__3.getRawRoot();
            v2 = this.cluster;
            this.cluster = null;
            v3 = v2;
            if (Util.classOf((Object)v2) == cluster$touch_ref$fn__10636.__cached_class__0) ** GOTO lbl12
            if (!(v3 instanceof ClusteredStore)) {
                v3 = v3;
                cluster$touch_ref$fn__10636.__cached_class__0 = Util.classOf((Object)v3);
lbl12:
                // 2 sources

                this.k = null;
                this.rev = null;
                v4 = Numbers.inc((Object)this.rev);
                v5 = cluster$touch_ref$fn__10636.__thunk__0__;
                v6 = this.prev;
                v7 = v5.get(v6);
                if (v5 == v7) {
                    cluster$touch_ref$fn__10636.__thunk__0__ = cluster$touch_ref$fn__10636.__site__0__.fault(v6);
                    v7 = cluster$touch_ref$fn__10636.__thunk__0__.get(v6);
                }
                v8 = cluster$touch_ref$fn__10636.const__4.getRawRoot().invoke(v3, this.k, (Object)v4, v7);
            } else {
                v9 = (ClusteredStore)v3;
                this.k = null;
                this.rev = null;
                v10 = Numbers.inc((Object)this.rev);
                v11 = cluster$touch_ref$fn__10636.__thunk__0__;
                v12 = this.prev;
                v13 = v11.get(v12);
                if (v11 == v13) {
                    cluster$touch_ref$fn__10636.__thunk__0__ = cluster$touch_ref$fn__10636.__site__0__.fault(v12);
                    v13 = cluster$touch_ref$fn__10636.__thunk__0__.get(v12);
                }
                v8 = v9.set_ref(this.k, v10, v13);
            }
            if (Util.equiv((Object)cluster$touch_ref$fn__10636.const__2, (Object)v1.invoke(v8))) {
                v14 = this.prev;
                this.prev = null;
            } else {
                v14 = null;
            }
            v0[1] = v14;
            var1_1 = RT.mapUniqueKeys((Object[])v0);
        }
        catch (Throwable t__8983__auto__) {
            v15 = new Object[2];
            v15[0] = cluster$touch_ref$fn__10636.const__7;
            t__8983__auto__ = null;
            v15[1] = t__8983__auto__;
            var1_1 = RT.mapUniqueKeys((Object[])v15);
        }
        return var1_1;
    }

    static {
        const__0 = RT.keyword(null, (String)"returned");
        const__2 = RT.keyword(null, (String)"ok");
        const__3 = RT.var((String)"clojure.core", (String)"deref");
        const__4 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__7 = RT.keyword(null, (String)"threw");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
        __thunk__0__ = __site__0__;
    }
}

