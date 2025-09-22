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

public final class kv_sql_ext$provider
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"re-find");
    public static final Object const__2 = Pattern.compile("[^:]*:([^:]*):.*");

    public static Object invokeStatic(Object url) {
        Object object = url;
        url = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2, object));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return kv_sql_ext$provider.invokeStatic(object2);
    }
}

