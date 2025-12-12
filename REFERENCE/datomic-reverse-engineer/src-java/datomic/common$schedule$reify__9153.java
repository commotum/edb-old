/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import java.io.Closeable;
import java.io.IOException;
import java.util.Timer;

public final class common$schedule$reify__9153
implements Closeable,
IObj {
    final IPersistentMap __meta;
    Object t;

    public common$schedule$reify__9153(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.t = object;
    }

    public common$schedule$reify__9153(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new common$schedule$reify__9153(iPersistentMap, this.t);
    }

    public void close() throws IOException {
        ((Timer)this.t).cancel();
    }
}

