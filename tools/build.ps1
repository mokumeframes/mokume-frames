param([string]$JavaHome=$env:JAVA_HOME,[switch]$ServerTests)
$ErrorActionPreference='Stop'
if(!$JavaHome){throw 'Pass -JavaHome with a 64-bit Java 21 JDK directory.'}
$jdkPath=[IO.Path]::GetFullPath($JavaHome)
if(!(Test-Path -LiteralPath (Join-Path $jdkPath 'bin\javac.exe'))){throw 'A JDK, including javac, is required.'}
$env:JAVA_HOME=$jdkPath
$env:PATH=(Join-Path $jdkPath 'bin')+';'+$env:PATH
Push-Location (Join-Path $PSScriptRoot '..')
try{
    & .\gradlew.bat build --console=plain
    if($LASTEXITCODE -ne 0){throw 'Build or logic tests failed.'}
    if($ServerTests){
        $testDir='run/gametest-'+(Get-Date -Format 'yyyyMMdd-HHmmss')
        & .\gradlew.bat runGameTestServer "-PmhaTestDir=$testDir" --console=plain
        if($LASTEXITCODE -ne 0){throw 'Minecraft GameTests failed; inspect the explicit test summary, not just Gradle startup.'}
    }
}finally{Pop-Location}
