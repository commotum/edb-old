/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Iterator;

public final class iter$iterable$reify__11735
implements Iterable,
IObj {
    final IPersistentMap __meta;
    Object iter_fn;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"iterator");

    public iter$iterable$reify__11735(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.iter_fn = object;
    }

    public iter$iterable$reify__11735(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new iter$iterable$reify__11735(iPersistentMap, this.iter_fn);
    }

    public Iterator iterator() {
        iter$iterable$reify__11735 this_ = null;
        return (Iterator)((IFn)const__0.getRawRoot()).invoke(((IFn)this_.iter_fn).invoke());
    }
}

