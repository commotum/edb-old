/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.stats$sizes$iter__17952__17958;

public final class stats$sizes
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"with-key-summary");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"raet"), (Object)RT.keyword(null, (String)"fulltext"));

    public static Object invokeStatic(Object db2, ISeq p__17950) {
        stats$sizes$iter__17952__17958 iter__6025__auto__17978;
        ISeq map__17951;
        ISeq iSeq;
        ISeq iSeq2 = p__17950;
        p__17950 = null;
        ISeq map__179512 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__179512);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__179512;
            map__179512 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__179512;
            map__179512 = null;
        }
        ISeq iSeq4 = map__17951 = iSeq;
        map__17951 = null;
        Object with_key_summary = RT.get((Object)iSeq4, (Object)const__3);
        Object object2 = db2;
        db2 = null;
        Object object3 = with_key_summary;
        with_key_summary = null;
        stats$sizes$iter__17952__17958 stats$sizes$iter__17952__17958 = iter__6025__auto__17978 = new stats$sizes$iter__17952__17958(object2, object3);
        iter__6025__auto__17978 = null;
        return ((IFn)stats$sizes$iter__17952__17958).invoke((Object)const__9);
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return stats$sizes.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

