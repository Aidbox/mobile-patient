(ns mobile-patient.screen.contacts
  (:require [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.nav :as nav]))


(defn ContactsScreen [{:keys [navigation]}]
  [ui/view {:style {:flex 1}}
   [ui/text "Contacts List"]])
