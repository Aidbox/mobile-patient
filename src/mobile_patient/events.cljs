(ns mobile-patient.events
  (:require [re-frame.core :refer [subscribe dispatch reg-fx reg-event-fx
                                   reg-event-db reg-sub-raw reg-sub]]
            [mobile-patient.db :as db :refer [app-db]]
            [mobile-patient.lib.helper :as h]
            [clojure.string :as str]))

(reg-event-db
 :initialize-db
 (fn [_ _]
   (println app-db)
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

;;
;; get-patient-data
;;
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

;;
;; get medications for user
;;
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
