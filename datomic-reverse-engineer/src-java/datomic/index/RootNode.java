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
package datomic.index;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;

public final class RootNode
implements IType {
    public final Object keydata;
    public final Object dirids;
    public final Object dirs;

    public RootNode(Object object, Object object2, Object object3) {
        this.keydata = object;
        this.dirids = object2;
        this.dirs = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"keydata")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"TransposedData")})), (Object)((IObj)Symbol.intern(null, (String)"dirids")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})), (Object)((IObj)Symbol.intern(null, (String)"dirs")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})));
    }
}

