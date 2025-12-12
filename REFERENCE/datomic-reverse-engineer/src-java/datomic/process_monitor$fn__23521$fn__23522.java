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

public final class process_monitor$fn__23521$fn__23522
extends AFunction {
    Object callback;
    public static final Var const__0 = RT.var((String)"datomic.monitor", (String)"add-stat");
    public static final Keyword const__1 = RT.keyword(null, (String)"SelfDestruct");
    public static final Object const__2 = 1L;
    public static final Var const__3 = RT.var((String)"datomic.process-monitor", (String)"report-metrics");

    public process_monitor$fn__23521$fn__23522(Object object) {
        this.callback = object;
    }

    public Object invoke() {
        ((IFn)const__0.getRawRoot()).invoke((Object)const__1, const__2);
        process_monitor$fn__23521$fn__23522 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(this_.callback);
    }
}

