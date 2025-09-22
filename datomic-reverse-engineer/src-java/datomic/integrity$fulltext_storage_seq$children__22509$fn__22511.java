/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class integrity$fulltext_storage_seq$children__22509$fn__22511
extends AFunction {
    Object olookup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__1 = RT.keyword(null, (String)"type");
    public static final Keyword const__2 = RT.keyword(null, (String)"clusterfs");
    public static final Keyword const__3 = RT.keyword(null, (String)"uuid");
    public static final Keyword const__4 = RT.keyword(null, (String)"seg");

    public integrity$fulltext_storage_seq$children__22509$fn__22511(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object p1__22501_SHARP_) {
        Object object = p1__22501_SHARP_;
        p1__22501_SHARP_ = null;
        Object uuid = ((IFn)const__0.getRawRoot()).invoke(object);
        Object[] objectArray = new Object[6];
        objectArray[0] = const__1;
        objectArray[1] = const__2;
        objectArray[2] = const__3;
        objectArray[3] = uuid;
        objectArray[4] = const__4;
        Object object2 = uuid;
        uuid = null;
        objectArray[5] = RT.get((Object)this.olookup, (Object)object2);
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}

