/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Iterator;
import java.util.List;

public final class TransposedData$reify__15000
implements Iterator,
IObj {
    final IPersistentMap __meta;
    Object i;
    int cnt;
    Object td;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"inc");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"swap!");

    public TransposedData$reify__15000(IPersistentMap iPersistentMap, Object object, int n, Object object2) {
        this.__meta = iPersistentMap;
        this.i = object;
        this.cnt = n;
        this.td = object2;
    }

    public TransposedData$reify__15000(Object object, int n, Object object2) {
        this(null, object, n, object2);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new TransposedData$reify__15000(iPersistentMap, this.i, this.cnt, this.td);
    }

    public Object next() {
        return ((List)this.td).get(RT.uncheckedIntCast((Object)((Number)((IFn)const__3.getRawRoot()).invoke(this.i, const__1.getRawRoot()))));
    }

    public boolean hasNext() {
        return Numbers.lt((Object)Numbers.unchecked_inc((Object)((IFn)const__2.getRawRoot()).invoke(this.i)), (long)this.cnt);
    }
}

