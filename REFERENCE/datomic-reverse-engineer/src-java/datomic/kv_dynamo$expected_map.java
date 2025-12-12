/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.kv_dynamo$expected_map$fn__20481;

public final class kv_dynamo$expected_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object expect_map) {
        Object object = expect_map;
        expect_map = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, ((IFn)const__1.getRawRoot()).invoke((Object)new kv_dynamo$expected_map$fn__20481(), object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_dynamo$expected_map.invokeStatic(object2);
    }
}

