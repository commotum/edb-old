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

public final class db$create_attribute$fn__13118
extends AFunction {
    public static final Keyword const__1 = RT.keyword((String)"db.type", (String)"ref");

    public Object invoke(Object idx, Object item) {
        Object object;
        Object object2 = item;
        item = null;
        if (Util.equiv((Object)const__1, (Object)object2)) {
            object = idx;
            idx = null;
        } else {
            object = null;
        }
        return object;
    }
}

