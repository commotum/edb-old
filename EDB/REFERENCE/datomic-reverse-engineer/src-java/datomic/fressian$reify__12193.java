/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.BigInt
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  org.fressian.Writer
 *  org.fressian.handlers.WriteHandler
 */
package datomic;

import clojure.lang.BigInt;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import java.io.IOException;
import java.math.BigInteger;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class fressian$reify__12193
implements WriteHandler,
IObj {
    final IPersistentMap __meta;

    public fressian$reify__12193(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public fressian$reify__12193() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$reify__12193(iPersistentMap);
    }

    public void write(Writer w, Object d) throws IOException {
        Object object;
        if (d instanceof BigInt) {
            Object object2 = d;
            d = null;
            object = ((BigInt)object2).toBigInteger();
        } else {
            object = d;
            d = null;
        }
        Object bi = object;
        w.writeTag((Object)"bigint", RT.intCast((long)1L));
        Writer writer2 = w;
        w = null;
        Object object3 = bi;
        bi = null;
        writer2.writeBytes(((BigInteger)object3).toByteArray());
    }
}

