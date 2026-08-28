(ns stage2.storage
  "TEST INFRASTRUCTURE ONLY.

  A small filesystem implementation of the recovered
  datomic.backup/Storage protocol.  It exists so Stage 2 can exercise the
  recovered backup engine without loading datomic.fsbackup (which is present
  only in the licensed transactor artifact).  This namespace must never be
  packaged as a production Datomic storage implementation.

  The adapter is deliberately synchronous.  Stored values are immutable
  byte-buffer snapshots, directory listings are sorted, and keys are always
  returned as full storage-relative paths because datomic.backup/Substorage
  performs its own prefix removal."
  (:require [clojure.string :as str]
            [datomic.backup :as backup])
  (:import [java.io File InputStream]
           [java.net URI]
           [java.nio ByteBuffer]
           [java.nio.channels Channels SeekableByteChannel]
           [java.nio.file AtomicMoveNotSupportedException
            FileAlreadyExistsException
            FileVisitOption
            Files
            LinkOption
            OpenOption
            Path
            Paths
            StandardCopyOption
            StandardOpenOption]
           [java.nio.file.attribute FileAttribute]
           [java.util.stream Stream]))

(set! *warn-on-reflection* true)

(def ^:private empty-file-attributes
  (make-array FileAttribute 0))

(def ^:private empty-file-visit-options
  (make-array FileVisitOption 0))

(def ^:private no-follow-link-options
  (into-array LinkOption [LinkOption/NOFOLLOW_LINKS]))

(def ^:private read-open-options
  (into-array OpenOption
              [StandardOpenOption/READ LinkOption/NOFOLLOW_LINKS]))

(def ^:private write-open-options
  (into-array OpenOption
              [StandardOpenOption/WRITE
               StandardOpenOption/TRUNCATE_EXISTING]))

(def ^:private atomic-move-options
  (into-array StandardCopyOption
              [StandardCopyOption/ATOMIC_MOVE
               StandardCopyOption/REPLACE_EXISTING]))

(def ^:private internal-temp-prefix ".stage2-storage-")

(defprotocol OperationLogged
  (operation-log-atom [storage]
    "Returns the atom containing this adapter or fault wrapper's operation log."))

(defn new-operation-log
  "Creates an operation log.  Events contain keys, sizes, and exception class
  names only; they never contain stored values."
  []
  (atom {:next-sequence 0
         :counts {}
         :events []}))

(defn- record-operation!
  [operations operation outcome key details]
  (swap! operations
         (fn [state]
           (let [sequence (:next-sequence state)
                 event (merge {:sequence sequence
                               :operation operation
                               :outcome outcome
                               :key key}
                              details)]
             (-> state
                 (assoc :next-sequence (inc sequence))
                 (update-in [:counts operation outcome] (fnil inc 0))
                 (update :events conj event)))))
  nil)

(defn operation-log
  "Returns a stable snapshot of an adapter or wrapper's operation log."
  [storage]
  (-> @(operation-log-atom storage)
      (dissoc :next-sequence)))

(defn reset-operation-log!
  [storage]
  (reset! (operation-log-atom storage)
          {:next-sequence 0
           :counts {}
           :events []})
  storage)

(defn- exception-details
  [^Throwable throwable]
  {:exception-class (.getName (class throwable))})

(defn- storage-error
  ([kind message data]
   (ex-info message (assoc data ::error kind)))
  ([kind message data cause]
   (ex-info message (assoc data ::error kind) cause)))

(defn- ->path
  ^Path [pathish]
  (cond
    (instance? Path pathish) pathish
    (instance? File pathish) (.toPath ^File pathish)
    (instance? URI pathish) (Paths/get ^URI pathish)
    :else (Paths/get (str pathish) (make-array String 0))))

(defn- prepare-root
  ^Path [root]
  (let [^Path absolute (-> root ->path .toAbsolutePath .normalize)]
    (Files/createDirectories absolute empty-file-attributes)
    (when (Files/isSymbolicLink absolute)
      (throw (storage-error
               :symbolic-link
               "Stage 2 storage root must not be a symbolic link"
               {:root (str absolute)})))
    ;; Resolve any symlinks in ancestors once and retain the real root.  Keys
    ;; are then checked component-by-component for new symlinks below it.
    (let [^Path real-root (.normalize (.toRealPath absolute (make-array LinkOption 0)))]
      (when-not (Files/isDirectory real-root no-follow-link-options)
        (throw (storage-error
                 :invalid-root
                 "Stage 2 storage root is not a directory"
                 {:root (str real-root)})))
      real-root)))

(defn- validate-key!
  [key]
  (when-not (string? key)
    (throw (storage-error
             :invalid-key
             "Stage 2 storage keys must be strings"
             {:key-type (some-> key class .getName)})))
  (when (or (str/blank? key)
            (str/includes? key "\u0000")
            (str/includes? key "\\")
            (str/starts-with? key "/")
            (re-find #"(?i)^[a-z]:" key))
    (throw (storage-error
             :invalid-key
             "Unsafe Stage 2 storage key"
             {:key key})))
  (let [parts (str/split key #"/" -1)]
    (when (some #{"" "." ".."} parts)
      (throw (storage-error
               :invalid-key
               "Stage 2 storage key contains an unsafe path component"
               {:key key}))))
  key)

(defn- validate-prefix!
  [prefix]
  (when-not (string? prefix)
    (throw (storage-error
             :invalid-prefix
             "Stage 2 storage prefixes must be strings"
             {:prefix-type (some-> prefix class .getName)})))
  (when-not (empty? prefix)
    (when (or (str/includes? prefix "\u0000")
              (str/includes? prefix "\\")
              (str/starts-with? prefix "/")
              (re-find #"(?i)^[a-z]:" prefix))
      (throw (storage-error
               :invalid-prefix
               "Unsafe Stage 2 storage prefix"
               {:prefix prefix})))
    (let [parts (str/split prefix #"/" -1)
          parts (if (= "" (last parts)) (butlast parts) parts)]
      (when (some #{"" "." ".."} parts)
        (throw (storage-error
                 :invalid-prefix
                 "Stage 2 storage prefix contains an unsafe path component"
                 {:prefix prefix})))))
  prefix)

(defn- existing-no-follow?
  [^Path path]
  (Files/exists path no-follow-link-options))

(defn- assert-no-symbolic-links!
  [^Path root ^Path target]
  (loop [^Path current root
         components (seq (iterator-seq (.iterator (.relativize root target))))]
    (when-let [^Path component (first components)]
      (let [^Path next-path (.resolve current component)]
        (when (and (existing-no-follow? next-path)
                   (Files/isSymbolicLink next-path))
          (throw (storage-error
                   :symbolic-link
                   "Symbolic links are not allowed inside Stage 2 storage"
                   {:root (str root)
                    :path (str next-path)})))
        (recur next-path (next components)))))
  target)

(defn- safe-key-path
  ^Path [^Path root key]
  (validate-key! key)
  (let [^Path candidate (.normalize (.resolve root ^String key))]
    (when-not (.startsWith candidate root)
      (throw (storage-error
               :path-escape
               "Stage 2 storage key escapes its root"
               {:root (str root)
                :key key})))
    (assert-no-symbolic-links! root candidate)
    candidate))

(defn- ensure-directory-chain!
  [^Path root ^Path directory]
  (loop [^Path current root
         components (seq (iterator-seq (.iterator (.relativize root directory))))]
    (when-let [^Path component (first components)]
      (let [^Path next-path (.resolve current component)]
        (when-not (existing-no-follow? next-path)
          (try
            (Files/createDirectory next-path empty-file-attributes)
            (catch FileAlreadyExistsException _
              ;; A concurrent backup worker may have created the shared
              ;; values/<prefix> directory after our existence check.
              nil)))
        (when (Files/isSymbolicLink next-path)
          (throw (storage-error
                   :symbolic-link
                   "Symbolic links are not allowed inside Stage 2 storage"
                   {:root (str root)
                    :path (str next-path)})))
        (when-not (Files/isDirectory next-path no-follow-link-options)
          (throw (storage-error
                   :not-a-directory
                   "A Stage 2 storage path component is not a directory"
                   {:root (str root)
                    :path (str next-path)})))
        (recur next-path (next components)))))
  directory)

(defn- byte-buffer->bytes
  ^bytes [^ByteBuffer buffer]
  (when-not buffer
    (throw (storage-error
             :invalid-buffer
             "Stage 2 storage cannot store a nil ByteBuffer"
             {})))
  (let [^ByteBuffer duplicate (.duplicate buffer)
        bytes (byte-array (.remaining duplicate))]
    (.get duplicate bytes)
    bytes))

(defn- read-all-bytes-no-follow
  ^bytes [^Path path]
  (with-open [channel (Files/newByteChannel path read-open-options)
              ^InputStream input (Channels/newInputStream channel)]
    (.readAllBytes input)))

(defn- write-all-bytes!
  [^Path path ^bytes bytes]
  (with-open [^SeekableByteChannel channel
              (Files/newByteChannel path write-open-options)]
    (let [^ByteBuffer buffer (ByteBuffer/wrap bytes)]
      (while (.hasRemaining buffer)
        (.write channel buffer))))
  path)

(defn- atomic-write!
  [^Path root ^Path target ^bytes bytes]
  (let [^Path parent (.getParent target)]
    (ensure-directory-chain! root parent)
    (assert-no-symbolic-links! root target)
    (let [^Path temp (Files/createTempFile
                       parent internal-temp-prefix ".tmp" empty-file-attributes)]
      (try
        (write-all-bytes! temp bytes)
        ;; Recheck after writing the sibling so a newly inserted symlink is
        ;; rejected before the destination name is replaced.
        (assert-no-symbolic-links! root target)
        (try
          (Files/move temp target atomic-move-options)
          (catch AtomicMoveNotSupportedException cause
            (throw (storage-error
                     :atomic-move-unsupported
                     "Stage 2 storage requires atomic sibling moves"
                     {:root (str root)
                      :path (str target)}
                     cause))))
        (finally
          (Files/deleteIfExists temp)))))
  target)

(defn- internal-temp-path?
  [^Path path]
  (str/starts-with? (str (.getFileName path)) internal-temp-prefix))

(defn- storage-relative-key
  [^Path root ^Path path]
  (str/replace (str (.relativize root path)) File/separator "/"))

(defn- sorted-keys-with-prefix
  [^Path root prefix]
  (validate-prefix! prefix)
  (with-open [^Stream paths (Files/walk root empty-file-visit-options)]
    (let [all-paths (doall (iterator-seq (.iterator paths)))]
      (doseq [^Path path all-paths]
        (when (Files/isSymbolicLink path)
          (throw (storage-error
                   :symbolic-link
                   "Symbolic links are not allowed inside Stage 2 storage"
                   {:root (str root)
                    :path (str path)}))))
      (->> all-paths
           (filter (fn [^Path path]
                     (and (not (internal-temp-path? path))
                          (Files/isRegularFile path no-follow-link-options))))
           (map (partial storage-relative-key root))
           (filter #(str/starts-with? % prefix))
           sort
           vec))))

(defrecord FileStorage [^Path root operations]
  backup/Storage
  (store [_ key buffer]
    (try
      (let [^bytes bytes (byte-buffer->bytes buffer)
            ^Path path (safe-key-path root key)]
        (atomic-write! root path bytes)
        (record-operation! operations :store :stored key {:bytes (alength bytes)})
        {:k key})
      (catch Throwable throwable
        (record-operation!
          operations :store :error key (exception-details throwable))
        (throw throwable))))

  (retrieve [_ key]
    (try
      (let [^Path path (safe-key-path root key)]
        (if (Files/isRegularFile path no-follow-link-options)
          (let [^bytes bytes (read-all-bytes-no-follow path)]
            (record-operation! operations :retrieve :hit key {:bytes (alength bytes)})
            {:k key
             :v (ByteBuffer/wrap bytes)})
          (do
            (record-operation! operations :retrieve :miss key {})
            nil)))
      (catch Throwable throwable
        (record-operation!
          operations :retrieve :error key (exception-details throwable))
        (throw throwable))))

  (exists? [_ key]
    (try
      (let [^Path path (safe-key-path root key)
            present? (Files/isRegularFile path no-follow-link-options)]
        (record-operation!
          operations :exists (if present? :present :absent) key {})
        present?)
      (catch Throwable throwable
        (record-operation!
          operations :exists :error key (exception-details throwable))
        (throw throwable))))

  (list-keys [_ prefix]
    (try
      (let [keys (sorted-keys-with-prefix root prefix)]
        (record-operation! operations :list-keys :listed prefix {:key-count (count keys)})
        {:ks keys})
      (catch Throwable throwable
        (record-operation!
          operations :list-keys :error prefix (exception-details throwable))
        (throw throwable))))

  OperationLogged
  (operation-log-atom [_] operations))

(defn file-storage
  "Creates a test-only filesystem Storage rooted at `root`.

  The optional operation log must be an atom produced by new-operation-log."
  ([root]
   (file-storage root (new-operation-log)))
  ([root operations]
   (when-not (instance? clojure.lang.IAtom operations)
     (throw (storage-error
              :invalid-operation-log
              "Stage 2 storage operation log must be an atom"
              {:operation-log-type (some-> operations class .getName)})))
   (->FileStorage (prepare-root root) operations)))

(defn storage-root
  ^Path [^FileStorage storage]
  (:root storage))

(defn stage2-file-uri
  "Returns a stage2-file URI for an absolute filesystem root.  URI creation
  does not create the directory; file-storage/create-storage does."
  ^URI [root]
  (let [^Path path (-> root ->path .toAbsolutePath .normalize)]
    (URI. "stage2-file" nil (str path) nil)))

(defn- uri-root
  [^URI uri]
  (when (or (some? (.getRawAuthority uri))
            (some? (.getRawQuery uri))
            (some? (.getRawFragment uri)))
    (throw (storage-error
             :invalid-uri
             "stage2-file URIs may contain only an absolute path"
             {:uri (str uri)})))
  (let [path (.getPath uri)]
    (when (str/blank? path)
      (throw (storage-error
               :invalid-uri
               "stage2-file URI is missing its root path"
               {:uri (str uri)})))
    (let [^Path root (->path path)]
      (when-not (.isAbsolute root)
        (throw (storage-error
                 :invalid-uri
                 "stage2-file URI root must be absolute"
                 {:uri (str uri)})))
      root)))

(defmethod backup/create-storage* "stage2-file"
  [^URI uri sse?]
  (when sse?
    (throw (storage-error
             :sse-not-supported
             "Server-side encryption is not available in test-only Stage 2 storage"
             {:uri (str uri)})))
  (file-storage (uri-root uri)))

(defrecord RootStoreFailureStorage [delegate operations]
  backup/Storage
  (store [_ key buffer]
    (if (str/starts-with? key "roots/")
      (let [fault (storage-error
                    :injected-root-store-failure
                    "Injected Stage 2 failure while storing backup roots"
                    {:key key})]
        (record-operation! operations :store :injected-failure key {})
        (throw fault))
      (backup/store delegate key buffer)))
  (retrieve [_ key] (backup/retrieve delegate key))
  (exists? [_ key] (backup/exists? delegate key))
  (list-keys [_ prefix] (backup/list-keys delegate prefix))

  OperationLogged
  (operation-log-atom [_] operations))

(defn root-store-failure-storage
  "Wraps a Storage and deterministically rejects writes below `roots/`.
  This models a backup interrupted after values are copied but before its
  restore point becomes visible."
  ([storage]
   (root-store-failure-storage storage (new-operation-log)))
  ([storage operations]
   (->RootStoreFailureStorage storage operations)))

(defrecord MissingRetrieveStorage
  [delegate missing-key? selector-description operations]
  backup/Storage
  (store [_ key buffer] (backup/store delegate key buffer))
  (retrieve [_ key]
    (if (missing-key? key)
      (do
        (record-operation!
          operations :retrieve :injected-miss key
          {:selector selector-description})
        nil)
      (backup/retrieve delegate key)))
  (exists? [_ key] (backup/exists? delegate key))
  (list-keys [_ prefix] (backup/list-keys delegate prefix))

  OperationLogged
  (operation-log-atom [_] operations))

(defn- missing-selector
  [selector]
  (cond
    (fn? selector)
    [selector :predicate]

    (string? selector)
    (do
      (validate-key! selector)
      [#{selector} [selector]])

    (or (set? selector) (sequential? selector))
    (let [keys (set selector)]
      (doseq [key keys] (validate-key! key))
      [keys (vec (sort keys))])

    :else
    (throw (storage-error
             :invalid-missing-selector
             "Missing-retrieve selector must be a key, collection of keys, or predicate"
             {:selector-type (some-> selector class .getName)}))))

(defn missing-retrieve-storage
  "Wraps a Storage and returns nil from retrieve for selected full keys.

  exists? and list-keys deliberately continue to delegate.  The wrapper is
  intended for interrupted-restore tests; physical missing-backup tests
  should operate on a disposable copy of the filesystem tree."
  ([storage selector]
   (missing-retrieve-storage storage selector (new-operation-log)))
  ([storage selector operations]
   (let [[predicate description] (missing-selector selector)]
     (->MissingRetrieveStorage
       storage predicate description operations))))
