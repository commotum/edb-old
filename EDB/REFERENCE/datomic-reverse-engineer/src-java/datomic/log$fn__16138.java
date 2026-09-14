/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import java.util.Map;

public final class log$fn__16138
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"t");

    public static Object invokeStatic(Object _) {
        Object object = _;
        _ = null;
        return ((Map)object).get(const__0);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return log$fn__16138.invokeStatic(object2);
    }
}

