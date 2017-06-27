(ns mobile-patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require
   [reagent.core :as r]
   [re-frame.core :refer [reg-event-db after reg-event-fx reg-fx
                          dispatch subscribe reg-sub-raw]]
   [mobile-patient.color :as color]
   [re-frame.loggers :as rf.log]
   [mobile-patient.ui :as ui]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]
   [mobile-patient.db :as db :refer [app-db]]))

(def warn (js/console.warn.bind js/console))
(rf.log/set-loggers!
 {:warn (fn [& args]
          (println (first args))
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

(reg-sub-raw
 :users
 (fn [db _] (reaction (:users @db))))

(reg-sub-raw
 :chats
 (fn [db _] (reaction (:chats @db))))

(reg-sub-raw
 :chat
 (fn [db _] (reaction (:chat @db))))

(reg-sub-raw
 :messages
 (fn [db _] (reaction (:messages @db))))

(reg-sub-raw
 :message
 (fn [db _] (reaction (:message @db))))

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
 :set-user
 (fn [db [_ value]]
   (assoc db :user value)))

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
   (println value)
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

(reg-fx
 :fetch
 (fn [{:keys [:uri :opts :success :success-parms]}]
   (let [base-url (subscribe [:get-in [:config :base-url]])]
     (-> (js/fetch (str @base-url uri (parms->query (:parms opts)))
                   (clj->js (merge {:method "GET"
                                    :headers {"Content-Type" "application/json"}}
                                   opts)))
         (.then #(.json %))
         (.then
          (fn [response]
            (when success
              (dispatch [success (js->clj response :keywordize-keys true)]))))
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

;; uncomment and refine
;; (reg-event-fx
;;  :get-users
;;  (fn [_]
;;    (let []
;;      {:fetch {:uri "/User"
;;               :success :on-get-users
;;               :opts {:method "GET"}}})))

;; (reg-event-fx
;;  :on-get-users
;;  (fn [cofx [_ value]]
;;    {:db (assoc db :users (map :resource (:entry value)))
;;     :dispatch [:get-contacts]}))

;; (reg-event-fx
;;  :set-user
;;  (fn [cofx [_ value]]
;;    {:db (assoc db :user value)
;;     :dispatch [:get-user-ref]}))

;; и(reg-event-fx
;;  :get-user-ref
;;  (fn [_]
;;    (let [user-ref @(subscribe [:user-ref])]
;;      (case (:resourceType user-ref)
;;        "Patient" {:fetch {:uri (str "/Patient/" (:id user-ref))
;;                           :success :on-get-user-ref
;;                           :opts {:method "GET"}}}
;;        {}))))
