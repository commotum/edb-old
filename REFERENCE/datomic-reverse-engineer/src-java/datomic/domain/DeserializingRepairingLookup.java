/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.ILookup
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
package datomic.domain;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.ILookup;
import clojure.lang.ILookupThunk;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.KeywordLookupSite;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.cluster.ClusteredStore;
import datomic.cluster.Get2;
import datomic.domain.DeserializingRepairingLookup$fn__16804;

public final class DeserializingRepairingLookup
implements ILookup,
IType {
    public final Object cluster;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final Var const__2;
    public static final Var const__4;
    public static final Var const__5;
    public static final AFn const__7;
    static final KeywordLookupSite __site__0__;
    static ILookupThunk __thunk__0__;
    static final KeywordLookupSite __site__1__;
    static ILookupThunk __thunk__1__;

    public DeserializingRepairingLookup(Object object) {
        this.cluster = object;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)Symbol.intern(null, (String)"cluster"));
    }

    /*
     * Unable to fully structure code
     */
    public Object valAt(Object k, Object not_found) {
        block11: {
            block10: {
                v0 = k;
                k = null;
                k = ((IFn)DeserializingRepairingLookup.const__0.getRawRoot()).invoke(v0);
                v1 = (IFn)DeserializingRepairingLookup.const__1.getRawRoot();
                v2 = this.cluster;
                if (Util.classOf((Object)v2) == DeserializingRepairingLookup.__cached_class__0) ** GOTO lbl10
                if (!(v2 instanceof ClusteredStore)) {
                    v2 = v2;
                    DeserializingRepairingLookup.__cached_class__0 = Util.classOf((Object)v2);
lbl10:
                    // 2 sources

                    v3 = DeserializingRepairingLookup.const__2.getRawRoot().invoke(v2, k);
                } else {
                    v3 = ((ClusteredStore)v2).get_val(k);
                }
                v = v1.invoke(v3);
                v4 = DeserializingRepairingLookup.__thunk__0__;
                v5 = v;
                v6 = v4.get(v5);
                if (v4 == v6) {
                    DeserializingRepairingLookup.__thunk__0__ = DeserializingRepairingLookup.__site__0__.fault(v5);
                    v6 = DeserializingRepairingLookup.__thunk__0__.get(v5);
                }
                if (v6 == null || v6 == Boolean.FALSE) break block10;
                try {
                    v7 = v;
                    v = null;
                    var5_5 = ((IFn)DeserializingRepairingLookup.const__4.getRawRoot()).invoke(v7);
                }
                catch (Throwable t) {
                    v8 = (IFn)DeserializingRepairingLookup.const__1.getRawRoot();
                    v9 = this.cluster;
                    if (Util.classOf((Object)v9) == DeserializingRepairingLookup.__cached_class__1) ** GOTO lbl34
                    if (!(v9 instanceof Get2)) {
                        v9 = v9;
                        DeserializingRepairingLookup.__cached_class__1 = Util.classOf((Object)v9);
lbl34:
                        // 2 sources

                        v10 = DeserializingRepairingLookup.const__5.getRawRoot().invoke(v9, k, (Object)DeserializingRepairingLookup.const__7);
                    } else {
                        v10 = ((Get2)v9).get_val2(k, DeserializingRepairingLookup.const__7);
                    }
                    v = v8.invoke(v10);
                    v11 = DeserializingRepairingLookup.__thunk__1__;
                    v12 = v;
                    v13 = v11.get(v12);
                    if (v11 == v13) {
                        DeserializingRepairingLookup.__thunk__1__ = DeserializingRepairingLookup.__site__1__.fault(v12);
                        v13 = DeserializingRepairingLookup.__thunk__1__.get(v12);
                    }
                    if (v13 != null && v13 != Boolean.FALSE) {
                        v14 = k;
                        k = null;
                        v15 = v;
                        v = null;
                        v16 = ((IFn)new DeserializingRepairingLookup$fn__16804(v14, v15)).invoke();
                    } else {
                        v16 = not_found;
                        not_found = null;
                    }
                    var5_5 = v16;
                }
                v17 = var5_5;
                break block11;
            }
            v17 = null;
        }
        return v17;
    }

    public Object valAt(Object k) {
        Object object = k;
        k = null;
        return ((ILookup)this).valAt(object, null);
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"str");
        const__1 = RT.var((String)"clojure.core", (String)"deref");
        const__2 = RT.var((String)"datomic.cluster", (String)"get-val");
        const__4 = RT.var((String)"datomic.domain", (String)"deserialize");
        const__5 = RT.var((String)"datomic.cluster", (String)"get-val2");
        const__7 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"datomic.core2.val-store.opts", (String)"reset-cache"), Boolean.TRUE});
        __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__0__ = __site__0__;
        __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"buf"));
        __thunk__1__ = __site__1__;
    }
}

