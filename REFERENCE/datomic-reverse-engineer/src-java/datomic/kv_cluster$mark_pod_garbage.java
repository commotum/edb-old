/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class kv_cluster$mark_pod_garbage
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.process.events", (String)"publish");
    public static final Keyword const__1 = RT.keyword(null, (String)"key");
    public static final Keyword const__2 = RT.keyword((String)"datomic.garbage", (String)"mark");
    public static final Keyword const__3 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__4 = RT.keyword(null, (String)"garbage");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"deref");

    public static Object invokeStatic(Object cs, Object tail_keys_ref) {
        Object[] objectArray = new Object[6];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        Object object = cs;
        cs = null;
        objectArray[3] = object;
        objectArray[4] = const__4;
        Object object2 = tail_keys_ref;
        tail_keys_ref = null;
        objectArray[5] = ((IFn)const__5.getRawRoot()).invoke(object2);
        return ((IFn)const__0.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return kv_cluster$mark_pod_garbage.invokeStatic(object3, object4);
    }
}

