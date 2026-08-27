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

public final class extension_resolver$resolve_xform_BANG_
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.extension-resolver", (String)"resolve-built-in-xform");
    public static final Var const__1 = RT.var((String)"datomic.extension-resolver", (String)"resolve!");
    public static final Keyword const__2 = RT.keyword(null, (String)"xforms");

    public static Object invokeStatic(Object sym) {
        Object object;
        Object or__5238__auto__14341;
        Object object2 = or__5238__auto__14341 = ((IFn)const__0.getRawRoot()).invoke(sym);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__14341;
            or__5238__auto__14341 = null;
        } else {
            Object object3 = sym;
            sym = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, (Object)const__2);
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return extension_resolver$resolve_xform_BANG_.invokeStatic(object2);
    }
}

