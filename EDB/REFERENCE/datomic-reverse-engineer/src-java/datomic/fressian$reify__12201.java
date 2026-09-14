/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import java.util.List;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class fressian$reify__12201
implements ReadHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");

    public fressian$reify__12201(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fressian$reify__12201() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$reify__12201(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        PersistentArrayMap persistentArrayMap;
        Reader reader2 = rdr;
        rdr = null;
        Object kvs = reader2.readObject();
        if ((long)((List)kvs).size() < 16L) {
            kvs = null;
            persistentArrayMap = new PersistentArrayMap(((List)kvs).toArray());
        } else {
            Object object = kvs;
            kvs = null;
            fressian$reify__12201 this_ = null;
            persistentArrayMap = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__2.getRawRoot()).invoke(object)));
        }
        return persistentArrayMap;
    }
}

