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

public final class query$query$fn__19545
extends AFunction {
    Object io_context;
    Object f;
    public static final Var const__0 = RT.var((String)"datomic.measure.io-stats", (String)"throw-if-ex!");
    public static final Var const__1 = RT.var((String)"datomic.measure.io-stats", (String)"with-io-stats");
    public static final Keyword const__2 = RT.keyword(null, (String)"io-context");
    public static final Keyword const__3 = RT.keyword(null, (String)"api");
    public static final Keyword const__4 = RT.keyword(null, (String)"query");

    public query$query$fn__19545(Object object, Object object2) {
        this.io_context = object;
        this.f = object2;
    }

    public Object invoke() {
        query$query$fn__19545 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this_.f, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__2, this_.io_context, const__3, const__4})));
    }
}

