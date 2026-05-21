import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { formatTime } from "../utils/dateFormat";

export const MetricsChart = ({ history }) => {
  if (!history || history.length === 0) {
    return (
      <div className="flex items-center justify-center h-48 bg-gray-50 dark:bg-gray-700 rounded-lg">
        <p className="text-gray-500 dark:text-gray-400">Sin datos históricos</p>
      </div>
    );
  }

  // Prepare data for the chart (reverse to show oldest to newest left to right)
  const chartData = [...history].reverse().map((metric) => ({
    time: formatTime(metric.timestamp),
    temperatureC: parseFloat(metric.temperatureC).toFixed(2),
    pressurePsi: parseFloat(metric.pressurePsi).toFixed(2),
    flowRateBpd: parseFloat(metric.flowRateBpd).toFixed(2),
  }));

  return (
    <div className="w-full h-48 mt-4">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart
          data={chartData}
          margin={{ top: 5, right: 30, left: 0, bottom: 5 }}
        >
          <CartesianGrid strokeDasharray="3 3" className="opacity-20" />
          <XAxis
            dataKey="time"
            tick={{ fontSize: 12 }}
            angle={-45}
            textAnchor="end"
            height={60}
          />
          <YAxis tick={{ fontSize: 12 }} />
          <Tooltip
            contentStyle={{
              backgroundColor: "#1f2937",
              border: "1px solid #374151",
              borderRadius: "0.5rem",
              color: "#fff",
            }}
          />
          <Legend wrapperStyle={{ fontSize: 12 }} />
          <Line
            type="monotone"
            dataKey="temperatureC"
            stroke="#ef4444"
            dot={false}
            strokeWidth={2}
            name="Temp (°C)"
          />
          <Line
            type="monotone"
            dataKey="pressurePsi"
            stroke="#3b82f6"
            dot={false}
            strokeWidth={2}
            name="Presión (PSI)"
          />
          <Line
            type="monotone"
            dataKey="flowRateBpd"
            stroke="#10b981"
            dot={false}
            strokeWidth={2}
            name="Flujo (bbl/d)"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};
