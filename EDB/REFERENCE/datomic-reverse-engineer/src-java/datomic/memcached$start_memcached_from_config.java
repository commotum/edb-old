/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.memcached$start_memcached_from_config$fn__10062;

public final class memcached$start_memcached_from_config
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.config", (String)"memcached-args");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"folsom?");
    public static final Var const__2 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"datomic.memcached.prod-266", (String)"create-cache");
    public static final Var const__4 = RT.var((String)"datomic.memcached", (String)"create-cache");
    public static final Keyword const__5 = RT.keyword(null, (String)"client");
    public static final Var const__6 = RT.var((String)"datomic.memcached", (String)"create-recovering-client");
    public static final Keyword const__7 = RT.keyword(null, (String)"metric-names");
    public static final Var const__8 = RT.var((String)"datomic.memcached", (String)"memcached-metric-names");

    public static Object invokeStatic() {
        Object object;
        Object temp__5457__auto__10065;
        Object object2 = temp__5457__auto__10065 = ((IFn)const__0.getRawRoot()).invoke();
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5457__auto__10065;
            temp__5457__auto__10065 = null;
            Object memcached_args2 = object3;
            Object object4 = ((IFn)const__1.getRawRoot()).invoke();
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = memcached_args2;
                memcached_args2 = null;
                object = ((IFn)const__2.getRawRoot()).invoke((Object)const__3, object5);
            } else {
                Object[] objectArray = new Object[4];
                objectArray[0] = const__5;
                Object object6 = memcached_args2;
                memcached_args2 = null;
                objectArray[1] = ((IFn)const__6.getRawRoot()).invoke((Object)new memcached$start_memcached_from_config$fn__10062(object6));
                objectArray[2] = const__7;
                objectArray[3] = const__8.getRawRoot();
                object = ((IFn)const__4.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray));
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke() {
        return memcached$start_memcached_from_config.invokeStatic();
    }
}

