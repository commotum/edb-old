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

public final class cache$safe_lookup_transformer$try_val_fn__9402
extends AFunction {
    Object val_fn;
    Object key_fn;
    public static final Var const__0 = RT.var((String)"datomic.cache", (String)"report-val-fn-fail");

    public cache$safe_lookup_transformer$try_val_fn__9402(Object object, Object object2) {
        this.val_fn = object;
        this.key_fn = object2;
    }

    public Object invoke(Object raw, Object k, Object not_found) {
        Object object;
        try {
            Object object2 = not_found;
            not_found = null;
            object = ((IFn)this.val_fn).invoke(((IFn)this.key_fn).invoke(k), raw, object2);
        }
        catch (Throwable t2) {
            Object t2 = null;
            Object object3 = raw;
            raw = null;
            Object object4 = k;
            k = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)t2, object3, object4);
        }
        return object;
    }
}

