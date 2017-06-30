(ns mobile-patient.screen.meds
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]))

(defn medication [med-st]
  (or (-> med-st :medication :CodeableConcept :coding first :display)
      (get-in med-st [:medication :CodeableConcept :text])))

(defn MedsScreen [{:keys [navigation]}]
  (let [active @(subscribe [:active-medication-statements])
        other @(subscribe [:other-medication-statements])]
    [ui/table-view
     [ui/text "Active"]
     [ui/section
      (for [med-st (map-indexed vector active)]
        ^{:key (first med-st)}
        [ui/cell {:title (medication (last med-st)) :on-press #()}])
      ;; [ui/cell {:title "Medication 1" :on-press #()}]
      ;; [ui/cell {:title "Medication 2" :on-press #()}]
      ;; [ui/cell {:title "Medication 3" :on-press #()}]
      ]]))
