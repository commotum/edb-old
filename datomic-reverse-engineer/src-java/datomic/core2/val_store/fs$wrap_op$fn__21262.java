/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic.core2.val_store;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;

public final class fs$wrap_op$fn__21262
extends RestFn {
    Object f;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"datomic.core2.anomalies", (String)"fault");

    public fs$wrap_op$fn__21262(Object object) {
        this.f = object;
    }

    public Object doInvoke(Object args) {
        Object object;
        try {
            Object object2 = args;
            args = null;
            object = ((IFn)const__0.getRawRoot()).invoke(this.f, object2);
        }
        catch (Throwable t__19459__auto__2) {
            Object t__19459__auto__2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)t__19459__auto__2);
        }
        return object;
    }

    public int getRequiredArity() {
        return 0;
    }
}

