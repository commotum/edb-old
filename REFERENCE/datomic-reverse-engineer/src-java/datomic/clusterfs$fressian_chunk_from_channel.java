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

public final class clusterfs$fressian_chunk_from_channel
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.fressian", (String)"fressian-val");
    public static final Var const__1 = RT.var((String)"datomic.clusterfs", (String)"->Chunk");
    public static final Var const__2 = RT.var((String)"datomic.io", (String)"alias-buf-bytes");
    public static final Var const__3 = RT.var((String)"datomic.io", (String)"read-n-bytes");
    public static final Var const__4 = RT.var((String)"datomic.clusterfs", (String)"write-handlers");

    public static Object invokeStatic(Object rc, Object n) {
        Object object = n;
        n = null;
        Object object2 = rc;
        rc = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object, object2))), const__4.getRawRoot());
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return clusterfs$fressian_chunk_from_channel.invokeStatic(object3, object4);
    }
}

