/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  com.datomic.lucene.document.Document
 *  com.datomic.lucene.document.Fieldable
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import com.datomic.lucene.document.Document;
import com.datomic.lucene.document.Fieldable;

public final class lucene$document
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(ISeq fields) {
        Document d = new Document();
        ISeq iSeq = fields;
        fields = null;
        Object seq_12301 = ((IFn)const__0.getRawRoot()).invoke((Object)iSeq);
        Object chunk_12302 = null;
        long count_12303 = 0L;
        long i_12304 = 0L;
        while (true) {
            Object f;
            Object temp__5457__auto__12307;
            if (i_12304 < count_12303) {
                Object f2;
                Object object = f2 = ((Indexed)chunk_12302).nth(RT.intCast((long)i_12304));
                f2 = null;
                d.add((Fieldable)object);
                Object object2 = seq_12301;
                seq_12301 = null;
                Object object3 = chunk_12302;
                chunk_12302 = null;
                ++i_12304;
                chunk_12302 = object3;
                seq_12301 = object2;
                continue;
            }
            Object object = seq_12301;
            seq_12301 = null;
            Object object4 = temp__5457__auto__12307 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object4 == null || object4 == Boolean.FALSE) break;
            Object object5 = temp__5457__auto__12307;
            temp__5457__auto__12307 = null;
            Object seq_123012 = object5;
            Object object6 = ((IFn)const__4.getRawRoot()).invoke(seq_123012);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object c__5719__auto__12306 = ((IFn)const__5.getRawRoot()).invoke(seq_123012);
                Object object7 = seq_123012;
                seq_123012 = null;
                Object object8 = c__5719__auto__12306;
                Object object9 = c__5719__auto__12306;
                c__5719__auto__12306 = null;
                i_12304 = RT.intCast((long)0L);
                count_12303 = RT.intCast((int)RT.count((Object)object9));
                chunk_12302 = object8;
                seq_12301 = ((IFn)const__6.getRawRoot()).invoke(object7);
                continue;
            }
            Object object10 = f = ((IFn)const__9.getRawRoot()).invoke(seq_123012);
            f = null;
            d.add((Fieldable)object10);
            Object object11 = seq_123012;
            seq_123012 = null;
            i_12304 = 0L;
            count_12303 = 0L;
            chunk_12302 = null;
            seq_12301 = ((IFn)const__10.getRawRoot()).invoke(object11);
        }
        Object var1_1 = null;
        return d;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return lucene$document.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

