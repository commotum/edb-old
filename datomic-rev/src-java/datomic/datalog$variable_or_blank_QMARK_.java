/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Util;
import clojure.lang.Var;

public final class datalog$variable_or_blank_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datalog", (String)"variable?");
    public static final AFn const__2 = (AFn)Symbol.intern(null, (String)"_");

    public static Object invokeStatic(Object x) {
        Object object;
        Object or__5238__auto__18419;
        Object object2 = or__5238__auto__18419 = ((IFn)const__0.getRawRoot()).invoke(x);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__18419;
            or__5238__auto__18419 = null;
        } else {
            Object object3 = x;
            x = null;
            object = Util.equiv((Object)const__2, (Object)object3) ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return datalog$variable_or_blank_QMARK_.invokeStatic(object2);
    }
}

