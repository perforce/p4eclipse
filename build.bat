@echo off
REM P4Eclipse Build Script - Builds P4Eclipse plugin with Maven/Tycho
REM Usage: build.bat <version>

setlocal enabledelayedexpansion

REM Configuration (defaults)
set DEFAULT_TYCHO_VERSION=0.20.0
set DEFAULT_QUALIFIER=MAIN-TEST_ONLY-SNAPSHOT
set DEFAULT_TARGET_PLATFORM=p4e-428
set DEFAULT_P2_REPO_URL=https://download.eclipse.org/releases/2023-06/

REM Initialize with defaults
set TYCHO_VERSION=%DEFAULT_TYCHO_VERSION%
set QUALIFIER=%DEFAULT_QUALIFIER%
set TARGET_PLATFORM=%DEFAULT_TARGET_PLATFORM%
set P2_REPO_URL=%DEFAULT_P2_REPO_URL%
set SKIP_TESTS=false

REM Paths
set SCRIPT_DIR=%~dp0
set BUILD_DIR=%SCRIPT_DIR%3.7
set POM_PATH=%BUILD_DIR%\build\p4eclipse_parent
set CORE_PLUGIN_PATH=%BUILD_DIR%\plugins\com.perforce.team.core
set REPLACE_XML=%POM_PATH%\replacep4java.xml

REM Color codes for Windows
set INFO_COLOR=[94m[INFO][0m
set OK_COLOR=[92m[OK][0m
set ERROR_COLOR=[91m[ERROR][0m
set WARN_COLOR=[93m[WARN][0m

REM Parse command line arguments
set VERSION=
call :parse_arguments %*

REM Check if version was provided
if "%VERSION%"=="" goto show_usage

REM Main execution
call :log "Building P4Eclipse %VERSION% with Tycho %TYCHO_VERSION%"
call :log "Using target: %TARGET_PLATFORM%, P2 repo: %P2_REPO_URL%"
call :validate_version "%VERSION%"
call :check_prerequisites
call :set_version "%VERSION%"
call :process_sources
call :build_project

if "%SKIP_TESTS%"=="true" (
    call :warn "Skipping tests as requested"
) else (
    call :run_tests
)

call :success "Build completed! Artifacts in target directories"
goto end

:show_usage
echo Usage: %0 ^<version^> [options]
echo.
echo Arguments:
echo   version     Version in X.Y.Z or X.Y.Z-SNAPSHOT format
echo.
echo Options:
echo   -t, --target PLATFORM    Target platform (default: p4e-428)
echo   -p, --p2repo URL         P2 repository URL (default: https://download.eclipse.org/releases/2023-06/)
echo   --skip-tests             Skip running tests after build
echo   -h, --help, /?           Show this help message
echo.
echo Note: --target and --p2repo must be used together (both or neither)
echo.
echo Examples:
echo   %0 12.0.0-SNAPSHOT
echo   %0 2025.1.1 --target p4e-428 --p2repo https://download.eclipse.org/releases/2023-06/
echo   %0 12.0.0-SNAPSHOT --skip-tests
echo   %0 2025.1.1 --target p4e-428 --p2repo https://example.com/ --skip-tests
echo.
echo Valid Version Formats:
echo   - X.Y.Z-SNAPSHOT (e.g., 12.0.0-SNAPSHOT)
echo   - X.Y.Z (e.g., 2025.1.1, 12.1.0)
echo.
echo Steps: prerequisites ^-^> set version ^-^> process sources ^-^> build ^-^> test
goto end

:parse_arguments
set EXPECT_TARGET=
set EXPECT_P2REPO=
set TARGET_SPECIFIED=false
set P2REPO_SPECIFIED=false

:parse_loop
if "%~1"=="" goto parse_done
if "%~1"=="-h" goto show_usage
if "%~1"=="--help" goto show_usage
if "%~1"=="/?" goto show_usage

if defined EXPECT_TARGET (
    if "%~1"=="" (
        call :error "Target platform cannot be empty"
        exit /b 1
    )
    set TARGET_PLATFORM=%~1
    set TARGET_SPECIFIED=true
    set EXPECT_TARGET=
    shift
    goto parse_loop
)

if defined EXPECT_P2REPO (
    if "%~1"=="" (
        call :error "P2 repository URL cannot be empty"
        exit /b 1
    )
    set P2_REPO_URL=%~1
    set P2REPO_SPECIFIED=true
    set EXPECT_P2REPO=
    shift
    goto parse_loop
)

if "%~1"=="-t" (
    set EXPECT_TARGET=1
    shift
    goto parse_loop
)
if "%~1"=="--target" (
    set EXPECT_TARGET=1
    shift
    goto parse_loop
)
if "%~1"=="-p" (
    set EXPECT_P2REPO=1
    shift
    goto parse_loop
)
if "%~1"=="--p2repo" (
    set EXPECT_P2REPO=1
    shift
    goto parse_loop
)
if "%~1"=="--skip-tests" (
    set SKIP_TESTS=true
    shift
    goto parse_loop
)

REM Check for unknown options
echo %~1 | findstr /B /C:"-" >nul
if not errorlevel 1 (
    call :error "Unknown option: %~1"
    exit /b 1
)

REM If not an option, assume it's the version
if "%VERSION%"=="" (
    set VERSION=%~1
) else (
    call :error "Multiple versions specified"
    exit /b 1
)

shift
goto parse_loop

:parse_done
REM Validate that --target and --p2repo are used together
if "%TARGET_SPECIFIED%"=="true" if "%P2REPO_SPECIFIED%"=="false" (
    call :error "--target specified but --p2repo is missing. Both --target and --p2repo must be used together."
    exit /b 1
)

if "%P2REPO_SPECIFIED%"=="true" if "%TARGET_SPECIFIED%"=="false" (
    call :error "--p2repo specified but --target is missing. Both --target and --p2repo must be used together."
    exit /b 1
)

exit /b 0

:validate_version
if "%~1"=="" (
    call :error "Version cannot be empty"
    exit /b 1
)
REM Strict version validation - only allow X.Y.Z or X.Y.Z-SNAPSHOT
echo %~1 | findstr /R "^[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*$" >nul
if not errorlevel 1 goto validate_version_ok

echo %~1 | findstr /R "^[0-9][0-9]*\.[0-9][0-9]*\.[0-9][0-9]*-SNAPSHOT$" >nul
if not errorlevel 1 goto validate_version_ok

call :error "Invalid version format: %~1
Only two formats allowed:
  - X.Y.Z-SNAPSHOT (e.g., 12.0.0-SNAPSHOT)
  - X.Y.Z (e.g., 2025.1.1)
Examples: 12.0.0-SNAPSHOT, 2025.1.1, 12.1.0"
exit /b 1

:validate_version_ok
exit /b 0

:check_prerequisites
call :log "Checking prerequisites..."

REM Check if Maven is installed
where mvn >nul 2>&1
if errorlevel 1 (
    call :error "Maven not found in PATH"
    exit /b 1
)

REM Get Maven version
for /f "tokens=3" %%i in ('mvn -version ^| findstr "Apache Maven"') do set MVN_VERSION=%%i
call :log "Maven version: !MVN_VERSION!"

REM Check required directories
if not exist "%POM_PATH%" (
    call :error "POM directory not found: %POM_PATH%"
    exit /b 1
)
if not exist "%CORE_PLUGIN_PATH%" (
    call :error "Core plugin directory not found: %CORE_PLUGIN_PATH%"
    exit /b 1
)
if not exist "%REPLACE_XML%" (
    call :error "Replace XML not found: %REPLACE_XML%"
    exit /b 1
)

call :success "Prerequisites OK"
exit /b 0

:set_version
call :log "Setting version to: %~1"
cd /d "%POM_PATH%" || (
    call :error "Failed to change to POM directory"
    exit /b 1
)

mvn -Dtycho.mode=maven "org.eclipse.tycho:tycho-versions-plugin:%TYCHO_VERSION%:set-version" "-DnewVersion=%~1" --batch-mode
if errorlevel 1 (
    call :error "Failed to set version"
    exit /b 1
)

call :success "Version set"
exit /b 0

:process_sources
call :log "Processing sources..."
mvn -f "%REPLACE_XML%" process-sources "-Dtargetprj=%CORE_PLUGIN_PATH%" --batch-mode
if errorlevel 1 (
    call :error "Failed to process sources"
    exit /b 1
)
call :success "Sources processed"
exit /b 0

:build_project
call :log "Building project..."
call :log "Target Platform: %TARGET_PLATFORM%"
call :log "P2 Repository: %P2_REPO_URL%"
mvn "-DforceContextQualifier=%QUALIFIER%" -P p4update,replace-help clean package "-DtargetPlatform=%TARGET_PLATFORM%" "-Dp2repo.url=%P2_REPO_URL%" --batch-mode
if errorlevel 1 (
    call :error "Build failed"
    exit /b 1
)
call :success "Build completed"
exit /b 0

:run_tests
call :log "Running tests..."
if exist "%POM_PATH%\build_linux.sh" (
    call "%POM_PATH%\build_linux.sh" testAlone
    if errorlevel 1 (
        call :error "Tests failed"
        exit /b 1
    )
    call :success "Tests passed"
) else (
    call :warn "Test script not found, skipping tests"
)
exit /b 0

REM Utility functions
:log
echo %INFO_COLOR% %~1
exit /b 0

:success
echo %OK_COLOR% %~1
exit /b 0

:error
echo %ERROR_COLOR% %~1 >&2
exit /b 1

:warn
echo %WARN_COLOR% %~1
exit /b 0

:end
endlocal
