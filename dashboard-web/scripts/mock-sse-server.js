#!/usr/bin/env node

/**
 * Mock SSE Server for Well Metrics Dashboard
 * Usage: node scripts/mock-sse-server.js
 * Listens on http://localhost:8083/api/notifications/stream
 */

import http from "http";

const PORT = 8083;

// Sample well IDs
const wellIds = ["POZO-001", "POZO-002", "POZO-003", "POZO-004", "POZO-005"];

// Store active connections
const connections = new Set();

/**
 * Generate random metric data
 */
function generateMetric(wellId) {
  return {
    eventName: "metric",
    id_pozo: wellId,
    temperatura: parseFloat((60 + Math.random() * 40).toFixed(2)), // 60-100°C
    pression: parseFloat((200 + Math.random() * 150).toFixed(2)), // 200-350 PSI
    flowRate: parseFloat((500 + Math.random() * 1500).toFixed(2)), // 500-2000 bbl/d
    timestamp: new Date().toISOString(),
  };
}

/**
 * Generate random alert
 */
function generateAlert(wellId) {
  return {
    eventName: "alert",
    id_pozo: wellId,
    tipo: Math.random() > 0.7 ? "ACTIVE" : "RESOLVED",
    timestamp: new Date().toISOString(),
  };
}

/**
 * Send SSE event to all connected clients
 */
function broadcastEvent(eventName, data) {
  const sseMessage = `event: ${eventName}\ndata: ${JSON.stringify(data)}\n\n`;
  console.log(
    `[${new Date().toLocaleTimeString()}] Broadcasting: ${eventName} for ${data.id_pozo}`,
  );

  connections.forEach((res) => {
    res.write(sseMessage);
  });
}

/**
 * Main server logic
 */
const server = http.createServer((req, res) => {
  // CORS headers
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type");

  if (req.method === "OPTIONS") {
    res.writeHead(204);
    res.end();
    return;
  }

  if (req.url === "/api/notifications/stream" && req.method === "GET") {
    // Set up SSE response
    res.writeHead(200, {
      "Content-Type": "text/event-stream",
      "Cache-Control": "no-cache",
      Connection: "keep-alive",
    });

    // Add connection to active set
    connections.add(res);
    console.log(
      `[${new Date().toLocaleTimeString()}] Client connected. Total: ${connections.size}`,
    );

    // Send initial comment
    res.write(": SSE Server for Well Metrics Dashboard\n\n");

    // Handle client disconnect
    req.on("close", () => {
      connections.delete(res);
      res.end();
      console.log(
        `[${new Date().toLocaleTimeString()}] Client disconnected. Total: ${connections.size}`,
      );
    });

    req.on("error", (err) => {
      console.error("Request error:", err);
      connections.delete(res);
    });
  } else if (req.url === "/health" && req.method === "GET") {
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ status: "ok", clients: connections.size }));
  } else {
    res.writeHead(404);
    res.end("Not Found");
  }
});

// Start metric broadcasting
setInterval(
  () => {
    // Send metrics every 2-3 seconds
    const randomWell = wellIds[Math.floor(Math.random() * wellIds.length)];
    const metric = generateMetric(randomWell);
    broadcastEvent("metric", metric);
  },
  2500 + Math.random() * 1000,
);

// Start alert broadcasting
setInterval(
  () => {
    // Send alerts every 10-15 seconds
    const randomWell = wellIds[Math.floor(Math.random() * wellIds.length)];
    const alert = generateAlert(randomWell);
    broadcastEvent("alert", alert);
  },
  10000 + Math.random() * 5000,
);

// Start server
server.listen(PORT, () => {
  console.log(`🚀 Mock SSE Server running on http://localhost:${PORT}`);
  console.log(
    `📡 Stream endpoint: http://localhost:${PORT}/api/notifications/stream`,
  );
  console.log(`🏥 Health check: http://localhost:${PORT}/health`);
  console.log(`\n📊 Wells being monitored: ${wellIds.join(", ")}`);
  console.log("\nPress Ctrl+C to stop the server\n");
});

// Graceful shutdown
process.on("SIGINT", () => {
  console.log("\n\n🛑 Shutting down server...");
  connections.forEach((res) => res.end());
  server.close(() => {
    console.log("Server closed");
    process.exit(0);
  });
});
