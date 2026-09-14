/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  org.fressian.Writer
 *  org.fressian.handlers.WriteHandler
 */
package datomic;

import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class transaction$write_handlers$reify__15894
implements WriteHandler,
IObj {
    final IPersistentMap __meta;
    Object cache;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"part"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"idx"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public transaction$write_handlers$reify__15894(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.cache = object;
    }

    public transaction$write_handlers$reify__15894(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new transaction$write_handlers$reify__15894(iPersistentMap, this.cache);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object dbid = object;
        w.writeTag((Object)"dbid", RT.intCast((long)2L));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = dbid;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        w.writeObject(object3, ((Boolean)this.cache).booleanValue());
        Writer writer2 = w;
        w = null;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = dbid;
        dbid = null;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        writer2.writeObject(object5, ((Boolean)this.cache).booleanValue());
    }
}

