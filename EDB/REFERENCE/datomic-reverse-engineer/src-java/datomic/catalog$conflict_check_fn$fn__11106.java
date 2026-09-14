/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class catalog$conflict_check_fn$fn__11106
extends AFunction {
    Object db_id;
    Object db_name;
    public static final Var const__0 = RT.var((String)"datomic.catalog", (String)"db-name->db-id");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"not");
    public static final Keyword const__3 = RT.keyword(null, (String)"exists");
    public static final Keyword const__4 = RT.keyword(null, (String)"name-conflict");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__6 = RT.var((String)"datomic.catalog", (String)"db-ids");
    public static final Keyword const__7 = RT.keyword(null, (String)"id-conflict");
    public static final Var const__8 = RT.var((String)"datomic.catalog", (String)"db-id->db-name");

    public catalog$conflict_check_fn$fn__11106(Object object, Object object2) {
        this.db_id = object;
        this.db_name = object2;
    }

    public Object invoke(Object catalog2) {
        IPersistentMap iPersistentMap;
        Object temp__5455__auto__11110;
        Object object = temp__5455__auto__11110 = ((IFn)const__0.getRawRoot()).invoke(catalog2, this.db_name);
        if (object != null && object != Boolean.FALSE) {
            Object object2;
            Object or__5238__auto__11108;
            Object object3 = temp__5455__auto__11110;
            temp__5455__auto__11110 = null;
            Object existing_id = object3;
            Object object4 = or__5238__auto__11108 = ((IFn)const__1.getRawRoot()).invoke(this.db_id);
            if (object4 != null && object4 != Boolean.FALSE) {
                object2 = or__5238__auto__11108;
                or__5238__auto__11108 = null;
            } else {
                Object object5 = existing_id;
                existing_id = null;
                object2 = Util.equiv((Object)this.db_id, (Object)object5) ? Boolean.TRUE : Boolean.FALSE;
            }
            iPersistentMap = object2 != null && object2 != Boolean.FALSE ? RT.mapUniqueKeys((Object[])new Object[]{const__3, this.db_name}) : RT.mapUniqueKeys((Object[])new Object[]{const__4, this.db_name});
        } else {
            Object object6;
            Object and__5236__auto__11109;
            Object object7 = and__5236__auto__11109 = this.db_id;
            if (object7 != null && object7 != Boolean.FALSE) {
                object6 = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(catalog2), this.db_id);
            } else {
                object6 = and__5236__auto__11109;
                Object var3_4 = null;
            }
            if (object6 != null && object6 != Boolean.FALSE) {
                Object[] objectArray = new Object[2];
                objectArray[0] = const__7;
                Object object8 = catalog2;
                catalog2 = null;
                objectArray[1] = ((IFn)const__8.getRawRoot()).invoke(object8, this.db_id);
                iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
            } else {
                iPersistentMap = null;
            }
        }
        return iPersistentMap;
    }
}

