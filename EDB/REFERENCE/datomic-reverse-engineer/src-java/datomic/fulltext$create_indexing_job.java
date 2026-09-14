/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.fulltext$create_indexing_job$fn__14550;

public final class fulltext$create_indexing_job
extends AFunction {
    public static final Var const__1 = RT.var((String)"datomic.fulltext", (String)"cluster-directory");
    public static final Var const__2 = RT.var((String)"datomic.fulltext", (String)"null-dir");
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"create-temp-directory");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__5 = RT.var((String)"datomic.fulltext", (String)"work-dir");
    public static final Var const__6 = RT.var((String)"datomic.lucene", (String)"fs-directory");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"atom");
    public static final Var const__8 = RT.var((String)"datomic.fulltext", (String)"hybrid-dir");
    public static final Keyword const__9 = RT.keyword(null, (String)"cstore");
    public static final Keyword const__10 = RT.keyword(null, (String)"baseid");
    public static final Keyword const__11 = RT.keyword(null, (String)"directory");
    public static final Keyword const__12 = RT.keyword(null, (String)"path");
    public static final Keyword const__13 = RT.keyword(null, (String)"basefs");
    public static final Keyword const__14 = RT.keyword(null, (String)"delete-requests");

    public static Object invokeStatic(Object cstore, Object olookup, Object baseid) {
        Object object;
        Object object2 = baseid;
        Object basefs = object2 != null && object2 != Boolean.FALSE ? RT.get((Object)olookup, (Object)baseid) : null;
        Object object3 = baseid;
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = olookup;
            olookup = null;
            object = ((IFn)const__1.getRawRoot()).invoke(basefs, object4);
        } else {
            object = const__2.getRawRoot();
        }
        Object reader_dir = object;
        Object path2 = ((IFn)const__3.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot()));
        Object writer_dir = ((IFn)const__6.getRawRoot()).invoke(path2);
        Object delete_requests = ((IFn)const__7.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
        Object object5 = writer_dir;
        writer_dir = null;
        Object object6 = reader_dir;
        reader_dir = null;
        Object directory = ((IFn)const__8.getRawRoot()).invoke(object5, object6, (Object)new fulltext$create_indexing_job$fn__14550(delete_requests));
        Object[] objectArray = new Object[12];
        objectArray[0] = const__9;
        Object object7 = cstore;
        cstore = null;
        objectArray[1] = object7;
        objectArray[2] = const__10;
        Object object8 = baseid;
        baseid = null;
        objectArray[3] = object8;
        objectArray[4] = const__11;
        Object object9 = directory;
        directory = null;
        objectArray[5] = object9;
        objectArray[6] = const__12;
        Object object10 = path2;
        path2 = null;
        objectArray[7] = object10;
        objectArray[8] = const__13;
        Object object11 = basefs;
        basefs = null;
        objectArray[9] = object11;
        objectArray[10] = const__14;
        Object object12 = delete_requests;
        delete_requests = null;
        objectArray[11] = object12;
        return RT.mapUniqueKeys((Object[])objectArray);
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fulltext$create_indexing_job.invokeStatic(object4, object5, object6);
    }
}

