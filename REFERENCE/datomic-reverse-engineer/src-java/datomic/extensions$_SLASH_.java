/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
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

public final class extensions$_SLASH_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"integer?");

    public static Object invokeStatic(Object a, Object b) {
        Number number;
        Object object;
        Object and__5236__auto__18037;
        Object object2 = and__5236__auto__18037 = ((IFn)const__0.getRawRoot()).invoke(a);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn)const__0.getRawRoot()).invoke(b);
        } else {
            object = and__5236__auto__18037;
            Object var2_2 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object3 = a;
            a = null;
            Object object4 = b;
            b = null;
            number = Numbers.quotient((Object)object3, (Object)object4);
        } else {
            Object object5 = a;
            a = null;
            Object object6 = b;
            b = null;
            number = Numbers.divide((Object)object5, (Object)object6);
        }
        return number;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extensions$_SLASH_.invokeStatic(object3, object4);
    }
}

