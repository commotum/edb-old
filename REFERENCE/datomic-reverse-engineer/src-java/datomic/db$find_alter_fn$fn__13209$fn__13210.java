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
import datomic.Database;

public final class db$find_alter_fn$fn__13209$fn__13210
extends AFunction {
    Object aid;
    Object db;
    Object preds;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"create-attr-pred");

    public db$find_alter_fn$fn__13209$fn__13210(Object object, Object object2, Object object3) {
        this.aid = object;
        this.db = object2;
        this.preds = object3;
    }

    public Object invoke() {
        db$find_alter_fn$fn__13209$fn__13210 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(((Database)this_.db).ident(this_.aid), this_.preds);
    }
}

