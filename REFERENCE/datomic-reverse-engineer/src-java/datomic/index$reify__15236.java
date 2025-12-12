/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  org.fressian.Writer
 *  org.fressian.handlers.WriteHandler
 */
package datomic;

import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import datomic.index.DirNode;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class index$reify__15236
implements WriteHandler,
IObj {
    final IPersistentMap __meta;

    public index$reify__15236(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public index$reify__15236() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new index$reify__15236(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object d = object;
        w.writeTag((Object)"index-dir-node", RT.uncheckedIntCast((long)4L));
        w.writeObject(((DirNode)d).keydata);
        w.writeObject(((DirNode)d).segids);
        w.writeObject((Object)Numbers.ints((Object)((DirNode)d).offsets));
        Writer writer2 = w;
        w = null;
        Object object2 = d;
        d = null;
        writer2.writeObject((Object)Numbers.ints((Object)((DirNode)object2).counts));
    }
}

