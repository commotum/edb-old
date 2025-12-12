/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.List;

public final class pull$attr_with_opts_QMARK_
extends AFunction {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"odd?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"keyword?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object expr) {
        Object object;
        boolean and__5236__auto__18943 = expr instanceof List;
        if (and__5236__auto__18943) {
            Object and__5236__auto__18942;
            Object object2 = and__5236__auto__18942 = ((IFn)const__2.getRawRoot()).invoke((Object)RT.count((Object)expr));
            if (object2 != null && object2 != Boolean.FALSE) {
                Object object3 = expr;
                expr = null;
                object = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object3));
            } else {
                object = and__5236__auto__18942;
                Object var2_2 = null;
            }
        } else {
            object = and__5236__auto__18943 ? Boolean.TRUE : Boolean.FALSE;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$attr_with_opts_QMARK_.invokeStatic(object2);
    }
}

