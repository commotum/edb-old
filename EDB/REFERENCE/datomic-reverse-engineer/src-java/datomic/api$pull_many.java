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
import datomic.Database;
import java.util.List;

public final class api$pull_many
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");

    public static Object invokeStatic(Object db2, Object pattern, Object eids, ISeq p__19612) {
        ISeq map__19613;
        ISeq iSeq;
        ISeq iSeq2 = p__19612;
        p__19612 = null;
        ISeq map__196132 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__196132);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__196132;
            map__196132 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__196132;
            map__196132 = null;
        }
        ISeq iSeq4 = map__19613 = iSeq;
        map__19613 = null;
        ISeq options = iSeq4;
        Object object2 = db2;
        db2 = null;
        Object object3 = pattern;
        pattern = null;
        Object object4 = eids;
        eids = null;
        ISeq iSeq5 = options;
        options = null;
        return ((Database)object2).pullMany(object3, (List)object4, iSeq5);
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return api$pull_many.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

