/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class ddb$deattr
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");
    public static final AFn const__2 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"n")});
    public static final Var const__3 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final AFn const__5 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"s")});
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"ex-info");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"n"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"s"));
    static ILookupThunk __thunk__1__ = __site__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object m) {
        G__17711 = ((IFn)ddb$deattr.const__0.getRawRoot()).invoke(((IFn)ddb$deattr.const__1.getRawRoot()).invoke(m));
        switch (Util.hash((Object)G__17711)) {
            case 1013911283: {
                if (Util.equiv((Object)G__17711, (Object)ddb$deattr.const__2)) {
                    v0 = (IFn)ddb$deattr.const__3.getRawRoot();
                    v1 = ddb$deattr.__thunk__0__;
                    v2 = m;
                    m = null;
                    v3 = v1.get(v2);
                    if (v1 == v3) {
                        ddb$deattr.__thunk__0__ = ddb$deattr.__site__0__.fault(v2);
                        v3 = ddb$deattr.__thunk__0__.get(v2);
                    }
                    v4 = v0.invoke(v3);
                    break;
                }
                ** GOTO lbl28
            }
            case 1013911711: {
                if (Util.equiv((Object)G__17711, (Object)ddb$deattr.const__5)) {
                    v5 = ddb$deattr.__thunk__1__;
                    v6 = m;
                    m = null;
                    v4 = v5.get(v6);
                    if (v5 != v4) break;
                    ddb$deattr.__thunk__1__ = ddb$deattr.__site__1__.fault(v6);
                    v4 = ddb$deattr.__thunk__1__.get(v6);
                    break;
                }
            }
lbl28:
            // 4 sources

            default: {
                v7 = m;
                m = null;
                throw (Throwable)((IFn)ddb$deattr.const__7.getRawRoot()).invoke((Object)"Could not parse DDB item ", v7);
            }
        }
        return v4;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$deattr.invokeStatic(object2);
    }
}

