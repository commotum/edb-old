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
import datomic.db.IDb;
import datomic.db.IDbImpl;
import datomic.db.Partition;
import datomic.impl.db.IDatum;

public final class db$install_partition_hook
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"validate-hook-target");

    public static Object invokeStatic(Object _, Object db2, Object d, Object check_QMARK_) {
        Object object = check_QMARK_;
        check_QMARK_ = null;
        if (object != null && object != Boolean.FALSE) {
            ((IFn)const__0.getRawRoot()).invoke(db2, d);
        }
        Object object2 = d;
        d = null;
        Object partid = ((IDatum)object2).getV();
        IDbImpl iDbImpl = (IDbImpl)db2;
        Object object3 = partid;
        Object object4 = db2;
        db2 = null;
        Object object5 = partid;
        partid = null;
        return iDbImpl.addElement(new Partition(object3, ((IDb)object4).keywordOf(object5)));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return db$install_partition_hook.invokeStatic(object5, object6, object7, object8);
    }
}

