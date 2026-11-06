# PowerShell Script to Seed Initial Data into Local Firebase Emulator

$projectId = "klass-5549e"
$firestoreUrl = "http://localhost:8080/v1/projects/$projectId/databases/(default)/documents/users"

Write-Host "Seeding test users into Local Firebase Firestore Emulator..." -ForegroundColor Green

# 1. Google Test User
$googleUserData = @{
    fields = @{
        email = @{ stringValue = "google.user@example.com" }
        name = @{ stringValue = "গুগল টেস্ট ইউজার" }
        provider = @{ stringValue = "google" }
        category = @{ stringValue = "STUDENT" }
        photo_url = @{ stringValue = "" }
        updated_at = @{ integerValue = [long](Get-Date -UFormat %s) * 1000 }
    }
} | ConvertTo-Json -Depth 5

try {
    Invoke-RestMethod -Uri "$firestoreUrl/google.user@example.com" -Method Patch -Body $googleUserData -ContentType "application/json"
    Write-Host "Google test user created in Local Firestore" -ForegroundColor Cyan
} catch {
    Write-Host "Note: Start emulators first with 'firebase emulators:start', then run this script." -ForegroundColor Yellow
}

# 2. Facebook Test User
$fbUserData = @{
    fields = @{
        email = @{ stringValue = "facebook.user@example.com" }
        name = @{ stringValue = "ফেসবুক টেস্ট ইউজার" }
        provider = @{ stringValue = "facebook" }
        category = @{ stringValue = "TEACHER" }
        photo_url = @{ stringValue = "" }
        updated_at = @{ integerValue = [long](Get-Date -UFormat %s) * 1000 }
    }
} | ConvertTo-Json -Depth 5

try {
    Invoke-RestMethod -Uri "$firestoreUrl/facebook.user@example.com" -Method Patch -Body $fbUserData -ContentType "application/json"
    Write-Host "Facebook test user created in Local Firestore" -ForegroundColor Cyan
} catch {}

# 3. Apple Test User
$appleUserData = @{
    fields = @{
        email = @{ stringValue = "apple.user@example.com" }
        name = @{ stringValue = "অ্যাপল টেস্ট ইউজার" }
        provider = @{ stringValue = "apple" }
        category = @{ stringValue = "ADMIN" }
        photo_url = @{ stringValue = "" }
        updated_at = @{ integerValue = [long](Get-Date -UFormat %s) * 1000 }
    }
} | ConvertTo-Json -Depth 5

try {
    Invoke-RestMethod -Uri "$firestoreUrl/apple.user@example.com" -Method Patch -Body $appleUserData -ContentType "application/json"
    Write-Host "Apple test user created in Local Firestore" -ForegroundColor Cyan
} catch {}

Write-Host "Local Firebase Seeding Complete!" -ForegroundColor Green
