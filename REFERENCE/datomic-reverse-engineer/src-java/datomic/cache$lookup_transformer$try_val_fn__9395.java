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

public final class cache$lookup_transformer$try_val_fn__9395
extends AFunction {
    Object val_fn;
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"report-val-fn-fail");

    public cache$lookup_transformer$try_val_fn__9395(Object object) {
        this.val_fn = object;
    }

    public Object invoke(Object raw, Object k) {
        Object object;
        try {
            object = ((IFn)this.val_fn).invoke(raw);
        }
        catch (Throwable t2) {
            Object t2 = null;
            Object object2 = raw;
            raw = null;
            Object object3 = k;
            k = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t2, object2, object3);
        }
        return object;
    }
}

