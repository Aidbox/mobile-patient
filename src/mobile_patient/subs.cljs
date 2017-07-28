(ns mobile-patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [reg-sub reg-sub-raw]]
            [cljs-time.core :as time]))

;; -- Subscriptions----------------------------------------------------------
(reg-sub-raw
 :user-id
 (fn [db _] (reaction (get-in @db [:user :id]))))

(reg-sub-raw
 :user-ref
 (fn [db _] (reaction (get-in @db [:user :ref :id]))))

(reg-sub
 :users
 (fn [db _] (:users db)))

(reg-sub
 :chat
 (fn [db _] (:chat db)))

(reg-sub
 :messages
 (fn [db _] (:messages db)))

(reg-sub
 :message
 (fn [db _] (:message db)))

(reg-sub
 :active-medication-statements
 (fn [db [_ pat-ref]] (get-in db [:active-medication-statements pat-ref])))

(reg-sub
 :other-medication-statements
 (fn [db [_ pat-ref]] (get-in db [:other-medication-statements pat-ref])))

(reg-sub
  :patient-ref
  (fn [db _]
    (get-in db [:patient-data :id])))

(reg-sub
  :practitioner-ref
  (fn [db _]
    (get-in db [:practitioner-data :id])))

(reg-sub
 :get-in
 (fn [db [_ path]]
   (get-in db path)))

(reg-sub
 :get-current-screen
 (fn [db _]
   (:current-screen db)))

(reg-sub
 :get-patients-general-practitioner-ids
 (fn [db [_]]
   (->> (get-in db [:patient-data :generalPractitioner])
        (filter #(= (:resourceType %) "Practitioner"))
        (map :id))))

(defn extract[item]
  {:value (:value item)
   :code (get-in item [:code :coding 0 :code])
   :date-time (time/date-time (js/Date. (get-in item [:effective :dateTime])))})

(defn prepare-observation [data]
  (->> data
       (map :resource)
       (map extract)
       (group-by :code)
       (map (fn [[key values]] [key (sort #(time/after? (:date-time %1) (:date-time %2))values)]))
       (into {})
       ))

(reg-sub
 :get-observations
 (fn [db _]
   (let [remote-data (:observations db)]
     (if (= (:status remote-data) :succeed)
       (update-in remote-data [:data] prepare-observation)
       remote-data))))

(reg-sub
 :get-patient-ref-by-id
 (fn [db [_ user-id]]
   (-> (:practitioner-patients db)
       (get user-id)
       (get-in [:ref :id]))))
