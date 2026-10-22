export default function Cover({
  color = "purple",
  icon = "♫",
  small = false,
}) {
  return (
    <div className={`cover ${color} ${small ? "cover-small" : ""}`}>
      <span>{icon}</span>
    </div>
  );
}
