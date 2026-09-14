/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.core2.algo;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.algo.hash$consistent_ring$fn__19390;
import datomic.core2.algo.hash$consistent_ring$fn__19401;
import java.util.TreeMap;

public final class hash$consistent_ring
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object name_weights, Object key_hashes_fn) {
        int nmembers = RT.count((Object)name_weights);
        TreeMap tm = new TreeMap();
        Object object = name_weights;
        name_weights = null;
        ((IFn)const__1.getRawRoot()).invoke((Object)new hash$consistent_ring$fn__19390(key_hashes_fn), tm, object);
        Object object2 = key_hashes_fn;
        key_hashes_fn = null;
        TreeMap treeMap = tm;
        tm = null;
        return new hash$consistent_ring$fn__19401(nmembers, object2, treeMap);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return hash$consistent_ring.invokeStatic(object3, object4);
    }
}

