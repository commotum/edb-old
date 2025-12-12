/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic.garbage;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class fressian$read_handler$reify__16636
implements ReadHandler,
IObj {
    final IPersistentMap __meta;
    Object factory;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into");

    public fressian$read_handler$reify__16636(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.factory = object;
    }

    public fressian$read_handler$reify__16636(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$read_handler$reify__16636(iPersistentMap, this.factory);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Reader reader2 = rdr;
        rdr = null;
        fressian$read_handler$reify__16636 this_ = null;
        return ((IFn)this_.factory).invoke(((IFn)const__0.getRawRoot()).invoke((Object)PersistentVector.EMPTY, reader2.readObject()));
    }
}

