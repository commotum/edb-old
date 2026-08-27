/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.Counted
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LL
 *  clojure.lang.IFn$LLL
 *  clojure.lang.IFn$OOL
 *  clojure.lang.ILookup
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

import clojure.lang.Counted;
import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.Datom;
import datomic.db.IDatumImpl;
import datomic.impl.db.IDatum;

public final class Datum
implements IDatumImpl,
Datom,
ILookup,
IDatum,
Counted,
Indexed,
IType {
    public final long e;
    public final int a;
    public final Object v;
    public final long tOp;
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"hash");
    public static final Var const__12 = RT.var((String)"datomic.db", (String)"eid->part");
    public static final Var const__14 = RT.var((String)"datomic.db", (String)"make-eid");
    public static final Var const__16 = RT.var((String)"datomic.db", (String)"eid->eidx");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__25 = RT.keyword(null, (String)"a");
    public static final Keyword const__26 = RT.keyword(null, (String)"e");
    public static final Keyword const__27 = RT.keyword(null, (String)"added");
    public static final Keyword const__28 = RT.keyword(null, (String)"tx");
    public static final Keyword const__29 = RT.keyword(null, (String)"v");

    public Datum(long l, int n, Object object, long l2) {
        this.e = l;
        this.a = n;
        this.v = object;
        this.tOp = l2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"e")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})), (Object)((IObj)Symbol.intern(null, (String)"a")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"int")})), (Object)Symbol.intern(null, (String)"v"), (Object)((IObj)Symbol.intern(null, (String)"tOp")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long")})));
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object valAt(Object k, Object not_found) {
        Object object;
        Object object2 = k;
        k = null;
        Object G__12520 = object2;
        switch (Util.hash((Object)G__12520) >> 1 & 0xF) {
            case 4: {
                if (G__12520 != const__25) break;
                object = this.a;
                return object;
            }
            case 8: {
                if (G__12520 != const__26) break;
                object = Numbers.num((long)this.e);
                return object;
            }
            case 9: {
                if (G__12520 != const__27) break;
                if (((Datom)this).added()) {
                    object = Boolean.TRUE;
                    return object;
                }
                object = Boolean.FALSE;
                return object;
            }
            case 11: {
                if (G__12520 != const__28) break;
                object = Numbers.num((long)((IDatum)this).getTx());
                return object;
            }
            case 12: {
                if (G__12520 != const__29) break;
                object = this.v;
                return object;
            }
        }
        object = not_found;
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object valAt(Object k) {
        Object object = k;
        k = null;
        Object G__12519 = object;
        switch (Util.hash((Object)G__12519) >> 1 & 0xF) {
            case 4: {
                if (G__12519 != const__25) break;
                Object object2 = this.a;
                return object2;
            }
            case 8: {
                if (G__12519 != const__26) break;
                Object object2 = Numbers.num((long)this.e);
                return object2;
            }
            case 9: {
                Object object2;
                if (G__12519 != const__27) break;
                if (((Datom)this).added()) {
                    object2 = Boolean.TRUE;
                    return object2;
                }
                object2 = Boolean.FALSE;
                return object2;
            }
            case 11: {
                if (G__12519 != const__28) break;
                Object object2 = Numbers.num((long)((IDatum)this).getTx());
                return object2;
            }
            case 12: {
                if (G__12519 != const__29) break;
                Object object2 = this.v;
                return object2;
            }
        }
        Object object3 = G__12519;
        G__12519 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__23.getRawRoot()).invoke((Object)"No matching clause: ", object3));
    }

    public Object get(int i) {
        return ((Indexed)this).nth(i);
    }

    public boolean added() {
        return RT.booleanCast((boolean)((IDatum)this).isAssertion());
    }

    public Object tx() {
        return Numbers.num((long)((IDatum)this).getTx());
    }

    public Object v() {
        return this.v;
    }

    public Object a() {
        return this.a;
    }

    public Object e() {
        return Numbers.num((long)this.e);
    }

    public Object nth(int i, Object _) {
        Object object;
        int G__12518 = i;
        switch (G__12518) {
            case 0: {
                object = Numbers.num((long)((IDatum)this).getE());
                break;
            }
            case 1: {
                object = ((IDatum)this).getA();
                break;
            }
            case 2: {
                object = ((IDatum)this).getV();
                break;
            }
            case 3: {
                object = Numbers.num((long)((IDatum)this).getTx());
                break;
            }
            case 4: {
                if (((Datom)this).added()) {
                    object = Boolean.TRUE;
                    break;
                }
                object = Boolean.FALSE;
                break;
            }
            default: {
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__23.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__12518));
            }
        }
        return object;
    }

    public Object nth(int i) {
        Object object;
        int G__12517 = i;
        switch (G__12517) {
            case 0: {
                object = Numbers.num((long)((IDatum)this).getE());
                break;
            }
            case 1: {
                object = ((IDatum)this).getA();
                break;
            }
            case 2: {
                object = ((IDatum)this).getV();
                break;
            }
            case 3: {
                object = Numbers.num((long)((IDatum)this).getTx());
                break;
            }
            case 4: {
                if (((Datom)this).added()) {
                    object = Boolean.TRUE;
                    break;
                }
                object = Boolean.FALSE;
                break;
            }
            default: {
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__23.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__12517));
            }
        }
        return object;
    }

    public int count() {
        return RT.intCast((long)5L);
    }

    public long eidx() {
        return ((IFn.LL)const__16.getRawRoot()).invokePrim(this.e);
    }

    public boolean getBooleanV() {
        return (Boolean)this.v;
    }

    public float getFloatV() {
        return ((Number)this.v).floatValue();
    }

    public int getIntV() {
        return ((Number)this.v).intValue();
    }

    public double getDoubleV() {
        return ((Number)this.v).doubleValue();
    }

    public long getLongV() {
        return ((Number)this.v).longValue();
    }

    public long getTx() {
        return ((IFn.LLL)const__14.getRawRoot()).invokePrim(3L, this.tOp >> (int)1L);
    }

    public long getT() {
        return this.tOp >> (int)1L;
    }

    public Object getV() {
        return this.v;
    }

    public int getA() {
        return this.a;
    }

    public long getE() {
        return this.e;
    }

    public int getP() {
        return (int)((IFn.LL)const__12.getRawRoot()).invokePrim(this.e);
    }

    public boolean isAssertion() {
        return Numbers.isPos((long)(this.tOp & 1L));
    }

    public int hashCode() {
        return (int)Numbers.xor((long)Numbers.xor((long)Numbers.xor((Object)((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)this.e)), (Object)((IFn)const__8.getRawRoot()).invoke((Object)this.a)), (Object)((IFn)const__8.getRawRoot()).invoke(this.v)), (Object)((IFn)const__8.getRawRoot()).invoke((Object)Numbers.num((long)this.tOp)));
    }

    public boolean equals(Object o) {
        boolean bl;
        boolean or__5238__auto__12527 = Util.identical((Object)this, (Object)o);
        if (or__5238__auto__12527) {
            bl = or__5238__auto__12527;
        } else {
            Object object = o;
            o = null;
            Object o2 = object;
            boolean and__5236__auto__12526 = o2 instanceof IDatum;
            if (and__5236__auto__12526) {
                boolean and__5236__auto__12525 = Util.equiv((long)((IDatum)this).getT(), (long)((IDatum)o2).getT());
                if (and__5236__auto__12525) {
                    boolean and__5236__auto__12524 = Util.equiv((long)this.e, (long)((IDatum)o2).getE());
                    if (and__5236__auto__12524) {
                        boolean and__5236__auto__12523 = Util.equiv((long)this.a, (long)((IDatum)o2).getA());
                        if (and__5236__auto__12523) {
                            boolean and__5236__auto__12522 = Numbers.isZero((long)((IFn.OOL)const__5.getRawRoot()).invokePrim(this.v, ((IDatum)o2).getV()));
                            if (and__5236__auto__12522) {
                                Object object2 = o2;
                                o2 = null;
                                bl = Util.equiv((boolean)((IDatum)this).isAssertion(), (boolean)((IDatum)object2).isAssertion());
                            } else {
                                bl = and__5236__auto__12522;
                            }
                        } else {
                            bl = and__5236__auto__12523;
                        }
                    } else {
                        bl = and__5236__auto__12524;
                    }
                } else {
                    bl = and__5236__auto__12525;
                }
            } else {
                bl = and__5236__auto__12526;
            }
        }
        return bl;
    }
}

