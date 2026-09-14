/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.builtins$build_retract_args$fn__23400$fn__23402;
import datomic.builtins$build_retract_args$fn__23400$fn__23406;

public final class builtins$build_retract_args$fn__23400
extends AFunction {
    Object retract;
    Object db;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"datoms");
    public static final Keyword const__2 = RT.keyword(null, (String)"eavt");
    public static final Keyword const__3 = RT.keyword(null, (String)"vaet");

    public builtins$build_retract_args$fn__23400(Object object, Object object2) {
        this.retract = object;
        this.db = object2;
    }

    public Object invoke(Object result2, Object e) {
        Object result3;
        Object object = result2;
        result2 = null;
        Object object2 = result3 = ((IFn)const__0.getRawRoot()).invoke((Object)new builtins$build_retract_args$fn__23400$fn__23402(this_.retract, this_.db), object, ((IFn)const__1.getRawRoot()).invoke(this_.db, (Object)const__2, (Object)Tuple.create((Object)e)));
        result3 = null;
        Object object3 = e;
        e = null;
        builtins$build_retract_args$fn__23400 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)new builtins$build_retract_args$fn__23400$fn__23406(this_.retract), object2, ((IFn)const__1.getRawRoot()).invoke(this_.db, (Object)const__3, (Object)Tuple.create((Object)object3)));
    }
}

