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
 *  com.datastax.driver.core.Cluster
 *  com.datastax.driver.core.Cluster$Builder
 *  com.datastax.driver.core.Session
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public final class kv_cassandra2$kv_cassandra$fn__20631
extends AFunction {
    Object user;
    Object port;
    Object provided_cluster;
    Object lockee__5436__auto__;
    Object endpoint;
    Object host;
    Object ssl;
    Object password;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.kv-cassandra2", (String)"cluster-sessions");
    public static final Var const__3 = RT.var((String)"datomic.cassandra", (String)"cluster-from-callback");
    public static final Keyword const__4 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__5 = RT.keyword(null, (String)"session");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");

    public kv_cassandra2$kv_cassandra$fn__20631(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.user = object;
        this.port = object2;
        this.provided_cluster = object3;
        this.lockee__5436__auto__ = object4;
        this.endpoint = object5;
        this.host = object6;
        this.ssl = object7;
        this.password = object8;
    }

    public Object invoke() {
        Object object;
        try {
            synchronized (this.lockee__5436__auto__) {
                Object object2;
                Object or__5238__auto__20636;
                Object object3 = or__5238__auto__20636 = RT.get((Object)((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)this.endpoint);
                if (object3 != null && object3 != Boolean.FALSE) {
                    object2 = or__5238__auto__20636;
                    or__5238__auto__20636 = null;
                } else {
                    Object object4;
                    Object or__5238__auto__20635;
                    Object object5 = or__5238__auto__20635 = (this.provided_cluster = null);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        object4 = or__5238__auto__20635;
                        or__5238__auto__20635 = null;
                    } else {
                        Object or__5238__auto__20634;
                        Object object6 = or__5238__auto__20634 = ((IFn)const__3.getRawRoot()).invoke(this.endpoint);
                        if (object6 != null && object6 != Boolean.FALSE) {
                            object4 = or__5238__auto__20634;
                            or__5238__auto__20634 = null;
                        } else {
                            Cluster.Builder builder;
                            Cluster.Builder builder2;
                            Cluster.Builder builder3;
                            Cluster.Builder builder4;
                            Cluster.Builder G__20632 = Cluster.builder();
                            Boolean bl = Boolean.TRUE;
                            if (bl != null && bl != Boolean.FALSE) {
                                Cluster.Builder builder5 = G__20632;
                                G__20632 = null;
                                this.host = null;
                                builder4 = builder5.addContactPoint((String)this.host);
                            } else {
                                builder4 = G__20632;
                                G__20632 = null;
                            }
                            Cluster.Builder G__206322 = builder4;
                            Boolean bl2 = Boolean.TRUE;
                            if (bl2 != null && bl2 != Boolean.FALSE) {
                                Cluster.Builder builder6 = G__206322;
                                G__206322 = null;
                                this.port = null;
                                builder3 = builder6.withPort(RT.intCast((Object)((Number)this.port)));
                            } else {
                                builder3 = G__206322;
                                G__206322 = null;
                            }
                            Cluster.Builder G__206323 = builder3;
                            Boolean bl3 = Boolean.TRUE;
                            if (bl3 != null && bl3 != Boolean.FALSE) {
                                Cluster.Builder builder7 = G__206323;
                                G__206323 = null;
                                this.user = null;
                                this.password = null;
                                builder2 = builder7.withCredentials((String)this.user, (String)this.password);
                            } else {
                                builder2 = G__206323;
                                G__206323 = null;
                            }
                            Cluster.Builder G__206324 = builder2;
                            Object object7 = this.ssl;
                            this.ssl = null;
                            if (object7 != null && object7 != Boolean.FALSE) {
                                Cluster.Builder builder8 = G__206324;
                                G__206324 = null;
                                builder = builder8.withSSL();
                            } else {
                                builder = G__206324;
                                G__206324 = null;
                            }
                            Cluster.Builder G__206325 = builder;
                            Boolean bl4 = Boolean.TRUE;
                            if (bl4 != null && bl4 != Boolean.FALSE) {
                                Cluster.Builder builder9 = G__206325;
                                G__206325 = null;
                                object4 = builder9.build();
                            } else {
                                object4 = G__206325;
                                G__206325 = null;
                            }
                        }
                    }
                    Object c = object4;
                    Session s = ((Cluster)c).connect();
                    Object[] objectArray = new Object[4];
                    objectArray[0] = const__4;
                    Object object8 = c;
                    c = null;
                    objectArray[1] = object8;
                    objectArray[2] = const__5;
                    Session session = s;
                    s = null;
                    objectArray[3] = session;
                    IPersistentMap m = RT.mapUniqueKeys((Object[])objectArray);
                    this.endpoint = null;
                    ((IFn)const__6.getRawRoot()).invoke(const__2.getRawRoot(), const__7.getRawRoot(), this.endpoint, (Object)m);
                    object2 = m;
                    m = null;
                }
                object = object2;
            }
        }
        finally {
            this.lockee__5436__auto__ = null;
            // ** MonitorExit[this.lockee__5436__auto__] (shouldn't be in output)
        }
        {
            return object;
        }
    }
}

