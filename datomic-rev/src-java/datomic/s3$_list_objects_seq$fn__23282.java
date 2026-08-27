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

public final class s3$_list_objects_seq$fn__23282
extends AFunction {
    Object s3;
    Object results;
    public static final Var const__0 = RT.var((String)"datomic.s3", (String)"-list-objects-seq");
    public static final Var const__1 = RT.var((String)"datomic.s3-api", (String)"list-next-batch-of-objects");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__3 = RT.keyword(null, (String)"objectSummaries");

    public s3$_list_objects_seq$fn__23282(Object object, Object object2) {
        this.s3 = object;
        this.results = object2;
    }

    public Object invoke() {
        s3$_list_objects_seq$fn__23282 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(this_.s3, ((IFn)const__1.getRawRoot()).invoke(this_.s3, ((IFn)const__2.getRawRoot()).invoke(this_.results, (Object)const__3)));
    }
}

