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

public final class catalog$update_succeeded_QMARK_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__1 = RT.keyword(null, (String)"new");

    public static Object invokeStatic(Object m) {
        Object object = m;
        m = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__1);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return catalog$update_succeeded_QMARK_.invokeStatic(object2);
    }
}

