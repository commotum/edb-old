/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.cluster$write_vals$fn__10675$fn__10677;

public final class cluster$write_vals$fn__10675
extends AFunction {
    Object vmap;
    Object cs;
    Object source;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.cluster", (String)"write-vals*");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"map");
    public static final Keyword const__3 = RT.keyword(null, (String)"threw");

    public cluster$write_vals$fn__10675(Object object, Object object2, Object object3) {
        this.vmap = object;
        this.cs = object2;
        this.source = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            this.cs = null;
            this.source = null;
            this.vmap = null;
            objectArray[1] = ((IFn)const__1.getRawRoot()).invoke(this.cs, this.source, ((IFn)const__2.getRawRoot()).invoke((Object)new cluster$write_vals$fn__10675$fn__10677(), this.vmap));
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__3;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

