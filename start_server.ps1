Get-Content .env | ForEach-Object {
    $line = $_.Trim()
    if ($line -and -not $line.StartsWith("#") -and $line -match "^([^=]+)=(.*)$") {
        $name = $matches[1].Trim()
        $val = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $val, "Process")
    }
}

[System.Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", "dev", "Process")
Write-Host "Starting Spring Boot with profile dev..."
.\mvnw.cmd spring-boot:run
