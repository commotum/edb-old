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

public final class config$set_pro
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"alter-var-root");
    public static final Var const__1 = RT.var((String)"datomic.config", (String)"datomic-edition");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"constantly");
    public static final Keyword const__3 = RT.keyword(null, (String)"pro");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)const__3));
    }

    public Object invoke() {
        return config$set_pro.invokeStatic();
    }
}

