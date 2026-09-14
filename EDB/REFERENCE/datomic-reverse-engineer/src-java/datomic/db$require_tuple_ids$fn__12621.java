/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;

public final class db$require_tuple_ids$fn__12621
extends AFunction {
    Object resolve;
    public static final Keyword const__1 = RT.keyword((String)"db.type", (String)"ref");

    public db$require_tuple_ids$fn__12621(Object object) {
        this.resolve = object;
    }

    public Object invoke(Object type, Object v) {
        Object object;
        Object object2 = type;
        type = null;
        if (Util.equiv((Object)object2, (Object)const__1)) {
            Object object3 = v;
            v = null;
            db$require_tuple_ids$fn__12621 this_ = null;
            object = ((IFn)this_.resolve).invoke(object3);
        } else {
            object = v;
            Object var2_2 = null;
        }
        return object;
    }
}

