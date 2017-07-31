(ns mobile-patient.events
  (:require [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db
                                   reg-fx]]
            [re-frame.loggers :as rf.log]
            [day8.re-frame.async-flow-fx]
            [mobile-patient.model.chat :as chat-model]
            [mobile-patient.db :as db :refer [app-db]]
            [clojure.string :as str]
            [mobile-patient.lib.jwt :as jwt]
            [mobile-patient.lib.helper :as h]
            [mobile-patient.ui :as ui]))


(def warn (js/console.warn.bind js/console))
(rf.log/set-loggers!
 {:warn (fn [& args]
          (cond
            (= "re-frame: overwriting" (first args)) nil
            :else (apply warn args)))})

;; -- Handlers --------------------------------------------------------------
(reg-event-db
 :assoc-in
 (fn [db [_ path value]]
   (assoc-in db path value)))

(reg-event-db
 :initialize-db
 (fn [_ _]
   app-db))

(reg-event-db
 :set-current-screen
 (fn [db [_ screen]]
   (assoc db :current-screen screen)))

(reg-fx
 :fetch
 (fn [{:keys [base-url uri opts success success-parms spinner-id]}]
   (let [base-url (or base-url @(subscribe [:get-in [:config :base-url]]))
         response (atom nil)]
     (if spinner-id (dispatch [:spinner spinner-id true]))
     (-> (js/fetch (str base-url uri (h/parms->query (:parms opts)))
                   (clj->js (merge {:redirect "manual"
                                    :method "GET"
                                    :headers {"Content-Type" "application/json"}}
                                   opts)))
         (.then (fn [resp]
                  (reset! response resp)
                  (if resp.ok
                    (if (str/starts-with? (-> resp (aget "headers") (.get "content-type"))
                                          "application/json")
                      (.json resp)
                      (.text resp)))))
         (.then
          (fn [response-body]
            (when success
              (if spinner-id (dispatch [:spinner spinner-id false]))
              (dispatch [success (if (= (type response-body) js/Object)
                                   (js->clj response-body :keywordize-keys true)
                                   response-body)
                         success-parms
                         @response]))))
         (.catch (fn [e]
                   (if spinner-id (dispatch [:spinner spinner-id false]))
                   (println "Fetch error" (.-message e)))))
     {})))

(reg-event-db
 :spinner
 (fn [db [_ path state]]
   (assoc-in db [:spinner path] state)))

;;
;; login
;;
(reg-event-fx
 :login
 (fn [_ [_ login password]]
   (let []
     {:fetch {:uri "/oauth2/authorize"
              :spinner-id :login
              :success :on-login
              :opts {:parms {:response_type "id_token token"
                             :client_id "sansara"
                             :state "state"
                             :scope "openid"}
                     :method "POST"
                     :headers {"Content-Type" "application/x-www-form-urlencoded"}
                     :body (str "email=" (js/encodeURIComponent login)
                                "&password=" (js/encodeURIComponent password))}}})))

(reg-event-fx
 :on-login
 (fn [{:keys [db]} [_ resp-body _ resp]]
   ;;(print (js/JSON.stringify resp))
   ;;(if resp.ok
     (let [invalid (boolean (re-find #"Wrong credentials" (str resp-body)))]
       (if invalid
         (ui/alert "" "Wrong credentials")
         (let [auth-data (-> (.-url resp) (str/split #"#") second h/query->params)
               id-token (:id_token auth-data)
               token-data (jwt/get-data-from-token id-token)
               user-id  (:user-id token-data)]
           (assert user-id)
           {:db (merge db {:access-token (:access_token auth-data)})
            :dispatch [:boot user-id]})))
     #_(do
         (ui/alert "Error" (str resp.status " " resp.statusText)))
   ;;  )
   ))

;;
;; load-user
;;
(reg-event-fx
 :do-load-user
 (fn [{:keys [db]} [_ user-id]]
   {:fetch {:uri (str "/User/" user-id)
            :success :success-load-user}}))

(reg-event-db
 :success-load-user
 (fn [db [_ user-data]]
   (assoc db :user user-data)))

;;
;; load-all-users
;;
(reg-event-fx
 :do-load-all-users
 (fn [_ _]
   {:fetch {:uri "/User"
            :opts {:method "GET"}
            :success :success-load-all-users}}))

(reg-event-db
 :success-load-all-users
 (fn [db [_ all-users]]
   (assoc db :all-users (->> all-users :entry (map :resource)))))

;;
;; do-load-medication-statements
;;
(reg-event-fx
 :do-load-medication-statements
 (fn [{:keys [db]} [_]]
   (let [patient-ref @(subscribe [:patient-ref])]
     {:fetch {:uri "/MedicationStatement"
              :success :success-load-medication-statements
              :opts {:parms {:subject patient-ref}
                     :method "GET"}}})))

(reg-event-db
 :success-load-medication-statements
 (fn [db [_ med-stms]]
   (let [patient-ref @(subscribe [:patient-ref])
         medication-statements (sort-by #(-> % :effective :dateTime) (map :resource (:entry med-stms)))
         groups (group-by #(= (:status %) "active") medication-statements)]
     (-> db
         (assoc-in [:active-medication-statements patient-ref] (groups true))
         (assoc-in [:other-medication-statements patient-ref] (groups false))))))


;; OLD

;; get medications for user
(reg-event-fx
 :get-medication-statements
 (fn [{:keys [db]}  _]
   (let [patient-ref @(subscribe [:patient-ref])]
     {:fetch {:uri "/MedicationStatement"
              :spinner-id :load-patient-data
              :success :set-medication-statements
              :success-parms patient-ref
              :opts {:parms {:subject patient-ref}
                     :method "GET"}}})))
(reg-event-db
 :set-medication-statements
 (fn [db [_ med-stms patient-ref]]
   (let [medication-statements (sort-by #(-> % :effective :dateTime) (map :resource (:entry med-stms)))
         groups (group-by #(= (:status %) "active") medication-statements)]
     (-> db
         (assoc-in [:active-medication-statements patient-ref] (groups true))
         (assoc-in [:other-medication-statements patient-ref] (groups false))))))

;; get-patient-data
(reg-event-fx
 :get-patient-data
 (fn [_ [_ user-ref where-to-go]]
   {:fetch {:uri (str "/Patient/" user-ref)
            :success :set-patient-data
            :success-parms where-to-go
            :opts {:method "GET"}}}))

(reg-event-fx
 :set-patient-data
 (fn [{:keys [db]} [_ patient-data where-to-go]]
   {:db (merge db {:patient-data patient-data})
    :dispatch [where-to-go]}))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CHAT
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn dispatch-get-chats-event []
  (dispatch [:get-chats]))

(defn dispatch-get-messages-event []
  (let [chat (subscribe [:chat])]
    (when chat
      (dispatch [:get-messages (:id @chat)]))))

(defonce do-get-chats (js/setInterval dispatch-get-chats-event 3000))
(defonce do-get-messages (js/setInterval dispatch-get-messages-event 1000))


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

(reg-event-fx
 :on-send-message
 (fn [db [_ value]]
   {}))

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


(reg-event-fx
 :on-get-users
 (fn [cofx [_ value]]
   {:db (assoc (:db cofx) :users (map :resource (:entry value)))
    :dispatch [:get-contacts]}))




(reg-event-fx
 :get-chats
 (fn [_]
   (let [user (subscribe [:user-id])]
     {:fetch {:uri "/Chat"
              :success :on-chats
              :opts {:parms {:participant @user}}}})))

(reg-event-db
 :on-chats
 (fn [db [_ value]]
   (let [chats (map :resource (:entry value))]
     (if (not (= (:chats db) chats))
       (assoc db :chats chats)
       db))))

(reg-event-db
 :on-messages
 (fn [db [_ value]]
   (let [messages (map :resource (:entry value))]
     (if (not (= (:messages db) messages))
       (assoc db :messages messages)
       db))))

(reg-event-fx
 :get-messages
 (fn [_ [_ chat-id]]
   (let [user (subscribe [:user-id])]
     {:fetch {:uri "/Message"
              :success :on-messages
              :opts {:parms {:chat chat-id}}}})))

(reg-event-db
 :get-chats-by-participants
 (fn [db [_ participants]]
   (->> (:chats db)
        (filter #(clojure.set/subset? (set participants)
                                      (chat-model/get-participants-set %)))
        )))
