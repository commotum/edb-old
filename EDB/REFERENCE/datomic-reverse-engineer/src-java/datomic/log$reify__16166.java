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

public final class log$reify__16166
implements WriteHandler,
IObj {
    final IPersistentMap __meta;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"t"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"uuid"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public log$reify__16166(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public log$reify__16166() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new log$reify__16166(iPersistentMap);
    }

    public void write(Writer w, Object o) throws IOException {
        Object object = o;
        o = null;
        Object entry = object;
        w.writeTag((Object)"log-dir", RT.intCast((long)2L));
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = entry;
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        w.writeInt(object3);
        Writer writer2 = w;
        w = null;
        ILookupThunk iLookupThunk2 = __thunk__1__;
        Object object4 = entry;
        entry = null;
        Object object5 = iLookupThunk2.get(object4);
        if (iLookupThunk2 == object5) {
            __thunk__1__ = __site__1__.fault(object4);
            object5 = __thunk__1__.get(object4);
        }
        writer2.writeObject(object5);
    }
}

