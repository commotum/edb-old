/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class config$pro_QMARK_
extends AFunction {
    public static final Keyword const__1 = RT.keyword(null, (String)"pro");
    public static final Var const__2 = RT.var((String)"datomic.config", (String)"datomic-edition");

    public static Object invokeStatic() {
        return Util.equiv((Object)const__1, (Object)const__2.getRawRoot()) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object invoke() {
        return config$pro_QMARK_.invokeStatic();
    }
}

