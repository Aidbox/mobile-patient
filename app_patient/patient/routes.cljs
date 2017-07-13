(ns patient.routes
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [mobile-patient.ui :as ui]
            [mobile-patient.color :as color]
            [mobile-patient.route-helpers :as rh]
            [mobile-patient.screen.login :refer [LoginScreen]]
            [mobile-patient.screen.demographics :refer [DemographicsScreen]]
            [mobile-patient.screen.vitals :refer [VitalsScreen]]
            [patient.screen.meds :refer [MedsScreen]]
            [mobile-patient.screen.chat :refer [ChatsScreen ChatScreen]]
            [mobile-patient.screen.settings :refer [SettingsScreen]]
            [mobile-patient.screen.contacts :refer [ContactsScreen]]
            [mobile-patient.screen.chart :refer [ChartScreen]]))

(defn dumb-component [text]
  (r/reactify-component (fn [] [ui/text text])))

(def drawer-routes
  (ui/DrawerNavigator
   (clj->js
    {"Medications" {:screen (rh/stack-navigator
                             {"Meds" {:screen (r/reactify-component MedsScreen)
                                      :navigationOptions (rh/drawer-nav-opts "Medications")}})}

     "Vitals Signs" {:screen (rh/stack-navigator
                              {"Vitals" {:screen (r/reactify-component VitalsScreen)
                                         :navigationOptions (rh/drawer-nav-opts "Vitals Signs")}

                               "ExampleChart" {:screen (r/reactify-component ChartScreen)
                                               :navigationOptions (fn [props]
                                                                    #js{:title "Example Chart"
                                                                        :headerLeft (rh/stack-navigator-back-button props)
                                                                        :headerTitleStyle #js{:fontWeight "normal"
                                                                                              :color "#6e6e6e"}})}})}

     "Chats" {:screen (rh/stack-navigator
                       {"Chats" {:screen (r/reactify-component ChatsScreen)
                                 :navigationOptions (rh/drawer-nav-opts "Chats" rh/add-chat-button)}

                        "Chat" {:screen (r/reactify-component ChatScreen)
                                :navigationOptions
                                (fn [props]
                                  (let [chat-name (-> props .-navigation .-state .-params (aget "chat-name"))]
                                    #js {:title chat-name
                                         :headerLeft (rh/stack-navigator-back-button props)
                                         :headerTitleStyle #js{:fontWeight "normal"
                                                               :color "#6e6e6e"}}))}

                        "Contacts" {:screen (r/reactify-component ContactsScreen)
                                    :navigationOptions
                                    (fn [props]
                                      #js{:title "Contacts"
                                          :headerLeft (rh/stack-navigator-back-button props)
                                          :headerTitleStyle #js{:fontWeight "normal"
                                                                :color "#6e6e6e"}})}})}

     "Settings" {:screen (rh/stack-navigator
                          {"Settings" {:screen (r/reactify-component SettingsScreen)
                                       :navigationOptions (rh/drawer-nav-opts "Settings")}})}

     "About App" {:screen (rh/stack-navigator
                           {"About App" {:screen (dumb-component "About App")
                                         :navigationOptions (rh/drawer-nav-opts "About App")}})}})
   (clj->js
    {:drawerWidth 300
     :contentComponent (fn [props]
                         (r/as-element [rh/drawer-content props]))})))

;; On navigation chnage handler
(defn on-navigation-callback [prev-state new-state action]
  (let [action (js->clj action :keywordize-keys true)
        route-name (:routeName action)]
    (case route-name
      "Medications" (rf/dispatch [:get-medication-statements])
      "Vitals Signs" (rf/dispatch [:on-vitals-sign-screen])
      "Contacts" (rf/dispatch [:load-contacts])
      nil)))
