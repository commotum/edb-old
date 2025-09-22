/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  com.datomic.lucene.search.BooleanClause$Occur
 *  com.datomic.lucene.search.BooleanQuery
 *  com.datomic.lucene.search.Query
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import com.datomic.lucene.search.BooleanClause;
import com.datomic.lucene.search.BooleanQuery;
import com.datomic.lucene.search.Query;

public final class lucene$boolean_query
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__2 = 2L;
    public static final Keyword const__8 = RT.keyword(null, (String)"should");
    public static final Keyword const__9 = RT.keyword(null, (String)"must");
    public static final Keyword const__10 = RT.keyword(null, (String)"must_not");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(ISeq queryoccurs) {
        BooleanQuery bq = new BooleanQuery();
        ISeq iSeq = queryoccurs;
        queryoccurs = null;
        Object seq_12254 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(const__2, (Object)iSeq));
        Object chunk_12255 = null;
        long count_12256 = 0L;
        long i_12257 = 0L;
        while (true) {
            Object temp__5457__auto__12266;
            if (i_12257 < count_12256) {
                Object vec__12258 = ((Indexed)chunk_12255).nth(RT.intCast((long)i_12257));
                Object q2 = RT.nth((Object)vec__12258, (int)RT.intCast((long)0L), null);
                Object object = vec__12258;
                vec__12258 = null;
                Object o = RT.nth((Object)object, (int)RT.intCast((long)1L), null);
                Object object2 = q2;
                q2 = null;
                Object object3 = o;
                Object object4 = o;
                o = null;
                bq.add((Query)object2, (BooleanClause.Occur)RT.get((Object)RT.mapUniqueKeys((Object[])new Object[]{const__8, BooleanClause.Occur.SHOULD, const__9, BooleanClause.Occur.MUST, const__10, BooleanClause.Occur.MUST_NOT}), (Object)object3, (Object)object4));
                Object object5 = seq_12254;
                seq_12254 = null;
                Object object6 = chunk_12255;
                chunk_12255 = null;
                ++i_12257;
                chunk_12255 = object6;
                seq_12254 = object5;
                continue;
            }
            Object object = seq_12254;
            seq_12254 = null;
            Object object7 = temp__5457__auto__12266 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object7 == null || object7 == Boolean.FALSE) break;
            Object object8 = temp__5457__auto__12266;
            temp__5457__auto__12266 = null;
            Object seq_122542 = object8;
            Object object9 = ((IFn)const__12.getRawRoot()).invoke(seq_122542);
            if (object9 != null && object9 != Boolean.FALSE) {
                Object c__5719__auto__12265 = ((IFn)const__13.getRawRoot()).invoke(seq_122542);
                Object object10 = seq_122542;
                seq_122542 = null;
                Object object11 = c__5719__auto__12265;
                Object object12 = c__5719__auto__12265;
                c__5719__auto__12265 = null;
                i_12257 = RT.intCast((long)0L);
                count_12256 = RT.intCast((int)RT.count((Object)object12));
                chunk_12255 = object11;
                seq_12254 = ((IFn)const__14.getRawRoot()).invoke(object10);
                continue;
            }
            Object vec__12261 = ((IFn)const__17.getRawRoot()).invoke(seq_122542);
            Object q3 = RT.nth((Object)vec__12261, (int)RT.intCast((long)0L), null);
            Object object13 = vec__12261;
            vec__12261 = null;
            Object o = RT.nth((Object)object13, (int)RT.intCast((long)1L), null);
            Object object14 = q3;
            q3 = null;
            Object object15 = o;
            Object object16 = o;
            o = null;
            bq.add((Query)object14, (BooleanClause.Occur)RT.get((Object)RT.mapUniqueKeys((Object[])new Object[]{const__8, BooleanClause.Occur.SHOULD, const__9, BooleanClause.Occur.MUST, const__10, BooleanClause.Occur.MUST_NOT}), (Object)object15, (Object)object16));
            Object object17 = seq_122542;
            seq_122542 = null;
            i_12257 = 0L;
            count_12256 = 0L;
            chunk_12255 = null;
            seq_12254 = ((IFn)const__18.getRawRoot()).invoke(object17);
        }
        Object var1_1 = null;
        return bq;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return lucene$boolean_query.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

