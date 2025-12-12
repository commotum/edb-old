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

public final class aws$aws_secret_key_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");

    public static Object invokeStatic(Object s) {
        Object object;
        Object and__5236__auto__17381;
        Object object2 = and__5236__auto__17381 = ((IFn)const__0.getRawRoot()).invoke(s);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = s;
            s = null;
            object = Util.equiv((long)40L, (long)RT.count((Object)object3)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            object = and__5236__auto__17381;
            Object var1_1 = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws$aws_secret_key_QMARK_.invokeStatic(object2);
    }
}

