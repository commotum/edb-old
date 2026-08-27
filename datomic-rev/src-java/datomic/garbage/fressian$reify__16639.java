/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Writer
 *  org.fressian.handlers.WriteHandler
 */
package datomic.garbage;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class fressian$reify__16639
implements WriteHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");

    public fressian$reify__16639(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fressian$reify__16639() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$reify__16639(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        w.writeTag((Object)"vec", RT.intCast((long)1L));
        Writer writer2 = w;
        w = null;
        Object object = o;
        o = null;
        writer2.writeObject(((IFn)const__1.getRawRoot()).invoke(object));
    }
}

