(do
  (clojure.core/in-ns 'datomic.memory-size)
  (clojure.core/with-loading-context
    (do (clojure.core/refer 'clojure.core) (clojure.core/import 'clojure.lang.IFn$OL)))
  (when-not (.equals 'datomic.memory-size 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.memory-size))
    (clojure.core/with-loading-context
      (do (clojure.core/refer 'clojure.core) (clojure.core/import 'clojure.lang.IFn$OL))))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7463 {:column (int 1)}]
    (defprotocol
      MemorySize*
      (memory-size*
        [_]
        "Return best guess of the memory size of an object.\nDo not call directly -- call 'memory-size' fn instead.\nSee also 'extend-memory-size'"))
    (reset-meta!
      (clojure.lang.RT/var "datomic.memory-size" "MemorySize*")
      (assoc (assoc protocol_metadata__7463 :doc nil) :name 'MemorySize* :ns *ns*))
    (let [protocol_signature__7464 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memory-size*
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc
                                      "Return best guess of the memory size of an object.\nDo not call directly -- call 'memory-size' fn instead.\nSee also 'extend-memory-size'"}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.memory-size" "MemorySize*"))
          protocol_method_name__7465 (with-meta
                                       (:name protocol_signature__7464)
                                       protocol_signature__7464)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memory-size" "memory-size*")
        (assoc protocol_signature__7464 :name protocol_method_name__7465 :ns *ns*))))
  (.setMeta
    (clojure.lang.RT/var "datomic.memory-size" "memory-size-handlers")
    {:tag java.util.Map, :private true, :column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.memory-size" "memory-size-handlers")
    (java.util.concurrent.ConcurrentHashMap.))
  (defn register-memory-size-handler!
    ([type f]
      (when-not (instance? clojure.lang.IFn$OL f)
        (throw
          (java.lang.IllegalArgumentException.
            (str type "'s memory size fn must return prim long"))))
      (.put memory-size-handlers type f)))
  (reset-meta!
    #'register-memory-size-handler!
    (assoc
      {:private true, :arglists (clojure.core/list ['type 'f]), :column (int 1)}
      :name
      'register-memory-size-handler!
      :ns
      *ns*))
  (defn memory-size
    (^long [o]
      (if (nil? o)
        0
        (if (instance? java.lang.Long o)
          24
          (let [hdlr (.get memory-size-handlers (.getClass o))]
            (if (nil? hdlr) (long (memory-size* o)) (^clojure.lang.IFn$OL hdlr o)))))))
  (reset-meta!
    #'memory-size
    (assoc
      {:arglists (clojure.core/list (.withMeta ['o] {:tag 'long})), :column (int 1)}
      :name
      'memory-size
      :ns
      *ns*))
  (def REFERENCE 8)
  (reset-meta! #'REFERENCE (assoc {:const true, :column (int 1)} :name 'REFERENCE :ns *ns*))
  (def LONG 8)
  (reset-meta! #'LONG (assoc {:const true, :column (int 1)} :name 'LONG :ns *ns*))
  (def INT 4)
  (reset-meta! #'INT (assoc {:const true, :column (int 1)} :name 'INT :ns *ns*))
  (def CHAR 2)
  (reset-meta! #'CHAR (assoc {:const true, :column (int 1)} :name 'CHAR :ns *ns*))
  (def ARRAY 16)
  (reset-meta! #'ARRAY (assoc {:const true, :column (int 1)} :name 'ARRAY :ns *ns*))
  (def STRING 56)
  (reset-meta! #'STRING (assoc {:const true, :column (int 1)} :name 'STRING :ns *ns*))
  (defn nice-sym
    ([sym]
      (let [s (str sym)]
        (symbol (.replace ^java.lang.String s (char (.charValue \.)) (char (.charValue \_)))))))
  (reset-meta!
    #'nice-sym
    (assoc {:arglists (clojure.core/list ['sym]), :column (int 1)} :name 'nice-sym :ns *ns*))
  (defn extend-memory-size*
    ([& klass+impls]
      (loop [seq_10146 (seq (partition 2 klass+impls)) chunk_10147 nil count_10148 0 i_10149 0]
        (if (< i_10149 count_10148)
          (let [vec__10150 (.nth ^clojure.lang.Indexed chunk_10147 (int i_10149))
                klass (nth vec__10150 (int 0) nil)
                f (nth vec__10150 (int 1) nil)]
            (extend klass MemorySize* {:memory-size* f})
            (when klass (register-memory-size-handler! klass f))
            (recur seq_10146 chunk_10147 count_10148 (inc i_10149)))
          (let [temp__5804__auto__ (seq seq_10146)]
            (when temp__5804__auto__
              (let [seq_10146 temp__5804__auto__]
                (if (chunked-seq? seq_10146)
                  (let [c__6065__auto__ (chunk-first seq_10146)]
                    (recur
                      (chunk-rest seq_10146)
                      c__6065__auto__
                      (int (count c__6065__auto__))
                      (int 0)))
                  (let [vec__10153 (first seq_10146)
                        klass (nth vec__10153 (int 0) nil)
                        f (nth vec__10153 (int 1) nil)]
                    (extend klass MemorySize* {:memory-size* f})
                    (when klass (register-memory-size-handler! klass f))
                    (recur (next seq_10146) nil 0 0))))))))))
  (reset-meta!
    #'extend-memory-size*
    (assoc
      {:arglists (clojure.core/list ['& 'klass+impls]), :column (int 1)}
      :name
      'extend-memory-size*
      :ns
      *ns*))
  (defn array-type
    ([t] (.getClass (java.lang.reflect.Array/newInstance ^java.lang.Class t (int 0)))))
  (reset-meta!
    #'array-type
    (assoc {:arglists (clojure.core/list ['t]), :column (int 1)} :name 'array-type :ns *ns*))
  (def handle-primitive-arrays
   (fn handle_primitive_arrays
     ([&form &env & typesizes]
       (seq
         (concat
           (clojure.core/list 'datomic.memory-size/extend-memory-size*)
           (mapcat
             (fn fn__10162
               ([p__10161]
                 (let [vec__10163 p__10161
                       atype (nth vec__10163 (int 0) nil)
                       size (nth vec__10163 (int 1) nil)]
                   (apply
                     vector
                     (seq
                       (-> (clojure.core/list 'datomic.memory-size/array-type)
                        (concat (clojure.core/list atype))
                        (seq)
                        (clojure.core/list)
                        (clojure.core/list 'clojure.core/fn)
                        (namespace atype)
                        (str "_array")
                        (symbol)
                        (clojure.core/list)
                        (concat
                          (clojure.core/list
                            (with-meta
                              (apply vector (seq (concat (clojure.core/list 'x__10160__auto__))))
                              (apply
                                hash-map
                                (seq
                                  (concat
                                    (clojure.core/list :tag)
                                    (clojure.core/list 'clojure.core/long))))))
                          (clojure.core/list
                            (seq
                              (concat
                                (clojure.core/list 'clojure.core/+)
                                (clojure.core/list 'datomic.memory-size/ARRAY)
                                (clojure.core/list
                                  (seq
                                    (concat
                                      (clojure.core/list 'clojure.core/*)
                                      (clojure.core/list size)
                                      (clojure.core/list
                                        (seq
                                          (concat
                                            (clojure.core/list 'clojure.core/count)
                                            (clojure.core/list 'x__10160__auto__)))))))))))
                        (seq)
                        (clojure.core/list)
                        (concat)))))))
             (partition 2 typesizes)))))))
  (reset-meta!
    #'handle-primitive-arrays
    (assoc
      {:arglists (clojure.core/list ['& 'typesizes]), :column (int 1)}
      :name
      'handle-primitive-arrays
      :ns
      *ns*))
  (.setMacro #'handle-primitive-arrays)
  (extend-memory-size*
    (array-type java.lang.Long/TYPE)
    (fn Long_array (^long [x__10160__auto__] (+ 16 (* 8 (count x__10160__auto__)))))
    (array-type java.lang.Integer/TYPE)
    (fn Integer_array (^long [x__10160__auto__] (+ 16 (* 4 (count x__10160__auto__)))))
    (array-type java.lang.Boolean/TYPE)
    (fn Boolean_array (^long [x__10160__auto__] (+ 16 (* 1 (count x__10160__auto__)))))
    (array-type java.lang.Double/TYPE)
    (fn Double_array (^long [x__10160__auto__] (+ 16 (* 8 (count x__10160__auto__)))))
    (array-type java.lang.Float/TYPE)
    (fn Float_array (^long [x__10160__auto__] (+ 16 (* 4 (count x__10160__auto__)))))
    (array-type java.lang.Short/TYPE)
    (fn Short_array (^long [x__10160__auto__] (+ 16 (* 2 (count x__10160__auto__)))))
    (array-type java.lang.Character/TYPE)
    (fn Character_array (^long [x__10160__auto__] (+ 16 (* 2 (count x__10160__auto__)))))
    (array-type java.lang.Byte/TYPE)
    (fn Byte_array (^long [x__10160__auto__] (+ 16 (* 1 (count x__10160__auto__))))))
  (defn hinted-fn
    ([kls fntail]
      (let [vec__10184 fntail
            seq__10185 (seq vec__10184)
            first__10186 (first seq__10185)
            seq__10185 (next seq__10185)
            vec__10187 first__10186
            target (nth vec__10187 (int 0) nil)
            body seq__10185]
        (seq
          (concat
            (clojure.core/list 'clojure.core/fn)
            (clojure.core/list (nice-sym kls))
            (clojure.core/list (with-meta (vector (vary-meta target assoc :tag kls)) {:tag 'long}))
            body)))))
  (reset-meta!
    #'hinted-fn
    (assoc
      {:private true, :arglists (clojure.core/list ['kls 'fntail]), :column (int 1)}
      :name
      'hinted-fn
      :ns
      *ns*))
  (defn fn-ize
    ([type+handlers]
      (mapcat
        (fn fn__10192
          ([p__10191]
            (let [vec__10193 p__10191
                  klass (nth vec__10193 (int 0) nil)
                  fntail (nth vec__10193 (int 1) nil)]
              [klass (hinted-fn klass fntail)])))
        (partition 2 type+handlers))))
  (reset-meta!
    #'fn-ize
    (assoc
      {:private true, :arglists (clojure.core/list ['type+handlers]), :column (int 1)}
      :name
      'fn-ize
      :ns
      *ns*))
  (def extend-memory-size
   (fn extend_memory_size
     ([&form &env & type+handlers]
       (seq
         (concat
           (clojure.core/list 'datomic.memory-size/extend-memory-size*)
           (fn-ize type+handlers))))))
  (reset-meta!
    #'extend-memory-size
    (assoc
      {:arglists (clojure.core/list ['& 'type+handlers]), :column (int 1)}
      :name
      'extend-memory-size
      :ns
      *ns*))
  (.setMacro #'extend-memory-size)
  (extend-memory-size*
    nil
    (fn __10199 (^long [_] 0))
    java.lang.Object
    (fn Object
      (^long [o]
        (.longValue
          (let [cls (.getClass o)]
            (if (.isArray ^java.lang.Class cls)
              (let [a o
                    a__6555__auto__ a
                    l__6556__auto__ (alength ^"[Ljava.lang.Object;" a__6555__auto__)]
                (loop [idx 0 ret 16]
                  (if (< idx l__6556__auto__)
                    (recur
                      (inc (int idx))
                      (+ ret (max 8 (memory-size (aget ^"[Ljava.lang.Object;" a (int idx))))))
                    (long ret))))
              (do
                (when (not (identical? cls Object))
                  (throw (java.lang.Error. (str "No size estimator for " cls))))
                16))))))
    java.lang.Long
    (fn Long (^long [o] 24))
    java.lang.Character
    (fn Character (^long [o] 24))
    java.lang.Integer
    (fn Integer (^long [o] 24))
    java.lang.Short
    (fn Short (^long [o] 24))
    java.lang.Double
    (fn Double (^long [o] 24))
    java.lang.Float
    (fn Float (^long [o] 24))
    java.lang.Boolean
    (fn Boolean (^long [o] 24))
    java.lang.Byte
    (fn Byte (^long [o] 24))
    clojure.lang.Keyword
    (fn clojure_lang_Keyword (^long [kw] 48))
    clojure.lang.Symbol
    (fn clojure_lang_Symbol (^long [s] (+ 48 (memory-size (meta s)))))
    clojure.lang.BigInt
    (fn clojure_lang_BigInt (^long [bi] (+ 16 (memory-size (.-bipart ^clojure.lang.BigInt bi)))))
    java.math.BigInteger
    (fn java_math_BigInteger (^long [_] 60))
    java.math.BigDecimal
    (fn java_math_BigDecimal
      (^long [bd] (+ 32 (memory-size (.unscaledValue ^java.math.BigDecimal bd)))))
    java.net.URI
    (fn java_net_URI
      (^long [uri]
        (+
          (-> (+ 16 32)
           (+ 160)
           (+ 48)
           (+ 8)
           (+ (* 2 (count (.getSchemeSpecificPart ^java.net.URI uri)))))
          (* 2 (count (.getScheme ^java.net.URI uri))))))
    java.util.UUID
    (fn java_util_UUID (^long [uuid] 64))
    java.util.Date
    (fn java_util_Date (^long [date] 32))
    java.util.Map
    (fn java_util_Map
      (^long [m]
        (let [lv (long-array 1)]
          (reduce-kv
            (fn fn__10238
              ([_ k v]
                (aset
                  ^longs lv
                  (int 0)
                  (long
                    (-> (aget ^longs lv (int 0))
                     (+ 36)
                     (+ (memory-size k))
                     (+ (memory-size v))
                     (long))))
                nil))
            nil
            m)
          (aget ^longs lv (int 0)))))
    java.util.Collection
    (fn java_util_Collection
      (^long [c]
        (let [lv (long-array 1)]
          (run!
            (fn fn__10242
              ([v]
                (aset ^longs lv (int 0) (long (+ (+ (aget ^longs lv (int 0)) 24) (memory-size v))))
                nil))
            c)
          (aget ^longs lv (int 0)))))
    java.lang.String
    (fn String (^long [s] (+ 56 (* 2 (.length ^java.lang.String s)))))))