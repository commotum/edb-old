/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
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
import clojure.lang.ISeq;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class fressian$reify__16641
implements ReadHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");

    public fressian$reify__16641(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fressian$reify__16641() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$reify__16641(iPersistentMap);
    }

    public Object read(Reader rdr, Object _, int _2) throws IOException {
        Object os;
        Reader reader2 = rdr;
        rdr = null;
        Object object = os = reader2.readObject();
        os = null;
        fressian$reify__16641 this_ = null;
        return PersistentVector.create((ISeq)((ISeq)((IFn)const__0.getRawRoot()).invoke(object)));
    }
}

