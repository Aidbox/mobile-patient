(ns practitioner.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [mobile-patient.ui :as ui]
            [mobile-patient.lib.jwt :as jwt]
            [clojure.string :as str]
            [mobile-patient.lib.helper :as h]))


(reg-event-fx
 :on-login
 (fn [{:keys [db]} [_ resp-body _ resp]]
   (if resp.ok
     (let [invalid (boolean (re-find #"Wrong credentials" resp-body))]
       (if invalid
         (ui/alert "" "Wrong credentials")
         (let [auth-data (-> (.-url resp) (str/split #"#") second h/query->params)
               id-token (:id_token auth-data)
               token-data (jwt/get-data-from-token id-token)
               user-id  (:user-id token-data)]
           (assert user-id)
           {:db (merge db {:access-token (:access_token auth-data)})
            :dispatch [:boot user-id]})))
     (do
       (ui/alert "Error" (str resp.status " " resp.statusText))))))

(reg-event-fx
 :boot
 (fn [_ [_ user-id]]
   {:async-flow
    {:first-dispatch [:do-load-user user-id]
     :rules
     [
      {:when     :seen?
       :events   :success-load-user
       :dispatch-n '([:do-load-practitioner] [:do-load-all-users])}

      {:when     :seen-both?
       :events   [:success-load-practitioner :success-load-all-users]
       :dispatch [:do-load-practitioner-patients]}
      ]}}))


;; load-user
(reg-event-fx
 :do-load-user
 (fn [{:keys [db]} [_ user-id]]
   {:fetch {:uri (str "/User/" user-id)
            :success :success-load-user}}))

(reg-event-db
 :success-load-user
 (fn [db [_ user-data]]
   (assoc db :user user-data)))


;; load-practitioner
(reg-event-fx
 :do-load-practitioner
 (fn [{:keys [db]} _]
   {:fetch {:uri (str "/Practitioner/" (get-in db [:user :ref :id]))
            :success :success-load-practitioner}}))

(reg-event-db
 :success-load-practitioner
 (fn [db [_ practitioner-data]]
   (assoc db :practitioner-data practitioner-data)))


;; load-practitioner-patients
(reg-event-fx
 :do-load-practitioner-patients
 (fn [{:keys [db]} _]
   (print "- " (get-in db [:user :ref :id]))
   {:fetch {:uri (str "/Patient?general-practitioner=" (get-in db [:user :ref :id]))
            :success :success-load-practitioner-patients}}))

(reg-event-db
 :success-load-practitioner-patients
 (fn [db [_ patients-data]]
   (let [all-users-data (:all-users db)
         pat-ids (->> patients-data
                      :entry
                      (map #(get-in % [:resource :id]))
                      set)
         filtered-users (->> all-users-data
                             :entry
                             (map #(get % :resource))
                             (filter #(pat-ids (get-in % [:ref :id]))))]
     ;;(print patients-data)
     (assoc db :practitioner-patients (into {}
                                            (map #(vector (:id %) %))
                                            filtered-users) ; from list to map by id
            :current-screen :main))))


;; load-all-users
(reg-event-fx
 :do-load-all-users
 (fn [_ _]
   {:fetch {:uri "/User"
            :opts {:method "GET"}
            :success :success-load-all-users}}))

(reg-event-db
 :success-load-all-users
 (fn [db [_ all-users]]
   (assoc db :all-users all-users)))
