/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class index$init_index
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Keyword const__2;
    public static final Var const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Object const__6;
    public static final Var const__7;

    /*
     * Unable to fully structure code
     */
    public static Object invokeStatic(Object cstore) {
        rootid = ((IFn)index$init_index.const__0.getRawRoot()).invoke(cstore);
        v0 = (IFn)index$init_index.const__3.getRawRoot();
        v1 = cstore;
        if (Util.classOf((Object)v1) == index$init_index.__cached_class__0) ** GOTO lbl8
        if (!(v1 instanceof ClusteredStore)) {
            v1 = v1;
            index$init_index.__cached_class__0 = Util.classOf((Object)v1);
lbl8:
            // 2 sources

            v2 = cstore;
            cstore = null;
            v3 = index$init_index.const__4.getRawRoot().invoke(v1, ((IFn)index$init_index.const__5.getRawRoot()).invoke(v2), index$init_index.const__6, ((IFn)index$init_index.const__7.getRawRoot()).invoke(rootid));
        } else {
            v4 = cstore;
            cstore = null;
            v3 = ((ClusteredStore)v1).set_ref(((IFn)index$init_index.const__5.getRawRoot()).invoke(v4), index$init_index.const__6, ((IFn)index$init_index.const__7.getRawRoot()).invoke(rootid));
        }
        if (Util.equiv((Object)index$init_index.const__2, (Object)v0.invoke(v3))) {
            v5 = rootid;
            rootid = null;
        } else {
            v5 = null;
        }
        return v5;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return index$init_index.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"datomic.index", (String)"init-index*");
        const__2 = RT.keyword(null, (String)"ok");
        const__3 = RT.var((String)"clojure.core", (String)"deref");
        const__4 = RT.var((String)"datomic.cluster", (String)"set-ref");
        const__5 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
        const__6 = 0L;
        const__7 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
    }
}

