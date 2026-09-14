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
import java.util.NoSuchElementException;

public final class peer$seq_iterator$reify__21670
implements Iterator,
IObj {
    final IPersistentMap __meta;
    Object a;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"next");

    public peer$seq_iterator$reify__21670(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.a = object;
    }

    public peer$seq_iterator$reify__21670(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new peer$seq_iterator$reify__21670(iPersistentMap, this.a);
    }

    public Object next() {
        Object object = ((IFn)const__2.getRawRoot()).invoke((Object)(((Iterator)this).hasNext() ? Boolean.TRUE : Boolean.FALSE));
        if (object != null && object != Boolean.FALSE) {
            throw (Throwable)new NoSuchElementException();
        }
        Object current = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(this.a));
        ((IFn)const__4.getRawRoot()).invoke(this.a, const__5.getRawRoot());
        Object var1_1 = null;
        return current;
    }

    public boolean hasNext() {
        return RT.booleanCast((Object)((IFn)const__1.getRawRoot()).invoke(this.a));
    }
}

