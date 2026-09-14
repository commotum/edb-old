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

public final class kv_cassandra$kv_cassandra$fn__17780
extends AFunction {
    Object ssl;
    Object provided_cluster;
    Object host;
    Object endpoint;
    Object password;
    Object user;
    Object port;
    Object lockee__5436__auto__;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.kv-cassandra", (String)"cluster-sessions");
    public static final Var const__3 = RT.var((String)"datomic.cassandra", (String)"cluster-from-callback");
    public static final Keyword const__4 = RT.keyword(null, (String)"cluster");
    public static final Keyword const__5 = RT.keyword(null, (String)"session");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");

    public kv_cassandra$kv_cassandra$fn__17780(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8) {
        this.ssl = object;
        this.provided_cluster = object2;
        this.host = object3;
        this.endpoint = object4;
        this.password = object5;
        this.user = object6;
        this.port = object7;
        this.lockee__5436__auto__ = object8;
    }

    public Object invoke() {
        Object object;
        try {
            synchronized (this.lockee__5436__auto__) {
                Object object2;
                Object or__5238__auto__17785;
                Object object3 = or__5238__auto__17785 = RT.get((Object)((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)this.endpoint);
                if (object3 != null && object3 != Boolean.FALSE) {
                    object2 = or__5238__auto__17785;
                    or__5238__auto__17785 = null;
                } else {
                    Object object4;
                    Object or__5238__auto__17784;
                    Object object5 = or__5238__auto__17784 = (this.provided_cluster = null);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        object4 = or__5238__auto__17784;
                        or__5238__auto__17784 = null;
                    } else {
                        Object or__5238__auto__17783;
                        Object object6 = or__5238__auto__17783 = ((IFn)const__3.getRawRoot()).invoke(this.endpoint);
                        if (object6 != null && object6 != Boolean.FALSE) {
                            object4 = or__5238__auto__17783;
                            or__5238__auto__17783 = null;
                        } else {
                            Cluster.Builder builder;
                            Cluster.Builder builder2;
                            Cluster.Builder builder3;
                            Cluster.Builder builder4;
                            Cluster.Builder G__17781 = Cluster.builder();
                            Boolean bl = Boolean.TRUE;
                            if (bl != null && bl != Boolean.FALSE) {
                                Cluster.Builder builder5 = G__17781;
                                G__17781 = null;
                                this.host = null;
                                builder4 = builder5.addContactPoint((String)this.host);
                            } else {
                                builder4 = G__17781;
                                G__17781 = null;
                            }
                            Cluster.Builder G__177812 = builder4;
                            Boolean bl2 = Boolean.TRUE;
                            if (bl2 != null && bl2 != Boolean.FALSE) {
                                Cluster.Builder builder6 = G__177812;
                                G__177812 = null;
                                this.port = null;
                                builder3 = builder6.withPort(RT.intCast((Object)((Number)this.port)));
                            } else {
                                builder3 = G__177812;
                                G__177812 = null;
                            }
                            Cluster.Builder G__177813 = builder3;
                            Boolean bl3 = Boolean.TRUE;
                            if (bl3 != null && bl3 != Boolean.FALSE) {
                                Cluster.Builder builder7 = G__177813;
                                G__177813 = null;
                                this.user = null;
                                this.password = null;
                                builder2 = builder7.withCredentials((String)this.user, (String)this.password);
                            } else {
                                builder2 = G__177813;
                                G__177813 = null;
                            }
                            Cluster.Builder G__177814 = builder2;
                            Object object7 = this.ssl;
                            this.ssl = null;
                            if (object7 != null && object7 != Boolean.FALSE) {
                                Cluster.Builder builder8 = G__177814;
                                G__177814 = null;
                                builder = builder8.withSSL();
                            } else {
                                builder = G__177814;
                                G__177814 = null;
                            }
                            Cluster.Builder G__177815 = builder;
                            Boolean bl4 = Boolean.TRUE;
                            if (bl4 != null && bl4 != Boolean.FALSE) {
                                Cluster.Builder builder9 = G__177815;
                                G__177815 = null;
                                object4 = builder9.build();
                            } else {
                                object4 = G__177815;
                                G__177815 = null;
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

