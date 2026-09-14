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

public final class uri$parse
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__1 = RT.var((String)"datomic.uri", (String)"parse*");
    public static final Keyword const__2 = RT.keyword(null, (String)"uri");

    public static Object invokeStatic(Object uri2) {
        Object object = ((IFn)const__1.getRawRoot()).invoke(uri2);
        Object object2 = uri2;
        uri2 = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__2, object2);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$parse.invokeStatic(object2);
    }
}

