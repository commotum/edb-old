/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class coordination_ext$fn__16758$fn__16759
extends AFunction {
    Object cluster_conf;
    Object ck;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__2 = RT.var((String)"datomic.require", (String)"require-and-run");
    public static final AFn const__3 = (AFn)Symbol.intern((String)"datomic.kv-sql-ext", (String)"kv-sql");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final AFn const__13 = (AFn)RT.vector((Object[])new Object[]{RT.keyword(null, (String)"sql-url"), RT.keyword(null, (String)"data-source"), RT.keyword(null, (String)"factory"), RT.keyword(null, (String)"sql-user"), RT.keyword(null, (String)"sql-password"), RT.keyword(null, (String)"sql-initial-size"), RT.keyword(null, (String)"sql-driver-class"), RT.keyword(null, (String)"sql-driver-params")});

    public coordination_ext$fn__16758$fn__16759(Object object, Object object2) {
        this.cluster_conf = object;
        this.ck = object2;
    }

    public Object invoke(Object cs) {
        Object object;
        Object object2 = RT.get((Object)cs, (Object)this_.ck);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = cs;
            cs = null;
        } else {
            Object object3 = cs;
            cs = null;
            coordination_ext$fn__16758$fn__16759 this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object3, this_.ck, ((IFn)const__2.getRawRoot()).invoke((Object)const__3, ((IFn)const__4.getRawRoot()).invoke(this_.cluster_conf, (Object)const__13)));
        }
        return object;
    }
}

