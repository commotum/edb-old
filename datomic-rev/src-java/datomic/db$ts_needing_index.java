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
import datomic.Database;

public final class db$ts_needing_index
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"memory-db");
    public static final Keyword const__1 = RT.keyword(null, (String)"excise");
    public static final Var const__2 = RT.var((String)"datomic.db", (String)"t-needing-excise");
    public static final Keyword const__3 = RT.keyword(null, (String)"schema");
    public static final Var const__4 = RT.var((String)"datomic.db", (String)"t-needing-avet");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"partial");
    public static final Keyword const__10 = RT.keyword(null, (String)"aevt");
    public static final Object const__12 = 15L;
    public static final Object const__13 = 44L;

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object type, Object limit_t) {
        Object object;
        Database mdb;
        block8: {
            Object object2 = limit_t;
            limit_t = null;
            mdb = ((Database)((IFn)const__0.getRawRoot()).invoke(db2)).asOf(object2);
            Object G__13193 = type;
            switch (Util.hash((Object)G__13193) >> 3 & 1) {
                case 0: {
                    if (G__13193 != const__1) break;
                    object = const__2.getRawRoot();
                    break block8;
                }
                case 1: {
                    if (G__13193 != const__3) break;
                    object = const__4.getRawRoot();
                    break block8;
                }
            }
            Object object3 = G__13193;
            G__13193 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"No matching clause: ", object3));
        }
        Object pred2 = object;
        IFn iFn = (IFn)const__6.getRawRoot();
        Object object4 = const__7.getRawRoot();
        IFn iFn2 = (IFn)const__8.getRawRoot();
        Object object5 = pred2;
        pred2 = null;
        Object object6 = db2;
        db2 = null;
        Object object7 = ((IFn)const__9.getRawRoot()).invoke(object5, object6);
        Database database = mdb;
        mdb = null;
        Object object8 = type;
        type = null;
        Object G__13194 = object8;
        switch (Util.hash((Object)G__13194) >> 3 & 1) {
            case 0: {
                if (G__13194 != const__1) break;
                Object object9 = const__12;
                return iFn.invoke(object4, iFn2.invoke(object7, database.datoms(const__10, RT.object_array((Object)Tuple.create((Object)object9)))));
            }
            case 1: {
                if (G__13194 != const__3) break;
                Object object9 = const__13;
                return iFn.invoke(object4, iFn2.invoke(object7, database.datoms(const__10, RT.object_array((Object)Tuple.create((Object)object9)))));
            }
        }
        Object object10 = G__13194;
        G__13194 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__5.getRawRoot()).invoke((Object)"No matching clause: ", object10));
    }

    public Object invoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        Object object6 = object3;
        object3 = null;
        return db$ts_needing_index.invokeStatic(object4, object5, object6);
    }
}

