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
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class db$key_hook$fn__13040
extends AFunction {
    Object d;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__1 = RT.keyword(null, (String)"kw");

    public db$key_hook$fn__13040(Object object) {
        this.d = object;
    }

    public Object invoke(Object p1__13039_SHARP_) {
        Object object;
        Object object2 = p1__13039_SHARP_;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = p1__13039_SHARP_;
            p1__13039_SHARP_ = null;
            db$key_hook$fn__13040 this_ = null;
            object = ((IFn)const__0.getRawRoot()).invoke(object3, (Object)const__1, ((IDatum)this_.d).getV());
        } else {
            object = null;
        }
        return object;
    }
}

