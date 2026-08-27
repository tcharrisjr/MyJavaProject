function SummaryCard({
  value,
  label,
  tone = "default",
}) {
  return (
    <article
      className={`summary-card summary-card-${tone}`}
    >
      <div className="summary-card-accent" />

      <div className="summary-card-content">
        <span className="summary-label">
          {label}
        </span>

        <span className="summary-number">
          {value}
        </span>
      </div>
    </article>
  );
}

export default SummaryCard;