/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Comparator;

public final class query$group_rel$reify__19452
implements Comparator,
IObj {
    final IPersistentMap __meta;
    Object grp_idxs;
    public static final Var const__3 = RT.var((String)"datomic.common", (String)"compare");

    public query$group_rel$reify__19452(IPersistentMap iPersistentMap, Object object) {
        this.__meta = iPersistentMap;
        this.grp_idxs = object;
    }

    public query$group_rel$reify__19452(Object object) {
        this(null, object);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new query$group_rel$reify__19452(iPersistentMap, this.grp_idxs);
    }

    public int compare(Object r1, Object r2) {
        long l;
        block2: {
            long c;
            long i = 0L;
            while (true) {
                if (i == (long)RT.count((Object)this.grp_idxs)) {
                    l = 0L;
                    break block2;
                }
                Object j = ((IFn)this.grp_idxs).invoke((Object)Numbers.num((long)i));
                Object object = RT.nth((Object)r1, (int)RT.intCast((Object)((Number)j)));
                Object object2 = j;
                j = null;
                c = ((IFn.OOL)const__3.getRawRoot()).invokePrim(object, RT.nth((Object)r2, (int)RT.intCast((Object)((Number)object2))));
                if (c != 0L) break;
                i = Numbers.inc((long)i);
            }
            l = c;
        }
        return RT.intCast((long)l);
    }
}

