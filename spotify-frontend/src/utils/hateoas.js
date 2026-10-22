// HATEOAS yanıtındaki gömülü listeyi bulur.
export function getEmbeddedList(data) {
  const embedded = data?._embedded ?? {};
  return Object.values(embedded).find(Array.isArray) ?? [];
}
