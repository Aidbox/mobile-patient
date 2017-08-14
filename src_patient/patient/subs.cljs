(ns patient.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [subscribe reg-sub reg-sub-raw]]
            [mobile-patient.model.core :refer [get-data-key]]
            [mobile-patient.model.patient :as patient-model]
            [mobile-patient.model.chat :as chat-model]
            ))


(reg-sub
 :domain-user
 (fn [db _]
   @(subscribe [:patient])))

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
         remote-data (:practitioners db)
         data-key (get-data-key remote-data)
         practs (->> (get remote-data data-key)
                     vals
                     #_(filter #((set gen-pract-ids) (:id %))))]
     (if (= (:status remote-data) :succeed)
       (assoc remote-data data-key practs)
       remote-data)
     )))

(reg-sub
 :personal-chats
 (fn [db _]
   (let [domain-user @(subscribe [:domain-user])
         chats (->> (:chats db)
                    (filter #(= "personal-chat" (:name %))))
         practitioners @(subscribe [:practitioners-data])
         persons-with-chat (map #(assoc (get practitioners (chat-model/other-participant-id % domain-user)) :chat %)
                                chats)]
     persons-with-chat)))
