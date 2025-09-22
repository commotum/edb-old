/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.APersistentMap
 *  clojure.lang.Counted
 *  clojure.lang.IFn
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
 *  clojure.lang.MapEntry
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.RecordIterator
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.db;

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
import clojure.lang.MapEntry;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.RecordIterator;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.db.Function$reify__12739;
import datomic.db.Function$reify__12741;
import datomic.db.Function$reify__12743;
import datomic.db.Function$reify__12745;
import datomic.db.Function$reify__12747;
import datomic.db.IElementImpl;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class Function
implements IElementImpl,
IRecord,
IHashEq,
IObj,
ILookup,
IKeywordLookup,
IPersistentMap,
Map,
Serializable {
    public final Object id;
    public final Object kw;
    public final Object lang;
    public final Object code;
    public final Object f;
    public final Object __meta;
    public final Object __extmap;
    int __hash;
    int __hasheq;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"set");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"vals");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"some");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Keyword const__8 = RT.keyword(null, (String)"kw");
    public static final Keyword const__9 = RT.keyword(null, (String)"lang");
    public static final Keyword const__10 = RT.keyword(null, (String)"id");
    public static final Keyword const__11 = RT.keyword(null, (String)"code");
    public static final Keyword const__12 = RT.keyword(null, (String)"f");
    public static final AFn const__13 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"kw"), RT.keyword(null, (String)"lang"), RT.keyword(null, (String)"id"), RT.keyword(null, (String)"code"), RT.keyword(null, (String)"f")});
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"with-meta");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"into");
    public static final Var const__17 = RT.var((String)"clojure.core", (String)"not-empty");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"identical?");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"assoc");
    public static final AFn const__20 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"id"), (Object)RT.keyword(null, (String)"kw"), (Object)RT.keyword(null, (String)"lang"), (Object)RT.keyword(null, (String)"code"), (Object)RT.keyword(null, (String)"f"));
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__22 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__23 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"imap-cons");
    public static final Var const__26 = RT.var((String)"clojure.core", (String)"str");

    public Function(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, int n, int n2) {
        this.id = object;
        this.kw = object2;
        this.lang = object3;
        this.code = object4;
        this.f = object5;
        this.__meta = object6;
        this.__extmap = object7;
        this.__hash = n;
        this.__hasheq = n2;
    }

    public Function(Object object, Object object2, Object object3, Object object4, Object object5) {
        this(object, object2, object3, object4, object5, null, null, 0, 0);
    }

    public Function(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7) {
        this(object, object2, object3, object4, object5, object6, object7, 0, 0);
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"id"), (Object)Symbol.intern(null, (String)"kw"), (Object)Symbol.intern(null, (String)"lang"), (Object)Symbol.intern(null, (String)"code"), (Object)Symbol.intern(null, (String)"f"));
    }

    public static Function create(IPersistentMap iPersistentMap) {
        Object object = iPersistentMap.valAt((Object)Keyword.intern((String)"id"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"id"));
        Object object2 = iPersistentMap.valAt((Object)Keyword.intern((String)"kw"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"kw"));
        Object object3 = iPersistentMap.valAt((Object)Keyword.intern((String)"lang"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"lang"));
        Object object4 = iPersistentMap.valAt((Object)Keyword.intern((String)"code"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"code"));
        Object object5 = iPersistentMap.valAt((Object)Keyword.intern((String)"f"), null);
        iPersistentMap = iPersistentMap.without((Object)Keyword.intern((String)"f"));
        return new Function(object, object2, object3, object4, object5, null, RT.seqOrElse((Object)iPersistentMap), 0, 0);
    }

    public Object id() {
        return this.id;
    }

    public Object kw() {
        return this.kw;
    }

    /*
     * WARNING - void declaration
     */
    public int hasheq() {
        void v0;
        int hq__7465__auto__12752 = this.__hasheq;
        if ((long)hq__7465__auto__12752 == 0L) {
            void var2_2;
            int h__7466__auto__12751;
            this.__hasheq = h__7466__auto__12751 = (int)(0xFFFFFFFFAF9E357CL ^ (long)APersistentMap.mapHasheq((IPersistentMap)this));
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
        int hash__7468__auto__12754 = this.__hash;
        if ((long)hash__7468__auto__12754 == 0L) {
            void var2_2;
            int h__7469__auto__12753;
            this.__hash = h__7469__auto__12753 = APersistentMap.mapHash((IPersistentMap)this);
            v0 = var2_2;
        } else {
            void var1_1;
            v0 = var1_1;
        }
        return (int)v0;
    }

    public boolean equals(Object G__12734) {
        Object object = G__12734;
        G__12734 = null;
        return APersistentMap.mapEquals((IPersistentMap)this, (Object)object);
    }

    public IPersistentMap meta() {
        return (IPersistentMap)this.__meta;
    }

    public IObj withMeta(IPersistentMap G__12734) {
        IPersistentMap iPersistentMap = G__12734;
        G__12734 = null;
        return new Function(this.id, this.kw, this.lang, this.code, this.f, iPersistentMap, this.__extmap, this.__hash, this.__hasheq);
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
        Object G__12749 = k__7476__auto__;
        switch (Util.hash((Object)G__12749) >> 6 & 7) {
            case 0: {
                if (G__12749 != const__9) break;
                object = this_.lang;
                return object;
            }
            case 1: {
                if (G__12749 != const__10) break;
                object = this_.id;
                return object;
            }
            case 3: {
                if (G__12749 != const__12) break;
                object = this_.f;
                return object;
            }
            case 4: {
                if (G__12749 != const__8) break;
                object = this_.kw;
                return object;
            }
            case 5: {
                if (G__12749 != const__11) break;
                object = this_.code;
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
        Object gclass = ((IFn)const__24.getRawRoot()).invoke((Object)this);
        Keyword keyword = k__7479__auto__;
        k__7479__auto__ = null;
        Keyword G__12738 = keyword;
        switch (Util.hash((Object)G__12738) >> 6 & 7) {
            case 0: {
                if (G__12738 != const__9) break;
                gclass = null;
                object = new Function$reify__12739(null, gclass);
                return object;
            }
            case 1: {
                if (G__12738 != const__10) break;
                gclass = null;
                object = new Function$reify__12741(null, gclass);
                return object;
            }
            case 3: {
                if (G__12738 != const__12) break;
                gclass = null;
                object = new Function$reify__12743(null, gclass);
                return object;
            }
            case 4: {
                if (G__12738 != const__8) break;
                gclass = null;
                object = new Function$reify__12745(null, gclass);
                return object;
            }
            case 5: {
                if (G__12738 != const__11) break;
                gclass = null;
                object = new Function$reify__12747(null, gclass);
                return object;
            }
        }
        object = null;
        return object;
    }

    public int count() {
        return RT.intCast((long)(5L + (long)RT.count((Object)this.__extmap)));
    }

    public IPersistentCollection empty() {
        throw (Throwable)new UnsupportedOperationException((String)((IFn)const__26.getRawRoot()).invoke((Object)"Can't create empty: ", (Object)"datomic.db.Function"));
    }

    public IPersistentCollection cons(Object e__7483__auto__) {
        Function function2 = this_;
        Object object = e__7483__auto__;
        e__7483__auto__ = null;
        Function this_ = null;
        return (IPersistentCollection)((IFn)const__25).invoke((Object)function2, object);
    }

    public boolean equiv(Object G__12734) {
        Boolean bl;
        boolean or__5238__auto__12760 = Util.identical((Object)this, (Object)G__12734);
        if (or__5238__auto__12760) {
            bl = or__5238__auto__12760 ? Boolean.TRUE : Boolean.FALSE;
        } else if (Util.identical((Object)((IFn)const__24.getRawRoot()).invoke((Object)this), (Object)((IFn)const__24.getRawRoot()).invoke(G__12734))) {
            Object object = G__12734;
            G__12734 = null;
            Object G__127342 = object;
            boolean and__5236__auto__12759 = Util.equiv((Object)this.id, (Object)((Function)G__127342).id);
            if (and__5236__auto__12759) {
                boolean and__5236__auto__12758 = Util.equiv((Object)this.kw, (Object)((Function)G__127342).kw);
                if (and__5236__auto__12758) {
                    boolean and__5236__auto__12757 = Util.equiv((Object)this.lang, (Object)((Function)G__127342).lang);
                    if (and__5236__auto__12757) {
                        boolean and__5236__auto__12756 = Util.equiv((Object)this.code, (Object)((Function)G__127342).code);
                        if (and__5236__auto__12756) {
                            boolean and__5236__auto__12755 = Util.equiv((Object)this.f, (Object)((Function)G__127342).f);
                            if (and__5236__auto__12755) {
                                Object object2 = G__127342;
                                G__127342 = null;
                                bl = Util.equiv((Object)this.__extmap, (Object)((Function)object2).__extmap) ? Boolean.TRUE : Boolean.FALSE;
                            } else {
                                bl = and__5236__auto__12755 ? Boolean.TRUE : Boolean.FALSE;
                            }
                        } else {
                            bl = and__5236__auto__12756 ? Boolean.TRUE : Boolean.FALSE;
                        }
                    } else {
                        bl = and__5236__auto__12757 ? Boolean.TRUE : Boolean.FALSE;
                    }
                } else {
                    bl = and__5236__auto__12758 ? Boolean.TRUE : Boolean.FALSE;
                }
            } else {
                bl = and__5236__auto__12759 ? Boolean.TRUE : Boolean.FALSE;
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
        return (Boolean)((IFn)const__23.getRawRoot()).invoke((Object)bl);
    }

    public IMapEntry entryAt(Object k__7488__auto__) {
        MapEntry mapEntry;
        Object v__7489__auto__12761 = ((ILookup)this_).valAt(k__7488__auto__, (Object)this_);
        if (Util.identical((Object)this_, (Object)v__7489__auto__12761)) {
            mapEntry = null;
        } else {
            Object object = k__7488__auto__;
            k__7488__auto__ = null;
            Object object2 = v__7489__auto__12761;
            v__7489__auto__12761 = null;
            Function this_ = null;
            mapEntry = MapEntry.create((Object)object, (Object)object2);
        }
        return (IMapEntry)mapEntry;
    }

    public ISeq seq() {
        Function this_ = null;
        return (ISeq)((IFn)const__21.getRawRoot()).invoke(((IFn)const__22.getRawRoot()).invoke((Object)Tuple.create((Object)MapEntry.create((Object)const__10, (Object)this_.id), (Object)MapEntry.create((Object)const__8, (Object)this_.kw), (Object)MapEntry.create((Object)const__9, (Object)this_.lang), (Object)MapEntry.create((Object)const__11, (Object)this_.code), (Object)MapEntry.create((Object)const__12, (Object)this_.f)), this_.__extmap));
    }

    public Iterator iterator() {
        return (Iterator)new RecordIterator((ILookup)this, (IPersistentVector)const__20, RT.iter((Object)this.__extmap));
    }

    public IPersistentMap assoc(Object k__7493__auto__, Object G__12734) {
        Function function2;
        Object pred__12736 = const__18.getRawRoot();
        Object expr__12737 = k__7493__auto__;
        Object object = ((IFn)pred__12736).invoke((Object)const__10, expr__12737);
        if (object != null && object != Boolean.FALSE) {
            G__12734 = null;
            function2 = new Function(G__12734, this.kw, this.lang, this.code, this.f, this.__meta, this.__extmap);
        } else {
            Object object2 = ((IFn)pred__12736).invoke((Object)const__8, expr__12737);
            if (object2 != null && object2 != Boolean.FALSE) {
                G__12734 = null;
                function2 = new Function(this.id, G__12734, this.lang, this.code, this.f, this.__meta, this.__extmap);
            } else {
                Object object3 = ((IFn)pred__12736).invoke((Object)const__9, expr__12737);
                if (object3 != null && object3 != Boolean.FALSE) {
                    G__12734 = null;
                    function2 = new Function(this.id, this.kw, G__12734, this.code, this.f, this.__meta, this.__extmap);
                } else {
                    Object object4 = ((IFn)pred__12736).invoke((Object)const__11, expr__12737);
                    if (object4 != null && object4 != Boolean.FALSE) {
                        G__12734 = null;
                        function2 = new Function(this.id, this.kw, this.lang, G__12734, this.f, this.__meta, this.__extmap);
                    } else {
                        Object object5 = pred__12736;
                        pred__12736 = null;
                        Object object6 = expr__12737;
                        expr__12737 = null;
                        Object object7 = ((IFn)object5).invoke((Object)const__12, object6);
                        if (object7 != null && object7 != Boolean.FALSE) {
                            G__12734 = null;
                            function2 = new Function(this.id, this.kw, this.lang, this.code, G__12734, this.__meta, this.__extmap);
                        } else {
                            k__7493__auto__ = null;
                            G__12734 = null;
                            function2 = new Function(this.id, this.kw, this.lang, this.code, this.f, this.__meta, ((IFn)const__19.getRawRoot()).invoke(this.__extmap, k__7493__auto__, G__12734));
                        }
                    }
                }
            }
        }
        return function2;
    }

    public IPersistentMap without(Object k__7495__auto__) {
        Object object;
        Object object2 = ((IFn)const__7.getRawRoot()).invoke((Object)const__13, k__7495__auto__);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__15.getRawRoot()).invoke(((IFn)const__16.getRawRoot()).invoke((Object)PersistentArrayMap.EMPTY, (Object)this_), this_.__meta);
            Object object4 = k__7495__auto__;
            k__7495__auto__ = null;
            Function this_ = null;
            object = ((IFn)const__14.getRawRoot()).invoke(object3, object4);
        } else {
            k__7495__auto__ = null;
            object = new Function(this_.id, this_.kw, this_.lang, this_.code, this_.f, this_.__meta, ((IFn)const__17.getRawRoot()).invoke(((IFn)const__14.getRawRoot()).invoke(this_.__extmap, k__7495__auto__)));
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
}

