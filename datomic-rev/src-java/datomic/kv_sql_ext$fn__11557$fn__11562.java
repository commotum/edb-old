/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  org.apache.tomcat.jdbc.pool.DataSourceProxy
 */
package datomic;

import clojure.lang.AFunction;
import org.apache.tomcat.jdbc.pool.DataSourceProxy;

public final class kv_sql_ext$fn__11557$fn__11562
extends AFunction {
    Object sql_password;

    public kv_sql_ext$fn__11557$fn__11562(Object object) {
        this.sql_password = object;
    }

    public Object invoke(Object p1__11554_SHARP_) {
        Object v2;
        Object object = this.sql_password;
        if (object != null && object != Boolean.FALSE) {
            Object object2 = p1__11554_SHARP_;
            p1__11554_SHARP_ = null;
            ((DataSourceProxy)object2).setPassword((String)this.sql_password);
            v2 = null;
        } else {
            v2 = null;
        }
        return v2;
    }
}

