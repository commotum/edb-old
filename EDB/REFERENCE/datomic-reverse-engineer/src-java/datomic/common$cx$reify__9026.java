/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Comparator;
import java.util.Map;

public final class common$cx$reify__9026
implements Comparator,
IObj {
    final IPersistentMap __meta;
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"key");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"val");

    public common$cx$reify__9026(IPersistentMap iPersistentMap) {
        this.__meta = iPersistentMap;
    }

    public common$cx$reify__9026() {
        this(null);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new common$cx$reify__9026(iPersistentMap);
    }

    /*
     * WARNING - void declaration
     */
    public int compare(Object a, Object b) {
        long l;
        void var3_3;
        boolean and__5236__auto__9028 = a instanceof Map.Entry;
        if (and__5236__auto__9028 ? b instanceof Map.Entry : var3_3) {
            long kc = ((IFn.OOL)const__2.getRawRoot()).invokePrim(((IFn)const__3.getRawRoot()).invoke(a), ((IFn)const__3.getRawRoot()).invoke(b));
            if (kc == 0L) {
                Object object = a;
                a = null;
                Object object2 = b;
                b = null;
                l = ((IFn.OOL)const__2.getRawRoot()).invokePrim(((IFn)const__5.getRawRoot()).invoke(object), ((IFn)const__5.getRawRoot()).invoke(object2));
            } else {
                l = kc;
            }
        } else {
            Object object = a;
            a = null;
            Object object3 = b;
            b = null;
            l = ((IFn.OOL)const__2.getRawRoot()).invokePrim(object, object3);
        }
        return RT.intCast((long)l);
    }
}

