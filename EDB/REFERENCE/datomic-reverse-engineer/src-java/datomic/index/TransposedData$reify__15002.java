/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic.index;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import java.util.List;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class TransposedData$reify__15002
implements List,
IObj {
    final IPersistentMap __meta;
    Object this;
    int from;
    int to;

    public TransposedData$reify__15002(IPersistentMap iPersistentMap, Object object, int n, int n2) {
        this.__meta = iPersistentMap;
        this.this = object;
        this.from = n;
        this.to = n2;
    }

    public TransposedData$reify__15002(Object object, int n, int n2) {
        this(null, object, n, n2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new TransposedData$reify__15002(iPersistentMap, this.this, this.from, this.to);
    }

    public int size() {
        return RT.intCast((long)((long)this.to - (long)this.from));
    }

    public Object get(int idx) {
        return ((List)this.this).get(RT.uncheckedIntCast((long)((long)this.from + (long)idx)));
    }
}

