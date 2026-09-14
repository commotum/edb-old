/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import datomic.common$schedule$fn__9151;
import datomic.common$schedule$reify__9153;
import datomic.common.proxy$java.util.TimerTask$ff19274a;
import java.util.Timer;
import java.util.TimerTask;

public final class common$schedule
extends RestFn {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"once");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"init-proxy");
    public static final AFn const__10 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 484, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic(Object taskname, Object f, Object msec, ISeq p__9149) {
        ISeq map__9150;
        ISeq iSeq;
        ISeq iSeq2 = p__9149;
        p__9149 = null;
        ISeq map__91502 = iSeq2;
        Object object = ((IFn)const__0.getRawRoot()).invoke((Object)map__91502);
        if (object != null && object != Boolean.FALSE) {
            ISeq iSeq3 = map__91502;
            map__91502 = null;
            iSeq = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke((Object)iSeq3)));
        } else {
            iSeq = map__91502;
            map__91502 = null;
        }
        ISeq iSeq4 = map__9150 = iSeq;
        map__9150 = null;
        Object once = RT.get((Object)iSeq4, (Object)const__3);
        Object object2 = msec;
        msec = null;
        long msec2 = RT.longCast((Object)object2);
        Object object3 = taskname;
        taskname = null;
        Timer t = new Timer((String)object3, Boolean.TRUE);
        TimerTask$ff19274a p__6882__auto__9156 = new TimerTask$ff19274a();
        Object[] objectArray = new Object[2];
        objectArray[0] = "run";
        Object object4 = f;
        f = null;
        objectArray[1] = new common$schedule$fn__9151(object4);
        ((IFn)const__5.getRawRoot()).invoke((Object)p__6882__auto__9156, (Object)RT.mapUniqueKeys((Object[])objectArray));
        TimerTask$ff19274a timerTask$ff19274a = p__6882__auto__9156;
        p__6882__auto__9156 = null;
        TimerTask$ff19274a tt = timerTask$ff19274a;
        Object object5 = once;
        once = null;
        if (object5 != null && object5 != Boolean.FALSE) {
            TimerTask$ff19274a timerTask$ff19274a2 = tt;
            tt = null;
            t.schedule((TimerTask)timerTask$ff19274a2, msec2);
        } else {
            TimerTask$ff19274a timerTask$ff19274a3 = tt;
            tt = null;
            t.schedule((TimerTask)timerTask$ff19274a3, msec2, msec2);
        }
        Timer timer = t;
        t = null;
        return ((IObj)new common$schedule$reify__9153(null, timer)).withMeta((IPersistentMap)const__10);
    }

    public Object doInvoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        ISeq iSeq = (ISeq)object4;
        object4 = null;
        return common$schedule.invokeStatic(object5, object6, object7, iSeq);
    }

    public int getRequiredArity() {
        return 3;
    }
}

