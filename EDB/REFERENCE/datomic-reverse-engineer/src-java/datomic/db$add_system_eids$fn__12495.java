/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Tuple
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IPersistentVector;
import clojure.lang.Tuple;
import datomic.Database;

public final class db$add_system_eids$fn__12495
extends AFunction {
    Object db;

    public db$add_system_eids$fn__12495(Object object) {
        this.db = object;
    }

    public Object invoke(Object ident2) {
        IPersistentVector iPersistentVector;
        Object temp__5457__auto__12497;
        Object object = temp__5457__auto__12497 = ((Database)this.db).entid(ident2);
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__12497;
            temp__5457__auto__12497 = null;
            Object eid = object2;
            Object object3 = ident2;
            ident2 = null;
            Object object4 = eid;
            eid = null;
            iPersistentVector = Tuple.create((Object)object3, (Object)object4);
        } else {
            iPersistentVector = null;
        }
        return iPersistentVector;
    }
}

