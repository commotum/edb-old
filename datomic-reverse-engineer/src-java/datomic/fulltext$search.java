/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.Database;
import datomic.fulltext$search$fn__14752;

public final class fulltext$search
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"resolve-id");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"remove");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"nil?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final AFn const__9 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"memidx"), (Object)RT.keyword(null, (String)"indexing"), (Object)RT.keyword(null, (String)"index"), (Object)RT.keyword(null, (String)"history"));
    public static final AFn const__10 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"memidx"), (Object)RT.keyword(null, (String)"indexing"), (Object)RT.keyword(null, (String)"index"));
    public static final Var const__11 = RT.var((String)"datomic.lucene", (String)"multi-reader");
    public static final Keyword const__12 = RT.keyword(null, (String)"close-subreaders");
    public static final Var const__13 = RT.var((String)"datomic.fulltext", (String)"search-iterable");
    public static final Var const__14 = RT.var((String)"datomic.lucene", (String)"index-searcher");

    public static Object invokeStatic(Object db2, Object a, Object search_map) {
        Object object;
        Object temp__5455__auto__14755;
        Object attrid = ((IFn)const__0.getRawRoot()).invoke(db2, a);
        Object object2 = attrid;
        attrid = null;
        Object object3 = temp__5455__auto__14755 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke((Object)new fulltext$search$fn__14752(object2, db2), (Object)(((Database)db2).isHistory() ? const__9 : const__10))));
        if (object3 != null && object3 != Boolean.FALSE) {
            Object reader2;
            Object readers;
            Object object4 = temp__5455__auto__14755;
            temp__5455__auto__14755 = null;
            Object object5 = readers = object4;
            readers = null;
            Object object6 = reader2 = ((IFn)const__11.getRawRoot()).invoke(object5, (Object)const__12, (Object)Boolean.FALSE);
            reader2 = null;
            Object object7 = db2;
            db2 = null;
            Object object8 = a;
            a = null;
            Object object9 = search_map;
            search_map = null;
            object = ((IFn)const__13.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(object6), object7, object8, object9);
        } else {
            object = PersistentVector.EMPTY;
        }
        return object;
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return fulltext$search.invokeStatic(object4, object5, object6);
    }
}

