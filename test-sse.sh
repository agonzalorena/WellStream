#!/bin/bash
echo "🔗 Conectando a SSE..."
curl -N \
  -H "Accept: text/event-stream" \
  -H "Cache-Control: no-cache" \
  http://localhost:8083/api/notifications/stream | while IFS= read -r line; do
  echo "[$(date +'%H:%M:%S')] $line"
done
