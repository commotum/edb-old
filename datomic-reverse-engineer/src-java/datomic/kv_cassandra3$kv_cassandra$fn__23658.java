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
 *  com.datastax.oss.driver.api.core.CqlSessionBuilder
 *  com.datastax.oss.driver.api.core.session.SessionBuilder
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentMap;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.session.SessionBuilder;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLContext;

public final class kv_cassandra3$kv_cassandra$fn__23658
extends AFunction {
    Object provided_session;
    Object ssl;
    Object port;
    Object endpoint;
    Object lockee__5436__auto__;
    Object user;
    Object local_datacenter;
    Object password;
    Object host;
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"deref");
    public static final Var const__2 = RT.var((String)"datomic.kv-cassandra3", (String)"sessions");
    public static final Var const__3 = RT.var((String)"datomic.cassandra-v4", (String)"session-from-callback");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__5 = RT.keyword(null, (String)"session");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"swap!");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");

    public kv_cassandra3$kv_cassandra$fn__23658(Object object, Object object2, Object object3, Object object4, Object object5, Object object6, Object object7, Object object8, Object object9) {
        this.provided_session = object;
        this.ssl = object2;
        this.port = object3;
        this.endpoint = object4;
        this.lockee__5436__auto__ = object5;
        this.user = object6;
        this.local_datacenter = object7;
        this.password = object8;
        this.host = object9;
    }

    public Object invoke() {
        Object object;
        try {
            synchronized (this.lockee__5436__auto__) {
                Object object2;
                Object or__5238__auto__23664;
                Object object3 = or__5238__auto__23664 = RT.get((Object)((IFn)const__1.getRawRoot()).invoke(const__2.getRawRoot()), (Object)this.endpoint);
                if (object3 != null && object3 != Boolean.FALSE) {
                    object2 = or__5238__auto__23664;
                    or__5238__auto__23664 = null;
                } else {
                    Object object4;
                    Object or__5238__auto__23663;
                    Object object5 = or__5238__auto__23663 = (this.provided_session = null);
                    if (object5 != null && object5 != Boolean.FALSE) {
                        object4 = or__5238__auto__23663;
                        or__5238__auto__23663 = null;
                    } else {
                        Object or__5238__auto__23662;
                        Object object6 = or__5238__auto__23662 = ((IFn)const__3.getRawRoot()).invoke(this.endpoint);
                        if (object6 != null && object6 != Boolean.FALSE) {
                            object4 = or__5238__auto__23662;
                            or__5238__auto__23662 = null;
                        } else {
                            SessionBuilder sessionBuilder;
                            SessionBuilder sessionBuilder2;
                            Object object7;
                            Object and__5236__auto__23661;
                            CqlSessionBuilder sb;
                            CqlSessionBuilder cqlSessionBuilder = sb = new CqlSessionBuilder();
                            sb = null;
                            this.host = null;
                            this.port = null;
                            this.local_datacenter = null;
                            SessionBuilder G__23659 = ((SessionBuilder)cqlSessionBuilder).addContactPoint(InetSocketAddress.createUnresolved((String)this.host, RT.intCast((Object)((Number)this.port)))).withLocalDatacenter((String)this.local_datacenter);
                            Object object8 = and__5236__auto__23661 = this.user;
                            if (object8 != null && object8 != Boolean.FALSE) {
                                object7 = this.password;
                            } else {
                                object7 = and__5236__auto__23661;
                                and__5236__auto__23661 = null;
                            }
                            if (object7 != null && object7 != Boolean.FALSE) {
                                SessionBuilder sessionBuilder3 = G__23659;
                                G__23659 = null;
                                this.user = null;
                                this.password = null;
                                sessionBuilder2 = sessionBuilder3.withAuthCredentials((String)((IFn)const__4.getRawRoot()).invoke(this.user), (String)((IFn)const__4.getRawRoot()).invoke(this.password));
                            } else {
                                sessionBuilder2 = G__23659;
                                G__23659 = null;
                            }
                            SessionBuilder G__236592 = sessionBuilder2;
                            Object object9 = this.ssl;
                            this.ssl = null;
                            if (object9 != null && object9 != Boolean.FALSE) {
                                SessionBuilder sessionBuilder4 = G__236592;
                                G__236592 = null;
                                sessionBuilder = sessionBuilder4.withSslContext(SSLContext.getDefault());
                            } else {
                                sessionBuilder = G__236592;
                                G__236592 = null;
                            }
                            SessionBuilder G__236593 = sessionBuilder;
                            Boolean bl = Boolean.TRUE;
                            if (bl != null && bl != Boolean.FALSE) {
                                SessionBuilder sessionBuilder5 = G__236593;
                                G__236593 = null;
                                object4 = sessionBuilder5.build();
                            } else {
                                object4 = G__236593;
                                G__236593 = null;
                            }
                        }
                    }
                    Object s = object4;
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__5;
                    Object object10 = s;
                    s = null;
                    objectArray[1] = object10;
                    IPersistentMap m = RT.mapUniqueKeys((Object[])objectArray);
                    this.endpoint = null;
                    ((IFn)const__6.getRawRoot()).invoke(const__2.getRawRoot(), const__7.getRawRoot(), this.endpoint, (Object)m);
                    object2 = m;
                    Object var3_3 = null;
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

