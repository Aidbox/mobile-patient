(ns mobile-patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [reg-event-db after reg-event-fx reg-fx
                          dispatch subscribe reg-sub-raw reg-sub]]
   [mobile-patient.color :as color]
   [re-frame.loggers :as rf.log]
   [mobile-patient.ui :as ui]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [mobile-patient.db :as db :refer [app-db]]
   [goog.json]
   [goog.crypt.base64]))

(def warn (js/console.warn.bind js/console))
(rf.log/set-loggers!
 {:warn (fn [& args]
          (cond
            (= "re-frame: overwriting" (first args)) nil
            :else (apply warn args)))})

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Views----------------------------------------------------------



;; -- Subscriptions----------------------------------------------------------
(reg-sub-raw
 :user-id
 (fn [db _] (reaction (get-in @db [:user :id]))))

(reg-sub-raw
 :user-ref
 (fn [db _] (reaction (get-in @db [:user :ref]))))

(reg-sub
 :contacts
 (fn [db _] (:contacts db)))

(reg-sub
 :users
 (fn [db _] (:users db)))

(reg-sub
 :chats
 (fn [db _] (:chats db)))

(reg-sub
 :chat
 (fn [db _] (:chat db)))

(reg-sub
 :messages
 (fn [db _] (:messages db)))

(reg-sub
 :message
 (fn [db _] (:message db)))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(reg-event-db
 :on-chats
 (fn [db [_ value]]
   (let [chats (map :resource (:entry value))]
     (if (not (= (:chats db) chats))
       (assoc db :chats chats)
       db))))

(reg-event-db
 :set-chat
 (fn [db [_ chat]]
   (-> db
       (assoc :chat chat)
       (assoc :messages []))))

(reg-event-db
 :set-message
 (fn [db [_ value]]
   (assoc db :message value)))

(reg-event-db
 :on-messages
 (fn [db [_ value]]
   (let [messages (map :resource (:entry value))]
     (if (not (= (:messages db) messages))
       (assoc db :messages messages)
       db))))

(reg-event-fx
 :on-send-message
 (fn [db [_ value]]
   {}))

(defn parms->query [parms]
  (if parms
    (str "?"
         (clojure.string/join
          "&"
          (map #(str
                 (js/encodeURIComponent (name (first %)))
                 "="
                 (js/encodeURIComponent (second %)))
               parms)))
    nil))

(defn query->params [str-params]
  "Get string of key=params separated by &
   Returns map of keys to params"
  (->> (str/split str-params #"[&=]")
       (map-indexed #(if (even? %1) (keyword %2) %2))
       (apply hash-map)))

(reg-fx
 :fetch
 (fn [{:keys [:uri :opts :success :success-parms]}]
   (let [base-url (subscribe [:get-in [:config :base-url]])
         response (atom nil)]
     (-> (js/fetch (str @base-url uri (parms->query (:parms opts)))
                   (clj->js (merge {:redirect "manual"
                                    :method "GET"
                                    :headers {"Content-Type" "application/json"}}
                                   opts)))
         (.then (fn [x]
                  (reset! response x)
                  (if (str/starts-with? (-> x (aget "headers") (.get "content-type"))
                                        "application/json")
                    (.json x)
                    (.text x))))
         (.then
          (fn [response-body]
            (when success
              (dispatch [success (if (= (type response-body) js/Object)
                                   (js->clj response-body :keywordize-keys true)
                                   response-body)
                         success-parms
                         @response]))))
         (.catch #(println "Fetch error" %)))
     {})))

(reg-event-fx
 :get-chats
 (fn [_]
   (let [user (subscribe [:user-id])]
     {:fetch {:uri "/Chat"
              :success :on-chats
              :opts {:parms {:participant @user}}}})))

(reg-event-fx
 :get-messages
 (fn [_ [_ chat-id]]
   (let [user (subscribe [:user-id])]
     {:fetch {:uri "/Message"
              :success :on-messages
              :opts {:parms {:chat chat-id}}}})))

(reg-event-fx
 :send-message
 (fn [_]
   (let [message @(subscribe [:get-in [:message]])
         user @(subscribe [:user-id])
         chat @(subscribe [:get-in [:chat]])
         msg {:resourceType "Message"
              :body message
              :chat {:id (:id chat)
                     :resourceType "Chat"}
              :author {:id user
                       :resourceType "User"}}]
     (if (and message (not (clojure.string/blank? message)))
       {:fetch {:uri "/Message"
                :success :on-send-message
                :opts {:method "POST"
                       :headers {"content-type" "application/json"}
                       :body (.stringify js/JSON (clj->js msg))}}
        :dispatch [:set-message ""]}
       {}))))

(reg-event-fx
 :create-chat
 (fn [_ [_ participants]]
   (let [user @(subscribe [:user-id])
         chat-name (first participants) ; todo: correct chat name
         chat {:resourceType "Chat"
               :name chat-name
               :participants (map (fn [p] {:id p :resourceType "User"}) (conj participants user))}]
     {:fetch {:uri "/Chat"
              :opts {:method "POST"
                     :headers {"content-type" "application/json"}
                     :body (.stringify js/JSON (clj->js chat))}}})))


(reg-event-db
 :set-contacts
 (fn [db [_ value ids]]
   (let [users (map :resource (:entry value))
         contacts (filter #((set ids) (-> % :ref :id)) users)]
     (assoc db :contacts contacts))))

(reg-event-fx
 :on-get-users
 (fn [cofx [_ value]]
   {:db (assoc (:db cofx) :users (map :resource (:entry value)))
    :dispatch [:get-contacts]}))

(reg-event-fx
 :set-user
 (fn [cofx [_ value]]
   {:db (->  (:db cofx)
             (assoc :user value)
             (assoc :chats [])
             (assoc :contacts []))
    :dispatch [:on-set-user]}))

(reg-event-fx
 :on-set-user
 (fn [_]
   (let [user-ref @(subscribe [:user-ref])]
     (case (:resourceType user-ref)
       "Patient" {:fetch {:uri (str "/Patient/" (:id user-ref))
                          :success :on-get-patient
                          :opts {:method "GET"}}}
       "Practitioner" {:fetch {:uri "/Patient"
                               :success :on-get-patients
                               :opts {:method "GET"}}}
       {}))))

(reg-event-fx
 :on-get-patient
 (fn [_ [_ value]]
   (let [prac-ids (map :id (filter #(= (:resourceType %) "Practitioner") (:generalPractitioner value)))]
     {:fetch {:uri "/User"
              :success :set-contacts
              :success-parms prac-ids
              :opts {:method "GET"}}})))

(reg-event-fx
 :on-get-patients
 (fn [_ [_ value]]
   (let [pat-ids (map #(-> % :resource :id) (:entry value))]
     {:fetch {:uri "/User"
              :success :set-contacts
              :success-parms pat-ids
              :opts {:method "GET"}}})))

(defn get-username-from-token [token]
  (let [[header payload signature] (clojure.string/split token #"\.")]
    (-> payload
        goog.crypt.base64/decodeString
        goog.json.parse
        (aget "email"))))

(reg-event-fx
 :on-login
 (fn [_ [_ resp-body _ resp]]
   (let [invalid (boolean (re-find #"Wrong credentials" resp-body))]
     (if invalid
       (ui/alert "" "Wrong credentials")
       (let [auth-data (-> (.-url resp) (str/split #"#") second query->params)
             id-token (:id_token auth-data)]
         {:db {:current-screen :chat
               :access-token (:access_token auth-data)
               :user (get-username-from-token id-token)}})))))

(reg-event-fx
 :login
 (fn [_ [_ login password]]
   (let []
     {:fetch {:uri "/oauth2/authorize"
              :success :on-login
              :opts {:parms {:response_type "id_token token"
                             :client_id "sansara"
                             :state "state"
                             :scope "openid"}
                     :method "POST"
                     :headers {"Content-Type" "application/x-www-form-urlencoded"}
                     :body (str "email=" (js/encodeURIComponent login)
                                "&password=" (js/encodeURIComponent password))}
              }})))

(reg-event-db
 :set-current-screen
 (fn [db [_ screen]]
   (assoc db :current-screen screen)))
