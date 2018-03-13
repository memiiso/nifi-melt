#!/bin/bash
echo "## Building the app..."
mvn clean package -DskipTests
echo "## Deploy to nifi server..."
cp -f nifi-melt-nar/target/nifi-melt-nar-1.0-SNAPSHOT.nar /opt/nifi-1.5.0/lib/
echo "## Restart..."
/opt/nifi-1.5.0/bin/nifi.sh restart
echo "## Done"
