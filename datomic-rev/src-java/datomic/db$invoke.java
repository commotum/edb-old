/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.db.IDb;

public final class db$invoke
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");

    public static Object invokeStatic(Object db2, Object eid_or_ident, ISeq args) {
        Object object = db2;
        db2 = null;
        Object object2 = eid_or_ident;
        eid_or_ident = null;
        ISeq iSeq = args;
        args = null;
        return ((IFn)const__0.getRawRoot()).invoke((Object)((IDb)object).getFn(object2), (Object)iSeq);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return db$invoke.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

