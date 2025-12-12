/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class memcached$set_client_mode_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");

    public static Object invokeStatic(Object builder, Object auto_discovery, Object config_timeout_msec) {
        Object object = auto_discovery;
        auto_discovery = null;
        Object client_mode = Enum.valueOf(Class.forName("datomic.spy.memcached.ClientMode"), object != null && object != Boolean.FALSE ? "Dynamic" : "Static");
        Method set_client_mode_method = Class.forName("datomic.spy.memcached.ConnectionFactoryBuilder").getDeclaredMethod("setClientMode", (Class[])((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create(Class.forName("datomic.spy.memcached.ClientMode"))));
        Method set_config_op_timeout_method = Class.forName("datomic.spy.memcached.ConnectionFactoryBuilder").getDeclaredMethod("setConfigOpTimeout", (Class[])((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create(Long.TYPE)));
        Method method = set_client_mode_method;
        set_client_mode_method = null;
        Object obj = client_mode;
        client_mode = null;
        method.invoke(builder, (Object[])((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create(obj)));
        Method method2 = set_config_op_timeout_method;
        set_config_op_timeout_method = null;
        Object object2 = builder;
        builder = null;
        Object object3 = config_timeout_msec;
        config_timeout_msec = null;
        return method2.invoke(object2, (Object[])((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)object3)));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return memcached$set_client_mode_STAR_.invokeStatic(object4, object5, object6);
    }
}

