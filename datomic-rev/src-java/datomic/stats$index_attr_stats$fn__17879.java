/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.Database;

public final class stats$index_attr_stats$fn__17879
extends AFunction {
    Object db;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__3 = RT.keyword(null, (String)"count");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__0__ = __site__0__;

    public stats$index_attr_stats$fn__17879(Object object) {
        this.db = object;
    }

    public Object invoke(Object acc, Object datoms2) {
        Object object;
        Object temp__5455__auto__17881;
        Database database = (Database)this_.db;
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(datoms2);
        Object object3 = iLookupThunk.get(object2);
        if (iLookupThunk == object3) {
            __thunk__0__ = __site__0__.fault(object2);
            object3 = __thunk__0__.get(object2);
        }
        Object object4 = temp__5455__auto__17881 = database.ident(object3);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = temp__5455__auto__17881;
            temp__5455__auto__17881 = null;
            Object k = object5;
            Object object6 = acc;
            acc = null;
            Object object7 = k;
            k = null;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__3;
            Object object8 = datoms2;
            datoms2 = null;
            objectArray[1] = RT.count((Object)object8);
            stats$index_attr_stats$fn__17879 this_ = null;
            object = ((IFn)const__2.getRawRoot()).invoke(object6, object7, (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            object = acc;
            Object var1_1 = null;
        }
        return object;
    }
}

