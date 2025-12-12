/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  com.datomic.lucene.analysis.standard.StandardAnalyzer
 *  com.datomic.lucene.util.Version
 */
package datomic;

import clojure.lang.AFunction;
import com.datomic.lucene.analysis.standard.StandardAnalyzer;
import com.datomic.lucene.util.Version;

public final class lucene$index_writer$fn__12240
extends AFunction {
    public Object invoke(Object p1__12237_SHARP_) {
        Object object = p1__12237_SHARP_;
        p1__12237_SHARP_ = null;
        return new StandardAnalyzer((Version)object);
    }
}

