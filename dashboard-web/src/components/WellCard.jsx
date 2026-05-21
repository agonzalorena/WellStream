import { AlertBadge } from "./AlertBadge";
import { formatTimestamp, formatNumber } from "../utils/dateFormat";

export const WellCard = ({ well }) => {
  const { wellId, currentMetrics, alert } = well;

  return (
    <div className="card flex flex-col h-full">
      {/* Header with ID and Alert Badge */}
      <div className="flex items-center justify-between mb-4 pb-4 border-b border-gray-200 dark:border-gray-700">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white">
          {wellId}
        </h2>
        <AlertBadge alert={alert} wellId={wellId} />
      </div>

      {/* Metrics Display */}
      {currentMetrics ? (
        <div className="flex-1">
          {/* Metrics Grid */}
          <div className="grid grid-cols-3 gap-2 mb-4">
            {/* Temperatura */}
            <div className="bg-red-50 dark:bg-red-900/20 rounded-lg p-2.5 flex flex-col justify-between h-24">
              <p className="text-xs text-gray-600 dark:text-gray-400 font-semibold leading-tight">
                TEMP
              </p>
              <p className="text-xl font-bold text-red-600 dark:text-red-400 break-words">
                {formatNumber(currentMetrics.temperatureC)}
              </p>
              <p className="text-xs text-red-600 dark:text-red-400">°C</p>
            </div>

            {/* Presión */}
            <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-2.5 flex flex-col justify-between h-24">
              <p className="text-xs text-gray-600 dark:text-gray-400 font-semibold leading-tight">
                PRESIÓN
              </p>
              <p className="text-xl font-bold text-blue-600 dark:text-blue-400 break-words">
                {formatNumber(currentMetrics.pressurePsi)}
              </p>
              <p className="text-xs text-blue-600 dark:text-blue-400">PSI</p>
            </div>

            {/* Flow Rate */}
            <div className="bg-green-50 dark:bg-green-900/20 rounded-lg p-2.5 flex flex-col justify-between h-24">
              <p className="text-xs text-gray-600 dark:text-gray-400 font-semibold leading-tight">
                FLUJO
              </p>
              <p className="text-xl font-bold text-green-600 dark:text-green-400 break-words">
                {formatNumber(currentMetrics.flowRateBpd)}
              </p>
              <p className="text-xs text-green-600 dark:text-green-400">
                bbl/d
              </p>
            </div>
          </div>

          {/* Last Update Time */}
          <p className="text-xs text-gray-500 dark:text-gray-400">
            📅 {formatTimestamp(currentMetrics.timestamp)}
          </p>
        </div>
      ) : (
        <div className="flex items-center justify-center h-32">
          <p className="text-gray-500 dark:text-gray-400">Esperando datos...</p>
        </div>
      )}
    </div>
  );
};
