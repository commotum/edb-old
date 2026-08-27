/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.fulltext;

import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.fulltext.SearchIterator;
import java.util.Iterator;

public final class SearchIterable
implements Iterable,
IType {
    public final Object searcher;
    public final Object search;
    public final Object score_docs;
    public final Object attr;
    public final float high_score;

    public SearchIterable(Object object, Object object2, Object object3, Object object4, float f) {
        this.searcher = object;
        this.search = object2;
        this.score_docs = object3;
        this.attr = object4;
        this.high_score = f;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"searcher"), (Object)Symbol.intern(null, (String)"search"), (Object)((IObj)Symbol.intern(null, (String)"score-docs")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Iterable")})), (Object)Symbol.intern(null, (String)"attr"), (Object)((IObj)Symbol.intern(null, (String)"high-score")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"float")})));
    }

    public Iterator iterator() {
        return new SearchIterator(this.searcher, this.search, ((Iterable)this.score_docs).iterator(), this.attr, this.high_score);
    }
}

