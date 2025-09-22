/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 */
package datomic.index;

import clojure.lang.AFunction;
import clojure.lang.Keyword;
import clojure.lang.RT;

public final class TreeIter$fn__15117$fn__15118
extends AFunction {
    Object segid;
    public static final Keyword const__0 = RT.keyword(null, (String)"segid");
    public static final Keyword const__1 = RT.keyword(null, (String)"item");

    public TreeIter$fn__15117$fn__15118(Object object) {
        this.segid = object;
    }

    public Object invoke(Object item) {
        Object[] objectArray = new Object[4];
        objectArray[0] = const__0;
        objectArray[1] = this.segid;
        objectArray[2] = const__1;
        Object object = item;
        item = null;
        objectArray[3] = object;
        return RT.mapUniqueKeys((Object[])objectArray);
    }
}

