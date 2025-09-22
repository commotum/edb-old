/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class index$reify__15240
implements ReadHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"root-node");

    public index$reify__15240(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public index$reify__15240() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new index$reify__15240(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Object object = rdr.readObject();
        Reader reader2 = rdr;
        rdr = null;
        index$reify__15240 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, reader2.readObject());
    }
}

