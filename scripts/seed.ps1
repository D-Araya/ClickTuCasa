# ---------------------------------------------------------------------------
# Poblar ClickTuCasa con datos de prueba (Windows PowerShell).
# Equivalente a scripts/seed.sh.
#
#   .\scripts\seed.ps1
#   .\scripts\seed.ps1 -Api "http://localhost:9090"
# ---------------------------------------------------------------------------
param([string]$Api = "http://localhost:8080")

$Raffles = "$Api/api/v1/raffles"

function Send-Json($Url, $Body) {
  try {
    Invoke-RestMethod -Method Post -Uri $Url -ContentType "application/json" -Body $Body | Out-Null
    Write-Host "  -> OK"
  } catch {
    Write-Host "  -> $($_.Exception.Message)"
  }
}

Write-Host "Sembrando datos de prueba en $Raffles"

Write-Host "[1/3] Casa Mediterranea con Vista al Mar (raf-001)"
Send-Json $Raffles '{"id":"raf-001","title":"Casa Mediterranea con Vista al Mar","houseAddress":"Camino Costero 1240, Zapallar","houseValue":185000000,"minTicketsToDraw":60,"totalTickets":100,"ticketPrice":15000}'

Write-Host "[2/3] Loft Urbano Barrio Italia (raf-002)"
Send-Json $Raffles '{"id":"raf-002","title":"Loft Urbano Barrio Italia","houseAddress":"Av. Italia 1520, Providencia","houseValue":96000000,"minTicketsToDraw":40,"totalTickets":80,"ticketPrice":9000}'

Write-Host "[3/3] Cabana de Montana en Pucon (raf-003)"
Send-Json $Raffles '{"id":"raf-003","title":"Cabana de Montana en Pucon","houseAddress":"Camino Volcan Km 7, Pucon","houseValue":74000000,"minTicketsToDraw":30,"totalTickets":60,"ticketPrice":7500}'

Write-Host ""
Write-Host "Generando movimiento en raf-001: 12 boletos vendidos y 3 reservados"
1..12 | ForEach-Object { Send-Json "$Raffles/raf-001/tickets/$_/purchases" '{"userId":"demo@clicktucasa.cl"}' }
13..15 | ForEach-Object { Send-Json "$Raffles/raf-001/tickets/$_/reservations" '{"userId":"visitante@clicktucasa.cl","durationMinutes":30}' }

Write-Host ""
Write-Host "Listo. Verifica con: curl $Raffles"
