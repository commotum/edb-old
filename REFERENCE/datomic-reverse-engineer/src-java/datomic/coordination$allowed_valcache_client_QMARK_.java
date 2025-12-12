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
import datomic.coordination$allowed_valcache_client_QMARK_$fn__11707;

public final class coordination$allowed_valcache_client_QMARK_
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"some");

    public static Object invokeStatic(Object server_specs, Object client2) {
        Boolean bl;
        boolean or__5238__auto__11710 = Util.equiv((Object)client2, (Object)"127.0.0.1");
        if (or__5238__auto__11710) {
            bl = or__5238__auto__11710 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            Object object = client2;
            client2 = null;
            Object object2 = server_specs;
            server_specs = null;
            bl = RT.booleanCast((Object)((IFn)const__2.getRawRoot()).invoke((Object)new coordination$allowed_valcache_client_QMARK_$fn__11707(object), object2)) ? Boolean.TRUE : Boolean.FALSE;
        }
        return bl;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return coordination$allowed_valcache_client_QMARK_.invokeStatic(object3, object4);
    }
}

