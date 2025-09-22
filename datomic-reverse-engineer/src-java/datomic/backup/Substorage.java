/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.Storage;
import datomic.backup.Substorage$fn__20013;

public final class Substorage
implements Storage,
IType {
    public final Object storage;
    public final Object prefix;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    private static Class __cached_class__2;
    private static Class __cached_class__3;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final AFn const__5;
    public static final Var const__6;
    public static final Var const__7;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;

    public Substorage(Object object, Object object2) {
        this.storage = object;
        this.prefix = object2;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"storage"), (Object)Symbol.intern(null, (String)"prefix"));
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object retrieve(Object k) {
        Object object;
        Object object2 = this_.storage;
        if (Util.classOf((Object)object2) != __cached_class__3) {
            if (object2 instanceof Storage) {
                Object object3 = k;
                k = null;
                object = ((Storage)object2).retrieve(((IFn)const__1.getRawRoot()).invoke(this_.prefix, object3));
                return object;
            }
            object2 = object2;
            __cached_class__3 = Util.classOf((Object)object2);
        }
        Object object4 = k;
        k = null;
        Substorage this_ = null;
        object = const__7.getRawRoot().invoke(object2, ((IFn)const__1.getRawRoot()).invoke(this_.prefix, object4));
        return object;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object exists_QMARK_(Object k) {
        Object object;
        Object object2 = this_.storage;
        if (Util.classOf((Object)object2) != __cached_class__2) {
            if (object2 instanceof Storage) {
                Object object3 = k;
                k = null;
                object = ((Storage)object2).exists_QMARK_(((IFn)const__1.getRawRoot()).invoke(this_.prefix, object3));
                return object;
            }
            object2 = object2;
            __cached_class__2 = Util.classOf((Object)object2);
        }
        Object object4 = k;
        k = null;
        Substorage this_ = null;
        object = const__6.getRawRoot().invoke(object2, ((IFn)const__1.getRawRoot()).invoke(this_.prefix, object4));
        return object;
    }

    /*
     * Unable to fully structure code
     */
    public Object list_keys(Object pre) {
        v0 = this.storage;
        if (Util.classOf((Object)v0) == Substorage.__cached_class__1) ** GOTO lbl6
        if (!(v0 instanceof Storage)) {
            v0 = v0;
            Substorage.__cached_class__1 = Util.classOf((Object)v0);
lbl6:
            // 2 sources

            v1 = pre;
            pre = null;
            v2 = Substorage.const__2.getRawRoot().invoke(v0, ((IFn)Substorage.const__1.getRawRoot()).invoke(this.prefix, v1));
        } else {
            v3 = pre;
            pre = null;
            v2 = ((Storage)v0).list_keys(((IFn)Substorage.const__1.getRawRoot()).invoke(this.prefix, v3));
        }
        result = v2;
        v4 = Substorage.__thunk__0__;
        v5 = result;
        v6 = v4.get(v5);
        if (v4 == v6) {
            Substorage.__thunk__0__ = Substorage.__site__0__.fault(v5);
            v6 = Substorage.__thunk__0__.get(v5);
        }
        if (v6 != null && v6 != Boolean.FALSE) {
            v7 = result;
            result = null;
            this = null;
            v8 = ((IFn)Substorage.const__4.getRawRoot()).invoke(v7, (Object)Substorage.const__5, (Object)new Substorage$fn__20013(this.prefix));
        } else {
            v8 = result;
            var2_2 = null;
        }
        return v8;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object store(Object k, Object buf) {
        Object object;
        Object object2 = this_.storage;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Storage) {
                Object object3 = k;
                k = null;
                Object object4 = buf;
                buf = null;
                object = ((Storage)object2).store(((IFn)const__1.getRawRoot()).invoke(this_.prefix, object3), object4);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object5 = k;
        k = null;
        Object object6 = buf;
        buf = null;
        Substorage this_ = null;
        object = const__0.getRawRoot().invoke(object2, ((IFn)const__1.getRawRoot()).invoke(this_.prefix, object5), object6);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"store");
        const__1 = RT.var((String)"datomic.backup", (String)"subkey");
        const__2 = RT.var((String)"datomic.backup", (String)"list-keys");
        const__4 = RT.var((String)"clojure.core", (String)"update-in");
        const__5 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"ks"));
        const__6 = RT.var((String)"datomic.backup", (String)"exists?");
        const__7 = RT.var((String)"datomic.backup", (String)"retrieve");
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"ks"));
        __thunk__0__ = __site__0__;
    }
}

