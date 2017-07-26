[![Build Status](https://travis-ci.org/Aidbox/mobile-patient.svg?branch=master)](https://travis-ci.org/Aidbox/mobile-patient)

## Test users

Login: patient@com.com Password: patient

Login: practitioner@com.com Password: practitioner


## Development
lein with-profile +patient figwheel android

lein with-profile +practitioner figwheel android

If you use spacemacs you need to connct to n-repl
`SPC-m-s-c` opens connect dialog
enter
```
Host: localhost
Port for localhost: 7003
```
Then open repl `SPC-m-s-s` and enter command (cljs-repl) to swith to cljs mode.
Now you have your repl set up.


## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

