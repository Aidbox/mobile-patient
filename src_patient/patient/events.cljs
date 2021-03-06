(ns patient.events
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe reg-event-fx reg-event-db]]
            [mobile-patient.lib.interceptor :refer [validate-spec]]
            [mobile-patient.ui :as ui]
            [clojure.string :as str]
            [mobile-patient.lib.helper :as h]
            [mobile-patient.lib.services :as service]
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
       :dispatch [:do-load-patient]}

      {:when     :seen?
       :events   [:success-load-patient]
       :dispatch [:do-check-demographics]}

      {:when   :seen-any-of?
       :events [:success-check-demographics :success-submit-demographics]
       :dispatch-n '([:do-load-practitioners]
                     [:do-get-chats]
                     [:do-load-medication-statements]
                     [:do-load-vitals-sign])}

      {:when :seen-both?
       :events [:success-load-practitioners :success-get-chats]
       :dispatch [:do-check-practice-group-chat-exists]}

      {:when :seen?
       :events [:success-load-medication-statements]
       :dispatch [:set-current-screen :main]}
      ]}}))

;;
;; load-patient
;;
(reg-event-fx
 :do-load-patient
 (fn [_ [_]]
   (let [user-ref @(subscribe [:user-ref])]
     (assert user-ref)
     {:fetch {:uri (str "/Patient/" user-ref)
                    :success :success-load-patient
                    :opts {:method "GET"}}})))

(reg-event-db
 :success-load-patient
 validate-spec
 (fn [db [_ patient-data]]
   (-> db
       (assoc :patient-data patient-data) ;; legacy
       (assoc :patient-id (:id patient-data))
       (assoc :patients{:status :succeed
                        :patients-data {(:id patient-data) patient-data}})))) ;; redo as service

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
   (let [user @(subscribe [:user])
         patient-data @(subscribe [:patient])]
     {:fetch {:uri (str "/Patient/" (get-in user [:ref :id]))
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
   (-> db
       (assoc :patient-data patient-data)
       (assoc-in [:patients (:id patient-data)] patient-data))))

;;
;; check-practice-group-exists
;;
(reg-event-fx
 :do-check-practice-group-chat-exists
 (fn [_ _]
   (let [chats @(subscribe [:chats])
         practice-group (->> chats (filter #(= "practice-group" (:name %))) first)]
     (if practice-group
       {:dispatch [:success-check-practice-group-exists]}
       {:dispatch [:do-create-practice-group]}
       ))))

(reg-event-db
 :success-check-practice-group-exists
 (fn [db _]
   db))


;;
;; create-practice-group
;;
(reg-event-fx
 :do-create-practice-group
 (fn [_ _]
   (let [patient @(subscribe [:domain-user])
         practitioners @(subscribe [:practitioners-data])
         participants (concat [patient] (vals practitioners))
         group {:resourceType "Chat"
                :name "practice-group"
                :participants (map #(hash-map :id (:id %)
                                              :resourceType (:resourceType %))
                                   participants)}]
     (assert (not-empty practitioners))
     {:fetch {:uri "/Chat"
              :success :success-create-practice-group
              :opts {:method "POST"
                     :headers {"content-type" "application/json"}
                     :body (js/JSON.stringify (clj->js group))}}})))

(reg-event-db
 :success-create-practice-group
 (fn [db [_ resp]]
   db))



;;
;; load-practitioners
;;
(reg-event-fx
  :do-load-practitioners
  (fn [_ _]
    {:dispatch [:load-practitioners
                {}]}))

(service/reg-get-service
  :load-practitioners
  [:practitioners]
  {:uri "/Practitioner"}
  :mutator #(list-to-map-by-id (map :resource %)))

