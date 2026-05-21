export const AlertBadge = ({ alert, wellId }) => {
  const isActive = alert && alert.alertStatus === "ACTIVE";

  return (
    <div
      className={`badge ${
        isActive
          ? "bg-red-100 dark:bg-red-900 text-red-800 dark:text-red-200"
          : "bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200"
      } ${isActive ? "animate-pulse" : ""}`}
    >
      <span className="text-lg">{isActive ? "🔴" : "🟢"}</span>
      <span className="font-semibold text-sm">
        {isActive ? "ALERTA" : "OK"}
      </span>
    </div>
  );
};
