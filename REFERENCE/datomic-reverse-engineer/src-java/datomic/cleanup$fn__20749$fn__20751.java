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
import datomic.cleanup$fn__20749$fn__20751$fn__20752;

public final class cleanup$fn__20749$fn__20751
extends AFunction {
    Object manager;
    public static final Var const__0 = RT.var((String)"datomic.cleanup", (String)"run-queue-loop");

    public cleanup$fn__20749$fn__20751(Object object) {
        this.manager = object;
    }

    public Object invoke() {
        cleanup$fn__20749$fn__20751 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.manager, (Object)new cleanup$fn__20749$fn__20751$fn__20752());
    }
}

