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
 *  com.datomic.lucene.analysis.Analyzer
 *  com.datomic.lucene.analysis.standard.StandardAnalyzer
 *  com.datomic.lucene.queryParser.QueryParser
 *  com.datomic.lucene.util.Version
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import com.datomic.lucene.analysis.Analyzer;
import com.datomic.lucene.analysis.standard.StandardAnalyzer;
import com.datomic.lucene.queryParser.QueryParser;
import com.datomic.lucene.util.Version;

public final class lucene$parse_query
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"analyzer");

    public static Object invokeStatic(Object f, Object s, ISeq p__12319) {
        Object object;
        Object or__5238__auto__12322;
        Object analyzer;
        ISeq map__12320;
        ISeq iSeq;
        ISeq iSeq2 = p__12319;
        p__12319 = null;
        ISeq map__123202 = iSeq2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke((Object)map__123202);
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq iSeq3 = map__123202;
            map__123202 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__123202;
            map__123202 = null;
        }
        ISeq iSeq4 = map__12320 = iSeq;
        map__12320 = null;
        Object object3 = analyzer = RT.get((Object)iSeq4, (Object)const__3);
        analyzer = null;
        Object object4 = or__5238__auto__12322 = object3;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5238__auto__12322;
            or__5238__auto__12322 = null;
        } else {
            object = new StandardAnalyzer(Version.LUCENE_33);
        }
        Object analyzer2 = object;
        Object object5 = f;
        f = null;
        Object object6 = analyzer2;
        analyzer2 = null;
        Object object7 = s;
        s = null;
        return new QueryParser(Version.LUCENE_33, (String)object5, (Analyzer)object6).parse((String)object7);
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return lucene$parse_query.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

