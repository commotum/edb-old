/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.datomic.lucene.document.Document
 *  com.datomic.lucene.search.IndexSearcher
 *  com.datomic.lucene.search.ScoreDoc
 */
package datomic.fulltext;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.datomic.lucene.document.Document;
import com.datomic.lucene.search.IndexSearcher;
import com.datomic.lucene.search.ScoreDoc;
import java.util.Iterator;

public final class SearchIterator
implements Iterator,
IType {
    public final Object searcher;
    public final Object search;
    public final Object score_docs;
    public final Object attr;
    public final float high_score;
    public static final Var const__0 = RT.var((String)"datomic.lucene", (String)"long-value");
    public static final Var const__1 = RT.var((String)"datomic.lucene", (String)"get-field");
    public static final Var const__2 = RT.var((String)"datomic.lucene", (String)"string-value");

    public SearchIterator(Object object, Object object2, Object object3, Object object4, float f) {
        this.searcher = object;
        this.search = object2;
        this.score_docs = object3;
        this.attr = object4;
        this.high_score = f;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"searcher")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IndexSearcher")})), (Object)Symbol.intern(null, (String)"search"), (Object)((IObj)Symbol.intern(null, (String)"score-docs")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iterator")})), (Object)Symbol.intern(null, (String)"attr"), (Object)((IObj)Symbol.intern(null, (String)"high-score")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"float")})));
    }

    public Object next() {
        Object sd = ((Iterator)this.score_docs).next();
        Document doc = ((IndexSearcher)this.searcher).doc(((ScoreDoc)sd).doc);
        Object object = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)doc, (Object)"e"));
        Object object2 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)doc, (Object)"v"));
        Document document2 = doc;
        doc = null;
        Object e = sd;
        sd = null;
        return Tuple.create((Object)this.search, (Object)this.attr, (Object)object, (Object)object2, (Object)((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)document2, (Object)"t")), (Object)Numbers.divide((double)((ScoreDoc)e).score, (double)this.high_score));
    }

    public boolean hasNext() {
        SearchIterator this_ = null;
        return ((Iterator)this_.score_docs).hasNext();
    }
}

