/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 */
package datomic;

import clojure.lang.AFunction;
import java.sql.PreparedStatement;
import java.sql.Statement;

public final class sql$execute_commands$fn__11528
extends AFunction {
    Object stmt;

    public sql$execute_commands$fn__11528(Object object) {
        this.stmt = object;
    }

    public Object invoke() {
        Boolean bl;
        try {
            bl = ((PreparedStatement)this.stmt).execute() ? Boolean.TRUE : Boolean.FALSE;
        }
        finally {
            ((Statement)this.stmt).close();
        }
        return bl;
    }
}
