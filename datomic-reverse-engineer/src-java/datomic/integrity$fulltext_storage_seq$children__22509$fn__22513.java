/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;

public final class integrity$fulltext_storage_seq$children__22509$fn__22513
extends AFunction {
    Object olookup;
    public static final Keyword const__0 = RT.keyword(null, (String)"type");
    public static final Keyword const__1 = RT.keyword(null, (String)"chunk");
    public static final Keyword const__2 = RT.keyword(null, (String)"path");
    public static final Keyword const__3 = RT.keyword(null, (String)"seg");

    public integrity$fulltext_storage_seq$children__22509$fn__22513(Object object) {
        this.olookup = object;
    }

    public Object invoke(Object k) {
        Object[] objectArray = new Object[6];
        objectArray[0] = const__0;
        objectArray[1] = const__1;
        objectArray[2] = const__2;
        objectArray[3] = k;
        objectArray[4] = const__3;
        Object object = k;
        k = null;
        objectArray[5] = RT.get((Object)this.olookup, (Object)object);
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}

