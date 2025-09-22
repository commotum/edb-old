/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;

public final class tools$get_index_ref
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object cluster2) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = cluster2;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof ClusteredStore) {
                Object object3 = cluster2;
                cluster2 = null;
                object = ((ClusteredStore)object2).get_ref(((IFn)const__2.getRawRoot()).invoke(object3));
                return iFn.invoke(object);
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object4 = cluster2;
        cluster2 = null;
        object = const__1.getRawRoot().invoke(object2, ((IFn)const__2.getRawRoot()).invoke(object4));
        return iFn.invoke(object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return tools$get_index_ref.invokeStatic(object2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-ref");
        const__2 = RT.var((String)"datomic.index", (String)"index-ref-key-name");
    }
}

