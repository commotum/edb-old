/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  com.datastax.driver.core.GettableByIndexData
 *  com.datastax.driver.core.ResultSet
 *  com.datastax.driver.core.Row
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import com.datastax.driver.core.GettableByIndexData;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;

public final class cassandra$updated_QMARK_
extends AFunction {
    public static Object invokeStatic(Object res) {
        Boolean bl;
        Row temp__5457__auto__17733;
        Object object = res;
        res = null;
        Row row = temp__5457__auto__17733 = ((ResultSet)object).one();
        if (row != null && row != Boolean.FALSE) {
            Row row2;
            Row row3 = temp__5457__auto__17733;
            temp__5457__auto__17733 = null;
            Row row4 = row2 = row3;
            row2 = null;
            bl = ((GettableByIndexData)row4).getBool(RT.intCast((long)0L)) ? Boolean.TRUE : Boolean.FALSE;
        } else {
            bl = null;
        }
        return bl;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cassandra$updated_QMARK_.invokeStatic(object2);
    }
}

