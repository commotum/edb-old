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

public final class DirNode
implements IType {
    public final Object keydata;
    public final Object segids;
    public final Object offsets;
    public final Object counts;
    public final Object segs;

    public DirNode(Object object, Object object2, Object object3, Object object4, Object object5) {
        this.keydata = object;
        this.segids = object2;
        this.offsets = object3;
        this.counts = object4;
        this.segs = object5;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"keydata")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"TransposedData")})), (Object)((IObj)Symbol.intern(null, (String)"segids")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})), (Object)((IObj)Symbol.intern(null, (String)"offsets")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ints")})), (Object)((IObj)Symbol.intern(null, (String)"counts")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ints")})), (Object)((IObj)Symbol.intern(null, (String)"segs")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"objects")})));
    }
}

