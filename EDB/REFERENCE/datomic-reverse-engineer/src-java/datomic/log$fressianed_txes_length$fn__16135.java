/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.ILookupThunk
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  org.fressian.impl.BytesOutputStream
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.ILookupThunk;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import org.fressian.impl.BytesOutputStream;

public final class log$fressianed_txes_length$fn__16135
extends AFunction {
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"fressianed-tx"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public Object invoke(Object tx) {
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = tx;
        tx = null;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        return ((BytesOutputStream)object2).length();
    }
}

