(ns practitioner.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx]]
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
            :fetch {:uri (str "/User/" user-id)
                    :opts {:method "GET"}
                    :success :load-user
                    }})))
     (ui/alert "Error" (str resp.status " " resp.statusText)))))

(reg-event-fx
 :load-user
 (fn [{:keys [db]} [_ user-data]]
   {:db (merge db {:user user-data})
    :fetch {:uri (str "/Practitioner/" (get-in user-data [:ref :id]))
            :opts {:method "GET"}
            :success :load-practitioner-patients}}))

(reg-event-fx
 :load-practitioner-patients
 (fn [{:keys [db]} [_ user-data]]
   {:db (merge db {:user user-data})
    :fetch {:uri (str "/Patient?general-practitioner=" (get-in db [:user :ref :id]))
            :success :load-all-users}}))

(reg-event-fx
 :load-all-users
 (fn [{:keys [db]} [_ patients-data]]
   {:fetch {:uri "/User"
            :opts {:method "GET"}
            :success :set-practitioner-patients
            :success-parms patients-data}}))

(reg-event-fx
 :set-practitioner-patients
 (fn [{:keys [db]} [_ all-users-data patients-data]]
   (let [pat-ids (->> patients-data
                      :entry
                      (map #(get-in % [:resource :id]))
                      set)
         filtered-users (->> all-users-data
                             :entry
                             (map #(get % :resource))
                             (filter #(pat-ids (get-in % [:ref :id]))))]
     {:db (merge db {:practitioner-patients filtered-users
                     :current-screen :main})})))
