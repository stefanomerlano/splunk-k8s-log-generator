#!/bin/bash
set -e

# Ensure log directory and file exist
mkdir -p /var/log/myapp
touch /var/log/myapp/app.log

# Start Splunk Universal Forwarder in background
echo "Starting Splunk Universal Forwarder..."
/opt/splunkforwarder/bin/splunk start --accept-license --answer-yes --no-prompt --seed-passwd "SplunkAdmin123!"

# Pause 5 seconds for service initialization
echo "Waiting 5 seconds..."
sleep 5

# Add file monitor for application log
echo "Adding monitor for log file..."
/opt/splunkforwarder/bin/splunk add monitor /var/log/myapp/app.log -auth admin:SplunkAdmin123!

SPLUNK_HOST="${SPLUNK_HOST:-splunk-service.splunk.svc.cluster.local}"
SPLUNK_PORT="${SPLUNK_PORT:-9997}"

# Configure forward server to target host
echo "Adding forward server ${SPLUNK_HOST}:${SPLUNK_PORT}..."
until /opt/splunkforwarder/bin/splunk add forward-server "${SPLUNK_HOST}:${SPLUNK_PORT}" -auth admin:SplunkAdmin123!; do
  echo "Splunk server not ready, retrying in 5 seconds..."
  sleep 5
done

# Launch Java application in foreground
echo "Starting Java LogGenerator application..."
exec java LogGenerator
