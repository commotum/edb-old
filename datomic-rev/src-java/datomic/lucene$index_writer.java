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
 *  com.datomic.lucene.index.IndexWriter
 *  com.datomic.lucene.index.IndexWriterConfig
 *  com.datomic.lucene.store.Directory
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
import com.datomic.lucene.index.IndexWriter;
import com.datomic.lucene.index.IndexWriterConfig;
import com.datomic.lucene.store.Directory;
import com.datomic.lucene.util.Version;
import datomic.lucene$index_writer$fn__12240;

public final class lucene$index_writer
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"version");
    public static final Keyword const__4 = RT.keyword(null, (String)"analyzer-fn");
    public static final Var const__5 = RT.var((String)"datomic.lucene", (String)"create-config");
    public static final Keyword const__6 = RT.keyword(null, (String)"analyzer");

    public static Object invokeStatic(Object directory, ISeq p__12238) {
        IndexWriter writer2;
        Object analyzer_fn;
        Object object;
        Object or__5238__auto__12244;
        Object object2;
        Object or__5238__auto__12243;
        ISeq iSeq;
        ISeq iSeq2 = p__12238;
        p__12238 = null;
        ISeq map__12239 = iSeq2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke((Object)map__12239);
        if (object3 != null && object3 != Boolean.FALSE) {
            ISeq iSeq3 = map__12239;
            map__12239 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__12239;
            map__12239 = null;
        }
        ISeq map__122392 = iSeq;
        Object version2 = RT.get((Object)map__122392, (Object)const__3);
        ISeq iSeq4 = map__122392;
        map__122392 = null;
        Object analyzer_fn2 = RT.get((Object)iSeq4, (Object)const__4);
        Object object4 = version2;
        version2 = null;
        Object object5 = or__5238__auto__12243 = object4;
        if (object5 != null && object5 != Boolean.FALSE) {
            object2 = or__5238__auto__12243;
            or__5238__auto__12243 = null;
        } else {
            object2 = Version.LUCENE_33;
        }
        Object version3 = object2;
        Object object6 = analyzer_fn2;
        analyzer_fn2 = null;
        Object object7 = or__5238__auto__12244 = object6;
        if (object7 != null && object7 != Boolean.FALSE) {
            object = or__5238__auto__12244;
            or__5238__auto__12244 = null;
        } else {
            object = new lucene$index_writer$fn__12240();
        }
        Object object8 = analyzer_fn = object;
        analyzer_fn = null;
        Object analyzer = ((IFn)object8).invoke(version3);
        Object object9 = version3;
        version3 = null;
        Object object10 = analyzer;
        analyzer = null;
        Object config2 = ((IFn)const__5.getRawRoot()).invoke((Object)const__3, object9, (Object)const__6, object10);
        Object object11 = directory;
        directory = null;
        Object object12 = config2;
        config2 = null;
        IndexWriter indexWriter = writer2 = new IndexWriter((Directory)object11, (IndexWriterConfig)object12);
        writer2 = null;
        return indexWriter;
    }

    public Object doInvoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        ISeq iSeq = (ISeq)object2;
        object2 = null;
        return lucene$index_writer.invokeStatic(object3, iSeq);
    }

    public int getRequiredArity() {
        return 1;
    }
}

