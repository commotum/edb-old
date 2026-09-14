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

public final class integrity$index_storage_seq$children__22536$fn__22540
extends AFunction {
    Object olookup;
    public static final Keyword const__0 = RT.keyword(null, (String)"type");
    public static final Keyword const__1 = RT.keyword(null, (String)"leaf");
    public static final Keyword const__2 = RT.keyword(null, (String)"uuid");
    public static final Keyword const__3 = RT.keyword(null, (String)"seg");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");

    public integrity$index_storage_seq$children__22536$fn__22540(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object id) {
        Object[] objectArray = new Object[6];
        objectArray[0] = const__0;
        objectArray[1] = const__1;
        objectArray[2] = const__2;
        objectArray[3] = id;
        objectArray[4] = const__3;
        Object object = id;
        id = null;
        objectArray[5] = RT.get((Object)this.olookup, (Object)((IFn)const__5.getRawRoot()).invoke(object));
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}

