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

public final class db$reserved_keyword_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__2 = RT.var((String)"clojure.string", (String)"starts-with?");

    public static Object invokeStatic(Object kw) {
        Object object;
        Object temp__5457__auto__13096;
        Object object2 = kw;
        kw = null;
        Object object3 = temp__5457__auto__13096 = ((IFn)const__0.getRawRoot()).invoke(object2);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = temp__5457__auto__13096;
            temp__5457__auto__13096 = null;
            Object ns = object4;
            boolean or__5238__auto__13095 = Util.equiv((Object)ns, (Object)"db");
            if (or__5238__auto__13095) {
                object = or__5238__auto__13095 ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object object5 = ns;
                ns = null;
                object = ((IFn)const__2.getRawRoot()).invoke(object5, (Object)"db.");
            }
        } else {
            object = null;
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return db$reserved_keyword_QMARK_.invokeStatic(object2);
    }
}

