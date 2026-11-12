# =========================================================================
# SCRIPT DE PRUEBAS AUTOMATIZADAS REST - API POLICIES & RISKS
# Compatible con Windows PowerShell 5.1 y PowerShell 7+
# =========================================================================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:8080"
$apiKey = "123456"
$defaultHeaders = @{
    "x-api-key" = $apiKey
}

function Test-Endpoint {
    param(
        [string]$Titulo,
        [string]$Metodo,
        [string]$Uri,
        [hashtable]$HeadersCustom,
        [string]$Body,
        [int]$CodigoEsperado
    )

    Write-Host "`n------------------------------------------------------------" -ForegroundColor Cyan
    Write-Host ">> $Titulo" -ForegroundColor Yellow
    Write-Host "$Metodo $baseUrl$Uri" -ForegroundColor Gray

    $statusCode = 0
    $responseBody = ""

    try {
        $params = @{
            Uri = "$baseUrl$Uri"
            Method = $Metodo
            UseBasicParsing = $true
            ErrorAction = "Stop"
        }

        if ($HeadersCustom -ne $null) {
            $params["Headers"] = $HeadersCustom
        }

        if ($Body) {
            $params["Body"] = [System.Text.Encoding]::UTF8.GetBytes($Body)
            $params["ContentType"] = "application/json; charset=utf-8"
        }

        $response = Invoke-WebRequest @params
        $statusCode = [int]$response.StatusCode
        $responseBody = $response.Content
    }
    catch {
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                if ($stream) {
                    $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
                    $responseBody = $reader.ReadToEnd()
                }
            } catch {
                $responseBody = $_.Exception.Message
            }
        } else {
            Write-Host "ERROR DE CONEXION: No se pudo conectar a $baseUrl$Uri" -ForegroundColor Red
            Write-Host "Asegurate de que la aplicacion este corriendo en otra terminal con '.\mvnw.bat spring-boot:run'." -ForegroundColor DarkYellow
            return
        }
    }

    if ($statusCode -eq $CodigoEsperado) {
        Write-Host "PASS [HTTP $statusCode]" -ForegroundColor Green
    } else {
        Write-Host "FALLO [HTTP $statusCode - Se esperaba: $CodigoEsperado]" -ForegroundColor Red
    }

    if ($responseBody) {
        try {
            $jsonObj = $responseBody | ConvertFrom-Json
            $jsonObj | ConvertTo-Json -Depth 4 | Write-Host -ForegroundColor White
        } catch {
            Write-Host $responseBody -ForegroundColor DarkGray
        }
    }
}

Write-Host "============================================================" -ForegroundColor Green
Write-Host "   INICIANDO PRUEBAS INTERACTIVAS DE LA API (REST TESTS)   " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

# 1. Seguridad: Sin header (espera 401)
Test-Endpoint -Titulo "1. Validar Seguridad: Petición sin header x-api-key" `
              -Metodo "GET" -Uri "/api/v1/policies" -HeadersCustom @{} -CodigoEsperado 401

# 2. Listar todas las pólizas (espera 200)
Test-Endpoint -Titulo "2. Listar todas las pólizas precargadas" `
              -Metodo "GET" -Uri "/api/v1/policies" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 3. Filtrar por tipo y estado (espera 200)
Test-Endpoint -Titulo "3. Filtrar pólizas individuales activas" `
              -Metodo "GET" -Uri "/api/v1/policies?type=INDIVIDUAL&status=ACTIVE" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 4. Consultar riesgos de Póliza 1 (espera 200)
Test-Endpoint -Titulo "4. Consultar riesgos de la Póliza 1 (Individual)" `
              -Metodo "GET" -Uri "/api/v1/policies/1/risks" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 5. Renovar póliza vigente con IPC (espera 200)
Test-Endpoint -Titulo "5. Renovar Póliza 1 con IPC del 5.10%" `
              -Metodo "POST" -Uri "/api/v1/policies/1/renew" -HeadersCustom $defaultHeaders `
              -Body '{"ipc": 5.10}' -CodigoEsperado 200

# 6. Regla de Negocio: Renovar cancelada (espera 422)
Test-Endpoint -Titulo "6. Regla de Negocio: Renovar Póliza 3 CANCELADA (Debe rechazar con 422)" `
              -Metodo "POST" -Uri "/api/v1/policies/3/renew" -HeadersCustom $defaultHeaders `
              -Body '{"ipc": 5.10}' -CodigoEsperado 422

# 7. Regla de Negocio: Agregar riesgo a individual (espera 422)
Test-Endpoint -Titulo "7. Regla de Negocio: Agregar riesgo a Póliza 1 INDIVIDUAL (Debe rechazar con 422)" `
              -Metodo "POST" -Uri "/api/v1/policies/1/risks" -HeadersCustom $defaultHeaders `
              -Body '{"propertyAddress":"Calle 10 # 20-30","city":"Bogota","insuredName":"Inquilino Rechazado","insuredAmount":1500000.00}' `
              -CodigoEsperado 422

# 8. Agregar riesgo a colectiva (espera 201)
Test-Endpoint -Titulo "8. Agregar riesgo a Póliza 2 COLECTIVA (Debe ser exitoso con 201)" `
              -Metodo "POST" -Uri "/api/v1/policies/2/risks" -HeadersCustom $defaultHeaders `
              -Body '{"propertyAddress":"Carrera 15 # 85-30 Of 501","city":"Bogota","insuredName":"Nuevo Arrendatario SAS","insuredAmount":3500000.00}' `
              -CodigoEsperado 201

# 9. Cancelar un riesgo (espera 200)
Test-Endpoint -Titulo "9. Cancelar individualmente Riesgo 2" `
              -Metodo "POST" -Uri "/api/v1/risks/2/cancel" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 10. Cancelar póliza completa en cascada (espera 200)
Test-Endpoint -Titulo "10. Regla de Negocio: Cancelar Póliza 2 y verificar cascada" `
              -Metodo "POST" -Uri "/api/v1/policies/2/cancel" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 11. Mock CORE (espera 200)
Test-Endpoint -Titulo "11. Enviar evento al Mock del CORE asegurador WebLogic" `
              -Metodo "POST" -Uri "/core-mock/event" -HeadersCustom $defaultHeaders `
              -Body '{"event":"MANUAL_TEST","policyId":999}' -CodigoEsperado 200

Write-Host "`n============================================================" -ForegroundColor Green
Write-Host "           TODAS LAS PRUEBAS HAN FINALIZADO                 " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
