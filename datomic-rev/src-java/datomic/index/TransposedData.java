/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IFn$LLOLO
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic.index;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.index.ITransposeData;
import datomic.index.TransposedData$reify__15000;
import datomic.index.TransposedData$reify__15002;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

public final class TransposedData
implements ITransposeData,
RandomAccess,
List,
IType {
    public final int cnt;
    public final Object eas;
    public final Object vs;
    public final Object ts;
    public final Object ops;
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__21 = -1L;
    public static final AFn const__26 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 105, RT.keyword(null, (String)"column"), 15});
    public static final Var const__27 = RT.var((String)"datomic.db", (String)"asserting-datum");
    public static final Var const__28 = RT.var((String)"datomic.db", (String)"retracting-datum");
    public static final AFn const__31 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 156, RT.keyword(null, (String)"column"), 12});

    public TransposedData(int n, Object object, Object object2, Object object3, Object object4) {
        this.cnt = n;
        this.eas = object;
        this.vs = object2;
        this.ts = object3;
        this.ops = object4;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"cnt")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"int")})), (Object)((IObj)Symbol.intern(null, (String)"eas")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"longs")})), (Object)Symbol.intern(null, (String)"vs"), (Object)((IObj)Symbol.intern(null, (String)"ts")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"longs")})), (Object)((IObj)Symbol.intern(null, (String)"ops")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"booleans")})));
    }

    public List subList(int from, int to) {
        return (List)((IObj)new TransposedData$reify__15002(null, this, from, to)).withMeta((IPersistentMap)const__31);
    }

    public Object get(int idx) {
        return ((boolean[])this.ops)[idx] ? ((IFn.LLOLO)const__27.getRawRoot()).invokePrim(((long[])this.eas)[(int)((long)idx << (int)1L)], ((long[])this.eas)[(int)(((long)idx << (int)1L) + 1L)], RT.aget((Object[])((Object[])this.vs), (int)idx), ((long[])this.ts)[idx]) : ((IFn.LLOLO)const__28.getRawRoot()).invokePrim(((long[])this.eas)[(int)((long)idx << (int)1L)], ((long[])this.eas)[(int)(((long)idx << (int)1L) + 1L)], RT.aget((Object[])((Object[])this.vs), (int)idx), ((long[])this.ts)[idx]);
    }

    public Iterator iterator() {
        Object i;
        Object object = i = ((IFn)const__20.getRawRoot()).invoke(const__21);
        i = null;
        return (Iterator)((IObj)new TransposedData$reify__15000(null, object, this.cnt, this)).withMeta((IPersistentMap)const__26);
    }

    public int size() {
        return this.cnt;
    }

    public Object getAs() {
        int[] as = Numbers.int_array((Object)Numbers.num((long)Numbers.quotient((long)((long[])this.eas).length, (long)2L)));
        long n__5742__auto__15005 = as.length;
        for (long i = 0L; i < n__5742__auto__15005; ++i) {
            RT.aset((int[])as, (int)((int)i), (int)RT.uncheckedIntCast((long)((long[])this.eas)[(int)((i << (int)1L) + 1L)]));
        }
        Object var1_1 = null;
        return as;
    }

    public Object getEs() {
        long[] es = Numbers.long_array((Object)Numbers.num((long)Numbers.quotient((long)((long[])this.eas).length, (long)2L)));
        long n__5742__auto__15006 = es.length;
        for (long i = 0L; i < n__5742__auto__15006; ++i) {
            RT.aset((long[])es, (int)((int)i), (long)((long[])this.eas)[(int)(i << (int)1L)]);
        }
        Object var1_1 = null;
        return es;
    }

    public boolean getBooleanV(int idx) {
        return Numbers.booleans((Object)this.vs)[idx];
    }

    public float getFloatV(int idx) {
        return Numbers.floats((Object)this.vs)[idx];
    }

    public int getIntV(int idx) {
        return Numbers.ints((Object)this.vs)[idx];
    }

    public double getDoubleV(int idx) {
        return Numbers.doubles((Object)this.vs)[idx];
    }

    public long getLongV(int idx) {
        return Numbers.longs((Object)this.vs)[idx];
    }

    public long getT(int idx) {
        return ((long[])this.ts)[idx];
    }

    public Object getV(int idx) {
        TransposedData this_ = null;
        return RT.aget((Object[])((Object[])this_.vs), (int)idx);
    }

    public int getA(int idx) {
        return RT.intCast((long)((long[])this.eas)[(int)(((long)idx << (int)1L) + 1L)]);
    }

    public long getE(int idx) {
        return ((long[])this.eas)[(int)((long)idx << (int)1L)];
    }

    public boolean isAssertion(int idx) {
        return ((boolean[])this.ops)[idx];
    }
}

