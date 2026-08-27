/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class query$resolve_qualified_fn
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"sequential?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"qualified-symbol?");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"maybe-require");
    public static final AFn const__4 = (AFn)Symbol.intern(null, (String)"datomic.extensions");

    public static Object invokeStatic(Object x) {
        Object object;
        Object temp__5457__auto__19428;
        Object object2;
        Object and__5236__auto__19427;
        Object object3 = and__5236__auto__19427 = ((IFn)const__0.getRawRoot()).invoke(x);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = x;
            x = null;
            object2 = ((IFn)const__1.getRawRoot()).invoke(object4);
        } else {
            object2 = and__5236__auto__19427;
            temp__5457__auto__19428 = null;
        }
        Object object5 = temp__5457__auto__19428 = object2;
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = temp__5457__auto__19428;
            temp__5457__auto__19428 = null;
            Object f = object6;
            Object object7 = ((IFn)const__2.getRawRoot()).invoke(f);
            if (object7 != null && object7 != Boolean.FALSE) {
                Object object8 = f;
                f = null;
                object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, object8);
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return query$resolve_qualified_fn.invokeStatic(object2);
    }
}

