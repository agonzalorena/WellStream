import { useEffect } from "react";
import { useSSEMetrics } from "./hooks/useSSEMetrics";
import { useWellsStore } from "./store/wellsStore";
import { Header } from "./components/Header";
import { WellCard } from "./components/WellCard";
import { WellTable } from "./components/WellTable";
import "./App.css";

function App() {
  const { wells, isConnected, wellsArray } = useSSEMetrics();
  const { setAllWells, filteredWells, viewMode } = useWellsStore();

  // Update store when wells change
  useEffect(() => {
    setAllWells(wells);
  }, [wells, setAllWells]);

  return (
    <div className="min-h-screen bg-gray-100 dark:bg-gray-900">
      {/* Header */}
      <Header isConnected={isConnected} />

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Empty State */}
        {wellsArray.length === 0 && isConnected && (
          <div className="text-center py-12">
            <p className="text-xl text-gray-500 dark:text-gray-400">
              📊 Esperando datos del servidor...
            </p>
            <p className="text-sm text-gray-400 dark:text-gray-500 mt-2">
              La conexión está activa, pero no se han recibido métricas aún.
            </p>
          </div>
        )}

        {/* Connection Error State */}
        {!isConnected && (
          <div className="card bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-700 text-center py-6 mb-6">
            <p className="text-yellow-800 dark:text-yellow-200 font-medium">
              ⚠️ Intentando conectar al servidor SSE...
            </p>
            <p className="text-sm text-yellow-700 dark:text-yellow-300 mt-2">
              Verifica que el servidor esté disponible en{" "}
              <code className="bg-yellow-100 dark:bg-yellow-800/50 px-2 py-1 rounded">
                http://localhost:8083/api/notifications/stream
              </code>
            </p>
          </div>
        )}

        {/* View: Cards Layout */}
        {viewMode === "cards" && wellsArray.length > 0 && (
          <>
            {filteredWells.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {filteredWells.map((well) => (
                  <WellCard key={well.wellId} well={well} />
                ))}
              </div>
            ) : (
              <div className="text-center py-12">
                <p className="text-lg text-gray-500 dark:text-gray-400">
                  🔍 No se encontraron pozos con los criterios de búsqueda
                </p>
              </div>
            )}
          </>
        )}

        {/* View: Table Layout */}
        {viewMode === "table" && wellsArray.length > 0 && (
          <WellTable wells={filteredWells} />
        )}
      </main>
    </div>
  );
}

export default App;
