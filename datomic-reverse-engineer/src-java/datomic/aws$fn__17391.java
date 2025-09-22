/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ILookupThunk
 *  clojure.lang.Keyword
 *  clojure.lang.KeywordLookupSite
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Var
 *  com.amazonaws.ClientConfiguration
 *  com.amazonaws.DnsResolver
 *  com.amazonaws.Protocol
 *  com.amazonaws.http.TlsKeyManagersProvider
 *  com.amazonaws.retry.RetryMode
 *  com.amazonaws.retry.RetryPolicy
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ILookupThunk;
import clojure.lang.Keyword;
import clojure.lang.KeywordLookupSite;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Var;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.DnsResolver;
import com.amazonaws.Protocol;
import com.amazonaws.http.TlsKeyManagersProvider;
import com.amazonaws.retry.RetryMode;
import com.amazonaws.retry.RetryPolicy;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.List;

public final class aws$fn__17391
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"remove");
    public static final Keyword const__2 = RT.keyword(null, (String)"proxyAuthenticationMethods");
    public static final Keyword const__3 = RT.keyword(null, (String)"localAddress");
    public static final Keyword const__4 = RT.keyword(null, (String)"responseMetadataCacheSize");
    public static final Keyword const__5 = RT.keyword(null, (String)"validateAfterInactivityMillis");
    public static final Keyword const__6 = RT.keyword(null, (String)"protocol");
    public static final Keyword const__7 = RT.keyword(null, (String)"socketTimeout");
    public static final Keyword const__8 = RT.keyword(null, (String)"useTcpKeepAlive");
    public static final Keyword const__9 = RT.keyword(null, (String)"proxyWorkstation");
    public static final Keyword const__10 = RT.keyword(null, (String)"userAgentSuffix");
    public static final Keyword const__11 = RT.keyword(null, (String)"connectionTTL");
    public static final Keyword const__12 = RT.keyword(null, (String)"retryPolicy");
    public static final Keyword const__13 = RT.keyword(null, (String)"maxErrorRetry");
    public static final Keyword const__14 = RT.keyword(null, (String)"maxConnections");
    public static final Keyword const__15 = RT.keyword(null, (String)"disableHostPrefixInjection");
    public static final Keyword const__16 = RT.keyword(null, (String)"secureRandom");
    public static final Keyword const__17 = RT.keyword(null, (String)"retryMode");
    public static final Keyword const__18 = RT.keyword(null, (String)"disableSocketProxy");
    public static final Keyword const__19 = RT.keyword(null, (String)"tlsKeyManagersProvider");
    public static final Keyword const__20 = RT.keyword(null, (String)"connectionMaxIdleMillis");
    public static final Keyword const__21 = RT.keyword(null, (String)"connectionTimeout");
    public static final Keyword const__22 = RT.keyword(null, (String)"useGzip");
    public static final Keyword const__23 = RT.keyword(null, (String)"requestTimeout");
    public static final Keyword const__24 = RT.keyword(null, (String)"cacheResponseMetadata");
    public static final Keyword const__25 = RT.keyword(null, (String)"useReaper");
    public static final Keyword const__26 = RT.keyword(null, (String)"userAgent");
    public static final Keyword const__27 = RT.keyword(null, (String)"clientExecutionTimeout");
    public static final Keyword const__28 = RT.keyword(null, (String)"nonProxyHosts");
    public static final Keyword const__29 = RT.keyword(null, (String)"maxConsecutiveRetriesBeforeThrottling");
    public static final Keyword const__30 = RT.keyword(null, (String)"signerOverride");
    public static final Keyword const__31 = RT.keyword(null, (String)"useThrottleRetries");
    public static final Keyword const__32 = RT.keyword(null, (String)"proxyProtocol");
    public static final Keyword const__33 = RT.keyword(null, (String)"proxyDomain");
    public static final Keyword const__34 = RT.keyword(null, (String)"preemptiveBasicProxyAuth");
    public static final Keyword const__35 = RT.keyword(null, (String)"userAgentPrefix");
    public static final Keyword const__36 = RT.keyword(null, (String)"proxyUsername");
    public static final Keyword const__37 = RT.keyword(null, (String)"proxyHost");
    public static final Keyword const__38 = RT.keyword(null, (String)"useExpectContinue");
    public static final Keyword const__39 = RT.keyword(null, (String)"proxyPassword");
    public static final Keyword const__40 = RT.keyword(null, (String)"proxyPort");
    public static final Keyword const__41 = RT.keyword(null, (String)"dnsResolver");
    public static final AFn const__42 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"proxyAuthenticationMethods"), RT.keyword(null, (String)"localAddress"), RT.keyword(null, (String)"responseMetadataCacheSize"), RT.keyword(null, (String)"validateAfterInactivityMillis"), RT.keyword(null, (String)"protocol"), RT.keyword(null, (String)"socketTimeout"), RT.keyword(null, (String)"useTcpKeepAlive"), RT.keyword(null, (String)"proxyWorkstation"), RT.keyword(null, (String)"userAgentSuffix"), RT.keyword(null, (String)"connectionTTL"), RT.keyword(null, (String)"retryPolicy"), RT.keyword(null, (String)"maxErrorRetry"), RT.keyword(null, (String)"maxConnections"), RT.keyword(null, (String)"disableHostPrefixInjection"), RT.keyword(null, (String)"secureRandom"), RT.keyword(null, (String)"retryMode"), RT.keyword(null, (String)"disableSocketProxy"), RT.keyword(null, (String)"tlsKeyManagersProvider"), RT.keyword(null, (String)"connectionMaxIdleMillis"), RT.keyword(null, (String)"connectionTimeout"), RT.keyword(null, (String)"useGzip"), RT.keyword(null, (String)"requestTimeout"), RT.keyword(null, (String)"cacheResponseMetadata"), RT.keyword(null, (String)"useReaper"), RT.keyword(null, (String)"userAgent"), RT.keyword(null, (String)"clientExecutionTimeout"), RT.keyword(null, (String)"nonProxyHosts"), RT.keyword(null, (String)"maxConsecutiveRetriesBeforeThrottling"), RT.keyword(null, (String)"signerOverride"), RT.keyword(null, (String)"useThrottleRetries"), RT.keyword(null, (String)"proxyProtocol"), RT.keyword(null, (String)"proxyDomain"), RT.keyword(null, (String)"preemptiveBasicProxyAuth"), RT.keyword(null, (String)"userAgentPrefix"), RT.keyword(null, (String)"proxyUsername"), RT.keyword(null, (String)"proxyHost"), RT.keyword(null, (String)"useExpectContinue"), RT.keyword(null, (String)"proxyPassword"), RT.keyword(null, (String)"proxyPort"), RT.keyword(null, (String)"dnsResolver")});
    public static final Var const__43 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__44 = RT.var((String)"clojure.core", (String)"ex-info");
    public static final Var const__45 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__46 = RT.var((String)"clojure.core", (String)"str");
    public static final Keyword const__47 = RT.keyword(null, (String)"legal-keys");
    public static final AFn const__48 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"proxyAuthenticationMethods"), RT.keyword(null, (String)"localAddress"), RT.keyword(null, (String)"responseMetadataCacheSize"), RT.keyword(null, (String)"validateAfterInactivityMillis"), RT.keyword(null, (String)"protocol"), RT.keyword(null, (String)"socketTimeout"), RT.keyword(null, (String)"useTcpKeepAlive"), RT.keyword(null, (String)"proxyWorkstation"), RT.keyword(null, (String)"userAgentSuffix"), RT.keyword(null, (String)"connectionTTL"), RT.keyword(null, (String)"retryPolicy"), RT.keyword(null, (String)"maxErrorRetry"), RT.keyword(null, (String)"maxConnections"), RT.keyword(null, (String)"disableHostPrefixInjection"), RT.keyword(null, (String)"secureRandom"), RT.keyword(null, (String)"retryMode"), RT.keyword(null, (String)"disableSocketProxy"), RT.keyword(null, (String)"tlsKeyManagersProvider"), RT.keyword(null, (String)"connectionMaxIdleMillis"), RT.keyword(null, (String)"connectionTimeout"), RT.keyword(null, (String)"useGzip"), RT.keyword(null, (String)"requestTimeout"), RT.keyword(null, (String)"cacheResponseMetadata"), RT.keyword(null, (String)"useReaper"), RT.keyword(null, (String)"userAgent"), RT.keyword(null, (String)"clientExecutionTimeout"), RT.keyword(null, (String)"nonProxyHosts"), RT.keyword(null, (String)"maxConsecutiveRetriesBeforeThrottling"), RT.keyword(null, (String)"signerOverride"), RT.keyword(null, (String)"useThrottleRetries"), RT.keyword(null, (String)"proxyProtocol"), RT.keyword(null, (String)"proxyDomain"), RT.keyword(null, (String)"preemptiveBasicProxyAuth"), RT.keyword(null, (String)"userAgentPrefix"), RT.keyword(null, (String)"proxyUsername"), RT.keyword(null, (String)"proxyHost"), RT.keyword(null, (String)"useExpectContinue"), RT.keyword(null, (String)"proxyPassword"), RT.keyword(null, (String)"proxyPort"), RT.keyword(null, (String)"dnsResolver")});
    public static final Keyword const__49 = RT.keyword(null, (String)"keys");
    public static final Keyword const__50 = RT.keyword(null, (String)"constructor");
    public static final Object const__51 = RT.classForName((String)"com.amazonaws.ClientConfiguration");
    public static final Var const__52 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__53 = RT.var((String)"datomic.datafy", (String)"property-to-object");
    public static final Var const__54 = RT.var((String)"clojure.core", (String)"class");
    public static final Object const__55 = RT.classForName((String)"java.lang.String");
    public static final Object const__56 = Integer.TYPE;
    public static final Object const__57 = Boolean.TYPE;
    public static final Object const__58 = Long.TYPE;
    public static final Object const__59 = RT.classForName((String)"com.amazonaws.retry.RetryPolicy");
    public static final Object const__60 = RT.classForName((String)"java.util.List");
    public static final Object const__61 = RT.classForName((String)"com.amazonaws.http.TlsKeyManagersProvider");
    public static final Object const__62 = RT.classForName((String)"java.security.SecureRandom");
    public static final Object const__63 = RT.classForName((String)"java.lang.Boolean");
    public static final Object const__64 = RT.classForName((String)"com.amazonaws.Protocol");
    public static final Object const__65 = RT.classForName((String)"java.net.InetAddress");
    public static final Object const__66 = RT.classForName((String)"com.amazonaws.retry.RetryMode");
    public static final Object const__67 = RT.classForName((String)"com.amazonaws.DnsResolver");
    static final KeywordLookupSite __site__0__ = new KeywordLookupSite(RT.keyword(null, (String)"signerOverride"));
    static ILookupThunk __thunk__0__ = __site__0__;
    static final KeywordLookupSite __site__1__ = new KeywordLookupSite(RT.keyword(null, (String)"maxConnections"));
    static ILookupThunk __thunk__1__ = __site__1__;
    static final KeywordLookupSite __site__2__ = new KeywordLookupSite(RT.keyword(null, (String)"socketTimeout"));
    static ILookupThunk __thunk__2__ = __site__2__;
    static final KeywordLookupSite __site__3__ = new KeywordLookupSite(RT.keyword(null, (String)"responseMetadataCacheSize"));
    static ILookupThunk __thunk__3__ = __site__3__;
    static final KeywordLookupSite __site__4__ = new KeywordLookupSite(RT.keyword(null, (String)"useReaper"));
    static ILookupThunk __thunk__4__ = __site__4__;
    static final KeywordLookupSite __site__5__ = new KeywordLookupSite(RT.keyword(null, (String)"connectionMaxIdleMillis"));
    static ILookupThunk __thunk__5__ = __site__5__;
    static final KeywordLookupSite __site__6__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyDomain"));
    static ILookupThunk __thunk__6__ = __site__6__;
    static final KeywordLookupSite __site__7__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyWorkstation"));
    static ILookupThunk __thunk__7__ = __site__7__;
    static final KeywordLookupSite __site__8__ = new KeywordLookupSite(RT.keyword(null, (String)"validateAfterInactivityMillis"));
    static ILookupThunk __thunk__8__ = __site__8__;
    static final KeywordLookupSite __site__9__ = new KeywordLookupSite(RT.keyword(null, (String)"maxConsecutiveRetriesBeforeThrottling"));
    static ILookupThunk __thunk__9__ = __site__9__;
    static final KeywordLookupSite __site__10__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyUsername"));
    static ILookupThunk __thunk__10__ = __site__10__;
    static final KeywordLookupSite __site__11__ = new KeywordLookupSite(RT.keyword(null, (String)"useTcpKeepAlive"));
    static ILookupThunk __thunk__11__ = __site__11__;
    static final KeywordLookupSite __site__12__ = new KeywordLookupSite(RT.keyword(null, (String)"userAgentPrefix"));
    static ILookupThunk __thunk__12__ = __site__12__;
    static final KeywordLookupSite __site__13__ = new KeywordLookupSite(RT.keyword(null, (String)"cacheResponseMetadata"));
    static ILookupThunk __thunk__13__ = __site__13__;
    static final KeywordLookupSite __site__14__ = new KeywordLookupSite(RT.keyword(null, (String)"retryPolicy"));
    static ILookupThunk __thunk__14__ = __site__14__;
    static final KeywordLookupSite __site__15__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyPassword"));
    static ILookupThunk __thunk__15__ = __site__15__;
    static final KeywordLookupSite __site__16__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyAuthenticationMethods"));
    static ILookupThunk __thunk__16__ = __site__16__;
    static final KeywordLookupSite __site__17__ = new KeywordLookupSite(RT.keyword(null, (String)"connectionTimeout"));
    static ILookupThunk __thunk__17__ = __site__17__;
    static final KeywordLookupSite __site__18__ = new KeywordLookupSite(RT.keyword(null, (String)"tlsKeyManagersProvider"));
    static ILookupThunk __thunk__18__ = __site__18__;
    static final KeywordLookupSite __site__19__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyPort"));
    static ILookupThunk __thunk__19__ = __site__19__;
    static final KeywordLookupSite __site__20__ = new KeywordLookupSite(RT.keyword(null, (String)"disableHostPrefixInjection"));
    static ILookupThunk __thunk__20__ = __site__20__;
    static final KeywordLookupSite __site__21__ = new KeywordLookupSite(RT.keyword(null, (String)"userAgent"));
    static ILookupThunk __thunk__21__ = __site__21__;
    static final KeywordLookupSite __site__22__ = new KeywordLookupSite(RT.keyword(null, (String)"nonProxyHosts"));
    static ILookupThunk __thunk__22__ = __site__22__;
    static final KeywordLookupSite __site__23__ = new KeywordLookupSite(RT.keyword(null, (String)"useThrottleRetries"));
    static ILookupThunk __thunk__23__ = __site__23__;
    static final KeywordLookupSite __site__24__ = new KeywordLookupSite(RT.keyword(null, (String)"requestTimeout"));
    static ILookupThunk __thunk__24__ = __site__24__;
    static final KeywordLookupSite __site__25__ = new KeywordLookupSite(RT.keyword(null, (String)"secureRandom"));
    static ILookupThunk __thunk__25__ = __site__25__;
    static final KeywordLookupSite __site__26__ = new KeywordLookupSite(RT.keyword(null, (String)"userAgentSuffix"));
    static ILookupThunk __thunk__26__ = __site__26__;
    static final KeywordLookupSite __site__27__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyHost"));
    static ILookupThunk __thunk__27__ = __site__27__;
    static final KeywordLookupSite __site__28__ = new KeywordLookupSite(RT.keyword(null, (String)"maxErrorRetry"));
    static ILookupThunk __thunk__28__ = __site__28__;
    static final KeywordLookupSite __site__29__ = new KeywordLookupSite(RT.keyword(null, (String)"disableSocketProxy"));
    static ILookupThunk __thunk__29__ = __site__29__;
    static final KeywordLookupSite __site__30__ = new KeywordLookupSite(RT.keyword(null, (String)"preemptiveBasicProxyAuth"));
    static ILookupThunk __thunk__30__ = __site__30__;
    static final KeywordLookupSite __site__31__ = new KeywordLookupSite(RT.keyword(null, (String)"clientExecutionTimeout"));
    static ILookupThunk __thunk__31__ = __site__31__;
    static final KeywordLookupSite __site__32__ = new KeywordLookupSite(RT.keyword(null, (String)"useExpectContinue"));
    static ILookupThunk __thunk__32__ = __site__32__;
    static final KeywordLookupSite __site__33__ = new KeywordLookupSite(RT.keyword(null, (String)"protocol"));
    static ILookupThunk __thunk__33__ = __site__33__;
    static final KeywordLookupSite __site__34__ = new KeywordLookupSite(RT.keyword(null, (String)"localAddress"));
    static ILookupThunk __thunk__34__ = __site__34__;
    static final KeywordLookupSite __site__35__ = new KeywordLookupSite(RT.keyword(null, (String)"retryMode"));
    static ILookupThunk __thunk__35__ = __site__35__;
    static final KeywordLookupSite __site__36__ = new KeywordLookupSite(RT.keyword(null, (String)"dnsResolver"));
    static ILookupThunk __thunk__36__ = __site__36__;
    static final KeywordLookupSite __site__37__ = new KeywordLookupSite(RT.keyword(null, (String)"proxyProtocol"));
    static ILookupThunk __thunk__37__ = __site__37__;
    static final KeywordLookupSite __site__38__ = new KeywordLookupSite(RT.keyword(null, (String)"useGzip"));
    static ILookupThunk __thunk__38__ = __site__38__;
    static final KeywordLookupSite __site__39__ = new KeywordLookupSite(RT.keyword(null, (String)"connectionTTL"));
    static ILookupThunk __thunk__39__ = __site__39__;

    public static Object invokeStatic(Object m, Object _) {
        Object k;
        Object v;
        Object temp__5457__auto__17393;
        Object object = temp__5457__auto__17393 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke((Object)const__42, ((IFn)const__43.getRawRoot()).invoke(m)));
        if (object != null && object != Boolean.FALSE) {
            Object object2 = temp__5457__auto__17393;
            temp__5457__auto__17393 = null;
            Object bad_ks = object2;
            Object object3 = ((IFn)const__45.getRawRoot()).invoke(const__46.getRawRoot(), (Object)"Unexpected keys ", bad_ks);
            Object[] objectArray = new Object[6];
            objectArray[0] = const__47;
            objectArray[1] = const__48;
            objectArray[2] = const__49;
            Object object4 = bad_ks;
            bad_ks = null;
            objectArray[3] = object4;
            objectArray[4] = const__50;
            objectArray[5] = const__51;
            throw (Throwable)((IFn)const__44.getRawRoot()).invoke(object3, (Object)RT.mapUniqueKeys((Object[])objectArray));
        }
        ClientConfiguration o = new ClientConfiguration();
        Object object5 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__30);
        if (object5 != null && object5 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__0__;
            Object object6 = m;
            Object object7 = iLookupThunk.get(object6);
            if (iLookupThunk == object7) {
                __thunk__0__ = __site__0__.fault(object6);
                object7 = __thunk__0__.get(object6);
            }
            Object object8 = v = object7;
            v = null;
            Object object9 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__30, object8, const__55);
            k = null;
            o.setSignerOverride((String)object9);
        }
        Object object10 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__14);
        if (object10 != null && object10 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__1__;
            Object object11 = m;
            Object object12 = iLookupThunk.get(object11);
            if (iLookupThunk == object12) {
                __thunk__1__ = __site__1__.fault(object11);
                object12 = __thunk__1__.get(object11);
            }
            Object object13 = v = object12;
            v = null;
            Object object14 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__14, object13, const__56);
            k = null;
            o.setMaxConnections(RT.intCast((Object)((Number)object14)));
        }
        Object object15 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__7);
        if (object15 != null && object15 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__2__;
            Object object16 = m;
            Object object17 = iLookupThunk.get(object16);
            if (iLookupThunk == object17) {
                __thunk__2__ = __site__2__.fault(object16);
                object17 = __thunk__2__.get(object16);
            }
            Object object18 = v = object17;
            v = null;
            Object object19 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__7, object18, const__56);
            k = null;
            o.setSocketTimeout(RT.intCast((Object)((Number)object19)));
        }
        Object object20 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__4);
        if (object20 != null && object20 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__3__;
            Object object21 = m;
            Object object22 = iLookupThunk.get(object21);
            if (iLookupThunk == object22) {
                __thunk__3__ = __site__3__.fault(object21);
                object22 = __thunk__3__.get(object21);
            }
            Object object23 = v = object22;
            v = null;
            Object object24 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__4, object23, const__56);
            k = null;
            o.setResponseMetadataCacheSize(RT.intCast((Object)((Number)object24)));
        }
        Object object25 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__25);
        if (object25 != null && object25 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__4__;
            Object object26 = m;
            Object object27 = iLookupThunk.get(object26);
            if (iLookupThunk == object27) {
                __thunk__4__ = __site__4__.fault(object26);
                object27 = __thunk__4__.get(object26);
            }
            Object object28 = v = object27;
            v = null;
            Object object29 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__25, object28, const__57);
            k = null;
            o.setUseReaper(((Boolean)object29).booleanValue());
        }
        Object object30 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__20);
        if (object30 != null && object30 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__5__;
            Object object31 = m;
            Object object32 = iLookupThunk.get(object31);
            if (iLookupThunk == object32) {
                __thunk__5__ = __site__5__.fault(object31);
                object32 = __thunk__5__.get(object31);
            }
            Object object33 = v = object32;
            v = null;
            Object object34 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__20, object33, const__58);
            k = null;
            o.setConnectionMaxIdleMillis(RT.longCast((Object)((Number)object34)));
        }
        Object object35 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__33);
        if (object35 != null && object35 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__6__;
            Object object36 = m;
            Object object37 = iLookupThunk.get(object36);
            if (iLookupThunk == object37) {
                __thunk__6__ = __site__6__.fault(object36);
                object37 = __thunk__6__.get(object36);
            }
            Object object38 = v = object37;
            v = null;
            Object object39 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__33, object38, const__55);
            k = null;
            o.setProxyDomain((String)object39);
        }
        Object object40 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__9);
        if (object40 != null && object40 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__7__;
            Object object41 = m;
            Object object42 = iLookupThunk.get(object41);
            if (iLookupThunk == object42) {
                __thunk__7__ = __site__7__.fault(object41);
                object42 = __thunk__7__.get(object41);
            }
            Object object43 = v = object42;
            v = null;
            Object object44 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__9, object43, const__55);
            k = null;
            o.setProxyWorkstation((String)object44);
        }
        Object object45 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__5);
        if (object45 != null && object45 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__8__;
            Object object46 = m;
            Object object47 = iLookupThunk.get(object46);
            if (iLookupThunk == object47) {
                __thunk__8__ = __site__8__.fault(object46);
                object47 = __thunk__8__.get(object46);
            }
            Object object48 = v = object47;
            v = null;
            Object object49 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__5, object48, const__56);
            k = null;
            o.setValidateAfterInactivityMillis(RT.intCast((Object)((Number)object49)));
        }
        Object object50 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__29);
        if (object50 != null && object50 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__9__;
            Object object51 = m;
            Object object52 = iLookupThunk.get(object51);
            if (iLookupThunk == object52) {
                __thunk__9__ = __site__9__.fault(object51);
                object52 = __thunk__9__.get(object51);
            }
            Object object53 = v = object52;
            v = null;
            Object object54 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__29, object53, const__56);
            k = null;
            o.setMaxConsecutiveRetriesBeforeThrottling(RT.intCast((Object)((Number)object54)));
        }
        Object object55 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__36);
        if (object55 != null && object55 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__10__;
            Object object56 = m;
            Object object57 = iLookupThunk.get(object56);
            if (iLookupThunk == object57) {
                __thunk__10__ = __site__10__.fault(object56);
                object57 = __thunk__10__.get(object56);
            }
            Object object58 = v = object57;
            v = null;
            Object object59 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__36, object58, const__55);
            k = null;
            o.setProxyUsername((String)object59);
        }
        Object object60 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__8);
        if (object60 != null && object60 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__11__;
            Object object61 = m;
            Object object62 = iLookupThunk.get(object61);
            if (iLookupThunk == object62) {
                __thunk__11__ = __site__11__.fault(object61);
                object62 = __thunk__11__.get(object61);
            }
            Object object63 = v = object62;
            v = null;
            Object object64 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__8, object63, const__57);
            k = null;
            o.setUseTcpKeepAlive(((Boolean)object64).booleanValue());
        }
        Object object65 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__35);
        if (object65 != null && object65 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__12__;
            Object object66 = m;
            Object object67 = iLookupThunk.get(object66);
            if (iLookupThunk == object67) {
                __thunk__12__ = __site__12__.fault(object66);
                object67 = __thunk__12__.get(object66);
            }
            Object object68 = v = object67;
            v = null;
            Object object69 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__35, object68, const__55);
            k = null;
            o.setUserAgentPrefix((String)object69);
        }
        Object object70 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__24);
        if (object70 != null && object70 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__13__;
            Object object71 = m;
            Object object72 = iLookupThunk.get(object71);
            if (iLookupThunk == object72) {
                __thunk__13__ = __site__13__.fault(object71);
                object72 = __thunk__13__.get(object71);
            }
            Object object73 = v = object72;
            v = null;
            Object object74 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__24, object73, const__57);
            k = null;
            o.setCacheResponseMetadata(((Boolean)object74).booleanValue());
        }
        Object object75 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__12);
        if (object75 != null && object75 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__14__;
            Object object76 = m;
            Object object77 = iLookupThunk.get(object76);
            if (iLookupThunk == object77) {
                __thunk__14__ = __site__14__.fault(object76);
                object77 = __thunk__14__.get(object76);
            }
            Object object78 = v = object77;
            v = null;
            Object object79 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__12, object78, const__59);
            k = null;
            o.setRetryPolicy((RetryPolicy)object79);
        }
        Object object80 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__39);
        if (object80 != null && object80 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__15__;
            Object object81 = m;
            Object object82 = iLookupThunk.get(object81);
            if (iLookupThunk == object82) {
                __thunk__15__ = __site__15__.fault(object81);
                object82 = __thunk__15__.get(object81);
            }
            Object object83 = v = object82;
            v = null;
            Object object84 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__39, object83, const__55);
            k = null;
            o.setProxyPassword((String)object84);
        }
        Object object85 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__2);
        if (object85 != null && object85 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__16__;
            Object object86 = m;
            Object object87 = iLookupThunk.get(object86);
            if (iLookupThunk == object87) {
                __thunk__16__ = __site__16__.fault(object86);
                object87 = __thunk__16__.get(object86);
            }
            Object object88 = v = object87;
            v = null;
            Object object89 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__2, object88, const__60);
            k = null;
            o.setProxyAuthenticationMethods((List)object89);
        }
        Object object90 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__21);
        if (object90 != null && object90 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__17__;
            Object object91 = m;
            Object object92 = iLookupThunk.get(object91);
            if (iLookupThunk == object92) {
                __thunk__17__ = __site__17__.fault(object91);
                object92 = __thunk__17__.get(object91);
            }
            Object object93 = v = object92;
            v = null;
            Object object94 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__21, object93, const__56);
            k = null;
            o.setConnectionTimeout(RT.intCast((Object)((Number)object94)));
        }
        Object object95 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__19);
        if (object95 != null && object95 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__18__;
            Object object96 = m;
            Object object97 = iLookupThunk.get(object96);
            if (iLookupThunk == object97) {
                __thunk__18__ = __site__18__.fault(object96);
                object97 = __thunk__18__.get(object96);
            }
            Object object98 = v = object97;
            v = null;
            Object object99 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__19, object98, const__61);
            k = null;
            o.setTlsKeyManagersProvider((TlsKeyManagersProvider)object99);
        }
        Object object100 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__40);
        if (object100 != null && object100 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__19__;
            Object object101 = m;
            Object object102 = iLookupThunk.get(object101);
            if (iLookupThunk == object102) {
                __thunk__19__ = __site__19__.fault(object101);
                object102 = __thunk__19__.get(object101);
            }
            Object object103 = v = object102;
            v = null;
            Object object104 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__40, object103, const__56);
            k = null;
            o.setProxyPort(RT.intCast((Object)((Number)object104)));
        }
        Object object105 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__15);
        if (object105 != null && object105 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__20__;
            Object object106 = m;
            Object object107 = iLookupThunk.get(object106);
            if (iLookupThunk == object107) {
                __thunk__20__ = __site__20__.fault(object106);
                object107 = __thunk__20__.get(object106);
            }
            Object object108 = v = object107;
            v = null;
            Object object109 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__15, object108, const__57);
            k = null;
            o.setDisableHostPrefixInjection(((Boolean)object109).booleanValue());
        }
        Object object110 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__26);
        if (object110 != null && object110 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__21__;
            Object object111 = m;
            Object object112 = iLookupThunk.get(object111);
            if (iLookupThunk == object112) {
                __thunk__21__ = __site__21__.fault(object111);
                object112 = __thunk__21__.get(object111);
            }
            Object object113 = v = object112;
            v = null;
            Object object114 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__26, object113, const__55);
            k = null;
            o.setUserAgent((String)object114);
        }
        Object object115 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__28);
        if (object115 != null && object115 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__22__;
            Object object116 = m;
            Object object117 = iLookupThunk.get(object116);
            if (iLookupThunk == object117) {
                __thunk__22__ = __site__22__.fault(object116);
                object117 = __thunk__22__.get(object116);
            }
            Object object118 = v = object117;
            v = null;
            Object object119 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__28, object118, const__55);
            k = null;
            o.setNonProxyHosts((String)object119);
        }
        Object object120 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__31);
        if (object120 != null && object120 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__23__;
            Object object121 = m;
            Object object122 = iLookupThunk.get(object121);
            if (iLookupThunk == object122) {
                __thunk__23__ = __site__23__.fault(object121);
                object122 = __thunk__23__.get(object121);
            }
            Object object123 = v = object122;
            v = null;
            Object object124 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__31, object123, const__57);
            k = null;
            o.setUseThrottleRetries(((Boolean)object124).booleanValue());
        }
        Object object125 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__23);
        if (object125 != null && object125 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__24__;
            Object object126 = m;
            Object object127 = iLookupThunk.get(object126);
            if (iLookupThunk == object127) {
                __thunk__24__ = __site__24__.fault(object126);
                object127 = __thunk__24__.get(object126);
            }
            Object object128 = v = object127;
            v = null;
            Object object129 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__23, object128, const__56);
            k = null;
            o.setRequestTimeout(RT.intCast((Object)((Number)object129)));
        }
        Object object130 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__16);
        if (object130 != null && object130 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__25__;
            Object object131 = m;
            Object object132 = iLookupThunk.get(object131);
            if (iLookupThunk == object132) {
                __thunk__25__ = __site__25__.fault(object131);
                object132 = __thunk__25__.get(object131);
            }
            Object object133 = v = object132;
            v = null;
            Object object134 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__16, object133, const__62);
            k = null;
            o.setSecureRandom((SecureRandom)object134);
        }
        Object object135 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__10);
        if (object135 != null && object135 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__26__;
            Object object136 = m;
            Object object137 = iLookupThunk.get(object136);
            if (iLookupThunk == object137) {
                __thunk__26__ = __site__26__.fault(object136);
                object137 = __thunk__26__.get(object136);
            }
            Object object138 = v = object137;
            v = null;
            Object object139 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__10, object138, const__55);
            k = null;
            o.setUserAgentSuffix((String)object139);
        }
        Object object140 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__37);
        if (object140 != null && object140 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__27__;
            Object object141 = m;
            Object object142 = iLookupThunk.get(object141);
            if (iLookupThunk == object142) {
                __thunk__27__ = __site__27__.fault(object141);
                object142 = __thunk__27__.get(object141);
            }
            Object object143 = v = object142;
            v = null;
            Object object144 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__37, object143, const__55);
            k = null;
            o.setProxyHost((String)object144);
        }
        Object object145 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__13);
        if (object145 != null && object145 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__28__;
            Object object146 = m;
            Object object147 = iLookupThunk.get(object146);
            if (iLookupThunk == object147) {
                __thunk__28__ = __site__28__.fault(object146);
                object147 = __thunk__28__.get(object146);
            }
            Object object148 = v = object147;
            v = null;
            Object object149 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__13, object148, const__56);
            k = null;
            o.setMaxErrorRetry(RT.intCast((Object)((Number)object149)));
        }
        Object object150 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__18);
        if (object150 != null && object150 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__29__;
            Object object151 = m;
            Object object152 = iLookupThunk.get(object151);
            if (iLookupThunk == object152) {
                __thunk__29__ = __site__29__.fault(object151);
                object152 = __thunk__29__.get(object151);
            }
            Object object153 = v = object152;
            v = null;
            Object object154 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__18, object153, const__57);
            k = null;
            o.setDisableSocketProxy(((Boolean)object154).booleanValue());
        }
        Object object155 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__34);
        if (object155 != null && object155 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__30__;
            Object object156 = m;
            Object object157 = iLookupThunk.get(object156);
            if (iLookupThunk == object157) {
                __thunk__30__ = __site__30__.fault(object156);
                object157 = __thunk__30__.get(object156);
            }
            Object object158 = v = object157;
            v = null;
            Object object159 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__34, object158, const__63);
            k = null;
            o.setPreemptiveBasicProxyAuth((Boolean)object159);
        }
        Object object160 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__27);
        if (object160 != null && object160 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__31__;
            Object object161 = m;
            Object object162 = iLookupThunk.get(object161);
            if (iLookupThunk == object162) {
                __thunk__31__ = __site__31__.fault(object161);
                object162 = __thunk__31__.get(object161);
            }
            Object object163 = v = object162;
            v = null;
            Object object164 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__27, object163, const__56);
            k = null;
            o.setClientExecutionTimeout(RT.intCast((Object)((Number)object164)));
        }
        Object object165 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__38);
        if (object165 != null && object165 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__32__;
            Object object166 = m;
            Object object167 = iLookupThunk.get(object166);
            if (iLookupThunk == object167) {
                __thunk__32__ = __site__32__.fault(object166);
                object167 = __thunk__32__.get(object166);
            }
            Object object168 = v = object167;
            v = null;
            Object object169 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__38, object168, const__57);
            k = null;
            o.setUseExpectContinue(((Boolean)object169).booleanValue());
        }
        Object object170 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__6);
        if (object170 != null && object170 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__33__;
            Object object171 = m;
            Object object172 = iLookupThunk.get(object171);
            if (iLookupThunk == object172) {
                __thunk__33__ = __site__33__.fault(object171);
                object172 = __thunk__33__.get(object171);
            }
            Object object173 = v = object172;
            v = null;
            Object object174 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__6, object173, const__64);
            k = null;
            o.setProtocol((Protocol)object174);
        }
        Object object175 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__3);
        if (object175 != null && object175 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__34__;
            Object object176 = m;
            Object object177 = iLookupThunk.get(object176);
            if (iLookupThunk == object177) {
                __thunk__34__ = __site__34__.fault(object176);
                object177 = __thunk__34__.get(object176);
            }
            Object object178 = v = object177;
            v = null;
            Object object179 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__3, object178, const__65);
            k = null;
            o.setLocalAddress((InetAddress)object179);
        }
        Object object180 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__17);
        if (object180 != null && object180 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__35__;
            Object object181 = m;
            Object object182 = iLookupThunk.get(object181);
            if (iLookupThunk == object182) {
                __thunk__35__ = __site__35__.fault(object181);
                object182 = __thunk__35__.get(object181);
            }
            Object object183 = v = object182;
            v = null;
            Object object184 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__17, object183, const__66);
            k = null;
            o.setRetryMode((RetryMode)object184);
        }
        Object object185 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__41);
        if (object185 != null && object185 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__36__;
            Object object186 = m;
            Object object187 = iLookupThunk.get(object186);
            if (iLookupThunk == object187) {
                __thunk__36__ = __site__36__.fault(object186);
                object187 = __thunk__36__.get(object186);
            }
            Object object188 = v = object187;
            v = null;
            Object object189 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__41, object188, const__67);
            k = null;
            o.setDnsResolver((DnsResolver)object189);
        }
        Object object190 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__32);
        if (object190 != null && object190 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__37__;
            Object object191 = m;
            Object object192 = iLookupThunk.get(object191);
            if (iLookupThunk == object192) {
                __thunk__37__ = __site__37__.fault(object191);
                object192 = __thunk__37__.get(object191);
            }
            Object object193 = v = object192;
            v = null;
            Object object194 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__32, object193, const__64);
            k = null;
            o.setProxyProtocol((Protocol)object194);
        }
        Object object195 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__22);
        if (object195 != null && object195 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__38__;
            Object object196 = m;
            Object object197 = iLookupThunk.get(object196);
            if (iLookupThunk == object197) {
                __thunk__38__ = __site__38__.fault(object196);
                object197 = __thunk__38__.get(object196);
            }
            Object object198 = v = object197;
            v = null;
            Object object199 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__22, object198, const__57);
            k = null;
            o.setUseGzip(((Boolean)object199).booleanValue());
        }
        Object object200 = ((IFn)const__52.getRawRoot()).invoke(m, (Object)const__11);
        if (object200 != null && object200 != Boolean.FALSE) {
            ILookupThunk iLookupThunk = __thunk__39__;
            Object object201 = m;
            m = null;
            Object object202 = iLookupThunk.get(object201);
            if (iLookupThunk == object202) {
                __thunk__39__ = __site__39__.fault(object201);
                object202 = __thunk__39__.get(object201);
            }
            Object object203 = v = object202;
            v = null;
            Object object204 = k = ((IFn)const__53.getRawRoot()).invoke(((IFn)const__54.getRawRoot()).invoke((Object)o), (Object)const__11, object203, const__58);
            k = null;
            o.setConnectionTTL(RT.longCast((Object)((Number)object204)));
        }
        Object var2_2 = null;
        return o;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return aws$fn__17391.invokeStatic(object3, object4);
    }
}

