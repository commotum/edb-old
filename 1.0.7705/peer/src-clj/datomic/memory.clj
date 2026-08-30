(do
  (clojure.core/in-ns 'datomic.memory)
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/require
        ['datomic.math :as 'math]
        ['datomic.config :as 'config]
        ['datomic.common :as 'common]
        ['clojure.string :as 'str])))
  (when-not (.equals 'datomic.memory 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.memory))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/require
          ['datomic.math :as 'math]
          ['datomic.config :as 'config]
          ['datomic.common :as 'common]
          ['clojure.string :as 'str]))))
  (.setMeta
    (clojure.lang.RT/var "datomic.memory" "MINIMUM_VM_SIZE")
    {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.memory" "MINIMUM_VM_SIZE") (long (* 96 1048576)))
  (.setMeta (clojure.lang.RT/var "datomic.memory" "SEGMENT_SIZE") {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.memory" "SEGMENT_SIZE") (long (* 64 1024)))
  (def DATOMS_PER_SEGMENT 8000)
  (reset-meta!
    #'DATOMS_PER_SEGMENT
    (assoc {:const true, :column (int 1)} :name 'DATOMS_PER_SEGMENT :ns *ns*))
  (def DATOM_OBJECT_SIZE 60)
  (reset-meta!
    #'DATOM_OBJECT_SIZE
    (assoc {:const true, :column (int 1)} :name 'DATOM_OBJECT_SIZE :ns *ns*))
  (.setMeta
    (clojure.lang.RT/var "datomic.memory" "SEGMENT_OBJECT_SIZE")
    {:const true, :column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.memory" "SEGMENT_OBJECT_SIZE") (long (* 8000 60)))
  (def TRANSACTOR_SPARE_MEM 0)
  (reset-meta!
    #'TRANSACTOR_SPARE_MEM
    (assoc {:const true, :column (int 1)} :name 'TRANSACTOR_SPARE_MEM :ns *ns*))
  (defn ram->bytes
    ([ram]
      (let [ram (str/trim ram)
            units (last (.toUpperCase ^java.lang.String ram))
            value (java.lang.Integer/parseInt (apply str (drop-last ram)))
            conversion {\B 1, \K 1024, \M 1048576, \G 1073741824}]
        (* value (get conversion units)))))
  (reset-meta!
    #'ram->bytes
    (assoc {:arglists (clojure.core/list ['ram]), :column (int 1)} :name 'ram->bytes :ns *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.memory" "aws-instance-mem") {:column (int 1)})
  (.bindRoot
    (clojure.lang.RT/var "datomic.memory" "aws-instance-mem")
    {"m1.large" (long (* 7500 1048576)),
     "c1.medium" (long (* 1700 1048576)),
     "r4.2xlarge" (long (* 61 1073741824)),
     "r3.8xlarge" (long (* 244 1073741824)),
     "m3.2xlarge" (long (* 30 1073741824)),
     "i2.8xlarge" (long (* 244 1073741824)),
     "c3.8xlarge" (long (* 60 1073741824)),
     "t2.large" (long (* 8 1073741824)),
     "m1.xlarge" (long (* 15 1073741824)),
     "m4.4xlarge" (long (* 64 1073741824)),
     "t2.2xlarge" (long (* 32 1073741824)),
     "r4.16xlarge" (long (* 488 1073741824)),
     "cr1.8xlarge" (long (* 244 1073741824)),
     "t1.micro" (long (* 613 1048576)),
     "c3.2xlarge" (long (* 15 1073741824)),
     "c4.8xlarge" (long (* 60 1073741824)),
     "m2.xlarge" (long (* 17 1073741824)),
     "m1.small" (long (* 1700 1048576)),
     "i2.4xlarge" (long (* 122 1073741824)),
     "c3.4xlarge" (long (* 30 1073741824)),
     "i3.16xlarge" (long (* 488 1073741824)),
     "m4.2xlarge" (long (* 32 1073741824)),
     "i3.4xlarge" (long (* 122 1073741824)),
     "m4.10xlarge" (long (* 160 1073741824)),
     "m1.medium" (long (* 3750 1048576)),
     "c4.4xlarge" (long (* 30 1073741824)),
     "hi1.4xlarge" (long (* 60 1073741824)),
     "r3.xlarge" (long (* 30 1073741824)),
     "c4.2xlarge" (long (* 15 1073741824)),
     "i3.8xlarge" (long (* 244 1073741824)),
     "t2.small" (long (* 2 1073741824)),
     "r4.xlarge" (long (* 30500 1048576)),
     "m3.xlarge" (long (* 15 1073741824)),
     "r3.2xlarge" (long (* 61 1073741824)),
     "r3.4xlarge" (long (* 122 1073741824)),
     "t2.xlarge" (long (* 16 1073741824)),
     "c4.xlarge" (long (* 7500 1048576)),
     "m2.2xlarge" (long (* 34 1073741824)),
     "m4.large" (long (* 8 1073741824)),
     "m4.16xlarge" (long (* 256 1073741824)),
     "r3.large" (long (* 15 1073741824)),
     "i3.2xlarge" (long (* 61 1073741824)),
     "c4.large" (long (* 3750 1048576)),
     "m4.xlarge" (long (* 16 1073741824)),
     "i2.2xlarge" (long (* 61 1073741824)),
     "i3.xlarge" (long (* 30500 1048576)),
     "m2.4xlarge" (long (* 68 1073741824)),
     "r4.large" (long (* 15250 1048576)),
     "c3.xlarge" (long (* 7500 1048576)),
     "i3.large" (long (* 15250 1048576)),
     "r4.8xlarge" (long (* 244 1073741824)),
     "t2.medium" (long (* 4 1073741824)),
     "c3.large" (long (* 3750 1048576)),
     "m3.medium" (long (* 3750 1048576)),
     "i2.xlarge" (long (* 30 1073741824)),
     "cc2.8xlarge" (long (* 60 1073741824)),
     "hs1.8xlarge" (long (* 117 1073741824)),
     "m3.large" (long (* 7500 1048576)),
     "c1.xlarge" (long (* 7 1073741824)),
     "r4.4xlarge" (long (* 122 1073741824))})
  (defn segment-cache-max ([segment_memory_size] (quot segment_memory_size 65536)))
  (reset-meta!
    #'segment-cache-max
    (assoc
      {:arglists (clojure.core/list ['segment-memory-size]), :column (int 1)}
      :name
      'segment-cache-max
      :ns
      *ns*))
  (defn object-cache-max ([virtual_memory_size] (quot virtual_memory_size 480000)))
  (reset-meta!
    #'object-cache-max
    (assoc
      {:arglists (clojure.core/list ['virtual-memory-size]), :column (int 1)}
      :name
      'object-cache-max
      :ns
      *ns*))
  (defn transactor-settings
    ([ram_bytes memidx_bytes]
      (let [available_bytes (- (- (- ram_bytes 100663296) 0) memidx_bytes)
            transactor_segment (long (* available_bytes 0.5))
            transactor_object (long (* available_bytes 0.5))]
        (when-not (< (* 256 1048576) ram_bytes)
          (throw
            (java.lang.AssertionError.
              (str
                "Assert failed: "
                "not enough memory to run a transactor"
                "\n"
                (pr-str (clojure.core/list '< (clojure.core/list '* 256 'math/M) 'ram-bytes))))))
        {:memidx-max (str (quot memidx_bytes 1048576) "m"),
         :xmx (str (quot ram_bytes 1048576) "m")}))
    ([ram_bytes] (transactor-settings ram_bytes 67108864)))
  (reset-meta!
    #'transactor-settings
    (assoc
      {:arglists (clojure.core/list ['ram-bytes] ['ram-bytes 'memidx-bytes]), :column (int 1)}
      :name
      'transactor-settings
      :ns
      *ns*))
  (defn aws-transactor-settings
    ([instance_type]
      (transactor-settings (long (* 0.7 (common/getx aws-instance-mem instance_type))))))
  (reset-meta!
    #'aws-transactor-settings
    (assoc
      {:arglists (clojure.core/list ['instance-type]), :column (int 1)}
      :name
      'aws-transactor-settings
      :ns
      *ns*))
  (.setMeta (clojure.lang.RT/var "datomic.memory" "peer-settings") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.memory" "peer-settings") transactor-settings)
  (.setMeta (clojure.lang.RT/var "datomic.memory" "aws-peer-settings") {:column (int 1)})
  (.bindRoot (clojure.lang.RT/var "datomic.memory" "aws-peer-settings") aws-transactor-settings)
  (defn transactor-cache-bytes
    ([memidx_max]
      (- (- (.maxMemory (java.lang.Runtime/getRuntime)) (* (* 100 1024) 1024)) memidx_max)))
  (reset-meta!
    #'transactor-cache-bytes
    (assoc
      {:arglists (clojure.core/list ['memidx-max]), :column (int 1)}
      :name
      'transactor-cache-bytes
      :ns
      *ns*)))