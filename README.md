Dies ist eine Portierung von iodine (http://code.kryo.se/iodine/)
auf Android

# Documentation

  - (german)[Anwenderdokumentation](doc/anwenderdoku.html) [(PDF)](doc/anwenderdoku.pdf)
  - (german)[Entwicklerdokumentation](doc/entwicklerdoku.html) [(PDF)](doc/entwicklerdoku.pdf)

# Building

``` bash
(cd jni && ndk-build)
gradle build
```

# Authors and License
Android Iodine Copyright (c) 2013 Yves Fischer <yvesf+andiodine@xapek.org>
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
