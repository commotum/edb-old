/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.core2.val_store.fs;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.core2.val_store.fs.Impl;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class FS$fn__21333
extends AFunction {
    Object this;
    Object opts;
    Object v;
    private static Class __cached_class__0;
    public static final Var const__0;

    public FS$fn__21333(Object object, Object object2, Object object3) {
        this.this = object;
        this.opts = object2;
        this.v = object3;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke(Object p1__21323_SHARP_) {
        Object object;
        Object object2 = this_.this;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof Impl) {
                Object object3 = p1__21323_SHARP_;
                p1__21323_SHARP_ = null;
                object = ((Impl)object2)._sync_put(object3, this_.v, this_.opts);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        Object object4 = p1__21323_SHARP_;
        p1__21323_SHARP_ = null;
        FS$fn__21333 this_ = null;
        object = const__0.getRawRoot().invoke(object2, object4, this_.v, this_.opts);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.core2.val-store.fs", (String)"-sync-put");
    }
}

