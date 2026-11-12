# =========================================================================
# AUTOMATED BUSINESS RULES TEST SCRIPT - POLICIES & RISKS API
# Compatible with Windows PowerShell 5.1 and PowerShell 7+
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
            Write-Host "CONNECTION ERROR: Could not reach $baseUrl$Uri" -ForegroundColor Red
            Write-Host "Make sure the application is running in another terminal with '.\mvnw.bat spring-boot:run'." -ForegroundColor DarkYellow
            return
        }
    }

    if ($statusCode -eq $CodigoEsperado) {
        Write-Host "PASS [HTTP $statusCode]" -ForegroundColor Green
    } else {
        Write-Host "FAIL [HTTP $statusCode - Expected: $CodigoEsperado]" -ForegroundColor Red
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
Write-Host "   STARTING BUSINESS RULES TESTS (REST API)                 " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green

# 1. Security: Without API key (expect 401)
Test-Endpoint -Titulo "1. Security: Request without x-api-key header" `
              -Metodo "GET" -Uri "/api/v1/policies" -HeadersCustom @{} -CodigoEsperado 401

# 2. List all policies (expect 200)
Test-Endpoint -Titulo "2. List all preloaded policies" `
              -Metodo "GET" -Uri "/api/v1/policies" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 3. Filter by type and status (expect 200)
Test-Endpoint -Titulo "3. Filter active individual policies" `
              -Metodo "GET" -Uri "/api/v1/policies?type=INDIVIDUAL&status=ACTIVE" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 4. List risks of Policy 1 (expect 200)
Test-Endpoint -Titulo "4. List risks of Policy 1 (Individual)" `
              -Metodo "GET" -Uri "/api/v1/policies/1/risks" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 5. Renew active policy with IPC (expect 200)
Test-Endpoint -Titulo "5. Renew Policy 1 with 5.10% IPC" `
              -Metodo "POST" -Uri "/api/v1/policies/1/renew" -HeadersCustom $defaultHeaders `
              -Body '{"ipc": 5.10}' -CodigoEsperado 200

# 6. Business rule: Renew cancelled policy (expect 422)
Test-Endpoint -Titulo "6. Business Rule: Renew CANCELLED Policy 3 (must reject with 422)" `
              -Metodo "POST" -Uri "/api/v1/policies/3/renew" -HeadersCustom $defaultHeaders `
              -Body '{"ipc": 5.10}' -CodigoEsperado 422

# 7. Business rule: Add risk to individual policy (expect 422)
Test-Endpoint -Titulo "7. Business Rule: Add risk to INDIVIDUAL Policy 1 (must reject with 422)" `
              -Metodo "POST" -Uri "/api/v1/policies/1/risks" -HeadersCustom $defaultHeaders `
              -Body '{"propertyAddress":"Calle 10 # 20-30","city":"Bogota","insuredName":"Rejected Tenant","insuredAmount":1500000.00}' `
              -CodigoEsperado 422

# 8. Add risk to collective policy (expect 201)
Test-Endpoint -Titulo "8. Add risk to COLLECTIVE Policy 2 (must succeed with 201)" `
              -Metodo "POST" -Uri "/api/v1/policies/2/risks" -HeadersCustom $defaultHeaders `
              -Body '{"propertyAddress":"Carrera 15 # 85-30 Of 501","city":"Bogota","insuredName":"New Tenant SAS","insuredAmount":3500000.00}' `
              -CodigoEsperado 201

# 9. Cancel individual risk (expect 200)
Test-Endpoint -Titulo "9. Cancel Risk 2 individually" `
              -Metodo "POST" -Uri "/api/v1/risks/2/cancel" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 10. Cancel policy with cascade (expect 200)
Test-Endpoint -Titulo "10. Business Rule: Cancel Policy 2 and verify cascade" `
              -Metodo "POST" -Uri "/api/v1/policies/2/cancel" -HeadersCustom $defaultHeaders -CodigoEsperado 200

# 11. Mock CORE (expect 200)
Test-Endpoint -Titulo "11. Send event to CORE mock (WebLogic)" `
              -Metodo "POST" -Uri "/api/v1/core-mock/event" -HeadersCustom $defaultHeaders `
              -Body '{"event":"MANUAL_TEST","policyId":999}' -CodigoEsperado 200

Write-Host "`n============================================================" -ForegroundColor Green
Write-Host "           ALL BUSINESS RULES TESTS COMPLETED               " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
