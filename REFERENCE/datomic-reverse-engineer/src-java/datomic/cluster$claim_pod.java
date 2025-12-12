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

public final class cluster$claim_pod
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Keyword const__5;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cs, Object pod_key, Object msec) {
        block6: {
            block5: {
                start = System.currentTimeMillis();
                do {
                    v0 = (IFn)cluster$claim_pod.const__0.getRawRoot();
                    v1 = cs;
                    if (Util.classOf((Object)v1) == cluster$claim_pod.__cached_class__0) ** GOTO lbl9
                    if (!(v1 instanceof ClusteredStore)) {
                        v1 = v1;
                        cluster$claim_pod.__cached_class__0 = Util.classOf((Object)v1);
lbl9:
                        // 2 sources

                        v2 = cluster$claim_pod.const__1.getRawRoot().invoke(v1, pod_key);
                    } else {
                        v2 = ((ClusteredStore)v1).get_pod(pod_key);
                    }
                    v3 = temp__5457__auto__10647 = v0.invoke(v2);
                    if (v3 == null || v3 == Boolean.FALSE) break block5;
                    v4 = temp__5457__auto__10647;
                    temp__5457__auto__10647 = null;
                    pod = v4;
                    touched = ((IFn)cluster$claim_pod.const__2.getRawRoot()).invoke(cs, pod_key, pod);
                    v5 = cluster$claim_pod.__thunk__0__;
                    v6 = touched;
                    v7 = v5.get(v6);
                    if (v5 == v7) {
                        cluster$claim_pod.__thunk__0__ = cluster$claim_pod.__site__0__.fault(v6);
                        v7 = cluster$claim_pod.__thunk__0__.get(v6);
                    }
                    if (v7 == null || v7 == Boolean.FALSE) continue;
                    v8 = (IFn)cluster$claim_pod.const__4.getRawRoot();
                    v9 = touched;
                    touched = null;
                    v10 = cluster$claim_pod.__thunk__1__;
                    v11 = pod;
                    pod = null;
                    v12 = v10.get(v11);
                    if (v10 == v12) {
                        cluster$claim_pod.__thunk__1__ = cluster$claim_pod.__site__1__.fault(v11);
                        v12 = cluster$claim_pod.__thunk__1__.get(v11);
                    }
                    v13 = v8.invoke(v9, (Object)cluster$claim_pod.const__5, v12);
                    break block6;
                } while (Numbers.lte((long)Numbers.minus((long)System.currentTimeMillis(), (long)start), (Object)msec));
                v13 = null;
                break block6;
            }
            v13 = null;
        }
        return v13;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cluster$claim_pod.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-pod");
        const__2 = RT.var((String)"datomic.cluster", (String)"touch-pod");
        const__4 = RT.var((String)"clojure.core", (String)"assoc");
        const__5 = RT.keyword(null, (String)"buf");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"rev"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__1__ = __site__1__;
    }
}

