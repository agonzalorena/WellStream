/**
 * Format a timestamp to a readable date and time string
 * @param {string | Date} timestamp - ISO string or Date object
 * @returns {string} Formatted timestamp (e.g., "May 21, 10:30:45")
 */
export const formatTimestamp = (timestamp) => {
  if (!timestamp) return "N/A";

  const date = typeof timestamp === "string" ? new Date(timestamp) : timestamp;
  if (isNaN(date.getTime())) return "Invalid date";

  const options = {
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  };

  return new Intl.DateTimeFormat("es-MX", options).format(date);
};

/**
 * Format a timestamp to just the time (HH:MM:SS)
 * @param {string | Date} timestamp - ISO string or Date object
 * @returns {string} Formatted time (e.g., "10:30:45")
 */
export const formatTime = (timestamp) => {
  if (!timestamp) return "N/A";

  const date = typeof timestamp === "string" ? new Date(timestamp) : timestamp;
  if (isNaN(date.getTime())) return "Invalid date";

  const options = {
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  };

  return new Intl.DateTimeFormat("es-MX", options).format(date);
};

/**
 * Format a number to 2 decimal places
 * @param {number} value - The number to format
 * @returns {string} Formatted number (e.g., "75.50")
 */
export const formatNumber = (value) => {
  if (value === null || value === undefined) return "N/A";
  return parseFloat(value).toFixed(2);
};
