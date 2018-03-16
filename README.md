# [NiFi MELT] -  Memiiso ELT

[![Build Status](https://travis-ci.org/ismailsimsek/nifi-melt.svg?branch=master)](https://travis-ci.org/ismailsimsek/nifi-melt)

### General

initiative to add ELT processors to Apache Nifi. idea is adding pushdown reusable SQL processors.


## Contributing
If you want to contribute to a project and make it better, your help is very welcome. Contributing is also a great way to learn more about social coding on Github, new technologies and and their ecosystems and how to make constructive, helpful bug reports, feature requests and the noblest of all contributions: a good, clean pull request.

### Build Instructions

        cd nifi-melt
        mvn clean package -DskipTests
        # copy following two files to your nifi/lib/ directory. 
        cp -f nifi-melt-nar/target/nifi-melt-nar-1.0-SNAPSHOT.nar /opt/nifi/lib/
        cp -f nifi-melt-service-api-nar/target/nifi-melt-service-api-nar-1.0-SNAPSHOT.nar /opt/nifi/lib/

#### Application

## License

Copyright (c) 2018. Memiiso and The NiFi MELT Authors

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
