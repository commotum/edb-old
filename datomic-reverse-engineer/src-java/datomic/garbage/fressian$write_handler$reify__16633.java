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
package datomic.garbage;

import clojure.lang.ILookupThunk;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import java.io.IOException;
import org.fressian.Writer;
import org.fressian.handlers.WriteHandler;

public final class fressian$write_handler$reify__16633
implements WriteHandler,
IObj {
    final IPersistentMap __meta;
    Object tag;
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"children"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public fressian$write_handler$reify__16633(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.tag = object;
    }

    public fressian$write_handler$reify__16633(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new fressian$write_handler$reify__16633(iPersistentMap, this.tag);
    }

    public void write(Writer w, Object o) throws IOException {
        w.writeTag(this.tag, RT.intCast((long)1L));
        Writer writer2 = w;
        w = null;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = o;
        o = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        writer2.writeObject(object2);
    }
}

