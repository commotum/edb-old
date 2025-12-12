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
package datomic.core2.aws;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.aws.ddb$item_map$fn__20432;

public final class ddb$item_map
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce-kv");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new ddb$item_map$fn__20432(), (Object)PersistentArrayMap.EMPTY, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$item_map.invokeStatic(object2);
    }
}

