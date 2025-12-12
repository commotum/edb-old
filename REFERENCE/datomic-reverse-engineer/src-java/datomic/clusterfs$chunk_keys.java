/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class clusterfs$chunk_keys
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"mapcat");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__2 = RT.var((String)"datomic.clusterfs", (String)"file-chunk-keys");

    public static Object invokeStatic(Object clusterfs2, Object files2) {
        Object object = clusterfs2;
        clusterfs2 = null;
        Object object2 = files2;
        files2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot(), object), object2);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return clusterfs$chunk_keys.invokeStatic(object3, object4);
    }
}

