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

public final class api$transact
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");

    public static Object invokeStatic(Object connection, Object tx_data2, ISeq p__19587) {
        ISeq map__19588;
        ISeq iSeq;
        ISeq iSeq2 = p__19587;
        p__19587 = null;
        ISeq map__195882 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__195882);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__195882;
            map__195882 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__195882;
            map__195882 = null;
        }
        ISeq iSeq4 = map__19588 = iSeq;
        map__19588 = null;
        ISeq options = iSeq4;
        Object object2 = connection;
        connection = null;
        Object object3 = tx_data2;
        tx_data2 = null;
        ISeq iSeq5 = options;
        options = null;
        return ((Connection)object2).transact((List)object3, iSeq5);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return api$transact.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

