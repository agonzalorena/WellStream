import { formatTimestamp, formatNumber } from "../utils/dateFormat";

export const WellTable = ({ wells }) => {
  if (!wells || wells.length === 0) {
    return (
      <div className="card text-center py-12">
        <p className="text-gray-500 dark:text-gray-400">
          No se encontraron pozos con los criterios de búsqueda
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-lg border border-gray-200 dark:border-gray-700">
      <table className="w-full">
        <thead>
          <tr className="border-b border-gray-200 dark:border-gray-700 bg-gray-50 dark:bg-gray-800">
            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300">
              ID Pozo
            </th>
            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300">
              Temperatura (°C)
            </th>
            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300">
              Presión (PSI)
            </th>
            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300">
              Flujo (bbl/d)
            </th>
            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300">
              Alerta
            </th>
            <th className="px-6 py-3 text-left text-xs font-semibold text-gray-700 dark:text-gray-300">
              Última Actualización
            </th>
          </tr>
        </thead>
        <tbody>
          {wells.map((well, index) => (
            <tr
              key={well.wellId}
              className={`border-b border-gray-200 dark:border-gray-700 transition hover:bg-gray-50 dark:hover:bg-gray-800 ${
                index % 2 === 0
                  ? "bg-white dark:bg-gray-900"
                  : "bg-gray-50 dark:bg-gray-800"
              }`}
            >
              <td className="px-6 py-4 text-sm font-semibold text-gray-900 dark:text-white">
                {well.wellId}
              </td>
              <td className="px-6 py-4 text-sm text-gray-700 dark:text-gray-300">
                {well.currentMetrics
                  ? formatNumber(well.currentMetrics.temperatureC)
                  : "N/A"}
              </td>
              <td className="px-6 py-4 text-sm text-gray-700 dark:text-gray-300">
                {well.currentMetrics
                  ? formatNumber(well.currentMetrics.pressurePsi)
                  : "N/A"}
              </td>
              <td className="px-6 py-4 text-sm text-gray-700 dark:text-gray-300">
                {well.currentMetrics
                  ? formatNumber(well.currentMetrics.flowRateBpd)
                  : "N/A"}
              </td>
              <td className="px-6 py-4">
                <span
                  className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${
                    well.alert && well.alert.alertStatus === "ACTIVE"
                      ? "bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200"
                      : "bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200"
                  }`}
                >
                  <span className="text-lg">
                    {well.alert && well.alert.alertStatus === "ACTIVE"
                      ? "🔴"
                      : "🟢"}
                  </span>
                  {well.alert && well.alert.alertStatus === "ACTIVE"
                    ? "ALERTA"
                    : "OK"}
                </span>
              </td>
              <td className="px-6 py-4 text-sm text-gray-600 dark:text-gray-400">
                {well.currentMetrics
                  ? formatTimestamp(well.currentMetrics.timestamp)
                  : "N/A"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
