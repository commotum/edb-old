/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.search.IndexSearcher
 *  com.datomic.lucene.search.Query
 *  com.datomic.lucene.search.ScoreDoc
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.search.IndexSearcher;
import com.datomic.lucene.search.Query;
import com.datomic.lucene.search.ScoreDoc;
import datomic.fulltext$search_iterable$fn__14534;
import datomic.fulltext.SearchIterable;

public final class fulltext$search_iterable
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"string?");
    public static final Keyword const__1 = RT.keyword(null, (String)"search");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__5 = RT.keyword(null, (String)"limit");
    public static final Object const__6 = 10000L;
    public static final Var const__7 = RT.var((String)"datomic.lucene", (String)"parse-query");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");

    public static Object invokeStatic(Object searcher, Object db2, Object attr, Object search_map) {
        Object object;
        Object query2;
        Object object2;
        Object qmap;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(search_map);
        Object object4 = qmap = object3 != null && object3 != Boolean.FALSE ? RT.mapUniqueKeys((Object[])new Object[]{const__1, search_map}) : search_map;
        qmap = null;
        Object map__14533 = object4;
        Object object5 = ((IFn)const__2.getRawRoot()).invoke(map__14533);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__14533;
            map__14533 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__3.getRawRoot()).invoke(object6)));
        } else {
            object2 = map__14533;
            map__14533 = null;
        }
        Object map__145332 = object2;
        Object search2 = RT.get((Object)map__145332, (Object)const__1);
        Object object7 = map__145332;
        map__145332 = null;
        Object limit2 = RT.get((Object)object7, (Object)const__5, (Object)const__6);
        Object object8 = search2;
        search2 = null;
        Object object9 = query2 = ((IFn)const__7.getRawRoot()).invoke((Object)"v", object8);
        query2 = null;
        Object object10 = limit2;
        limit2 = null;
        ScoreDoc[] scoredocs = ((IndexSearcher)searcher).search((Query)((Query)object9), (int)RT.intCast((Object)object10)).scoreDocs;
        Object object11 = ((IFn)const__3.getRawRoot()).invoke((Object)scoredocs);
        if (object11 != null && object11 != Boolean.FALSE) {
            search_map = null;
            searcher = null;
            db2 = null;
            attr = null;
            scoredocs = null;
            object = new SearchIterable(searcher, search_map, ((IFn)const__9.getRawRoot()).invoke(const__10.getRawRoot(), ((IFn)const__11.getRawRoot()).invoke((Object)new fulltext$search_iterable$fn__14534(attr, searcher, db2), (Object)scoredocs)), attr, ((ScoreDoc)((IFn)fulltext$search_iterable.const__12.getRawRoot()).invoke((Object)scoredocs)).score);
        } else {
            object = PersistentVector.EMPTY;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return fulltext$search_iterable.invokeStatic(object5, object6, object7, object8);
    }
}

