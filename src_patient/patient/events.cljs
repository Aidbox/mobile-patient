(ns patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [mobile-patient.ui :as ui]
            [clojure.string :as str]
            [mobile-patient.lib.helper :as h]
            [mobile-patient.lib.services :as service]))


(reg-event-fx
 :boot
 (fn [_ [_ user-id]]
   {:async-flow
    {:first-dispatch [:do-load-user user-id]
     :rules
     [
      {:when     :seen?
       :events   :success-load-user
       :dispatch-n '([:do-load-patient] [:do-load-all-users])}

      {:when     :seen-both?
       :events   [:success-load-patient :success-load-all-users]
       :dispatch [:do-check-demographics]}

      {:when   :seen-any-of?
       :events [:success-check-demographics :success-submit-demographics]
       :dispatch-n '([:do-load-medication-statements]
                     [:do-load-vitals-sign-screen])}

      {:when :seen?
       :events :success-load-medication-statements
       :dispatch [:set-current-screen :main]}

      ]}}))

;;
;; load-patient
;;
(reg-event-fx
 :do-load-patient
 (fn [_ [_]]
   {:fetch {:uri (str "/Patient/" @(subscribe [:user-ref]))
            :success :success-load-patient
            :opts {:method "GET"}}}))

(reg-event-db
 :success-load-patient
 (fn [db [_ patient-data]]
   (assoc db :patient-data patient-data)))

;;
;; check-is-set-demographics
;;
(reg-event-fx
 :do-check-demographics
 (fn [{:keys [db]} [_]]
   (let [patient-data (:patient-data db)]
     (if-not (h/contains-many? patient-data :gender :birthDate :address)
       {:dispatch [:set-current-screen :demographics]}
       {:dispatch [:success-check-demographics]}
       ))))

(reg-event-fx
 :success-check-demographics
 (fn [_ _]
   ))
;;
;; submit-demographics
;;
(reg-event-fx
 :do-submit-demographics
 (fn [_ [_ form-data]]
   (let [user @(subscribe [:get-in [:user]])
         patient-data @(subscribe [:get-in [:patient-data]])]
     {:fetch {:uri (str "/" (get-in user [:ref :resourceType ])
                        "/" (get-in user [:ref :id ]))
              :success :success-submit-demographics
              :opts {:method "PUT"
                     :headers {"content-type" "application/json"}
                     :body (js/JSON.stringify
                            (clj->js
                             (merge patient-data
                                    {:gender (:sex form-data)
                                     :birthDate (:birthday form-data)
                                     :address [{:use "home"
                                                :type "postal"
                                                :text (:address form-data)
                                                }]})))}}})))
(reg-event-db
 :success-submit-demographics
 (fn [db [_ patient-data]]
   (assoc db :patient-data patient-data)))


;;
;; load-vitals-sign-screen
;;
(reg-event-fx
  :do-load-vitals-sign-screen
  (fn [_ _]
    {:dispatch [:fetch-vitals-sign-screen-data
                {:params {:patient @(subscribe [:user-ref])}}]}))

(service/reg-get-service
  :fetch-vitals-sign-screen-data
  [:observations]
  {:uri "/Observation"})
