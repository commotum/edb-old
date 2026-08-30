(do
  (clojure.core/in-ns 'datomic.memory-size)
  (clojure.core/with-loading-context (clojure.core/refer 'clojure.core))
  (when-not (.equals 'datomic.memory-size 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.memory-size))
    (clojure.core/with-loading-context (clojure.core/refer 'clojure.core)))
  (set! *warn-on-reflection* true)
  (let [protocol_metadata__7420 {:column (int 1)}]
    (defprotocol MemorySize (memory-size [_] "Return best guess of the memory size of an object."))
    (reset-meta!
      (clojure.lang.RT/var "datomic.memory-size" "MemorySize")
      (assoc (assoc protocol_metadata__7420 :doc nil) :name 'MemorySize :ns *ns*))
    (let [protocol_signature__7421 (assoc
                                     {:tag nil,
                                      :name
                                      (.withMeta
                                        'memory-size
                                        {:arglists (clojure.core/list ['_])}),
                                      :arglists (clojure.core/list ['_]),
                                      :doc "Return best guess of the memory size of an object."}
                                     :protocol
                                     (clojure.lang.RT/var "datomic.memory-size" "MemorySize"))
          protocol_method_name__7422 (with-meta
                                       (:name protocol_signature__7421)
                                       protocol_signature__7421)]
      (reset-meta!
        (clojure.lang.RT/var "datomic.memory-size" "memory-size")
        (assoc protocol_signature__7421 :name protocol_method_name__7422 :ns *ns*))))
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
           (clojure.core/list 'do)
           (map
             (fn fn__9143
               ([p__9142]
                 (let [vec__9144 p__9142
                       type (nth vec__9144 (int 0) nil)
                       size (nth vec__9144 (int 1) nil)]
                   (seq
                     (concat
                       (clojure.core/list 'clojure.core/extend-type)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'datomic.memory-size/array-type)
                             (clojure.core/list type))))
                       (clojure.core/list 'datomic.memory-size/MemorySize)
                       (clojure.core/list
                         (seq
                           (concat
                             (clojure.core/list 'memory-size)
                             (clojure.core/list
                               (apply vector (seq (concat (clojure.core/list 'x)))))
                             (clojure.core/list
                               (seq
                                 (concat
                                   (clojure.core/list 'clojure.core/+)
                                   (clojure.core/list 16)
                                   (clojure.core/list
                                     (seq
                                       (concat
                                         (clojure.core/list 'clojure.core/*)
                                         (clojure.core/list size)
                                         (clojure.core/list
                                           (seq
                                             (concat
                                               (clojure.core/list 'clojure.core/count)
                                               (clojure.core/list 'x))))))))))))))))))
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
  (extend
    (array-type java.lang.Long/TYPE)
    MemorySize
    {:memory-size (fn fn__9149 ([x] (long (+ 16 (* 8 (count x))))))})
  (extend
    (array-type java.lang.Integer/TYPE)
    MemorySize
    {:memory-size (fn fn__9151 ([x] (long (+ 16 (* 4 (count x))))))})
  (extend
    (array-type java.lang.Boolean/TYPE)
    MemorySize
    {:memory-size (fn fn__9153 ([x] (long (+ 16 (* 1 (count x))))))})
  (extend
    (array-type java.lang.Double/TYPE)
    MemorySize
    {:memory-size (fn fn__9155 ([x] (long (+ 16 (* 8 (count x))))))})
  (extend
    (array-type java.lang.Float/TYPE)
    MemorySize
    {:memory-size (fn fn__9157 ([x] (long (+ 16 (* 4 (count x))))))})
  (extend
    (array-type java.lang.Short/TYPE)
    MemorySize
    {:memory-size (fn fn__9159 ([x] (long (+ 16 (* 2 (count x))))))})
  (extend
    (array-type java.lang.Character/TYPE)
    MemorySize
    {:memory-size (fn fn__9161 ([x] (long (+ 16 (* 2 (count x))))))})
  (extend
    (array-type java.lang.Byte/TYPE)
    MemorySize
    {:memory-size (fn fn__9163 ([x] (long (+ 16 (* 1 (count x))))))})
  (extend nil MemorySize {:memory-size (fn fn__9165 ([_] 0))})
  (extend
    java.lang.Object
    MemorySize
    {:memory-size
     (fn fn__9167
       ([o]
         (let [cls (.getClass o)]
           (if (.isArray ^java.lang.Class cls)
             (let [a o
                   a__6555__auto__ a
                   l__6556__auto__ (alength ^"[Ljava.lang.Object;" a__6555__auto__)]
               (loop [idx 0 ret 16]
                 (if (< idx l__6556__auto__)
                   (recur
                     (inc (int idx))
                     (+
                       ret
                       (max 8 (long (memory-size (aget ^"[Ljava.lang.Object;" a (int idx)))))))
                   (long ret))))
             (do
               (when (not (identical? cls java.lang.Object))
                 (throw (java.lang.Error. (str "No size estimator for " cls))))
               16)))))})
  (extend java.math.BigInteger MemorySize {:memory-size (fn fn__9171 ([_] 60))})
  (extend java.lang.Double MemorySize {:memory-size (fn fn__9173 ([o] 24))})
  (extend java.util.Date MemorySize {:memory-size (fn fn__9175 ([date] 32))})
  (extend
    java.lang.String
    MemorySize
    {:memory-size (fn fn__9177 ([s] (long (+ 56 (* 2 (.length ^java.lang.String s))))))})
  (extend
    java.util.Map
    MemorySize
    {:memory-size
     (fn fn__9179
       ([m]
         (reduce
           (fn fn__9181
             ([s p__9180]
               (let [vec__9182 p__9180 k (nth vec__9182 (int 0) nil) v (nth vec__9182 (int 1) nil)]
                 (+ (+ (+ s 36) (memory-size k)) (memory-size v)))))
           0
           m)))})
  (extend java.util.UUID MemorySize {:memory-size (fn fn__9187 ([uuid] 64))})
  (extend
    java.util.Collection
    MemorySize
    {:memory-size
     (fn fn__9189 ([c] (reduce (fn fn__9190 ([s v] (+ (+ s 24) (memory-size v)))) 0 c)))})
  (extend java.lang.Boolean MemorySize {:memory-size (fn fn__9193 ([o] 24))})
  (extend java.lang.Integer MemorySize {:memory-size (fn fn__9195 ([o] 24))})
  (extend
    java.net.URI
    MemorySize
    {:memory-size
     (fn fn__9197
       ([uri]
         (long
           (+
             (-> (+ 16 32)
              (+ 160)
              (+ 48)
              (+ 8)
              (+ (* 2 (count (.getSchemeSpecificPart ^java.net.URI uri)))))
             (* 2 (count (.getScheme ^java.net.URI uri)))))))})
  (extend java.lang.Short MemorySize {:memory-size (fn fn__9199 ([o] 24))})
  (extend java.lang.Character MemorySize {:memory-size (fn fn__9201 ([o] 24))})
  (extend
    clojure.lang.BigInt
    MemorySize
    {:memory-size (fn fn__9203 ([bi] (+ 16 (memory-size (.-bipart ^clojure.lang.BigInt bi)))))})
  (extend
    java.math.BigDecimal
    MemorySize
    {:memory-size
     (fn fn__9205 ([bd] (+ 32 (memory-size (.unscaledValue ^java.math.BigDecimal bd)))))})
  (extend java.lang.Float MemorySize {:memory-size (fn fn__9207 ([o] 24))})
  (extend clojure.lang.Keyword MemorySize {:memory-size (fn fn__9209 ([kw] 48))})
  (extend java.lang.Long MemorySize {:memory-size (fn fn__9211 ([o] 24))})
  (extend
    clojure.lang.Symbol
    MemorySize
    {:memory-size (fn fn__9213 ([s] (+ 48 (memory-size (meta s)))))})
  (extend java.lang.Byte MemorySize {:memory-size (fn fn__9215 ([o] 24))}))