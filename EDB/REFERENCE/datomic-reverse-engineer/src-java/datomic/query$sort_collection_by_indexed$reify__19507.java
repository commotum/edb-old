/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Indexed
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Indexed;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Comparator;

public final class query$sort_collection_by_indexed$reify__19507
implements Comparator,
IObj {
    final IPersistentMap __meta;
    long idx;
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"compare");

    public query$sort_collection_by_indexed$reify__19507(IPersistentMap iPersistentMap, long l) {
        this.__meta = iPersistentMap;
        this.idx = l;
    }

    public query$sort_collection_by_indexed$reify__19507(long l) {
        this(null, l);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new query$sort_collection_by_indexed$reify__19507(iPersistentMap, this.idx);
    }

    public int compare(Object a, Object b) {
        Number number;
        Object object = a;
        a = null;
        Object aval = ((Indexed)object).nth(RT.intCast((long)this.idx));
        Object object2 = b;
        b = null;
        Object bval = ((Indexed)object2).nth(RT.intCast((long)this.idx));
        boolean and__5236__auto__19509 = aval instanceof Long;
        if (and__5236__auto__19509 ? bval instanceof Long : and__5236__auto__19509) {
            Object object3 = aval;
            aval = null;
            Object object4 = bval;
            bval = null;
            number = ((Long)object3).compareTo((Long)object4);
        } else {
            Object object5 = aval;
            aval = null;
            Object object6 = bval;
            bval = null;
            number = Numbers.num((long)((IFn.OOL)const__2.getRawRoot()).invokePrim(object5, object6));
        }
        return ((Number)number).intValue();
    }
}

