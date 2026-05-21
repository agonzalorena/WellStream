import { useWellsStore } from "../store/wellsStore";

export const Header = ({ isConnected }) => {
  const { searchTerm, setSearchTerm, clearSearch, viewMode, setViewMode } =
    useWellsStore();

  return (
    <header className="sticky top-0 z-50 border-b border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        {/* Title and Connection Status */}
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
              🛢️ Dashboard
            </h1>
            <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
              Monitoreo en tiempo real de métricas
            </p>
          </div>

          {/* Connection Status */}
          <div
            className={`flex items-center gap-2 px-3 py-2 rounded-full text-sm font-medium ${
              isConnected
                ? "bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200"
                : "bg-yellow-100 dark:bg-yellow-900 text-yellow-800 dark:text-yellow-200"
            }`}
          >
            <span className="text-lg">{isConnected ? "🟢" : "🟡"}</span>
            {isConnected ? "Conectado" : "Reconectando..."}
          </div>
        </div>

        {/* Search Bar and Controls */}
        <div className="flex gap-3 flex-wrap items-center">
          {/* Search Input */}
          <div className="flex-1 min-w-64">
            <div className="relative">
              <input
                type="text"
                placeholder="Buscar pozo por ID (ej: Cerro-Dragon-1)..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full px-4 py-2 pl-10 border border-gray-300 dark:border-gray-600 rounded-lg bg-gray-50 dark:bg-gray-700 text-gray-900 dark:text-white placeholder-gray-500 dark:placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
              <span className="absolute left-3 top-2.5 text-gray-400">🔍</span>
              {searchTerm && (
                <button
                  onClick={clearSearch}
                  className="absolute right-3 top-2.5 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                >
                  ✕
                </button>
              )}
            </div>
          </div>

          {/* View Mode Toggle */}
          <div className="flex gap-2 bg-gray-200 dark:bg-gray-700 rounded-lg p-1">
            <button
              onClick={() => setViewMode("cards")}
              className={`px-3 py-1.5 rounded text-sm font-medium transition ${
                viewMode === "cards"
                  ? "bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-300 shadow-sm"
                  : "text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200"
              }`}
            >
              📊 Cards
            </button>
            <button
              onClick={() => setViewMode("table")}
              className={`px-3 py-1.5 rounded text-sm font-medium transition ${
                viewMode === "table"
                  ? "bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-300 shadow-sm"
                  : "text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200"
              }`}
            >
              📋 Tabla
            </button>
          </div>
        </div>
      </div>
    </header>
  );
};
