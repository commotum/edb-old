/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 *  org.apache.activemq.artemis.api.core.client.ClientSession
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import org.apache.activemq.artemis.api.core.client.ClientSession;

public final class artemis_client$create_consumer
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"window-size");
    public static final Keyword const__7 = RT.keyword(null, (String)"max-rate");
    public static final Object const__8 = -1L;
    public static final Keyword const__9 = RT.keyword(null, (String)"browse");

    public static Object invokeStatic(Object session, Object queue_name, ISeq p__20844) {
        ISeq iSeq;
        ISeq iSeq2 = p__20844;
        p__20844 = null;
        ISeq map__20845 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__20845);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__20845;
            map__20845 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__20845;
            map__20845 = null;
        }
        ISeq map__208452 = iSeq;
        Object window_size = RT.get((Object)map__208452, (Object)const__3, (Object)Numbers.num((long)Numbers.multiply((long)1024L, (long)1024L)));
        Object max_rate = RT.get((Object)map__208452, (Object)const__7, (Object)const__8);
        ISeq iSeq4 = map__208452;
        map__208452 = null;
        RT.get((Object)iSeq4, (Object)const__9, (Object)Boolean.FALSE);
        Object object2 = session;
        session = null;
        Object object3 = queue_name;
        queue_name = null;
        Object object4 = window_size;
        window_size = null;
        Object object5 = max_rate;
        max_rate = null;
        return ((ClientSession)object2).createConsumer((String)object3, (String)null, RT.intCast((Object)object4), RT.intCast((Object)object5), RT.booleanCast((Object)Boolean.FALSE));
    }

    public Object doInvoke(Object object, Object object2, Object object3) {
        Object object4 = object;
        object = null;
        Object object5 = object2;
        object2 = null;
        ISeq iSeq = (ISeq)object3;
        object3 = null;
        return artemis_client$create_consumer.invokeStatic(object4, object5, iSeq);
    }

    public int getRequiredArity() {
        return 2;
    }
}

