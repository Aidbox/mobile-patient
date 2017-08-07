(ns patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe reg-sub reg-sub-raw]]
            [mobile-patient.model.patient :as patient-model]))


(reg-sub
 :user-name
 (fn [db _]
   (patient-model/get-official-name @(subscribe [:patient]))))

(reg-sub
 :user-picture
 (fn [db _]
   (patient-model/get-photo @(subscribe [:patient]))))

(reg-sub
 :chats
 (fn [db _]
   (:chats db)))

(reg-sub
 :contacts
 (fn [db _]
   (let [gen-pract-ids @(subscribe [:get-patients-general-practitioner-ids])
         {:keys [status data]}  (:practitioners db)
         practs (->> data
                     vals
                     (map :resource)
                     (filter #((set gen-pract-ids) (:id %)))
                     )]
     (if (= status :succeed)
       {:status status
        :data practs}
       {:status status
        :data data})
     )))
