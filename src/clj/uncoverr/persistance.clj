(ns uncoverr.persistance
  (require [clj-time.core :as t]
           [clj-time.format :as f]
           [clojure.java.jdbc :refer :all]))

(defn- time-until [dt]
  (let [interval (t/interval (t/now) dt)]
    (if (= 0 (t/in-hours interval))
      (str (t/in-minutes interval) " minutes")
      (str (t/in-hours interval) " hours"))))

(defn- embargo [msg]
  (if (t/before? (t/now) (:datetime msg))
    (assoc msg
           :embargoed? true
           :message (str "This message is embargoed until " (:datetime msg) "\n"
                         "(which is "
                         (time-until (:datetime msg))
                         " from now)"))
    (assoc msg
           :embargoed? false)))

;; -------- sqlite ----------------

(def db-datetime-fmt (f/formatter "yyyy-MM-dd'T'HH:mm"))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/messages.db"
   })

(defn create-db []
  (try (db-do-commands db
                       (create-table-ddl :messages
                                         [:id :text]
                                         [:datetime :text]
                                         [:pw_cnt :int]
                                         [:title :text]
                                         [:message :text]))
       (catch Exception e (println e))))

(create-db)

(defn write-message [title message datetime pw-cnt]
  (let [id (str (java.util.UUID/randomUUID))
        title (if (= "" title) "UNTITLED" title)]
    (insert! db :messages {:id id
                           :title title
                           :message message
                           :datetime (f/unparse db-datetime-fmt datetime)
                           :pw_cnt pw-cnt})
    id))

(defn read-message [id]
  (when-let [msg (-> (query db ["select * from messages where id = ?" id])
                     first)]
    (let [msg (assoc msg :datetime (f/parse db-datetime-fmt (:datetime msg))
                     :pw-cnt (:pw_cnt msg))]
      (clojure.pprint/pprint msg)
      (embargo msg))))

(comment
  @messages
  )
