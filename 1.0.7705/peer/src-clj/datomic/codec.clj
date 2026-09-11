(do
  (clojure.core/in-ns 'datomic.codec)
  (.resetMeta
    (clojure.lang.Namespace/find 'datomic.codec)
    {:doc
     "UTF-8 and Base64 conversions used at text-oriented configuration, transport, and storage boundaries."})
  (clojure.core/with-loading-context
    (do
      (clojure.core/refer 'clojure.core)
      (clojure.core/import 'org.apache.commons.codec.binary.Base64)))
  (when-not (.equals 'datomic.codec 'clojure.core)
    (dosync (commute (deref #'clojure.core/*loaded-libs*) conj 'datomic.codec))
    (clojure.core/with-loading-context
      (do
        (clojure.core/refer 'clojure.core)
        (clojure.core/import 'org.apache.commons.codec.binary.Base64))))
  ;; ATOMIC-NOTE BEGIN serialization-text-counterpart
  ;; Baseline-identical to transactor/src-clj/datomic/codec.clj at cd7192e63d883a4a34aa7de4d5bcd17e6edb692d.
  ;; See serialization-text-codec there: these are text-boundary conversions,
  ;; not Datomic's tagged value codec or a durability/security guarantee.
  ;; ATOMIC-NOTE END serialization-text-counterpart
  (defn string->bytes ([s] (.getBytes ^java.lang.String s "UTF-8")))
  (reset-meta!
    #'string->bytes
    (assoc
      {:arglists (clojure.core/list [(.withMeta 's {:tag 'String})]),
       :doc "Encodes a string as UTF-8 bytes.",
       :column (int 1)}
      :name
      'string->bytes
      :ns
      *ns*))
  (defn bytes->string ([b] (java.lang.String. ^bytes b "UTF-8")))
  (reset-meta!
    #'bytes->string
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'b {:tag 'bytes})]),
       :doc "Decodes UTF-8 bytes into a string.",
       :column (int 1)}
      :name
      'bytes->string
      :ns
      *ns*))
  (defn encode-64 ([raw] (Base64/encodeBase64 ^bytes raw (boolean (.booleanValue false)))))
  (reset-meta!
    #'encode-64
    (assoc
      {:arglists (clojure.core/list (.withMeta ['raw] {:tag 'bytes})),
       :doc "Returns the standard Base64 encoding of raw without chunk separators.",
       :column (int 1)}
      :name
      'encode-64
      :ns
      *ns*))
  (defn decode-64 ([coded] (Base64/decodeBase64 ^bytes coded)))
  (reset-meta!
    #'decode-64
    (assoc
      {:arglists (clojure.core/list [(.withMeta 'coded {:tag 'bytes})]),
       :doc "Decodes Base64 bytes into their original byte array.",
       :column (int 1)}
      :name
      'decode-64
      :ns
      *ns*))
  (defn base-64-literal ([literal] (decode-64 (string->bytes literal))))
  (reset-meta!
    #'base-64-literal
    (assoc
      {:arglists (clojure.core/list ['literal]),
       :doc "Decodes a Base64 string literal into bytes using UTF-8 for its textual representation.",
       :column (int 1)}
      :name
      'base-64-literal
      :ns
      *ns*)))
