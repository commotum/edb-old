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

public final class index$reify__15238
implements ReadHandler,
IObj {
    final IPersistentMap __meta;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"ensure-vectors-in-array");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"transposed-data");

    public index$reify__15238(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public index$reify__15238() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new index$reify__15238(iPersistentMap);
    }

    public Object read(Reader rdr, Object tag, int component_count) throws IOException {
        Object vs;
        Object object;
        Object vs2 = rdr.readObject();
        if (((Class)((IFn)const__0.getRawRoot()).invoke(vs2)).isArray()) {
            object = vs2;
            vs2 = null;
        } else {
            Object object2 = vs2;
            vs2 = null;
            object = ((IFn)const__1.getRawRoot()).invoke(object2);
        }
        Object object3 = vs = object;
        vs = null;
        Object vs3 = ((IFn)const__2.getRawRoot()).invoke(object3);
        Object es = rdr.readObject();
        Object as = rdr.readObject();
        Object ts = rdr.readObject();
        Reader reader2 = rdr;
        rdr = null;
        Object ops = reader2.readObject();
        Object object4 = es;
        es = null;
        Object object5 = as;
        as = null;
        Object object6 = vs3;
        vs3 = null;
        Object object7 = ts;
        ts = null;
        Object object8 = ops;
        ops = null;
        index$reify__15238 this_ = null;
        return ((IFn)const__3.getRawRoot()).invoke(object4, object5, object6, object7, object8);
    }
}

