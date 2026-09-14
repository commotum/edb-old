/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.client.ClientSessionFactory
 *  org.apache.activemq.artemis.api.core.client.ServerLocator
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.artemis_client$create_session_factory$fn__20818;
import datomic.artemis_client$create_session_factory$fn__20822;
import datomic.artemis_client$create_session_factory$fn__20826;
import datomic.artemis_client$create_session_factory$fn__20828;
import datomic.artemis_client.SessionFactoryBundle;
import org.apache.activemq.artemis.api.core.client.ClientSessionFactory;
import org.apache.activemq.artemis.api.core.client.ServerLocator;

public final class artemis_client$create_session_factory
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.artemis-client", (String)"create-server-locator");
    public static final Var const__1 = RT.var((String)"datomic.error", (String)"runonce");

    public static Object invokeStatic(Object connector2, Object opts) {
        SessionFactoryBundle sessionFactoryBundle;
        Object object = connector2;
        connector2 = null;
        Object object2 = opts;
        opts = null;
        Object loc = ((IFn)const__0.getRawRoot()).invoke(object, object2);
        Object cleanup2 = ((IFn)const__1.getRawRoot()).invoke((Object)new artemis_client$create_session_factory$fn__20818(loc));
        try {
            SessionFactoryBundle sessionFactoryBundle2;
            ClientSessionFactory session_factory = ((ServerLocator)loc).createSessionFactory();
            Object object3 = cleanup2;
            cleanup2 = null;
            Object cleanup3 = ((IFn)const__1.getRawRoot()).invoke((Object)new artemis_client$create_session_factory$fn__20822(session_factory, object3));
            try {
                Object object4 = cleanup3;
                cleanup3 = null;
                sessionFactoryBundle2 = new SessionFactoryBundle(loc, session_factory, object4);
            }
            catch (Throwable t__709__auto__2) {
                ClientSessionFactory clientSessionFactory = session_factory;
                session_factory = null;
                ((IFn)new artemis_client$create_session_factory$fn__20826(clientSessionFactory)).invoke();
                Object t__709__auto__2 = null;
                throw t__709__auto__2;
            }
            sessionFactoryBundle = sessionFactoryBundle2;
        }
        catch (Throwable t__709__auto__3) {
            Object object5 = loc;
            loc = null;
            ((IFn)new artemis_client$create_session_factory$fn__20828(object5)).invoke();
            Object t__709__auto__3 = null;
            throw t__709__auto__3;
        }
        return sessionFactoryBundle;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return artemis_client$create_session_factory.invokeStatic(object3, object4);
    }
}

