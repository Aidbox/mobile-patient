(ns mobile-patient.model.chat)

(defn get-participants-set [chat]
  (->> chat
       :participants
       (filter #(= "User" (:resourceType %)))
       (map :id)
       set))

(defn other-participant-id [chat domain-user]
  {:post [(string? %)]}
  (let [participants (->> (:participants chat) (map :id) set)]
    (first (disj participants (:id domain-user)))))

