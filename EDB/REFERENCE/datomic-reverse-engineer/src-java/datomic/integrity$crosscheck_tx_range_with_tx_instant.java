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
import datomic.integrity$crosscheck_tx_range_with_tx_instant$progress__22465;

public final class integrity$crosscheck_tx_range_with_tx_instant
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__1 = 0L;
    public static final Var const__2 = RT.var((String)"datomic.integrity", (String)"seq-diffs");
    public static final Var const__3 = RT.var((String)"datomic.integrity", (String)"tx-range-ts");
    public static final Var const__4 = RT.var((String)"datomic.integrity", (String)"tx-instant-ts");
    public static final Var const__5 = RT.var((String)"datomic.api", (String)"db");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Keyword const__7 = RT.keyword(null, (String)"desc");
    public static final Keyword const__8 = RT.keyword(null, (String)"diffs");

    public static Object invokeStatic(Object conn) {
        Object temp__5457__auto__22468;
        Object count2;
        Object object = count2 = ((IFn)const__0.getRawRoot()).invoke(const__1);
        count2 = null;
        integrity$crosscheck_tx_range_with_tx_instant$progress__22465 progress = new integrity$crosscheck_tx_range_with_tx_instant$progress__22465(object);
        Object object2 = ((IFn)const__3.getRawRoot()).invoke(conn);
        Object object3 = conn;
        conn = null;
        integrity$crosscheck_tx_range_with_tx_instant$progress__22465 integrity$crosscheck_tx_range_with_tx_instant$progress__22465 = progress;
        progress = null;
        Object object4 = temp__5457__auto__22468 = ((IFn)const__2.getRawRoot()).invoke(object2, ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object3)), (Object)integrity$crosscheck_tx_range_with_tx_instant$progress__22465);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5457__auto__22468;
            temp__5457__auto__22468 = null;
            Object diffs = object5;
            Object[] objectArray = new Object[4];
            objectArray[0] = const__7;
            objectArray[1] = ":a values are from tx-range, :b values from :db/txInstant, :n offset";
            objectArray[2] = const__8;
            Object object6 = diffs;
            diffs = null;
            objectArray[3] = object6;
            throw (Throwable)((IFn)const__6.getRawRoot()).invoke((Object)"tx-range and :db/txInstant did not agree on ts.", (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        return null;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$crosscheck_tx_range_with_tx_instant.invokeStatic(object2);
    }
}

