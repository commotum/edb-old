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

public final class common$maybe_require
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"qualified-symbol?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"ns-resolve");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"ns-imports");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"maybe-class");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"require");

    public static Object invokeStatic(Object ns, Object s) {
        Object v10;
        Object temp__5457__auto__9140;
        Object object;
        Object and__5236__auto__9136;
        Object object2 = and__5236__auto__9136 = ((IFn)const__0.getRawRoot()).invoke(s);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = ((IFn)const__1.getRawRoot()).invoke(s);
        } else {
            object = and__5236__auto__9136;
            temp__5457__auto__9140 = null;
        }
        Object object3 = temp__5457__auto__9140 = object;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object or__5238__auto__9139;
            Object object4 = temp__5457__auto__9140;
            temp__5457__auto__9140 = null;
            Object sns = object4;
            Object object5 = s;
            s = null;
            Object object6 = or__5238__auto__9139 = ((IFn)const__2.getRawRoot()).invoke(ns, object5);
            if (object6 == null || object6 == Boolean.FALSE) {
                Object or__5238__auto__9138;
                Object object7 = ns;
                ns = null;
                Object object8 = or__5238__auto__9138 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(object7), ((IFn)const__5.getRawRoot()).invoke(sns));
                if (object8 == null || object8 == Boolean.FALSE) {
                    Object or__5238__auto__9137;
                    Object object9 = or__5238__auto__9137 = ((IFn)const__6.getRawRoot()).invoke(sns);
                    if (object9 == null || object9 == Boolean.FALSE) {
                        Object object10 = sns;
                        sns = null;
                        ((IFn)const__7.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object10));
                    }
                }
            }
            v10 = null;
        } else {
            v10 = null;
        }
        return v10;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$maybe_require.invokeStatic(object3, object4);
    }
}

