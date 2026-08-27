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
 *  com.datomic.lucene.search.IndexSearcher
 *  com.datomic.lucene.search.Query
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import com.datomic.lucene.search.IndexSearcher;
import com.datomic.lucene.search.Query;
import datomic.lucene$search_seq$fn__12298;

public final class lucene$search_seq
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"max");
    public static final Object const__4 = 10000L;
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"map");

    public static Object invokeStatic(Object searcher, Object query2, ISeq p__12296) {
        ISeq map__12297;
        ISeq iSeq;
        ISeq iSeq2 = p__12296;
        p__12296 = null;
        ISeq map__122972 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__122972);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__122972;
            map__122972 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__122972;
            map__122972 = null;
        }
        ISeq iSeq4 = map__12297 = iSeq;
        map__12297 = null;
        Object max2 = RT.get((Object)iSeq4, (Object)const__3, (Object)const__4);
        Object object2 = searcher;
        searcher = null;
        Object object3 = query2;
        query2 = null;
        Object object4 = max2;
        max2 = null;
        return ((IFn)const__5.getRawRoot()).invoke((Object)new lucene$search_seq$fn__12298(), (Object)((IndexSearcher)object2).search((Query)((Query)object3), (int)RT.intCast((Object)object4)).scoreDocs);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return lucene$search_seq.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

