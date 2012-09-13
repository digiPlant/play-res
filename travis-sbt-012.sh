#!/bin/sh

rm -rf /usr/bin/sbt

wget http://files.travis-ci.org/packages/deb/sbt/sbt-0.12.0.deb

dpkg -i sbt-0.12.0.deb