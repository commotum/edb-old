/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;

public final class db$require_tuple_ids$fn__12619
extends AFunction {
    public static final Keyword const__1 = RT.keyword((String)"db.type", (String)"ref");

    public Object invoke(Object p1__12615_SHARP_) {
        Object object = p1__12615_SHARP_;
        p1__12615_SHARP_ = null;
        db$require_tuple_ids$fn__12619 this_ = null;
        return Util.equiv((Object)const__1, (Object)object) ? Boolean.TRUE : Boolean.FALSE;
    }
}

