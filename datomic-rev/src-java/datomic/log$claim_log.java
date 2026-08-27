/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class log$claim_log
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__5;
    public static final Var const__6;
    public static final Var const__7;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs, Object desc) {
        block5: {
            block3: {
                block4: {
                    v0 = temp__5457__auto__16190 = ((IFn)log$claim_log.const__0.getRawRoot()).invoke(cs, ((IFn)log$claim_log.const__1.getRawRoot()).invoke(desc), null);
                    if (v0 == null || v0 == Boolean.FALSE) break block3;
                    v1 = temp__5457__auto__16190;
                    temp__5457__auto__16190 = null;
                    new_desc = v1;
                    v2 = log$claim_log.__thunk__0__;
                    v3 = desc;
                    desc = null;
                    v4 = v2.get(v3);
                    if (v2 == v4) {
                        log$claim_log.__thunk__0__ = log$claim_log.__site__0__.fault(v3);
                        v4 = log$claim_log.__thunk__0__.get(v3);
                    }
                    if (!Util.equiv((long)1L, (Object)v4)) break block4;
                    v5 = (IFn)log$claim_log.const__5.getRawRoot();
                    v6 = cs;
                    if (Util.classOf((Object)v6) == log$claim_log.__cached_class__0) ** GOTO lbl21
                    if (!(v6 instanceof ClusteredStore)) {
                        v6 = v6;
                        log$claim_log.__cached_class__0 = Util.classOf((Object)v6);
lbl21:
                        // 2 sources

                        v7 = cs;
                        cs = null;
                        v8 = log$claim_log.const__6.getRawRoot().invoke(v6, ((IFn)log$claim_log.const__7.getRawRoot()).invoke(v7));
                    } else {
                        v9 = cs;
                        cs = null;
                        v8 = ((ClusteredStore)v6).delete_reference(((IFn)log$claim_log.const__7.getRawRoot()).invoke(v9));
                    }
                    v5.invoke(v8);
                }
                v10 = new_desc;
                var3_3 = null;
                break block5;
            }
            v10 = null;
        }
        return v10;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return log$claim_log.invokeStatic(object3, object4);
    }

    static {
        const__0 = RT.var((String)"datomic.log", (String)"write-tail-descriptor");
        const__1 = RT.var((String)"datomic.log", (String)"inc-rev");
        const__5 = RT.var((String)"clojure.core", (String)"deref");
        const__6 = RT.var((String)"datomic.cluster", (String)"delete-reference");
        const__7 = RT.var((String)"datomic.log", (String)"legacy-root-ref-key");
        __site__0__ = new KeywordLookupSite(RT.keyword((String)"d", (String)"l"));
        __thunk__0__ = __site__0__;
    }
}

