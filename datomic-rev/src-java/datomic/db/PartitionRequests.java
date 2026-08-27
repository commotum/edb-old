/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$LO
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.db.AssignPartitions;
import datomic.db.GetPartition;
import datomic.db.PartitionRequests$fn__13705;
import java.util.HashMap;

public final class PartitionRequests
implements GetPartition,
AssignPartitions,
IType {
    public final Object id__GT_part;
    public final Object id__GT_match;
    public static final Var const__0 = RT.var((String)"datomic.db", (String)"reserved-partition?");
    public static final Var const__1 = RT.var((String)"datomic.db", (String)"eid->part");

    public PartitionRequests(Object object, Object object2) {
        this.id__GT_part = object;
        this.id__GT_match = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"id->part")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"HashMap")})), (Object)((IObj)Symbol.intern(null, (String)"id->match")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"HashMap")})));
    }

    public Object getPart(Object id) {
        Object object;
        Object or__5238__auto__13712;
        Object object2 = or__5238__auto__13712 = RT.get((Object)this.id__GT_part, (Object)id);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = or__5238__auto__13712;
            or__5238__auto__13712 = null;
        } else {
            Object or__5238__auto__13711;
            Object object3;
            Object temp__5457__auto__13710;
            Object object4 = temp__5457__auto__13710 = RT.get((Object)this.id__GT_match, (Object)id);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object nxt;
                Object object5 = temp__5457__auto__13710;
                temp__5457__auto__13710 = null;
                Object object6 = nxt = object5;
                nxt = null;
                object3 = ((IFn)new PartitionRequests$fn__13705(this.id__GT_match, this.id__GT_part, id, object6)).invoke();
            } else {
                object3 = null;
            }
            Object object7 = or__5238__auto__13711 = object3;
            if (object7 != null && object7 != Boolean.FALSE) {
                object = or__5238__auto__13711;
                or__5238__auto__13711 = null;
            } else {
                Object object8 = id;
                id = null;
                object = Numbers.num((long)((IFn.LL)const__1.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)object8))));
            }
        }
        return object;
    }

    public Object matchPart(Object id, Object id_to_match) {
        Object object;
        Object object2;
        Object or__5238__auto__13713;
        Object object3 = or__5238__auto__13713 = ((IFn.LO)const__0.getRawRoot()).invokePrim(((IFn.LL)const__1.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)id))));
        if (object3 != null && object3 != Boolean.FALSE) {
            object2 = or__5238__auto__13713;
            or__5238__auto__13713 = null;
        } else {
            object2 = ((IFn.LO)const__0.getRawRoot()).invokePrim(((IFn.LL)const__1.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)id_to_match))));
        }
        if (object2 != null && object2 != Boolean.FALSE) {
            object = null;
        } else {
            Object object4 = id;
            id = null;
            Object object5 = id_to_match;
            id_to_match = null;
            object = ((HashMap)this.id__GT_match).put(object4, object5);
        }
        return object;
    }

    public Object forcePart(Object id, Object partbits2) {
        Object object;
        Object object2 = ((IFn.LO)const__0.getRawRoot()).invokePrim(((IFn.LL)const__1.getRawRoot()).invokePrim(RT.uncheckedLongCast((Object)((Number)id))));
        if (object2 != null && object2 != Boolean.FALSE) {
            object = null;
        } else {
            Object temp__5455__auto__13714;
            Object object3 = temp__5455__auto__13714 = RT.get((Object)this.id__GT_part, (Object)id);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object existing;
                Object object4 = temp__5455__auto__13714;
                temp__5455__auto__13714 = null;
                Object object5 = existing = object4;
                existing = null;
                if (Numbers.isZero((Object)object5)) {
                    object = null;
                } else {
                    Object object6 = id;
                    id = null;
                    Object object7 = partbits2;
                    partbits2 = null;
                    object = ((HashMap)this.id__GT_part).put(object6, object7);
                }
            } else {
                Object object8 = id;
                id = null;
                Object object9 = partbits2;
                partbits2 = null;
                object = ((HashMap)this.id__GT_part).put(object8, object9);
            }
        }
        return object;
    }
}

