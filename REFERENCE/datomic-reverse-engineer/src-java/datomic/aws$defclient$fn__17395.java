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
import datomic.aws$defclient$fn__17395$fn__17396;

public final class aws$defclient$fn__17395
extends AFunction {
    Object s__6071__auto__;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"push-thread-bindings");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"*print-length*");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"pop-thread-bindings");

    public aws$defclient$fn__17395(Object object) {
        this.s__6071__auto__ = object;
    }

    public Object invoke() {
        Object object;
        try {
            ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__2, null));
            ((IFn)new aws$defclient$fn__17395$fn__17396()).invoke();
            this.s__6071__auto__ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(this.s__6071__auto__);
        }
        finally {
            ((IFn)const__4.getRawRoot()).invoke();
        }
        return object;
    }
}

