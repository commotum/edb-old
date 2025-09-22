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
package datomic.btset;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;

public final class BTSetSplit
implements IType {
    public final Object left;
    public final Object k;
    public final Object right;

    public BTSetSplit(Object object, Object object2, Object object3) {
        this.left = object;
        this.k = object2;
        this.right = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"left")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IBTSetNode")})), (Object)Symbol.intern(null, (String)"k"), (Object)((IObj)Symbol.intern(null, (String)"right")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IBTSetNode")})));
    }
}

