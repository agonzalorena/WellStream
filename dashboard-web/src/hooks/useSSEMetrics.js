import { useEffect, useRef, useState } from "react";

const SSE_URL = "http://localhost:8083/api/notifications/stream";
const MAX_HISTORY = 30;

export const useSSEMetrics = () => {
  const [wells, setWells] = useState(new Map());
  const [isConnected, setIsConnected] = useState(false);
  const eventSourceRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);

  useEffect(() => {
    let mounted = true;

    const connectSSE = () => {
      if (!mounted) return;

      // Limpiar conexión anterior
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }

      console.log("🔌 Conectando a SSE...", SSE_URL);
      setIsConnected(false);

      try {
        const es = new EventSource(SSE_URL);

        es.onopen = () => {
          if (!mounted) {
            es.close();
            return;
          }
          console.log("✅ SSE Conectado");
          setIsConnected(true);
        };

        es.addEventListener("metric", (event) => {
          if (!mounted) return;

          try {
            const data = JSON.parse(event.data);
            const {
              wellId,
              temperatureC,
              pressurePsi,
              flowRateBpd,
              timestamp,
            } = data;

            if (!wellId) return;

            setWells((prev) => {
              const newWells = new Map(prev);
              const well = newWells.get(wellId) || {
                wellId,
                currentMetrics: null,
                history: [],
                alert: null,
              };

              const newMetric = {
                timestamp: timestamp || new Date().toISOString(),
                temperatureC,
                pressurePsi,
                flowRateBpd,
              };

              well.currentMetrics = newMetric;
              well.history = [newMetric, ...well.history].slice(0, MAX_HISTORY);

              newWells.set(wellId, well);
              return newWells;
            });
          } catch (err) {
            console.error("Error parsing metric:", err);
          }
        });

        es.addEventListener("alert", (event) => {
          if (!mounted) return;

          try {
            const data = JSON.parse(event.data);
            const { wellId, alertStatus } = data;

            if (!wellId) return;

            setWells((prev) => {
              const newWells = new Map(prev);
              const well = newWells.get(wellId) || {
                wellId,
                currentMetrics: null,
                history: [],
                alert: null,
              };

              well.alert =
                alertStatus === "ACTIVE"
                  ? {
                      alertStatus: "ACTIVE",
                      timestamp: new Date().toISOString(),
                    }
                  : null;

              newWells.set(wellId, well);
              return newWells;
            });
          } catch (err) {
            console.error("Error parsing alert:", err);
          }
        });

        es.onerror = (error) => {
          console.error("❌ Error SSE:", error);
          if (!mounted) return;

          setIsConnected(false);
          es.close();

          // Reintentar en 5 segundos
          console.log("⏱️ Reintentando en 5s...");
          reconnectTimeoutRef.current = setTimeout(() => {
            if (mounted) connectSSE();
          }, 5000);
        };

        eventSourceRef.current = es;
      } catch (err) {
        console.error("❌ Error creando EventSource:", err);
        if (!mounted) return;

        setIsConnected(false);

        // Reintentar en 5 segundos
        console.log("⏱️ Reintentando en 5s...");
        reconnectTimeoutRef.current = setTimeout(() => {
          if (mounted) connectSSE();
        }, 5000);
      }
    };

    connectSSE();

    return () => {
      mounted = false;

      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }

      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }

      setIsConnected(false);
    };
  }, []);

  return {
    wells,
    isConnected,
    wellsArray: Array.from(wells.values()),
  };
};
