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

public final class db$string_tempid
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"next-id");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)"datomic.temp-", ((IFn)const__1.getRawRoot()).invoke());
    }

    public Object invoke() {
        return db$string_tempid.invokeStatic();
    }
}

