/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Attribute;

public final class db$create_attribute$fn__13120
extends AFunction {
    Object db;
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"require-attr");

    public db$create_attribute$fn__13120(Object object) {
        this.db = object;
    }

    public Object invoke(Object idx, Object item) {
        Object object;
        Object object2 = item;
        item = null;
        if (Util.equiv((long)20L, (Object)((Attribute)((IFn)db$create_attribute$fn__13120.const__2.getRawRoot()).invoke((Object)this.db, (Object)object2)).vtypeid)) {
            object = idx;
            idx = null;
        } else {
            object = null;
        }
        return object;
    }
}

