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
import datomic.fulltext.SearchIterable;

public final class fulltext$fn__14528$__GT_SearchIterable__14530
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
        return new SearchIterable(object, object2, object3, object4, RT.floatCast((Object)((Number)object5)));
    }
}

