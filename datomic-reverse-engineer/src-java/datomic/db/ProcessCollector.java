/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.db;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.db.IProcess;
import java.util.ArrayList;
import java.util.Map;

public final class ProcessCollector
implements IProcess,
IType {
    public final Object arraylist;

    public ProcessCollector(Object object) {
        this.arraylist = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"arraylist")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"java.util.ArrayList")})));
    }

    public IProcess inject(Object procargs, Map local_tempids) {
        Object object = procargs;
        procargs = null;
        Boolean bl = ((ArrayList)this.arraylist).add(object) ? Boolean.TRUE : Boolean.FALSE;
        return this;
    }
}

