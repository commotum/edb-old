/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import datomic.log.LogDir;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class log$reify__16168
implements ReadHandler,
IObj {
    final IPersistentMap __meta;

    public log$reify__16168(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public log$reify__16168() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new log$reify__16168(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        long l = rdr.readInt();
        Reader reader2 = rdr;
        rdr = null;
        return new LogDir(l, reader2.readObject());
    }
}

