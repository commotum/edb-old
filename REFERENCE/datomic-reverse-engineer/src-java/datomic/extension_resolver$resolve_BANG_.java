/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class extension_resolver$resolve_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"qualified-symbol?");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"allow?");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"resolve");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"namespace");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"require");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"symbol");
    public static final Var const__6 = RT.var((String)"datomic.extension-resolver", (String)"anomaly!");
    public static final Keyword const__7 = RT.keyword(null, (String)"not-found");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__9 = RT.keyword(null, (String)"forbidden");

    public static Object invokeStatic(Object x, Object context) {
        Object object;
        Object object2;
        Object and__5236__auto__14335;
        Object object3 = and__5236__auto__14335 = ((IFn)const__0.getRawRoot()).invoke(x);
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = ((IFn)const__1.getRawRoot()).invoke(x, context);
        } else {
            object2 = and__5236__auto__14335;
            and__5236__auto__14335 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            Object or__5238__auto__14338;
            Object object4 = or__5238__auto__14338 = ((IFn)const__2.getRawRoot()).invoke(x);
            if (object4 != null && object4 != Boolean.FALSE) {
                object = or__5238__auto__14338;
                or__5238__auto__14338 = null;
            } else {
                Object temp__5457__auto__14337;
                Object object5 = temp__5457__auto__14337 = ((IFn)const__3.getRawRoot()).invoke(x);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object or__5238__auto__14336;
                    Object nsname;
                    Object object6 = temp__5457__auto__14337;
                    temp__5457__auto__14337 = null;
                    Object object7 = nsname = object6;
                    nsname = null;
                    ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object7));
                    Object object8 = or__5238__auto__14336 = ((IFn)const__2.getRawRoot()).invoke(x);
                    if (object8 != null && object8 != Boolean.FALSE) {
                        object = or__5238__auto__14336;
                        or__5238__auto__14336 = null;
                    } else {
                        Object object9 = x;
                        x = null;
                        object = ((IFn)const__6.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)"Unable to resolve '", object9, (Object)"'"));
                    }
                } else {
                    object = null;
                }
            }
        } else {
            Object object10 = x;
            x = null;
            Object object11 = context;
            context = null;
            object = ((IFn)const__6.getRawRoot()).invoke((Object)const__9, ((IFn)const__8.getRawRoot()).invoke((Object)"'", object10, (Object)"' needs to be listed under ", object11, (Object)" in datomic/extensions.edn"));
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return extension_resolver$resolve_BANG_.invokeStatic(object3, object4);
    }
}

