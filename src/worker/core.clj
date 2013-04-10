
(ns worker.core)

(def workers (ref {}))

;; Public
;; ------

(defmulti worker-id
  "Return a vector for an ID"
  class)

(defn worker-get
  "Try and fetch a future for the worker if it exists"
  [id]
  (get-in @workers (worker-id id)))

(defn worker-set
  "Store the future for the specified worker"
  [id f]
  (dosync
    (alter workers assoc-in (worker-id id) f)))

(defn worker-clear
  "A worker has finished, clear it from the ref"
  [id]
  (worker-set id nil))

(defmacro worker
  "Use futures to handle waiting on tasks already being processed"
  [id & body]
  `(if-let [waiting# (worker-get ~id)]
     (with-meta @waiting# {:via-worker false})
     (let [f# (future ~@body)]
       (worker-set ~id f#)
       (try
         (with-meta @f# {:via-worker true})
         (finally
           (worker-clear ~id))))))

;; Worker ID Impls
;; ---------------

(defmethod worker-id
  java.lang.String
  [id] [id])

(defmethod worker-id
  clojure.lang.PersistentVector
  [id] id)

