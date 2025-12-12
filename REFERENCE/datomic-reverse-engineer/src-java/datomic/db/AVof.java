/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
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
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;

public final class AVof
implements IType {
    public final Object d;
    public static final Var const__2 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"hash");

    public AVof(Object object) {
        this.d = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"d")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Datom")})));
    }

    public int hashCode() {
        return (int)Numbers.xor((Object)((IFn)const__5.getRawRoot()).invoke(((Datom)this.d).a()), (Object)((IFn)const__5.getRawRoot()).invoke(((Datom)this.d).v()));
    }

    /*
     * WARNING - void declaration
     */
    public boolean equals(Object other) {
        boolean bl;
        Object object = other;
        other = null;
        Object o = ((AVof)object).d;
        boolean and__5236__auto__13283 = Util.equiv((Object)((Datom)this.d).a(), (Object)((Datom)o).a());
        if (and__5236__auto__13283) {
            Object object2 = o;
            o = null;
            bl = Numbers.isZero((long)((IFn.OOL)const__2.getRawRoot()).invokePrim(((Datom)this.d).v(), ((Datom)object2).v()));
        } else {
            void var3_3;
            bl = var3_3;
        }
        return bl;
    }
}

