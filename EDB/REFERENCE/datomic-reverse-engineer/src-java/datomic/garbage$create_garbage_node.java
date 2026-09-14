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

public final class garbage$create_garbage_node
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Var const__2;
    public static final Keyword const__3;
    public static final Var const__4;
    public static final Var const__5;
    public static final Var const__6;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object cluster2, Object uuid, Object o) {
        Object object;
        Object buf;
        Object object2 = o;
        o = null;
        Object object3 = buf = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)const__1, const__2.getRawRoot(), (Object)const__3, (Object)Boolean.TRUE);
        buf = null;
        Object zipped = ((IFn)const__4.getRawRoot()).invoke(object3);
        Object object4 = uuid;
        uuid = null;
        Object key = ((IFn)const__5.getRawRoot()).invoke(object4);
        Object object5 = cluster2;
        cluster2 = null;
        Object object6 = object5;
        if (Util.classOf((Object)object5) != __cached_class__0) {
            if (object6 instanceof ClusteredStore) {
                Object object7 = key;
                key = null;
                Object object8 = zipped;
                zipped = null;
                object = ((ClusteredStore)object6).create_val(object7, object8);
                return object;
            }
            object6 = object6;
            __cached_class__0 = Util.classOf((Object)object6);
        }
        Object object9 = key;
        key = null;
        Object object10 = zipped;
        zipped = null;
        object = const__6.getRawRoot().invoke(object6, object9, object10);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return garbage$create_garbage_node.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"datomic.fressian", (String)"byte-buf");
        const__1 = RT.keyword(null, (String)"handlers");
        const__2 = RT.var((String)"datomic.garbage.fressian", (String)"write-handlers");
        const__3 = RT.keyword(null, (String)"footer");
        const__4 = RT.var((String)"datomic.io", (String)"gzip-buffer");
        const__5 = RT.var((String)"datomic.cluster", (String)"uuid->val-key");
        const__6 = RT.var((String)"datomic.cluster", (String)"create-val");
    }
}

