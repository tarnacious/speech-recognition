# Voice Recognition Demo

*Please note this demo is in a very early stage of development and far from
complete. Almost everything is solved with the quickest hack I could find to
get it working. That being said the basic recording and speech recognition is
working.*

A demo website that analyses speech captured with the users microphone and
attempts to detect specific words.

This is a small personal project to investigate the current state of voice
open-source voice recognition and is built using the [CMU Sphinx][cmu-sphinx]
project.

### Requirements

[leiningen][leiningen]
[sox][sox]
[maven][maven]

### Building

There are some problems downloading the [sphinx4-data][sphinx4-data] package
from oss.sonatype.org repository. Fortunately downloading the source, building
and installing it solves this problem and ensures we are using the [latest
code][sphinx4-source].

    git clone https://github.com/cmusphinx/sphinx4.git
    cd sphinx
    mvn install

The project uses the leiningen build tool so running should be as simple as:

    lein ring server

[cmu-sphinx]: http://cmusphinx.sourceforge.net/
[sphinx4-data]: https://oss.sonatype.org/#nexus-search;quick~sphinx4-data
[leiningen]: http://leiningen.org/
[sox]: http://sox.sourceforge.net/
[maven]: https://maven.apache.org/
[sphinx4-source]: https://github.com/cmusphinx/sphinx4
