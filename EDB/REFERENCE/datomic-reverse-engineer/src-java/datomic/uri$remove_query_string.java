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
import java.util.regex.Pattern;

public final class uri$remove_query_string
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.string", (String)"replace");
    public static final Object const__1 = Pattern.compile("\\?.*");

    public static Object invokeStatic(Object s) {
        Object object = s;
        s = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, const__1, (Object)"");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return uri$remove_query_string.invokeStatic(object2);
    }
}

