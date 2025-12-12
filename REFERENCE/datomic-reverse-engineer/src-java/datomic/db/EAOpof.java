/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
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

public final class EAOpof
implements IType {
    public final Object d;
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"hash");

    public EAOpof(Object object) {
        this.d = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"d")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"Datom")})));
    }

    public int hashCode() {
        return (int)Numbers.xor((long)Numbers.xor((Object)((IFn)const__3.getRawRoot()).invoke(((Datom)this.d).e()), (Object)((IFn)const__3.getRawRoot()).invoke(((Datom)this.d).a())), (Object)((IFn)const__3.getRawRoot()).invoke((Object)(((Datom)this.d).added() ? Boolean.TRUE : Boolean.FALSE)));
    }

    /*
     * WARNING - void declaration
     */
    public boolean equals(Object other) {
        boolean bl;
        Object object = other;
        other = null;
        Object o = ((EAOpof)object).d;
        boolean and__5236__auto__13297 = Util.equiv((Object)((Datom)this.d).e(), (Object)((Datom)o).e());
        if (and__5236__auto__13297) {
            boolean and__5236__auto__13296 = Util.equiv((Object)((Datom)this.d).a(), (Object)((Datom)o).a());
            if (and__5236__auto__13296) {
                Object object2 = o;
                o = null;
                bl = Util.equiv((boolean)((Datom)this.d).added(), (boolean)((Datom)object2).added());
            } else {
                bl = and__5236__auto__13296;
            }
        } else {
            void var3_3;
            bl = var3_3;
        }
        return bl;
    }
}

