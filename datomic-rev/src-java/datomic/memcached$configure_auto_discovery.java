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

public final class memcached$configure_auto_discovery
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.memcached", (String)"set-client-mode*");
    public static final Var const__1 = RT.var((String)"datomic.memcached", (String)"memcached-client-supports-autodiscovery?");
    public static final Keyword const__2 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object builder, Object auto_discovery, Object config_timeout_msec) {
        Object object;
        Object object2 = auto_discovery;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = builder;
            builder = null;
            Object object4 = auto_discovery;
            auto_discovery = null;
            Object object5 = config_timeout_msec;
            config_timeout_msec = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object3, object4, object5);
        } else {
            Object object6 = ((IFn)const__1.getRawRoot()).invoke();
            if (object6 != null && object6 != Boolean.FALSE) {
                Object object7 = builder;
                builder = null;
                Object object8 = auto_discovery;
                auto_discovery = null;
                Object object9 = config_timeout_msec;
                config_timeout_msec = null;
                object = ((IFn)const__0.getRawRoot()).invoke(object7, object8, object9);
            } else {
                Keyword keyword = const__2;
                if (keyword != null && keyword != Boolean.FALSE) {
                    object = builder;
                    builder = null;
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return memcached$configure_auto_discovery.invokeStatic(object4, object5, object6);
    }
}

