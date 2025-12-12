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
 *  org.fressian.Reader
 */
package datomic.fressian;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.iter.Iter;
import java.io.EOFException;
import org.fressian.Reader;

public final class FressianIter
implements Iter,
IType {
    public final Object reader;
    Object item;

    public FressianIter(Object object, Object object2) {
        this.reader = object;
        this.item = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"reader")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"org.fressian.Reader")})), (Object)((IObj)Symbol.intern(null, (String)"item")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object next() {
        FressianIter fressianIter;
        try {
            this.item = ((Reader)this.reader).readObject();
            fressianIter = this;
        }
        catch (EOFException _) {
            fressianIter = null;
        }
        return fressianIter;
    }

    public Object get() {
        return this.item;
    }
}

