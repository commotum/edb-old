/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IPersistentSet
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentSet;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class PartitionRequests$fn__13705
extends AFunction {
    Object id__GT_match;
    Object id__GT_part;
    Object id;
    Object nxt;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"cycle-in-affinity");
    public static final Keyword const__3 = RT.keyword(null, (String)"e");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__6 = RT.var((String)"datomic.db", (String)"eid->part");

    public PartitionRequests$fn__13705(Object object, Object object2, Object object3, Object object4) {
        this.id__GT_match = object;
        this.id__GT_part = object2;
        this.id = object3;
        this.nxt = object4;
    }

    public Object invoke() {
        Object object;
        block3: {
            Object visited = RT.set((Object[])new Object[]{this_.id});
            Object nxt = this_.nxt;
            while (true) {
                Object temp__5455__auto__13707;
                Object or__5238__auto__13708;
                Object object2 = ((IFn)const__0.getRawRoot()).invoke(visited, nxt);
                if (object2 != null && object2 != Boolean.FALSE) {
                    PartitionRequests$fn__13705 this_ = null;
                    object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, (Object)RT.mapUniqueKeys((Object[])new Object[]{const__3, this_.id}));
                    break block3;
                }
                Object object3 = or__5238__auto__13708 = RT.get((Object)this_.id__GT_part, (Object)nxt);
                if (object3 != null && object3 != Boolean.FALSE) {
                    object = or__5238__auto__13708;
                    or__5238__auto__13708 = null;
                    break block3;
                }
                Object object4 = temp__5455__auto__13707 = RT.get((Object)this_.id__GT_match, (Object)nxt);
                if (object4 == null || object4 == Boolean.FALSE) break;
                Object object5 = temp__5455__auto__13707;
                temp__5455__auto__13707 = null;
                Object nnxt = object5;
                IPersistentSet iPersistentSet = visited;
                visited = null;
                Object object6 = nxt;
                nxt = null;
                Object object7 = nnxt;
                nnxt = null;
                nxt = object7;
                visited = ((IFn)const__5.getRawRoot()).invoke((Object)iPersistentSet, object6);
            }
            Object object8 = nxt;
            nxt = null;
            object = Numbers.num((long)((IFn.LL)const__6.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object8))));
        }
        return object;
    }
}

