/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.search.IndexSearcher
 *  com.datomic.lucene.search.Query
 *  com.datomic.lucene.search.ScoreDoc
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.search.IndexSearcher;
import com.datomic.lucene.search.Query;
import com.datomic.lucene.search.ScoreDoc;
import datomic.impl.db.IDatum;

public final class fulltext$find_historic_docid
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.lucene", (String)"boolean-query");
    public static final Var const__1 = RT.var((String)"datomic.lucene", (String)"long-query");
    public static final Keyword const__2 = RT.keyword(null, (String)"must");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__7 = RT.var((String)"datomic.fulltext", (String)"doc->datum");
    public static final Var const__9 = RT.var((String)"datomic.common", (String)"compare");

    public static Object invokeStatic(Object searcher, Object datum2) {
        Integer n;
        block4: {
            ScoreDoc[] G__14588;
            ScoreDoc[] vec__14589;
            Object q2;
            Object object = q2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)"e", (Object)Numbers.num((long)((IDatum)datum2).getE())), (Object)const__2, ((IFn)const__1.getRawRoot()).invoke((Object)"t", (Object)Numbers.num((long)((IDatum)datum2).getT())), (Object)const__2);
            q2 = null;
            ScoreDoc[] scoreDocArray = vec__14589 = (G__14588 = ((IndexSearcher)searcher).search((Query)((Query)object), (int)RT.intCast((long)1000L)).scoreDocs);
            vec__14589 = null;
            Object seq__14590 = ((IFn)const__4.getRawRoot()).invoke((Object)scoreDocArray);
            Object first__14591 = ((IFn)const__5.getRawRoot()).invoke(seq__14590);
            Object object2 = seq__14590;
            seq__14590 = null;
            Object seq__145902 = ((IFn)const__6.getRawRoot()).invoke(object2);
            first__14591 = null;
            seq__145902 = null;
            ScoreDoc[] scoreDocArray2 = G__14588;
            G__14588 = null;
            Object G__145882 = scoreDocArray2;
            while (true) {
                Object object3;
                Object found;
                Object and__5236__auto__14596;
                ScoreDoc[] vec__14592;
                ScoreDoc[] scoreDocArray3 = G__145882;
                G__145882 = null;
                ScoreDoc[] scoreDocArray4 = vec__14592 = scoreDocArray3;
                vec__14592 = null;
                Object seq__14593 = ((IFn)const__4.getRawRoot()).invoke((Object)scoreDocArray4);
                Object first__14594 = ((IFn)const__5.getRawRoot()).invoke(seq__14593);
                Object object4 = seq__14593;
                seq__14593 = null;
                Object seq__145932 = ((IFn)const__6.getRawRoot()).invoke(object4);
                Object object5 = first__14594;
                first__14594 = null;
                Object sd = object5;
                Object object6 = seq__145932;
                seq__145932 = null;
                Object more = object6;
                Object object7 = sd;
                if (object7 == null || object7 == Boolean.FALSE) break;
                Object object8 = sd;
                sd = null;
                int docid = ((ScoreDoc)object8).doc;
                Object object9 = and__5236__auto__14596 = (found = ((IFn.OLO)const__7.getRawRoot()).invokePrim((Object)((IndexSearcher)searcher).doc(docid), (long)((IDatum)datum2).getA()));
                if (object9 != null && object9 != Boolean.FALSE) {
                    Object object10 = found;
                    found = null;
                    object3 = Numbers.isZero((long)((IFn.OOL)const__9.getRawRoot()).invokePrim(((IDatum)datum2).getV(), ((IDatum)object10).getV())) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object3 = and__5236__auto__14596;
                    and__5236__auto__14596 = null;
                }
                if (object3 != null && object3 != Boolean.FALSE) {
                    n = docid;
                    break block4;
                }
                Object object11 = more;
                more = null;
                G__145882 = object11;
            }
            n = null;
        }
        return n;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext$find_historic_docid.invokeStatic(object3, object4);
    }
}

