(ns mobile-patient.screen.settings
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))


(defn SettingsScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1}}
   [ui/text "Settings Page"]])
