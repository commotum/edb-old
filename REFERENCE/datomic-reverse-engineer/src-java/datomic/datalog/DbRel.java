/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.datalog;

import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Symbol;
import clojure.lang.Tuple;

public final class DbRel
implements IType {
    public final Object db;
    public final Object isref;
    public final Object iskey;
    public final Object consts;
    public final Object starts;
    public final Object whiles;

    public DbRel(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.db = object;
        this.isref = object2;
        this.iskey = object3;
        this.consts = object4;
        this.starts = object5;
        this.whiles = object6;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"db"), (Object)Symbol.intern(null, (String)"isref"), (Object)Symbol.intern(null, (String)"iskey"), (Object)Symbol.intern(null, (String)"consts"), (Object)Symbol.intern(null, (String)"starts"), (Object)Symbol.intern(null, (String)"whiles"));
    }
}

