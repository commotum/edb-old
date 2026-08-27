/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  datomic.spy.memcached.ConnectionFactoryBuilder
 *  datomic.spy.memcached.ConnectionFactoryBuilder$Locator
 *  datomic.spy.memcached.ConnectionFactoryBuilder$Protocol
 *  datomic.spy.memcached.DefaultHashAlgorithm
 *  datomic.spy.memcached.FailureMode
 *  datomic.spy.memcached.HashAlgorithm
 *  datomic.spy.memcached.auth.AuthDescriptor
 *  datomic.spy.memcached.auth.PlainCallbackHandler
 *  datomic.spy.memcached.transcoders.Transcoder
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import datomic.spy.memcached.ConnectionFactoryBuilder;
import datomic.spy.memcached.DefaultHashAlgorithm;
import datomic.spy.memcached.FailureMode;
import datomic.spy.memcached.HashAlgorithm;
import datomic.spy.memcached.auth.AuthDescriptor;
import datomic.spy.memcached.auth.PlainCallbackHandler;
import datomic.spy.memcached.transcoders.Transcoder;
import javax.security.auth.callback.CallbackHandler;

public final class memcached$factory_STAR_
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"timeout-msec");
    public static final Object const__4 = 10L;
    public static final Keyword const__5 = RT.keyword(null, (String)"config-timeout-msec");
    public static final Object const__6 = 100L;
    public static final Keyword const__7 = RT.keyword(null, (String)"username");
    public static final Keyword const__8 = RT.keyword(null, (String)"password");
    public static final Keyword const__9 = RT.keyword(null, (String)"auto-discovery");
    public static final Var const__10 = RT.var((String)"datomic.memcached", (String)"configure-auto-discovery");
    public static final Var const__11 = RT.var((String)"datomic.memcached", (String)"legacy-transcoder");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"into-array");
    public static final Object const__13 = RT.classForName((String)"java.lang.String");
    public static final AFn const__14 = (AFn)Tuple.create((Object)"PLAIN");

    public static Object invokeStatic(Object p__9952) {
        Object object;
        Object and__5236__auto__9955;
        Object object2;
        Object object3 = p__9952;
        p__9952 = null;
        Object map__9953 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__9953);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__9953;
            map__9953 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__9953;
            map__9953 = null;
        }
        Object map__99532 = object2;
        Object timeout_msec = RT.get((Object)map__99532, (Object)const__3, (Object)const__4);
        Object config_timeout_msec = RT.get((Object)map__99532, (Object)const__5, (Object)const__6);
        Object username = RT.get((Object)map__99532, (Object)const__7);
        Object password = RT.get((Object)map__99532, (Object)const__8);
        Object object6 = map__99532;
        map__99532 = null;
        Object auto_discovery = RT.get((Object)object6, (Object)const__9);
        Object object7 = timeout_msec;
        timeout_msec = null;
        Object object8 = auto_discovery;
        auto_discovery = null;
        Object object9 = config_timeout_msec;
        config_timeout_msec = null;
        Object fact = ((IFn)const__10.getRawRoot()).invoke((Object)new ConnectionFactoryBuilder().setProtocol(ConnectionFactoryBuilder.Protocol.BINARY).setLocatorType(ConnectionFactoryBuilder.Locator.CONSISTENT).setFailureMode(FailureMode.Redistribute).setHashAlg((HashAlgorithm)DefaultHashAlgorithm.KETAMA_HASH).setOpTimeout(RT.longCast((Object)((Number)timeout_msec))).setOpQueueMaxBlockTime(RT.longCast((Object)((Number)timeout_msec))).setAuthWaitTime(RT.longCast((Object)((Number)object7))).setTranscoder((Transcoder)const__11.getRawRoot()), object8, object9);
        Object object10 = and__5236__auto__9955 = username;
        if (object10 != null && object10 != Boolean.FALSE) {
            object = password;
        } else {
            object = and__5236__auto__9955;
            and__5236__auto__9955 = null;
        }
        if (object != null && object != Boolean.FALSE) {
            Object object11 = username;
            username = null;
            Object object12 = password;
            password = null;
            ((ConnectionFactoryBuilder)fact).setAuthDescriptor(new AuthDescriptor((String[])((IFn)const__12.getRawRoot()).invoke(const__13, (Object)const__14), (CallbackHandler)new PlainCallbackHandler((String)object11, (String)object12)));
        }
        Object object13 = fact;
        fact = null;
        return ((ConnectionFactoryBuilder)object13).build();
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return memcached$factory_STAR_.invokeStatic(object2);
    }
}

