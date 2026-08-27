/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.Connection;
import java.util.List;

public final class api$transact_async
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");

    public static Object invokeStatic(Object connection, Object tx_data2, ISeq p__19590) {
        ISeq map__19591;
        ISeq iSeq;
        ISeq iSeq2 = p__19590;
        p__19590 = null;
        ISeq map__195912 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__195912);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__195912;
            map__195912 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__195912;
            map__195912 = null;
        }
        ISeq iSeq4 = map__19591 = iSeq;
        map__19591 = null;
        ISeq options = iSeq4;
        Object object2 = connection;
        connection = null;
        Object object3 = tx_data2;
        tx_data2 = null;
        ISeq iSeq5 = options;
        options = null;
        return ((Connection)object2).transactAsync((List)object3, iSeq5);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return api$transact_async.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

