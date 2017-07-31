[![Build Status](https://travis-ci.org/Aidbox/mobile-patient.svg?branch=master)](https://travis-ci.org/Aidbox/mobile-patient)

## Test users

Login: patient@com.com Password: patient

Login: practitioner@com.com Password: practitioner


## Development
This instruction is written for MacOS, but you can use Linux for the app development. However, you can run only an Android version on Linux.
In case of using Linux, consider using native setup tools such as `apt` or `yum` or whatever else.
Also, you can use `brew` to setup all stuff in MacOS. But the following instruction uses the simplest way to download pkg and install it via native MacOS tools.
If you start from scratch, grab some coffee, sit back, relax and enjoy while all this stuff will be downloaded and installed on your computer. 
### Setup Clojure tooling
Install jdk http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html  
Install leiningen https://leiningen.org/#install

### Setup Javascript tooling
Install nodejs https://nodejs.org/en/download/current/  
Install yarn https://yarnpkg.com/en/docs/install  

### Setup global dependencies
Install React Native command line tools `yarn global add react-native-cli`  
Install re-natal clojureScript wrapper for React Native `yarn global add re-natal`  


### Compile and run iOS version
#### Setup Xcode
The easiest way to install Xcode is via the [Mac App Store](https://itunes.apple.com/us/app/xcode/id497799835?mt=12).  
Installing Xcode will also install the iOS Simulator and all the necessary tools to build your iOS app.
#### Compile and run the app
Configure build target `re-natal use-ios-device simulator`  
Generate React Native entry point for ClojureScript `re-natal use-figwheel`   
Compile and run app on iOS simulator `react-native run-ios`  
Run repl for the patient app `lein with-profile +patient figwheel ios`  
[Here](https://github.com/drapanjanas/re-natal/tree/v0.5.0#ios) you can find more details about running the app in dev mode, including running the app on a real device. 

### Compile and run Android version
TODO

### Practitioner mode
To launch the app in the practitioner mode, please select `practitioner` profile for figwheel
```
lein with-profile +practitioner figwheel ios
```
or
```
lein with-profile +practitioner figwheel android
```
### Integration with spacemacs
Press `SPC-m-s-c` for connect dialog.  
To connect to the repl enter
```
Host: localhost
Port for localhost: 7003
```
Then open repl `SPC-m-s-s` and execute command `(cljs-repl)` to swith to cljs mode.
Now you have your repl set up.

### Integration with other editors
TODO

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

