/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.datomic.lucene.index.IndexReader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datomic.lucene.index.IndexReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class fulltext$remove_data_from_reader
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"partial");
    public static final Var const__1 = RT.var((String)"datomic.fulltext", (String)"find-historic-docid");
    public static final Var const__2 = RT.var((String)"datomic.lucene", (String)"index-searcher");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__6 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final Keyword const__7 = RT.keyword((String)"fulltext", (String)"remove-data");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object reader2, Object data2) {
        Object findid = ((IFn)const__0.getRawRoot()).invoke(const__1.getRawRoot(), ((IFn)const__2.getRawRoot()).invoke(reader2));
        Object object = data2;
        data2 = null;
        Object seq_14597 = ((IFn)const__3.getRawRoot()).invoke(object);
        Object chunk_14598 = null;
        long count_14599 = 0L;
        long i_14600 = 0L;
        while (true) {
            Object temp__5457__auto__14604;
            Object datum2;
            Object temp__5457__auto__14605;
            if (i_14600 < count_14599) {
                Object temp__5457__auto__14602;
                Object datum3;
                Object object2 = datum3 = ((Indexed)chunk_14598).nth(RT.intCast((long)i_14600));
                datum3 = null;
                Object object3 = temp__5457__auto__14602 = ((IFn)findid).invoke(object2);
                if (object3 != null && object3 != Boolean.FALSE) {
                    Object object4 = temp__5457__auto__14602;
                    temp__5457__auto__14602 = null;
                    Object docid = object4;
                    ((IndexReader)reader2).deleteDocument(RT.intCast((Object)((Number)docid)));
                    Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
                    if (logger.isDebugEnabled()) {
                        Logger logger2 = logger;
                        logger = null;
                        Object[] objectArray = new Object[2];
                        objectArray[0] = const__7;
                        Object object5 = docid;
                        docid = null;
                        objectArray[1] = object5;
                        logger2.debug((String)((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                    }
                }
                Object object6 = seq_14597;
                seq_14597 = null;
                Object object7 = chunk_14598;
                chunk_14598 = null;
                ++i_14600;
                chunk_14598 = object7;
                seq_14597 = object6;
                continue;
            }
            Object object8 = seq_14597;
            seq_14597 = null;
            Object object9 = temp__5457__auto__14605 = ((IFn)const__3.getRawRoot()).invoke(object8);
            if (object9 == null || object9 == Boolean.FALSE) break;
            Object object10 = temp__5457__auto__14605;
            temp__5457__auto__14605 = null;
            Object seq_145972 = object10;
            Object object11 = ((IFn)const__9.getRawRoot()).invoke(seq_145972);
            if (object11 != null && object11 != Boolean.FALSE) {
                Object c__5719__auto__14603 = ((IFn)const__10.getRawRoot()).invoke(seq_145972);
                Object object12 = seq_145972;
                seq_145972 = null;
                Object object13 = c__5719__auto__14603;
                Object object14 = c__5719__auto__14603;
                c__5719__auto__14603 = null;
                i_14600 = RT.intCast((long)0L);
                count_14599 = RT.intCast((int)RT.count((Object)object14));
                chunk_14598 = object13;
                seq_14597 = ((IFn)const__11.getRawRoot()).invoke(object12);
                continue;
            }
            Object object15 = datum2 = ((IFn)const__14.getRawRoot()).invoke(seq_145972);
            datum2 = null;
            Object object16 = temp__5457__auto__14604 = ((IFn)findid).invoke(object15);
            if (object16 != null && object16 != Boolean.FALSE) {
                Object object17 = temp__5457__auto__14604;
                temp__5457__auto__14604 = null;
                Object docid = object17;
                ((IndexReader)reader2).deleteDocument(RT.intCast((Object)((Number)docid)));
                Logger logger = LoggerFactory.getLogger((String)"datomic.fulltext");
                if (logger.isDebugEnabled()) {
                    Logger logger3 = logger;
                    logger = null;
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__7;
                    Object object18 = docid;
                    docid = null;
                    objectArray[1] = object18;
                    logger3.debug((String)((IFn)const__6.getRawRoot()).invoke((Object)RT.mapUniqueKeys((Object[])objectArray)));
                }
            }
            Object object19 = seq_145972;
            seq_145972 = null;
            i_14600 = 0L;
            count_14599 = 0L;
            chunk_14598 = null;
            seq_14597 = ((IFn)const__15.getRawRoot()).invoke(object19);
        }
        return null;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return fulltext$remove_data_from_reader.invokeStatic(object3, object4);
    }
}

