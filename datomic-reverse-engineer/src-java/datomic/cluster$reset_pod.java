/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class cluster$reset_pod
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Var const__6;
    public static final Object const__8;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cluster, Object k, Object buf, Object metamap) {
        v0 = (IFn)cluster$reset_pod.const__0.getRawRoot();
        v1 = cluster;
        if (Util.classOf((Object)v1) == cluster$reset_pod.__cached_class__0) ** GOTO lbl7
        if (!(v1 instanceof ClusteredStore)) {
            v1 = v1;
            cluster$reset_pod.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = cluster$reset_pod.const__1.getRawRoot().invoke(v1, k);
        } else {
            v2 = ((ClusteredStore)v1).get_pod(k);
        }
        map__10643 = v0.invoke(v2);
        v3 = ((IFn)cluster$reset_pod.const__2.getRawRoot()).invoke(map__10643);
        if (v3 != null && v3 != Boolean.FALSE) {
            v4 = map__10643;
            map__10643 = null;
            v5 = PersistentHashMap.create((ISeq)((ISeq)((IFn)cluster$reset_pod.const__3.getRawRoot()).invoke(v4)));
        } else {
            v5 = map__10643;
            map__10643 = null;
        }
        v6 = map__10643 = v5;
        map__10643 = null;
        rev = RT.get((Object)v6, (Object)cluster$reset_pod.const__5);
        v7 = (IFn)cluster$reset_pod.const__6.getRawRoot();
        v8 = cluster;
        cluster = null;
        v9 = k;
        k = null;
        v10 = rev;
        if (v10 != null && v10 != Boolean.FALSE) {
            v11 = rev;
            rev = null;
            v12 = Numbers.inc((Object)v11);
        } else {
            v12 = cluster$reset_pod.const__8;
        }
        v13 = buf;
        buf = null;
        v14 = metamap;
        metamap = null;
        return v7.invoke(v8, v9, v12, null, v13, v14);
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return cluster$reset_pod.invokeStatic(object5, object6, object7, object8);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-pod");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"rev");
        const__6 = RT.var((String)"datomic.cluster", (String)"update-pod");
        const__8 = 0L;
    }
}

