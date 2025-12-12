/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.peer;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;

public final class Connection$fn__21505$fn__21519$fn__21520$fn__21521
extends AFunction {
    Object _;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"report");

    public Connection$fn__21505$fn__21519$fn__21520$fn__21521(Object object) {
        this._ = object;
    }

    public Object invoke() {
        Object object;
        try {
            this._ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this._);
        }
        catch (Throwable t__708__auto__2) {
            Object t__708__auto__2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)t__708__auto__2);
        }
        return object;
    }
}

