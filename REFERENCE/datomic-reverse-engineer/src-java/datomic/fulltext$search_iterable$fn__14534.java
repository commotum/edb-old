/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  com.datomic.lucene.document.Document
 *  com.datomic.lucene.search.IndexSearcher
 *  com.datomic.lucene.search.ScoreDoc
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import com.datomic.lucene.document.Document;
import com.datomic.lucene.search.IndexSearcher;
import com.datomic.lucene.search.ScoreDoc;
import datomic.db.IDb;
import datomic.impl.db.IDatum;

public final class fulltext$search_iterable$fn__14534
extends AFunction {
    Object attr;
    Object searcher;
    Object db;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"datum");
    public static final Keyword const__1 = RT.keyword(null, (String)"e");
    public static final Var const__2 = RT.var((String)"datomic.lucene", (String)"long-value");
    public static final Var const__3 = RT.var((String)"datomic.lucene", (String)"get-field");
    public static final Keyword const__4 = RT.keyword(null, (String)"a");
    public static final Keyword const__5 = RT.keyword(null, (String)"v");
    public static final Var const__6 = RT.var((String)"datomic.lucene", (String)"string-value");
    public static final Keyword const__7 = RT.keyword(null, (String)"asserting");
    public static final Var const__8 = RT.var((String)"datomic.db", (String)"windowed");
    public static final Var const__9 = RT.var((String)"datomic.db", (String)"dget");
    public static final Var const__12 = RT.var((String)"datomic.common", (String)"compare");

    public fulltext$search_iterable$fn__14534(Object object, Object object2, Object object3) {
        this.attr = object;
        this.searcher = object2;
        this.db = object3;
    }

    public Object invoke(Object sd) {
        Object object;
        Object object2;
        Object it;
        Object and__5236__auto__14538;
        Object iter2;
        Document doc = ((IndexSearcher)this.searcher).doc(((ScoreDoc)sd).doc);
        Object object3 = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)doc, (Object)"e"));
        Document document2 = doc;
        doc = null;
        Object d = ((IFn)const__0.getRawRoot()).invoke(this.db, (Object)const__1, object3, (Object)const__4, this.attr, (Object)const__5, ((IFn)const__6.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke((Object)document2, (Object)"v")), (Object)const__7, (Object)Boolean.FALSE);
        Object object4 = iter2 = ((IFn)const__8.getRawRoot()).invoke(this.db, null, (Object)((IDb)this.db).seekAEVT((IDatum)d));
        iter2 = null;
        Object object5 = and__5236__auto__14538 = (it = ((IFn)const__9.getRawRoot()).invoke(object4));
        if (object5 != null && object5 != Boolean.FALSE) {
            boolean and__5236__auto__14537 = Util.equiv((long)((IDatum)d).getE(), (long)((IDatum)it).getE());
            if (and__5236__auto__14537) {
                boolean and__5236__auto__14536 = Util.equiv((long)((IDatum)d).getA(), (long)((IDatum)it).getA());
                if (and__5236__auto__14536) {
                    Object object6 = d;
                    d = null;
                    Object object7 = it;
                    it = null;
                    object2 = Numbers.isZero((long)((IFn.OOL)const__12.getRawRoot()).invokePrim(((IDatum)object6).getV(), ((IDatum)object7).getV())) ? Boolean.TRUE : Boolean.FALSE;
                } else {
                    object2 = and__5236__auto__14536 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                object2 = and__5236__auto__14537 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            object2 = and__5236__auto__14538;
            and__5236__auto__14538 = null;
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = sd;
            sd = null;
        } else {
            object = null;
        }
        return object;
    }
}

