
#!/bin/bash
# P4Eclipse Build Script - Builds P4Eclipse plugin with Maven/Tycho
# Usage: ./build.sh <version>

set -euo pipefail

# Configuration
readonly SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly TYCHO_VERSION="0.20.0"
readonly QUALIFIER="MAIN-TEST_ONLY-SNAPSHOT"
readonly TARGET_PLATFORM="p4e-428"
readonly P2_REPO_URL="https://download.eclipse.org/releases/2023-06/"

# Paths
readonly BUILD_DIR="${SCRIPT_DIR}/3.7"
readonly POM_PATH="${BUILD_DIR}/build/p4eclipse_parent"
readonly CORE_PLUGIN_PATH="${BUILD_DIR}/plugins/com.perforce.team.core"
readonly REPLACE_XML="${POM_PATH}/replacep4java.xml"

# Utility functions
log() { echo -e "\033[0;34m[INFO]\033[0m $1"; }
success() { echo -e "\033[0;32m[OK]\033[0m $1"; }
error() { echo -e "\033[0;31m[ERROR]\033[0m $1" >&2; exit 1; }
warn() { echo -e "\033[0;33m[WARN]\033[0m $1"; }

show_usage() {
    cat << EOF
Usage: $0 <version> [options]

Arguments:
    version     Version in X.Y.Z or X.Y.Z-SNAPSHOT format

Options:
    -t, --target PLATFORM    Target platform (default: p4e-428)
    -p, --p2repo URL         P2 repository URL (default: https://download.eclipse.org/releases/2023-06/)
    --skip-tests             Skip running tests after build
    -h, --help               Show this help message

Note: --target and --p2repo must be used together (both or neither)

Examples:
  $0 12.0.0-SNAPSHOT
  $0 2025.1.1 --target p4e-428 --p2repo https://download.eclipse.org/releases/2023-06/
  $0 12.0.0-SNAPSHOT --skip-tests
  $0 2025.1.1 --target p4e-428 --p2repo https://example.com/ --skip-tests

Valid Version Formats:
  - X.Y.Z-SNAPSHOT (e.g., 12.0.0-SNAPSHOT)
  - X.Y.Z (e.g., 2025.1.1, 12.1.0)

Steps: prerequisites → set version → process sources → build → test
EOF
}

check_prerequisites() {
    log "Checking prerequisites..."
    
    command -v mvn &> /dev/null || error "Maven not found in PATH"
    
    local mvn_version=$(mvn -version | head -n 1 | cut -d' ' -f3)
    log "Maven version: ${mvn_version}"
    
    [[ -d "${POM_PATH}" ]] || error "POM directory not found: ${POM_PATH}"
    [[ -d "${CORE_PLUGIN_PATH}" ]] || error "Core plugin directory not found: ${CORE_PLUGIN_PATH}"
    [[ -f "${REPLACE_XML}" ]] || error "Replace XML not found: ${REPLACE_XML}"
    
    success "Prerequisites OK"
}

validate_version() {
    [[ $# -ge 1 ]] || { show_usage; exit 1; }
    [[ -n "$1" ]] || error "Version cannot be empty"
    
    # Only allow two specific formats:
    # 1. X.Y.Z-SNAPSHOT (e.g., 12.0.0-SNAPSHOT)
    # 2. X.Y.Z with two dots (e.g., 2025.1.1)
    if [[ ! "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$ ]]; then
        error "Invalid version format: $1
Only two formats allowed:
  - X.Y.Z-SNAPSHOT (e.g., 12.0.0-SNAPSHOT)
  - X.Y.Z (e.g., 2025.1.1)
Examples: 12.0.0-SNAPSHOT, 2025.1.1, 12.1.0"
    fi
}

# Parse command line arguments
parse_arguments() {
    local version=""
    local target_platform="$TARGET_PLATFORM"
    local p2_repo_url="$P2_REPO_URL"
    local skip_tests="false"
    local target_specified="false"
    local p2repo_specified="false"
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -t|--target)
                [[ -n "$2" ]] || error "Target platform cannot be empty"
                target_platform="$2"
                target_specified="true"
                shift 2
                ;;
            -p|--p2repo)
                [[ -n "$2" ]] || error "P2 repository URL cannot be empty"
                p2_repo_url="$2"
                p2repo_specified="true"
                shift 2
                ;;
            --skip-tests)
                skip_tests="true"
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            -*)
                error "Unknown option: $1"
                ;;
            *)
                if [[ -z "$version" ]]; then
                    version="$1"
                else
                    error "Multiple versions specified"
                fi
                shift
                ;;
        esac
    done
    
    [[ -n "$version" ]] || error "Version is required"
    
    # Validate that --target and --p2repo are used together
    if [[ "$target_specified" == "true" && "$p2repo_specified" == "false" ]]; then
        error "--target specified but --p2repo is missing. Both --target and --p2repo must be used together."
    fi
    
    if [[ "$p2repo_specified" == "true" && "$target_specified" == "false" ]]; then
        error "--p2repo specified but --target is missing. Both --target and --p2repo must be used together."
    fi
    
    echo "$version|$target_platform|$p2_repo_url|$skip_tests"
}

set_version() {
    log "Setting version to: $1"
    cd "${POM_PATH}" || error "Failed to change to POM directory"
    
    mvn -Dtycho.mode=maven \
        "org.eclipse.tycho:tycho-versions-plugin:${TYCHO_VERSION}:set-version" \
        "-DnewVersion=$1" --batch-mode || error "Failed to set version"
    
    success "Version set"
}

process_sources() {
    log "Processing sources..."
    mvn -f "${REPLACE_XML}" process-sources "-Dtargetprj=${CORE_PLUGIN_PATH}" \
        --batch-mode || error "Failed to process sources"
    success "Sources processed"
}

build_project() {
    local target_platform="$1"
    local p2_repo_url="$2"
    
    log "Building project..."
    log "Target Platform: $target_platform"
    log "P2 Repository: $p2_repo_url"
    
    mvn "-DforceContextQualifier=${QUALIFIER}" -P p4update,replace-help clean package \
        "-DtargetPlatform=$target_platform" "-Dp2repo.url=$p2_repo_url" \
        --batch-mode || error "Build failed"
    success "Build completed"
}

run_tests() {
    log "Running tests..."
    if [[ -f "${POM_PATH}/build_linux.sh" ]]; then
        "${POM_PATH}/build_linux.sh" testAlone || error "Tests failed"
        success "Tests passed"
    else
        warn "Test script not found, skipping tests"
    fi
}

# Main execution
main() {
    local args
    args=$(parse_arguments "$@")
    IFS='|' read -r version target_platform p2_repo_url skip_tests <<< "$args"
    
    log "Building P4Eclipse ${version} with Tycho ${TYCHO_VERSION}"
    log "Using target: $target_platform, P2 repo: $p2_repo_url"
    
    validate_version "$version"
    check_prerequisites
    set_version "${version}"
    process_sources
    build_project "$target_platform" "$p2_repo_url"
    
    if [[ "$skip_tests" == "true" ]]; then
        warn "Skipping tests as requested"
    else
        run_tests
    fi
    
    success "Build completed! Artifacts in target directories"
}

# Entry point
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    # Handle help and no arguments before calling main
    [[ $# -eq 0 ]] && { show_usage; exit 0; }
    [[ "$1" == "-h" || "$1" == "--help" ]] && { show_usage; exit 0; }
    main "$@"
fi