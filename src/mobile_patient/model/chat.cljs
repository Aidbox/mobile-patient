(ns mobile-patient.model.chat)

(defn get-participants-set [chat]
  (->> chat
       :participants
       (filter #(= "User" (:resourceType %)))
       (map :id)
       set))
