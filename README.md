![Logo](src/main/res/drawable/logo.png)

This is a port of iodine (http://code.kryo.se/iodine/) to the Android VPN Framework.
It doesn't require "root" on the phone.

# Documentation

The following two documents exist in German language only:
  - [Anwenderdokumentation](doc/anwenderdoku.pdf)
  - [Entwicklerdokumentation](doc/entwicklerdoku.pdf)

# Building

Requires the Android SDK and NDK to be installed.

``` bash
./gradlew build
```

Then find the APK files in folder `build/outputs/apk/`

# Authors and License
Android Iodine Copyright (c) 2016 Yves Fischer <yvesf+andiodine@xapek.org> and contributors
Same license as iodine.

Iodine Copyright (c) 2006-2009 Bjorn Andersson <flex@kryo.se>, Erik Ekman <yarrick@kryo.se>
Also major contributions by Anne Bezemer.

Permission to use, copy, modify, and distribute this software for any purpose
with or without fee is hereby granted, provided that the above copyright notice
and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.


MD5 implementation by L. Peter Deutsch (license and source in src/md5.[ch])
Copyright (C) 1999, 2000, 2002 Aladdin Enterprises.  All rights reserved.
