/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic.backup;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.backup.IValueBackup;
import datomic.backup.ValueBackup$fn__20107$fn__20108$fn__20109;

/*
 * Illegal identifiers - consider using --renameillegalidents true
 */
public final class ValueBackup$fn__20107$fn__20108
extends AFunction {
    Object progress;
    Object value_storage;
    Object leaf_id;
    Object this;
    private static Class __cached_class__0;
    public static final Var const__0;
    public static final Var const__1;
    public static final Keyword const__2;
    public static final Var const__3;

    public ValueBackup$fn__20107$fn__20108(Object object, Object object2, Object object3, Object object4) {
        this.progress = object;
        this.value_storage = object2;
        this.leaf_id = object3;
        this.this = object4;
    }

    /*
     * Enabled aggressive block sorting
     */
    public Object invoke() {
        Object object;
        ValueBackup$fn__20107$fn__20108 this_;
        Object backup_leaf_id = ((IFn)const__0.getRawRoot()).invoke(this_.leaf_id);
        Object object2 = ((IFn)const__1.getRawRoot()).invoke((Object)new ValueBackup$fn__20107$fn__20108$fn__20109(backup_leaf_id, this_.value_storage));
        if (object2 != null && object2 != Boolean.FALSE) {
            this_ = null;
            object = ((IFn)this_.progress).invoke((Object)const__2);
            return object;
        }
        Object object3 = this_.this;
        if (Util.classOf((Object)object3) != __cached_class__0) {
            if (object3 instanceof IValueBackup) {
                Object object4 = backup_leaf_id;
                backup_leaf_id = null;
                object = ((IValueBackup)object3).backup_val(this_.leaf_id, object4);
                return object;
            }
            object3 = object3;
            __cached_class__0 = Util.classOf((Object)object3);
        }
        Object object5 = backup_leaf_id;
        backup_leaf_id = null;
        this_ = null;
        object = const__3.getRawRoot().invoke(object3, this_.leaf_id, object5);
        return object;
    }

    static {
        const__0 = RT.var((String)"datomic.backup", (String)"add-key-prefix");
        const__1 = RT.var((String)"datomic.backup", (String)"retry");
        const__2 = RT.keyword(null, (String)"skipped");
        const__3 = RT.var((String)"datomic.backup", (String)"backup-val");
    }
}

