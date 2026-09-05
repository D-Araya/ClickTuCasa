#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Poblar ClickTuCasa con datos de prueba.
#
# Crea tres rifas con inventarios pequeños (para que la grilla cargue al
# instante) y deja una de ellas parcialmente vendida, de modo que la
# interfaz muestre los tres estados de boleto y una barra de progreso real.
#
#   ./scripts/seed.sh                      # contra http://localhost:8080
#   API=http://localhost:9090 ./scripts/seed.sh
#
# Requisitos: el backend levantado y `curl` en el PATH.
# Es idempotente por omisión: si las rifas ya existen, el POST devuelve un
# error de negocio y el script continúa.
# ---------------------------------------------------------------------------
set -uo pipefail

API="${API:-http://localhost:8080}"
RAFFLES="${API}/api/v1/raffles"

post() {
  curl -sS -o /dev/null -w "  -> HTTP %{http_code}\n" \
    -X POST "$1" -H "Content-Type: application/json" -d "$2"
}

echo "Sembrando datos de prueba en ${RAFFLES}"

echo "[1/3] Casa Mediterranea con Vista al Mar (raf-001)"
post "${RAFFLES}" '{
  "id": "raf-001",
  "title": "Casa Mediterranea con Vista al Mar",
  "houseAddress": "Camino Costero 1240, Zapallar",
  "houseValue": 185000000,
  "minTicketsToDraw": 60,
  "totalTickets": 100,
  "ticketPrice": 15000
}'

echo "[2/3] Loft Urbano Barrio Italia (raf-002)"
post "${RAFFLES}" '{
  "id": "raf-002",
  "title": "Loft Urbano Barrio Italia",
  "houseAddress": "Av. Italia 1520, Providencia",
  "houseValue": 96000000,
  "minTicketsToDraw": 40,
  "totalTickets": 80,
  "ticketPrice": 9000
}'

echo "[3/3] Cabana de Montana en Pucon (raf-003)"
post "${RAFFLES}" '{
  "id": "raf-003",
  "title": "Cabana de Montana en Pucon",
  "houseAddress": "Camino Volcan Km 7, Pucon",
  "houseValue": 74000000,
  "minTicketsToDraw": 30,
  "totalTickets": 60,
  "ticketPrice": 7500
}'

echo
echo "Generando movimiento en raf-001: 12 boletos vendidos y 3 reservados"
for n in $(seq 1 12); do
  post "${RAFFLES}/raf-001/tickets/${n}/purchases" '{"userId":"demo@clicktucasa.cl"}' >/dev/null
done
for n in 13 14 15; do
  post "${RAFFLES}/raf-001/tickets/${n}/reservations" '{"userId":"visitante@clicktucasa.cl","durationMinutes":30}' >/dev/null
done

echo
echo "Listo. Verifica con:"
echo "  curl -s ${RAFFLES} | jq"
