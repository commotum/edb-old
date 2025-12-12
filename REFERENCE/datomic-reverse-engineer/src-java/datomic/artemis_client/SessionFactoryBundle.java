/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.ISeq
 *  clojure.lang.IType
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.client.ClientSession
 *  org.apache.activemq.artemis.api.core.client.ClientSessionFactory
 *  org.apache.activemq.artemis.api.core.client.SessionFailureListener
 */
package datomic.artemis_client;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.ISeq;
import clojure.lang.IType;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.artemis_client.HornetImpl;
import datomic.artemis_client.SessionFactoryBundle$fn__20808;
import datomic.common.AsyncShutdown;
import org.apache.activemq.artemis.api.core.client.ClientSession;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.SessionFailureListener;

public final class SessionFactoryBundle
implements HornetImpl,
AsyncShutdown,
IType {
    public final Object locator;
    public final Object factory;
    public final Object cleanup;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"xa");
    public static final Keyword const__4 = RT.keyword(null, (String)"auto-commit-sends");
    public static final Keyword const__5 = RT.keyword(null, (String)"auto-commit-acks");
    public static final Keyword const__6 = RT.keyword(null, (String)"pre-acknowledge");
    public static final Keyword const__7 = RT.keyword(null, (String)"ack-batch-size");
    public static final Object const__8 = 1L;
    public static final Keyword const__9 = RT.keyword(null, (String)"on-failure");
    public static final Var const__10 = RT.var((String)"datomic.common", (String)"require-keys");
    public static final Keyword const__11 = RT.keyword(null, (String)"username");
    public static final Keyword const__12 = RT.keyword(null, (String)"password");
    public static final AFn const__13 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"username"), (Object)RT.keyword(null, (String)"password"));
    public static final Var const__14 = RT.var((String)"datomic.artemis-client", (String)"wrap-as-failure-listener");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"future-call");

    public SessionFactoryBundle(Object object, Object object2, Object object3) {
        this.locator = object;
        this.factory = object2;
        this.cleanup = object3;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"locator")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ServerLocator")})), (Object)((IObj)Symbol.intern(null, (String)"factory")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"ClientSessionFactory")})), (Object)Symbol.intern(null, (String)"cleanup"));
    }

    public Object async_shutdown() {
        SessionFactoryBundle this_ = null;
        return ((IFn)const__15.getRawRoot()).invoke((Object)new SessionFactoryBundle$fn__20808(this_.cleanup));
    }

    public Object start_session_STAR_(Object creds, Object p__20804) {
        Object object;
        Object object2;
        Object object3 = p__20804;
        p__20804 = null;
        Object map__20806 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__20806);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__20806;
            map__20806 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__20806;
            map__20806 = null;
        }
        Object map__208062 = object2;
        Object xa = RT.get((Object)map__208062, (Object)const__3, (Object)Boolean.FALSE);
        Object auto_commit_sends = RT.get((Object)map__208062, (Object)const__4, (Object)Boolean.TRUE);
        Object auto_commit_acks = RT.get((Object)map__208062, (Object)const__5, (Object)Boolean.TRUE);
        Object pre_acknowledge = RT.get((Object)map__208062, (Object)const__6, (Object)Boolean.FALSE);
        Object ack_batch_size = RT.get((Object)map__208062, (Object)const__7, (Object)const__8);
        Object object6 = map__208062;
        map__208062 = null;
        Object on_failure = RT.get((Object)object6, (Object)const__9);
        Object object7 = creds;
        creds = null;
        Object map__20807 = ((IFn)const__10.getRawRoot()).invoke(object7, (Object)const__13);
        Object object8 = ((IFn)const__0.getRawRoot()).invoke(map__20807);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = map__20807;
            map__20807 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object9)));
        } else {
            object = map__20807;
            map__20807 = null;
        }
        Object map__208072 = object;
        Object username = RT.get((Object)map__208072, (Object)const__11);
        Object object10 = map__208072;
        map__208072 = null;
        Object password = RT.get((Object)object10, (Object)const__12);
        Object object11 = username;
        username = null;
        Object object12 = password;
        password = null;
        Object object13 = xa;
        xa = null;
        Object object14 = auto_commit_sends;
        auto_commit_sends = null;
        Object object15 = auto_commit_acks;
        auto_commit_acks = null;
        Object object16 = pre_acknowledge;
        pre_acknowledge = null;
        Object object17 = ack_batch_size;
        ack_batch_size = null;
        ClientSession session = ((ClientSessionFactory)this.factory).createSession((String)object11, (String)object12, ((Boolean)object13).booleanValue(), ((Boolean)object14).booleanValue(), ((Boolean)object15).booleanValue(), ((Boolean)object16).booleanValue(), RT.intCast((Object)((Number)object17)));
        Object object18 = on_failure;
        if (object18 != null && object18 != Boolean.FALSE) {
            Object object19 = on_failure;
            on_failure = null;
            session.addFailureListener((SessionFailureListener)((IFn)const__14.getRawRoot()).invoke(object19));
        }
        session.start();
        ClientSession clientSession = session;
        session = null;
        return clientSession;
    }
}

