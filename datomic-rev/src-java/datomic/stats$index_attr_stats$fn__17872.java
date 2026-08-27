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

public final class stats$index_attr_stats$fn__17872
extends AFunction {
    Object db;
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Keyword const__4 = RT.keyword(null, (String)"count");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"map");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"key"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"a"));
    static ILookupThunk __thunk__1__ = __site__1__;

    public stats$index_attr_stats$fn__17872(Object object) {
        this.db = object;
    }

    public Object invoke(Object acc, Object entries) {
        Object object;
        Object temp__5455__auto__17874;
        Object object2;
        Database database = (Database)this_.db;
        ILookupThunk iLookupThunk = __thunk__1__;
        ILookupThunk iLookupThunk2 = __thunk__0__;
        Object object3 = ((IFn)const__2.getRawRoot()).invoke(entries);
        Object object4 = iLookupThunk2.get(object3);
        if (iLookupThunk2 == object4) {
            __thunk__0__ = __site__0__.fault(object3);
            object4 = __thunk__0__.get(object3);
        }
        if (iLookupThunk == (object2 = iLookupThunk.get(object4))) {
            __thunk__1__ = __site__1__.fault(object4);
            object2 = __thunk__1__.get(object4);
        }
        Object object5 = temp__5455__auto__17874 = database.ident(object2);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = temp__5455__auto__17874;
            temp__5455__auto__17874 = null;
            Object k = object6;
            Object object7 = acc;
            acc = null;
            Object object8 = k;
            k = null;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__4;
            Object object9 = entries;
            entries = null;
            objectArray[1] = ((IFn)const__5.getRawRoot()).invoke(const__6.getRawRoot(), ((IFn)const__7.getRawRoot()).invoke((Object)const__4, object9));
            stats$index_attr_stats$fn__17872 this_ = null;
            object = ((IFn)const__3.getRawRoot()).invoke(object7, object8, (Object)RT.mapUniqueKeys((Object[])objectArray));
        } else {
            object = acc;
            Object var1_1 = null;
        }
        return object;
    }
}

