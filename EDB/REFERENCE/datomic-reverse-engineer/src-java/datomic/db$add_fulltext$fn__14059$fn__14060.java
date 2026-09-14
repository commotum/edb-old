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
import datomic.db$add_fulltext$fn__14059$fn__14060$fn__14061;

public final class db$add_fulltext$fn__14059$fn__14060
extends AFunction {
    Object db;
    Object data;
    public static final Var const__0 = RT.var((String)"datomic.fulltext-index", (String)"update-fulltext");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"filter");

    public db$add_fulltext$fn__14059$fn__14060(Object object, Object object2) {
        this.db = object;
        this.data = object2;
    }

    public Object invoke(Object previous) {
        Object object = previous;
        previous = null;
        db$add_fulltext$fn__14059$fn__14060 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, ((IFn)const__1.getRawRoot()).invoke((Object)new db$add_fulltext$fn__14059$fn__14060$fn__14061(this_.db), this_.data));
    }
}

