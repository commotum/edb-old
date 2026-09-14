/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.btset;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.btset.IBTSetIterLink;

public final class BTSetIterLink
implements IBTSetIterLink,
IType {
    public final Object branch;
    long offset;
    public final Object parent;

    public BTSetIterLink(Object object, long l, Object object2) {
        this.branch = object;
        this.offset = l;
        this.parent = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"branch")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IBTSetBranch")})), (Object)((IObj)Symbol.intern(null, (String)"offset")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), (Object)Symbol.intern(null, (String)"parent"));
    }

    public void decOffset() {
        --this.offset;
        Numbers.num((long)this.offset);
    }

    public void incOffset() {
        ++this.offset;
        Numbers.num((long)this.offset);
    }

    public long offset() {
        return this.offset;
    }
}

