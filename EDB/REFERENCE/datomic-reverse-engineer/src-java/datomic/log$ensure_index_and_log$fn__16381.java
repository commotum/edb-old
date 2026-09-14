/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentMap
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class log$ensure_index_and_log$fn__16381
extends AFunction {
    Object db_id;
    Object olookup;
    Object cluster;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.index", (String)"find-index-root-id");
    public static final Keyword const__2 = RT.keyword(null, (String)"idxroot");
    public static final Var const__3 = RT.var((String)"datomic.index", (String)"init-index");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__5 = RT.keyword(null, (String)"log");
    public static final Var const__6 = RT.var((String)"datomic.log", (String)"find-log");
    public static final Var const__7 = RT.var((String)"datomic.log", (String)"create-new-log");
    public static final Keyword const__8 = RT.keyword(null, (String)"threw");

    public log$ensure_index_and_log$fn__16381(Object object, Object object2, Object object3) {
        this.db_id = object;
        this.olookup = object2;
        this.cluster = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object or__5238__auto__16385;
            Object object;
            Object object2;
            Object or__5238__auto__16384;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object existing_root = ((IFn)const__1.getRawRoot()).invoke(this.cluster);
            Object[] objectArray2 = new Object[4];
            objectArray2[0] = const__2;
            Object object3 = or__5238__auto__16384 = existing_root;
            if (object3 != null && object3 != Boolean.FALSE) {
                object2 = or__5238__auto__16384;
                or__5238__auto__16384 = null;
            } else {
                Object or__5238__auto__16383;
                Object object4 = or__5238__auto__16383 = ((IFn)const__3.getRawRoot()).invoke(this.cluster);
                if (object4 != null && object4 != Boolean.FALSE) {
                    object2 = or__5238__auto__16383;
                    or__5238__auto__16383 = null;
                } else {
                    throw (Throwable)new RuntimeException((String)((IFn)const__4.getRawRoot()).invoke((Object)"Unable to create index root for ", this.db_id));
                }
            }
            objectArray2[1] = object2;
            objectArray2[2] = const__5;
            Object object5 = existing_root;
            existing_root = null;
            if (object5 != null && object5 != Boolean.FALSE) {
                this.cluster = null;
                this.olookup = null;
                object = ((IFn)const__6.getRawRoot()).invoke(this.cluster, this.olookup);
            } else {
                this.cluster = null;
                this.olookup = null;
                object = ((IFn)const__7.getRawRoot()).invoke(this.cluster, this.olookup);
            }
            Object object6 = or__5238__auto__16385 = object;
            if (object6 == null || object6 == Boolean.FALSE) {
                this.db_id = null;
                throw (Throwable)new RuntimeException((String)((IFn)const__4.getRawRoot()).invoke((Object)"Unable to read log for db id ", this.db_id));
            }
            Object object7 = or__5238__auto__16385;
            or__5238__auto__16385 = null;
            objectArray2[3] = object7;
            objectArray[1] = RT.mapUniqueKeys((Object[])objectArray2);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__8;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

