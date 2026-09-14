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
 *  com.datomic.lucene.index.IndexWriterConfig
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
import com.datomic.lucene.index.IndexWriterConfig;
import com.datomic.lucene.util.Version;

public final class lucene$create_config
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"version");
    public static final Keyword const__4 = RT.keyword(null, (String)"analyzer");

    public static Object invokeStatic(ISeq p__12232) {
        ISeq iSeq;
        ISeq iSeq2 = p__12232;
        p__12232 = null;
        ISeq map__12233 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__12233);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__12233;
            map__12233 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__12233;
            map__12233 = null;
        }
        ISeq map__122332 = iSeq;
        Object version2 = RT.get((Object)map__122332, (Object)const__3);
        ISeq iSeq4 = map__122332;
        map__122332 = null;
        Object analyzer = RT.get((Object)iSeq4, (Object)const__4);
        Object object2 = version2;
        version2 = null;
        Object object3 = analyzer;
        analyzer = null;
        return new IndexWriterConfig((Version)object2, (Analyzer)object3);
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return lucene$create_config.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

