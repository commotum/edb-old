/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.fressian.Reader
 *  org.fressian.handlers.ReadHandler
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.io.IOException;
import org.fressian.Reader;
import org.fressian.handlers.ReadHandler;

public final class index$reify__15242
implements ReadHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"datomic.index", (String)"dir-node");

    public index$reify__15242(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public index$reify__15242() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new index$reify__15242(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Object object = rdr.readObject();
        Object object2 = rdr.readObject();
        int[] nArray = Numbers.ints((Object)rdr.readObject());
        Reader reader2 = rdr;
        rdr = null;
        index$reify__15242 this_ = null;
        return ((IFn)const__0.getRawRoot()).invoke(object, object2, (Object)nArray, (Object)Numbers.ints((Object)reader2.readObject()));
    }
}

