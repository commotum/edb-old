/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$_LT__SINGLEQUOTE_
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object a, Object b) {
        Object object = a;
        a = null;
        Object object2 = b;
        b = null;
        return Numbers.isNeg((long)((IFn.OOL)const__1.getRawRoot()).invokePrim(object, object2)) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$_LT__SINGLEQUOTE_.invokeStatic(object3, object4);
    }
}

