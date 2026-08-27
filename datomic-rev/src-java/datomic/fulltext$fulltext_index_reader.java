/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.fulltext_index.LuceneProvider;

public final class fulltext$fulltext_index_reader
extends AFunction {
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Keyword const__1;
    public static final Var const__2;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object idx, Object attrid) {
        Object object;
        Object lprov;
        Object temp__5457__auto__14750;
        Object object2 = db2;
        db2 = null;
        Object object3 = idx;
        idx = null;
        Object object4 = temp__5457__auto__14750 = ((IFn)const__0.getRawRoot()).invoke(object2, (Object)Tuple.create((Object)object3, (Object)const__1));
        if (object4 == null) return null;
        if (object4 == Boolean.FALSE) return null;
        Object object5 = temp__5457__auto__14750;
        temp__5457__auto__14750 = null;
        Object object6 = lprov = object5;
        lprov = null;
        Object object7 = object6;
        if (Util.classOf((Object)object6) != __cached_class__0) {
            if (object7 instanceof LuceneProvider) {
                Object object8 = attrid;
                attrid = null;
                object = ((LuceneProvider)object7).fulltext_attr_reader(object8);
                return object;
            }
            object7 = object7;
            __cached_class__0 = Util.classOf((Object)object7);
        }
        Object object9 = attrid;
        attrid = null;
        object = const__2.getRawRoot().invoke(object7, object9);
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fulltext$fulltext_index_reader.invokeStatic(object4, object5, object6);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"get-in");
        const__1 = RT.keyword(null, (String)"fulltext");
        const__2 = RT.var((String)"datomic.fulltext-index", (String)"fulltext-attr-reader");
    }
}

