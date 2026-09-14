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

public final class peer$send_admin_request$fn__21699
extends AFunction {
    Object cluster_conf;
    Object arg;
    Object request;
    public static final Keyword const__0 = RT.keyword(null, (String)"returned");
    public static final Var const__1 = RT.var((String)"datomic.coordination", (String)"lookup-compatible-transactor-endpoint");
    public static final Var const__2 = RT.var((String)"datomic.coordination", (String)"create-system-cluster");
    public static final Var const__3 = RT.var((String)"datomic.error", (String)"raise");
    public static final Keyword const__4 = RT.keyword((String)"db.error", (String)"transactor-not-registered");
    public static final Var const__5 = RT.var((String)"datomic.connector", (String)"admin-request");
    public static final Var const__6 = RT.var((String)"datomic.connector", (String)"create-transactor-hornet-connector");
    public static final Keyword const__7 = RT.keyword(null, (String)"threw");

    public peer$send_admin_request$fn__21699(Object object, Object object2, Object object3) {
        this.cluster_conf = object;
        this.arg = object2;
        this.request = object3;
    }

    public Object invoke() {
        IPersistentMap iPersistentMap;
        try {
            Object object;
            Object or__5238__auto__21701;
            Object[] objectArray = new Object[2];
            objectArray[0] = const__0;
            Object object2 = or__5238__auto__21701 = ((IFn)const__1.getRawRoot()).invoke(((IFn)const__2.getRawRoot()).invoke(this.cluster_conf));
            if (object2 != null && object2 != Boolean.FALSE) {
                object = or__5238__auto__21701;
                or__5238__auto__21701 = null;
            } else {
                object = ((IFn)const__3.getRawRoot()).invoke((Object)const__4, (Object)"No transactor registered");
            }
            Object endpoint = object;
            this.cluster_conf = null;
            Object object3 = endpoint;
            endpoint = null;
            this.request = null;
            this.arg = null;
            objectArray[1] = ((IFn)const__5.getRawRoot()).invoke(((IFn)const__6.getRawRoot()).invoke(this.cluster_conf, object3), this.request, this.arg);
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        catch (Throwable t__8983__auto__2) {
            Object[] objectArray = new Object[2];
            objectArray[0] = const__7;
            Object t__8983__auto__2 = null;
            objectArray[1] = t__8983__auto__2;
            iPersistentMap = RT.mapUniqueKeys((Object[])objectArray);
        }
        return iPersistentMap;
    }
}

