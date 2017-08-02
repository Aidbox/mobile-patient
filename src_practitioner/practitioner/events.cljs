(ns practitioner.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [mobile-patient.ui :as ui]
            [clojure.string :as str]
            [mobile-patient.lib.helper :as h]
            [mobile-patient.model.core :refer [list-to-map-by-id]]))

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

      {:when :seen?
       :events :success-load-practitioner-patients
       :dispatch [:set-current-screen :main]}
      ]}}))


;; load-practitioner
(reg-event-fx
 :do-load-practitioner
 (fn [{:keys [db]} _]
   (let [user-ref @(subscribe [:user-ref])]
     (assert user-ref)
     {:fetch {:uri (str "/Practitioner/" user-ref)
              :success :success-load-practitioner}})))

(reg-event-db
 :success-load-practitioner
 (fn [db [_ practitioner-data]]
   (assoc db :practitioner-data practitioner-data)))


;; load-practitioner-patients
(reg-event-fx
 :do-load-practitioner-patients
 (fn [{:keys [db]} _]
   (let [user-ref @(subscribe [:user-ref])]
     (assert user-ref)
     {:fetch {:uri (str "/Patient?general-practitioner=" user-ref)
              :success :success-load-practitioner-patients}})))

(reg-event-db
 :success-load-practitioner-patients
 (fn [db [_ raw-patients-data]]
   (let [user-ref->user-name (into {} (map #(vector (get-in % [:ref :id]) (:id %))
                                           (:users db)))
         patients-data (->> raw-patients-data
                            :entry
                            (map :resource)
                            (map #(assoc % :username
                                         (user-ref->user-name (:id %)))))]
     (assoc db :practitioner-patients (list-to-map-by-id patients-data)))))


;;
(reg-event-db
 :set-current-patient
 (fn [db [_ id]]
   (assoc db :patient-data
          (get-in db [:practitioner-patients id]))))
