/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.IValueRestore;
import datomic.cluster.Get2;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValueRestore$fn__20188$fn__20189
extends AFunction {
    Object this;
    Object progress;
    Object to_cluster;
    Object leaf_id;
    private static Class __cached_class__0;
    private static Class __cached_class__1;
    public static final Var const__0;
    public static final Var const__1;
    public static final AFn const__3;
    public static final Keyword const__4;
    public static final Var const__5;

    public ValueRestore$fn__20188$fn__20189(Object object, Object object2, Object object3, Object object4) {
        this.this = object;
        this.progress = object2;
        this.to_cluster = object3;
        this.leaf_id = object4;
    }

    /*
     * Unable to fully structure code
     */
    public Object invoke() {
        block5: {
            block4: {
                v0 = (IFn)ValueRestore$fn__20188$fn__20189.const__0.getRawRoot();
                v1 = this.to_cluster;
                if (Util.classOf((Object)v1) == ValueRestore$fn__20188$fn__20189.__cached_class__0) ** GOTO lbl7
                if (!(v1 instanceof Get2)) {
                    v1 = v1;
                    ValueRestore$fn__20188$fn__20189.__cached_class__0 = Util.classOf((Object)v1);
lbl7:
                    // 2 sources

                    v2 = ValueRestore$fn__20188$fn__20189.const__1.getRawRoot().invoke(v1, this.leaf_id, (Object)ValueRestore$fn__20188$fn__20189.const__3);
                } else {
                    v2 = ((Get2)v1).get_val2(this.leaf_id, ValueRestore$fn__20188$fn__20189.const__3);
                }
                v3 = v0.invoke(v2);
                if (v3 == null || v3 == Boolean.FALSE) break block4;
                this = null;
                v4 = ((IFn)this.progress).invoke((Object)ValueRestore$fn__20188$fn__20189.const__4);
                break block5;
            }
            v5 = this.this;
            if (Util.classOf((Object)v5) == ValueRestore$fn__20188$fn__20189.__cached_class__1) ** GOTO lbl21
            if (!(v5 instanceof IValueRestore)) {
                v5 = v5;
                ValueRestore$fn__20188$fn__20189.__cached_class__1 = Util.classOf((Object)v5);
lbl21:
                // 2 sources

                this = null;
                v4 = ValueRestore$fn__20188$fn__20189.const__5.getRawRoot().invoke(v5, this.leaf_id);
            } else {
                v4 = ((IValueRestore)v5).restore_val(this.leaf_id);
            }
        }
        return v4;
    }

    static {
        const__0 = RT.var((String)"clojure.core", (String)"deref");
        const__1 = RT.var((String)"datomic.cluster", (String)"get-val2");
        const__3 = (AFn)RT.map((Object[])new Object[]{RT.keyword((String)"datomic.core2.val-store.opts", (String)"skip-cache"), Boolean.TRUE});
        const__4 = RT.keyword(null, (String)"skipped");
        const__5 = RT.var((String)"datomic.backup", (String)"restore-val");
    }
}

