/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.RT;
import datomic.fulltext.SearchIterator;

public final class fulltext$fn__14523$__GT_SearchIterator__14525
extends AFunction {
    public Object invoke(Object searcher, Object search2, Object score_docs, Object attr, Object high_score) {
        Object object = searcher;
        searcher = null;
        Object object2 = search2;
        search2 = null;
        Object object3 = score_docs;
        score_docs = null;
        Object object4 = attr;
        attr = null;
        Object object5 = high_score;
        high_score = null;
        return new SearchIterator(object, object2, object3, object4, RT.floatCast((Object)((Number)object5)));
    }
}

