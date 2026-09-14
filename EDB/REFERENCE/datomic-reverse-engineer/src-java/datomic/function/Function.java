/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.APersistentMap
 *  clojure.lang.Counted
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.IHashEq
 *  clojure.lang.IKeywordLookup
 *  clojure.lang.ILookup
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IMapEntry
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentCollection
 *  clojure.lang.IPersistentMap
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IRecord
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.MapEntry
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.RecordIterator
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.function;

import clojure.lang.AFn;
import clojure.lang.APersistentMap;
import clojure.lang.Counted;
import clojure.lang.IFn;
import clojure.lang.IHashEq;
import clojure.lang.IKeywordLookup;
import clojure.lang.ILookup;
import clojure.lang.ILookupThunk;
import clojure.lang.IMapEntry;
import clojure.lang.IObj;
import clojure.lang.IPersistentCollection;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IRecord;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.MapEntry;
import clojure.lang.Numbers;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.RecordIterator;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.function.Function$reify__11939;
import datomic.function.Function$reify__11941;
import datomic.function.Function$reify__11943;
import datomic.function.Function$reify__11945;
import datomic.function.Function$reify__11947;
import datomic.function.Function$reify__11949;
import datomic.functions.Fn;
import datomic.functions.Fn0;
import datomic.functions.Fn1;
import datomic.functions.Fn10;
import datomic.functions.Fn2;
import datomic.functions.Fn3;
import datomic.functions.Fn4;
import datomic.functions.Fn5;
import datomic.functions.Fn6;
import datomic.functions.Fn7;
import datomic.functions.Fn8;
import datomic.functions.Fn9;
import datomic.memory_size.MemorySize;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Function
implements Fn10,
Fn4,
Fn2,
Fn6,
Fn7,
MemorySize,
Fn,
Fn1,
Comparable,
IFn,
Fn9,
Fn8,
Fn0,
Fn5,
Fn3,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object lang;
    public final Object imports;
    public final Object requires;
    public final Object params;
    public final Object code;
    public final Object fnref;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__7;
    public static final Keyword const__8;
    public static final Keyword const__9;
    public static final Keyword const__10;
    public static final Keyword const__11;
    public static final Keyword const__12;
    public static final Keyword const__13;
    public static final AFn const__14;
    public static final Var const__15;
    public static final Var const__16;
    public static final Var const__17;
    public static final Var const__18;
    public static final Var const__19;
    public static final Var const__20;
    public static final AFn const__21;
    public static final Var const__22;
    public static final Var const__23;
    public static final Var const__24;
    public static final Var const__25;
    public static final Var const__26;
    public static final Var const__27;
    public static final Var const__37;
    public static final Var const__38;
    public static final Var const__39;
    public static final Var const__40;
    public static final Var const__41;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;
    static final KeywordLookupSite __site__2__;
    static ILookupThunk __thunk__2__;

    public Function(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, int n, int n2) {
        this.lang = object;
        this.imports = object2;
        this.requires = object3;
        this.params = object4;
        this.code = object5;
        this.fnref = object6;
        this.__meta = object7;
        this.__extmap = object8;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public Function(Object object, Object object2, Object object3, Object object4, Object object5, Object object6) {
        this(object, object2, object3, object4, object5, object6, null, null, 0, 0);
    }

    public Function(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this(object, object2, object3, object4, object5, object6, object7, object8, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"lang"), (Object)Symbol.intern(null, (String)"imports"), (Object)Symbol.intern(null, (String)"requires"), (Object)Symbol.intern(null, (String)"params"), (Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"fnref"));
    }

    public static Function create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"lang"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"lang"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"imports"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"imports"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"requires"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"requires"));
        Object object4 = iPersistentMap.valAt((Object)Keyword.intern((String)"params"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"params"));
        Object object5 = iPersistentMap.valAt((Object)Keyword.intern((String)"code"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"code"));
        Object object6 = iPersistentMap.valAt((Object)Keyword.intern((String)"fnref"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"fnref"));
        return new Function(object, object2, object3, object4, object5, object6, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object applyTo(ISeq args) {
        IFn iFn = this_;
        ISeq iSeq = args;
        args = null;
        Function this_ = null;
        return AFn.applyToHelper((IFn)iFn, (ISeq)iSeq);
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9, Object a10) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Object object4 = a4;
        a4 = null;
        Object object5 = a5;
        a5 = null;
        Object object6 = a6;
        a6 = null;
        Object object7 = a7;
        a7 = null;
        Object object8 = a8;
        a8 = null;
        Object object9 = a9;
        a9 = null;
        Object object10 = a10;
        a10 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3, object4, object5, object6, object7, object8, object9, object10);
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8, Object a9) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Object object4 = a4;
        a4 = null;
        Object object5 = a5;
        a5 = null;
        Object object6 = a6;
        a6 = null;
        Object object7 = a7;
        a7 = null;
        Object object8 = a8;
        a8 = null;
        Object object9 = a9;
        a9 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3, object4, object5, object6, object7, object8, object9);
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7, Object a8) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Object object4 = a4;
        a4 = null;
        Object object5 = a5;
        a5 = null;
        Object object6 = a6;
        a6 = null;
        Object object7 = a7;
        a7 = null;
        Object object8 = a8;
        a8 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3, object4, object5, object6, object7, object8);
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6, Object a7) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Object object4 = a4;
        a4 = null;
        Object object5 = a5;
        a5 = null;
        Object object6 = a6;
        a6 = null;
        Object object7 = a7;
        a7 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3, object4, object5, object6, object7);
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5, Object a6) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Object object4 = a4;
        a4 = null;
        Object object5 = a5;
        a5 = null;
        Object object6 = a6;
        a6 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3, object4, object5, object6);
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4, Object a5) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Object object4 = a4;
        a4 = null;
        Object object5 = a5;
        a5 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3, object4, object5);
    }

    public Object invoke(Object a1, Object a2, Object a3, Object a4) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Object object4 = a4;
        a4 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3, object4);
    }

    public Object invoke(Object a1, Object a2, Object a3) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Object object3 = a3;
        a3 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2, object3);
    }

    public Object invoke(Object a1, Object a2) {
        Object object = a1;
        a1 = null;
        Object object2 = a2;
        a2 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object, object2);
    }

    public Object invoke(Object a1) {
        Object object = a1;
        a1 = null;
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke(object);
    }

    public Object invoke() {
        Function this_ = null;
        return ((IFn)((IFn)const__41.getRawRoot()).invoke(this_.fnref)).invoke();
    }

    public int compareTo(Object o) {
        long l;
        IFn.OOL oOL = (IFn.OOL)const__40.getRawRoot();
        ILookupThunk iLookupThunk = __thunk__0__;
        Object object = o;
        Object object2 = iLookupThunk.get(object);
        if (iLookupThunk == object2) {
            __thunk__0__ = __site__0__.fault(object);
            object2 = __thunk__0__.get(object);
        }
        long c = oOL.invokePrim(this.lang, object2);
        Object object3 = ((IFn)const__24.getRawRoot()).invoke((Object)(Numbers.isZero((long)c) ? Boolean.TRUE : Boolean.FALSE));
        if (object3 != null && object3 != Boolean.FALSE) {
            l = c;
        } else {
            IFn.OOL oOL2 = (IFn.OOL)const__40.getRawRoot();
            ILookupThunk iLookupThunk2 = __thunk__1__;
            Object object4 = o;
            Object object5 = iLookupThunk2.get(object4);
            if (iLookupThunk2 == object5) {
                __thunk__1__ = __site__1__.fault(object4);
                object5 = __thunk__1__.get(object4);
            }
            long c2 = oOL2.invokePrim(this.params, object5);
            Object object6 = ((IFn)const__24.getRawRoot()).invoke((Object)(Numbers.isZero((long)c2) ? Boolean.TRUE : Boolean.FALSE));
            if (object6 != null && object6 != Boolean.FALSE) {
                l = c2;
            } else {
                IFn.OOL oOL3 = (IFn.OOL)const__40.getRawRoot();
                ILookupThunk iLookupThunk3 = __thunk__2__;
                Object object7 = o;
                o = null;
                Object object8 = iLookupThunk3.get(object7);
                if (iLookupThunk3 == object8) {
                    __thunk__2__ = __site__2__.fault(object7);
                    object8 = __thunk__2__.get(object7);
                }
                l = oOL3.invokePrim(this.code, object8);
            }
        }
        return RT.intCast((long)l);
    }

    public String code() {
        return (String)this.code;
    }

    public List params() {
        Function this_ = null;
        return (List)((IFn)const__39.getRawRoot()).invoke(const__27.getRawRoot(), this_.params);
    }

    public String lang() {
        Function this_ = null;
        return (String)((IFn)const__38.getRawRoot()).invoke(this_.lang);
    }

    /*
     * Unable to fully structure code
     */
    public Object memory_size() {
        v0 = this.code;
        if (Util.classOf((Object)v0) == Function.__cached_class__0) ** GOTO lbl6
        if (!(v0 instanceof MemorySize)) {
            v0 = v0;
            Function.__cached_class__0 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = Function.const__37.getRawRoot().invoke(v0);
        } else {
            v1 = ((MemorySize)v0).memory_size();
        }
        this = null;
        return Numbers.add((long)1024L, (Object)v1);
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__11954 = this.__hasheq;
        if ((long)hq__7465__auto__11954 == 0L) {
            void var2_2;
            int h__7466__auto__11953;
            this.__hasheq = h__7466__auto__11953 = RT.intCast((long)(0xFFFFFFFFFB771718L ^ (long)APersistentMap.mapHasheq((IPersistentMap)this)));
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    /*
     * WARNING - void declaration
     */
    public int hashCode() {
        void v0;
        int hash__7468__auto__11956 = this.__hash;
        if ((long)hash__7468__auto__11956 == 0L) {
            void var2_2;
            int h__7469__auto__11955;
            this.__hash = h__7469__auto__11955 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__11934) {
        Object object = G__11934;
        G__11934 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__11934) {
        IPersistentMap iPersistentMap = G__11934;
        G__11934 = null;
        return new Function(this.lang, this.imports, this.requires, this.params, this.code, this.fnref, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
    }

    public Object valAt(Object k__7474__auto__) {
        Object object = k__7474__auto__;
        k__7474__auto__ = null;
        return ((ILookup)this).valAt(object, null);
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object valAt(Object k__7476__auto__, Object else__7477__auto__) {
        Object object;
        Object G__11951 = k__7476__auto__;
        switch (Util.hash((Object)G__11951) >> 10 & 7) {
            case 0: {
                if (G__11951 != const__11) break;
                object = this_.lang;
                return object;
            }
            case 1: {
                if (G__11951 != const__13) break;
                object = this_.code;
                return object;
            }
            case 3: {
                if (G__11951 != const__9) break;
                object = this_.imports;
                return object;
            }
            case 5: {
                if (G__11951 != const__12) break;
                object = this_.requires;
                return object;
            }
            case 6: {
                if (G__11951 != const__8) break;
                object = this_.params;
                return object;
            }
            case 7: {
                if (G__11951 != const__10) break;
                object = this_.fnref;
                return object;
            }
        }
        Object object2 = k__7476__auto__;
        k__7476__auto__ = null;
        Object object3 = else__7477__auto__;
        else__7477__auto__ = null;
        Function this_ = null;
        object = RT.get((Object)this_.__extmap, (Object)object2, (Object)object3);
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public ILookupThunk getLookupThunk(Keyword k__7479__auto__) {
        Object object;
        Object gclass = ((IFn)const__25.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__11938 = keyword;
        switch (Util.hash((Object)G__11938) >> 10 & 7) {
            case 0: {
                if (G__11938 != const__11) break;
                gclass = null;
                object = new Function$reify__11939(null, gclass);
                return object;
            }
            case 1: {
                if (G__11938 != const__13) break;
                gclass = null;
                object = new Function$reify__11941(null, gclass);
                return object;
            }
            case 3: {
                if (G__11938 != const__9) break;
                gclass = null;
                object = new Function$reify__11943(null, gclass);
                return object;
            }
            case 5: {
                if (G__11938 != const__12) break;
                gclass = null;
                object = new Function$reify__11945(null, gclass);
                return object;
            }
            case 6: {
                if (G__11938 != const__8) break;
                gclass = null;
                object = new Function$reify__11947(null, gclass);
                return object;
            }
            case 7: {
                if (G__11938 != const__10) break;
                gclass = null;
                object = new Function$reify__11949(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)Numbers.add((long)6L, (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__27.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.function.Function"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        Function function2 = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        Function this_ = null;
        return (IPersistentCollection)((IFn)const__26).invoke((Object)function2, object);
    }

    public boolean equiv(Object G__11934) {
        Boolean bl;
        boolean or__5238__auto__11963 = Util.identical((Object)this, (Object)G__11934);
        if (or__5238__auto__11963) {
            bl = or__5238__auto__11963 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__25.getRawRoot()).invoke((Object)this), (Object)((IFn)const__25.getRawRoot()).invoke(G__11934))) {
            Object object = G__11934;
            G__11934 = null;
            Object G__119342 = object;
            boolean and__5236__auto__11962 = Util.equiv((Object)this.lang, (Object)((Function)G__119342).lang);
            if (and__5236__auto__11962) {
                boolean and__5236__auto__11961 = Util.equiv((Object)this.imports, (Object)((Function)G__119342).imports);
                if (and__5236__auto__11961) {
                    boolean and__5236__auto__11960 = Util.equiv((Object)this.requires, (Object)((Function)G__119342).requires);
                    if (and__5236__auto__11960) {
                        boolean and__5236__auto__11959 = Util.equiv((Object)this.params, (Object)((Function)G__119342).params);
                        if (and__5236__auto__11959) {
                            boolean and__5236__auto__11958 = Util.equiv((Object)this.code, (Object)((Function)G__119342).code);
                            if (and__5236__auto__11958) {
                                boolean and__5236__auto__11957 = Util.equiv((Object)this.fnref, (Object)((Function)G__119342).fnref);
                                if (and__5236__auto__11957) {
                                    Object object2 = G__119342;
                                    G__119342 = null;
                                    bl = Util.equiv((Object)this.__extmap, (Object)((Function)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                                } else {
                                    bl = and__5236__auto__11957 ? Boolean.TRUE : Boolean.FALSE;
                                }
                            } else {
                                bl = and__5236__auto__11958 ? Boolean.TRUE : Boolean.FALSE;
                            }
                        } else {
                            bl = and__5236__auto__11959 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        bl = and__5236__auto__11960 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__11961 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__11962 ? Boolean.TRUE : Boolean.FALSE;
            }
        } else {
            bl = null;
        }
        return RT.booleanCast((Object)bl);
    }

    public boolean containsKey(Object k__7486__auto__) {
        Object object = k__7486__auto__;
        k__7486__auto__ = null;
        Boolean bl = Util.identical((Object)this_, (Object)((ILookup)this_).valAt(object, (Object)this_)) ? Boolean.TRUE : Boolean.FALSE;
        Function this_ = null;
        return (Boolean)((IFn)const__24.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__11964 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__11964)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__11964;
            v__7489__auto__11964 = null;
            Function this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        Function this_ = null;
        return (ISeq)((IFn)const__22.getRawRoot()).invoke(((IFn)const__23.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__11, (Object)this_.lang), (Object)MapEntry.create((Object)const__9, (Object)this_.imports), (Object)MapEntry.create((Object)const__12, (Object)this_.requires), (Object)MapEntry.create((Object)const__8, (Object)this_.params), (Object)MapEntry.create((Object)const__13, (Object)this_.code), (Object)MapEntry.create((Object)const__10, (Object)this_.fnref)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__21, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__11934) {
        Function function2;
        Object pred__11936 = const__19.getRawRoot();
        Object expr__11937 = k__7493__auto__;
        Object object = ((IFn)pred__11936).invoke((Object)const__11, expr__11937);
        if (object != null && object != Boolean.FALSE) {
            G__11934 = null;
            function2 = new Function(G__11934, this.imports, this.requires, this.params, this.code, this.fnref, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__11936).invoke((Object)const__9, expr__11937);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__11934 = null;
                function2 = new Function(this.lang, G__11934, this.requires, this.params, this.code, this.fnref, this.__meta, this.__extmap);
            } else {
                Object object3 = ((IFn)pred__11936).invoke((Object)const__12, expr__11937);
                if (object3 != null && object3 != Boolean.FALSE) {
                    G__11934 = null;
                    function2 = new Function(this.lang, this.imports, G__11934, this.params, this.code, this.fnref, this.__meta, this.__extmap);
                } else {
                    Object object4 = ((IFn)pred__11936).invoke((Object)const__8, expr__11937);
                    if (object4 != null && object4 != Boolean.FALSE) {
                        G__11934 = null;
                        function2 = new Function(this.lang, this.imports, this.requires, G__11934, this.code, this.fnref, this.__meta, this.__extmap);
                    } else {
                        Object object5 = ((IFn)pred__11936).invoke((Object)const__13, expr__11937);
                        if (object5 != null && object5 != Boolean.FALSE) {
                            G__11934 = null;
                            function2 = new Function(this.lang, this.imports, this.requires, this.params, G__11934, this.fnref, this.__meta, this.__extmap);
                        } else {
                            Object object6 = pred__11936;
                            pred__11936 = null;
                            Object object7 = expr__11937;
                            expr__11937 = null;
                            Object object8 = ((IFn)object6).invoke((Object)const__10, object7);
                            if (object8 != null && object8 != Boolean.FALSE) {
                                G__11934 = null;
                                function2 = new Function(this.lang, this.imports, this.requires, this.params, this.code, G__11934, this.__meta, this.__extmap);
                            } else {
                                k__7493__auto__ = null;
                                G__11934 = null;
                                function2 = new Function(this.lang, this.imports, this.requires, this.params, this.code, this.fnref, this.__meta, ((IFn)const__20.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__11934));
                            }
                        }
                    }
                }
            }
        }
        return function2;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__14, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__16.getRawRoot()).invoke(((IFn)const__17.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            Function this_ = null;
            object = ((IFn)const__15.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new Function(this_.lang, this_.imports, this_.requires, this_.params, this_.code, this_.fnref, this_.__meta, ((IFn)const__18.getRawRoot()).invoke(((IFn)const__15.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
        }
        return (IPersistentMap)object;
    }

    public int size() {
        Counted counted = (Counted)this_;
        Function this_ = null;
        return counted.count();
    }

    public boolean isEmpty() {
        return Util.equiv((long)0L, (long)((Counted)this).count());
    }

    public boolean containsValue(Object v__7499__auto__) {
        Object[] objectArray = new Object[1];
        Object object = v__7499__auto__;
        v__7499__auto__ = null;
        objectArray[0] = object;
        return RT.booleanCast((Object)((IFn)const__4.getRawRoot()).invoke((Object)RT.set((Object[])objectArray), ((IFn)const__1.getRawRoot()).invoke((Object)this)));
    }

    public Object get(Object k__7501__auto__) {
        Object object = k__7501__auto__;
        k__7501__auto__ = null;
        return ((ILookup)this).valAt(object);
    }

    public Object put(Object k__7503__auto__, Object v__7504__auto__) {
        throw (Throwable)new UnsupportedOperationException();
    }

    public Object remove(Object k__7506__auto__) {
        throw (Throwable)new UnsupportedOperationException();
    }

    public void putAll(Map m__7508__auto__) {
        throw (Throwable)new UnsupportedOperationException();
    }

    public void clear() {
        throw (Throwable)new UnsupportedOperationException();
    }

    public Set keySet() {
        Object object = ((IFn)const__2.getRawRoot()).invoke((Object)this_);
        Function this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke(object);
    }

    public Collection values() {
        Function function2 = this_;
        Function this_ = null;
        return (Collection)((IFn)const__1.getRawRoot()).invoke((Object)function2);
    }

    public Set entrySet() {
        Function function2 = this_;
        Function this_ = null;
        return (Set)((IFn)const__0.getRawRoot()).invoke((Object)function2);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"set");
        const__1 = RT.var((String)"clojure.core", (String)"vals");
        const__2 = RT.var((String)"clojure.core", (String)"keys");
        const__4 = RT.var((String)"clojure.core", (String)"some");
        const__7 = RT.var((String)"clojure.core", (String)"contains?");
        const__8 = RT.keyword(null, (String)"params");
        const__9 = RT.keyword(null, (String)"imports");
        const__10 = RT.keyword(null, (String)"fnref");
        const__11 = RT.keyword(null, (String)"lang");
        const__12 = RT.keyword(null, (String)"requires");
        const__13 = RT.keyword(null, (String)"code");
        const__14 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"params"), RT.keyword(null, (String)"imports"), RT.keyword(null, (String)"fnref"), RT.keyword(null, (String)"lang"), RT.keyword(null, (String)"requires"), RT.keyword(null, (String)"code")});
        const__15 = RT.var((String)"clojure.core", (String)"dissoc");
        const__16 = RT.var((String)"clojure.core", (String)"with-meta");
        const__17 = RT.var((String)"clojure.core", (String)"into");
        const__18 = RT.var((String)"clojure.core", (String)"not-empty");
        const__19 = RT.var((String)"clojure.core", (String)"identical?");
        const__20 = RT.var((String)"clojure.core", (String)"assoc");
        const__21 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"lang"), (Object)RT.keyword(null, (String)"imports"), (Object)RT.keyword(null, (String)"requires"), (Object)RT.keyword(null, (String)"params"), (Object)RT.keyword(null, (String)"code"), (Object)RT.keyword(null, (String)"fnref"));
        const__22 = RT.var((String)"clojure.core", (String)"seq");
        const__23 = RT.var((String)"clojure.core", (String)"concat");
        const__24 = RT.var((String)"clojure.core", (String)"not");
        const__25 = RT.var((String)"clojure.core", (String)"class");
        const__26 = RT.var((String)"clojure.core", (String)"imap-cons");
        const__27 = RT.var((String)"clojure.core", (String)"str");
        const__37 = RT.var((String)"datomic.memory-size", (String)"memory-size");
        const__38 = RT.var((String)"clojure.core", (String)"name");
        const__39 = RT.var((String)"clojure.core", (String)"mapv");
        const__40 = RT.var((String)"datomic.common", (String)"compare");
        const__41 = RT.var((String)"clojure.core", (String)"deref");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"lang"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"params"));
        __thunk__1__ = __site__1__;
        __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"code"));
        __thunk__2__ = __site__2__;
    }
}

