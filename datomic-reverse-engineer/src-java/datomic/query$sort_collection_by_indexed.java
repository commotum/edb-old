/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OLO
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import datomic.query$sort_collection_by_indexed$reify__19507;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public final class query$sort_collection_by_indexed
extends AFunction
implements IFn.OLO {
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 723, RT.keyword(null, (String)"column"), 13});

    public static Object invokeStatic(Object coll, long idx) {
        IObj cmp = ((IObj)new query$sort_collection_by_indexed$reify__19507(null, idx)).withMeta((IPersistentMap)const__4);
        Object object = coll;
        coll = null;
        ArrayList G__19510 = new ArrayList((Collection)object);
        IObj iObj = cmp;
        cmp = null;
        G__19510.sort((Comparator)iObj);
        ArrayList arrayList = G__19510;
        G__19510 = null;
        return arrayList;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        return query$sort_collection_by_indexed.invokeStatic(object3, RT.longCast((Object)((Number)object2)));
    }

    public final Object invokePrim(Object object, long l) {
        Object object2 = object;
        object = null;
        return query$sort_collection_by_indexed.invokeStatic(object2, l);
    }
}

