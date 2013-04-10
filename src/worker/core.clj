
(ns worker.core)

(def workers (ref {}))

(defn- worker-id
  "Create a valid worker ID (needs to be a vector)"
  [id]
  (if (vector? id)
    id
    (vector id)))

;; Public
;; ------

(defn worker-get
  "Try and fetch a future for the worker if it exists"
  [id]
  (get-in @workers id))

(defn worker-set
  "Store the future for the specified worker"
  [id f]
  (dosync
    (alter workers assoc-in id f)))

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

