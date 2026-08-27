/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;

public final class index$drop_avet_indexes$fn__15621$fn__15628
extends AFunction {
    Object olookup;
    Object attrids;
    Object root_id;
    Object store;
    Object garbage;
    Object root_ids;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"drop-avets");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");
    public static final Keyword const__6 = RT.keyword(null, (String)"threw");

    public index$drop_avet_indexes$fn__15621$fn__15628(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this.olookup = object;
        this.attrids = object2;
        this.root_id = object3;
        this.store = object4;
        this.garbage = object5;
        this.root_ids = object6;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object vec__15629 = ((IFn)const__1.getRawRoot()).invoke(this.store, this.olookup, this.root_id, this.attrids, this.garbage);
            Object new_id = RT.nth((Object)vec__15629, (int)RT.uncheckedIntCast((long)0L), null);
            Object object = vec__15629;
            vec__15629 = null;
            Object garbage2 = RT.nth((Object)object, (int)RT.uncheckedIntCast((long)1L), null);
            Object object2 = new_id;
            new_id = null;
            Object object3 = garbage2;
            garbage2 = null;
            objectArray[1] = Tuple.create((Object)((IFn)const__5.getRawRoot()).invoke(this.root_ids, object2), (Object)object3);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__6;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

