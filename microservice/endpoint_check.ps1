function Invoke-Check {
  param([string]$Method,[string]$Url,[string]$BodyJson='')
  try {
    if($Method -eq 'GET'){ $resp = Invoke-WebRequest -Uri $Url -Method Get -TimeoutSec 20 }
    else { $resp = Invoke-WebRequest -Uri $Url -Method Post -ContentType 'application/json' -Body $BodyJson -TimeoutSec 20 }
    $status=[int]$resp.StatusCode; $content=[string]$resp.Content
  } catch {
    $status = if($_.Exception.Response){ [int]$_.Exception.Response.StatusCode } else { -1 }
    if($_.Exception.Response){ $sr = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream()); $content=$sr.ReadToEnd(); $sr.Close() }
    else { $content=$_.Exception.Message }
  }
  $snippet = ($content -replace '\s+',' ')
  if($snippet.Length -gt 90){ $snippet=$snippet.Substring(0,90) + '...' }
  [PSCustomObject]@{ Method=$Method; Url=$Url; Status=$status; Snippet=$snippet }
}
$body='{"message":"xin chao","accountId":"acc-001"}'
$rows = @(
  Invoke-Check 'GET' 'http://localhost:8080/'
  Invoke-Check 'GET' 'http://localhost:8080/ai-service/actuator/health'
  Invoke-Check 'POST' 'http://localhost:8080/ai-service/api/ai/chat' $body
  Invoke-Check 'GET' 'http://localhost:8080/catalog-service/api/catalog/categories/33333333-3333-3333-3333-333333333333'
  Invoke-Check 'GET' 'http://localhost:8080/account-service/actuator/health'
  Invoke-Check 'POST' 'http://localhost:8086/api/ai/chat' $body
)
$rows | Format-Table -AutoSize | Out-String -Width 240
