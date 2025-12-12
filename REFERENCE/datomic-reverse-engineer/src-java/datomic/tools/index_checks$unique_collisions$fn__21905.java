/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.tools;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index_checks$unique_collisions$fn__21905
extends AFunction {
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.api", (String)"datoms");
    public static final Keyword const__1 = RT.keyword(null, (String)"avet");

    public index_checks$unique_collisions$fn__21905(Object object) {
        this.db = object;
    }

    public Object invoke(Object a) {
        Object object = a;
        a = null;
        index_checks$unique_collisions$fn__21905 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.db, (Object)const__1, object);
    }
}

