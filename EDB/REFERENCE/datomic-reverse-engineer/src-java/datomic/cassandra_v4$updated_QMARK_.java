/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  com.datastax.oss.driver.api.core.PagingIterable
 *  com.datastax.oss.driver.api.core.data.GettableByIndex
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.data.GettableByIndex;

public final class cassandra_v4$updated_QMARK_
extends AFunction {
    public static Object invokeStatic(Object res) {
        Boolean bl;
        Object temp__5457__auto__10109;
        Object object = res;
        res = null;
        Object object2 = temp__5457__auto__10109 = ((PagingIterable)object).one();
        if (object2 != null && object2 != Boolean.FALSE) {
            Object row;
            Object object3 = temp__5457__auto__10109;
            temp__5457__auto__10109 = null;
            Object object4 = row = object3;
            row = null;
            bl = ((GettableByIndex)object4).getBoolean(RT.intCast((long)0L)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = null;
        }
        return bl;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cassandra_v4$updated_QMARK_.invokeStatic(object2);
    }
}

