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

public final class db$safe_compile_function$try_compile__13238$fn__13239
extends AFunction {
    Object t;
    public static final Var const__0 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__1 = RT.keyword((String)"db.error", (String)"data-function-compile-failed");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"str");

    public db$safe_compile_function$try_compile__13238$fn__13239(Object object) {
        this.t = object;
    }

    public Object invoke(Object db2, Object m) {
        return ((IFn)const__0.getRawRoot()).invoke((Object)const__1, ((IFn)const__2.getRawRoot()).invoke((Object)"Data function failed to compile", this.t));
    }
}

