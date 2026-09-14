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
import datomic.peer.ConnectionState;

public final class peer$create_connection$fn__21590$fn__21591
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"not");

    public Object invoke(Object p1__21587_SHARP_) {
        Object object = p1__21587_SHARP_;
        p1__21587_SHARP_ = null;
        peer$create_connection$fn__21590$fn__21591 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)(object instanceof ConnectionState ? Boolean.TRUE : Boolean.FALSE));
    }
}

