/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class peer$rename_local_database$fn__21622
extends AFunction {
    Object dbname;
    Object newname;
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__2 = RT.keyword((String)"db.error", (String)"db-exists");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"dissoc");
    public static final Keyword const__6 = RT.keyword((String)"db.error", (String)"db-not-found");

    public peer$rename_local_database$fn__21622(Object object, Object object2) {
        this.dbname = object;
        this.newname = object2;
    }

    public Object invoke(Object dbs) {
        Object object;
        peer$rename_local_database$fn__21622 this_;
        Object temp__5455__auto__21624;
        Object object2 = temp__5455__auto__21624 = RT.get((Object)dbs, (Object)this_.dbname);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = temp__5455__auto__21624;
            temp__5455__auto__21624 = null;
            Object db2 = object3;
            Object object4 = RT.get((Object)dbs, (Object)this_.newname);
            if (object4 != null && object4 != Boolean.FALSE) {
                this_ = null;
                object = ((IFn)const__1.getRawRoot()).invoke((Object)const__2, ((IFn)const__3.getRawRoot()).invoke(this_.newname, (Object)" already exists in catalog"));
            } else {
                Object object5 = dbs;
                dbs = null;
                Object object6 = db2;
                db2 = null;
                this_ = null;
                object = ((IFn)const__4.getRawRoot()).invoke(((IFn)const__5.getRawRoot()).invoke(object5, this_.dbname), this_.newname, object6);
            }
        } else {
            this_ = null;
            object = ((IFn)const__1.getRawRoot()).invoke((Object)const__6, ((IFn)const__3.getRawRoot()).invoke((Object)"Could not find ", this_.dbname, (Object)" in catalog"));
        }
        return object;
    }
}

