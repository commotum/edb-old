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

public final class db$reverse_key_QMARK_
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"name");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"blank?");
    public static final Object const__4 = Character.valueOf('_');

    public static Object invokeStatic(Object k) {
        Boolean bl;
        Object object = k;
        k = null;
        Object n = ((IFn)const__1.getRawRoot()).invoke(object);
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(n);
        if (object2 != null && object2 != Boolean.FALSE) {
            bl = null;
        } else {
            Object object3 = n;
            n = null;
            bl = Util.equiv((char)((Character)const__4).charValue(), (char)((String)object3).charAt(RT.uncheckedIntCast((long)0L))) ? Boolean.TRUE : Boolean.FALSE;
        }
        return RT.booleanCast(bl) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$reverse_key_QMARK_.invokeStatic(object2);
    }
}

