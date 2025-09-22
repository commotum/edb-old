/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 *  org.infinispan.client.hotrod.RemoteCache
 *  org.infinispan.client.hotrod.VersionedValue
 */
package datomic.kv_hotrod;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.VersionedValue;

public final class KVHotRod$put_when__17803
extends AFunction {
    Object cache;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"select-keys");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"keys");

    public KVHotRod$put_when__17803(Object object) {
        this.cache = object;
    }

    public Object invoke(Object k, Object val, Object emap2) {
        Boolean bl;
        block3: {
            block2: {
                while (true) {
                    Object oldv;
                    VersionedValue temp__5457__auto__17806;
                    VersionedValue versionedValue = temp__5457__auto__17806 = ((RemoteCache)this.cache).getVersioned(k);
                    if (versionedValue == null || versionedValue == Boolean.FALSE) break block2;
                    VersionedValue versionedValue2 = temp__5457__auto__17806;
                    temp__5457__auto__17806 = null;
                    VersionedValue vv = versionedValue2;
                    long ver = vv.getVersion();
                    VersionedValue versionedValue3 = vv;
                    vv = null;
                    Object object = oldv = versionedValue3.getValue();
                    oldv = null;
                    if (!Util.equiv((Object)emap2, (Object)((IFn)const__1.getRawRoot()).invoke(object, ((IFn)const__2.getRawRoot()).invoke(emap2)))) break;
                    boolean or__5238__auto__17805 = ((RemoteCache)this.cache).replaceWithVersion(k, val, ver);
                    if (or__5238__auto__17805) {
                        bl = or__5238__auto__17805 ? Boolean.TRUE : Boolean.FALSE;
                        break block3;
                    }
                    Object object2 = k;
                    k = null;
                    Object object3 = val;
                    val = null;
                    Object object4 = emap2;
                    emap2 = null;
                    emap2 = object4;
                    val = object3;
                    k = object2;
                }
                bl = null;
                break block3;
            }
            bl = null;
        }
        return bl;
    }
}

