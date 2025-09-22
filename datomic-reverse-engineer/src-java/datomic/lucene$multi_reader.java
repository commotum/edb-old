/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  com.datomic.lucene.index.IndexReader
 *  com.datomic.lucene.index.MultiReader
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import com.datomic.lucene.index.IndexReader;
import com.datomic.lucene.index.MultiReader;

public final class lucene$multi_reader
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"close-subreaders");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__5 = RT.classForName((String)"com.datomic.lucene.index.IndexReader");

    public static Object invokeStatic(Object rdrs, ISeq p__12290) {
        ISeq map__12291;
        ISeq iSeq;
        ISeq iSeq2 = p__12290;
        p__12290 = null;
        ISeq map__122912 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__122912);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__122912;
            map__122912 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__122912;
            map__122912 = null;
        }
        ISeq iSeq4 = map__12291 = iSeq;
        map__12291 = null;
        Object close_subreaders = RT.get((Object)iSeq4, (Object)const__3);
        Object object2 = rdrs;
        rdrs = null;
        Object object3 = close_subreaders;
        close_subreaders = null;
        return new MultiReader((IndexReader[])((IFn)const__4.getRawRoot()).invoke(const__5, object2), ((Boolean)object3).booleanValue());
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return lucene$multi_reader.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

