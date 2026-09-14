/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index$repair_disjoined$fn__15707$fn__15708;

public final class index$repair_disjoined$fn__15707
extends AFunction {
    Object db;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"reduce");
    public static final AFn const__6 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"eavt"), (Object)RT.keyword(null, (String)"aevt"), (Object)RT.keyword(null, (String)"avet"), (Object)RT.keyword(null, (String)"raet"));
    public static final Keyword const__7 = RT.keyword(null, (String)"threw");

    public index$repair_disjoined$fn__15707(Object object) {
        this.db = object;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            iPersistentMap = RT.mapUniqueKeys((Object[])new Object[]{const__0, ((IFn)const__1.getRawRoot()).invoke((Object)new index$repair_disjoined$fn__15707$fn__15708(), this.db, (Object)const__6)});
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__7;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

