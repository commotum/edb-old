/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class cluster$clone_pod
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__3;
    public static final Keyword const__5;
    public static final Keyword const__6;
    public static final Keyword const__7;
    public static final Var const__8;
    public static final Object const__9;
    public static final AFn const__12;

    /*
     * Unable to fully structure code
     * Could not resolve type clashes
     */
    public static Object invokeStatic(Object cs, Object from_key, Object to_key) {
        v0 = (IFn)cluster$clone_pod.const__0.getRawRoot();
        v1 = cs;
        if (Util.classOf((Object)v1) == cluster$clone_pod.__cached_class__0) ** GOTO lbl7
        if (!(v1 instanceof ClusteredStore)) {
            v1 = v1;
            cluster$clone_pod.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
            // 2 sources

            v2 = from_key;
            from_key = null;
            v3 = cluster$clone_pod.const__1.getRawRoot().invoke(v1, v2);
        } else {
            v4 = from_key;
            from_key = null;
            v3 = ((ClusteredStore)v1).get_pod(v4);
        }
        v5 = temp__5455__auto__10650 = v0.invoke(v3);
        if (v5 != null && v5 != Boolean.FALSE) {
            v6 = temp__5455__auto__10650;
            temp__5455__auto__10650 = null;
            map__10648 = v6;
            v7 = ((IFn)cluster$clone_pod.const__2.getRawRoot()).invoke(map__10648);
            if (v7 != null && v7 != Boolean.FALSE) {
                v8 = map__10648;
                map__10648 = null;
                v9 = PersistentHashMap.create((ISeq)((ISeq)((IFn)cluster$clone_pod.const__3.getRawRoot()).invoke(v8)));
            } else {
                v9 = map__10648;
                map__10648 = null;
            }
            map__10648 = v9;
            RT.get((Object)map__10648, (Object)cluster$clone_pod.const__5);
            RT.get((Object)map__10648, (Object)cluster$clone_pod.const__6);
            v10 = map__10648;
            map__10648 = null;
            buf = RT.get((Object)v10, (Object)cluster$clone_pod.const__7);
            v11 = cs;
            cs = null;
            v12 = to_key;
            to_key = null;
            v13 = buf;
            buf = null;
            v14 /* !! */  = ((IFn)cluster$clone_pod.const__0.getRawRoot()).invoke(((IFn)cluster$clone_pod.const__8.getRawRoot()).invoke(v11, v12, cluster$clone_pod.const__9, null, v13));
        } else {
            v14 /* !! */  = cluster$clone_pod.const__12;
        }
        return v14 /* !! */ ;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return cluster$clone_pod.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-pod");
        const__2 = RT.var((String)"clojure.core", (String)"seq?");
        const__3 = RT.var((String)"clojure.core", (String)"seq");
        const__5 = RT.keyword(null, (String)"rev");
        const__6 = RT.keyword(null, (String)"etag");
        const__7 = RT.keyword(null, (String)"buf");
        const__8 = RT.var((String)"datomic.cluster", (String)"update-pod");
        const__9 = 0L;
        const__12 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"failed"), RT.keyword(null, (String)"absent")});
    }
}

