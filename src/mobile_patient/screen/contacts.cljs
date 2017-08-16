(ns mobile-patient.screen.contacts
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch]]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.model.patient :as patient-model]
            [mobile-patient.component.person-row :refer [person-row-component]]))


(defn header-component []
  [ui/text {:style {:color color/pink
                    :font-weight :bold
                    :padding 25}}
      "Select a Person"])


(defn on-press-callback [item navigation chat-name]
  (dispatch [:create-chat item])
  (navigation.goBack nil))

(defn ContactsScreen [{:keys [navigation]}]
  (let [contacts @(subscribe [:contacts])]
    (ui/show-remote-data
     contacts
     (fn [data]
       [ui/flat-list {:style {:background-color :white}
                      :data (clj->js data)
                      :key-extractor #(.-id %)
                      :ListHeaderComponent (r/reactify-component header-component)
                      :render-item (fn [row]
                                     (r/as-element [person-row-component
                                                    (js->clj row :keywordize-keys true)
                                                    navigation
                                                    on-press-callback
                                                    "add"]))}]))))
