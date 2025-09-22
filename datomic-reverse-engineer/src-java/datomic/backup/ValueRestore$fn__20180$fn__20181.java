/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.IValueRestore;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValueRestore$fn__20180$fn__20181
extends AFunction {
    Object this;
    Object branch;
    private static Class __cached_class__0;
    public static final Var const__0;

    public ValueRestore$fn__20180$fn__20181(Object object, Object object2) {
        this.this = object;
        this.branch = object2;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        Object object2 = this_.this;
        if (Util.classOf((Object)object2) != __cached_class__0) {
            if (object2 instanceof IValueRestore) {
                object = ((IValueRestore)object2).restore_node(this_.branch);
                return object;
            }
            object2 = object2;
            __cached_class__0 = Util.classOf((Object)object2);
        }
        ValueRestore$fn__20180$fn__20181 this_ = null;
        object = const__0.getRawRoot().invoke(object2, this_.branch);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"restore-node");
    }
}

