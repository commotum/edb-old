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

public final class h2$init_embedded$fn__11637
extends AFunction {
    Object conn;
    public static final Var const__0 = RT.var((String)"datomic.sql", (String)"execute-commands");

    public h2$init_embedded$fn__11637(Object object) {
        this.conn = object;
    }

    public Object invoke() {
        Object object;
        try {
            object = ((IFn)const__0.getRawRoot()).invoke(this.conn, (Object)"create table if not exists datomic_kvs (id varchar primary key, rev integer, map varchar, val bytea)", (Object)"create user if not exists datomic password 'datomic'", (Object)"grant all on datomic_kvs to datomic", (Object)"grant all on datomic_kvs to public");
        }
        finally {
            this.conn = null;
            ((AutoCloseable)this.conn).close();
        }
        return object;
    }
}

