
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
    version     Version to set for the build (e.g., 11.1.1-SNAPSHOT)

Options:
    -t, --target PLATFORM    Target platform (default: p4e-428)
    -p, --p2repo URL         P2 repository URL (default: Eclipse 2023-06)
    -h, --help               Show this help message

Examples:
  $0 11.1.1-SNAPSHOT
  $0 11.1.1-SNAPSHOT -t p4e-43 -p https://download.eclipse.org/releases/neon/
  $0 12.0.0-RC1 --target p4e-44 --p2repo https://download.eclipse.org/releases/oxygen/

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
    
    if [[ ! "$1" =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[A-Za-z0-9]+)?$ ]]; then
        warn "Non-standard version format: $1 (expected: X.Y.Z or X.Y.Z-QUALIFIER)"
    fi
}

# Parse command line arguments
parse_arguments() {
    local version=""
    local target_platform="$TARGET_PLATFORM"
    local p2_repo_url="$P2_REPO_URL"
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            -t|--target)
                [[ -n "$2" ]] || error "Target platform cannot be empty"
                target_platform="$2"
                shift 2
                ;;
            -p|--p2repo)
                [[ -n "$2" ]] || error "P2 repository URL cannot be empty"
                p2_repo_url="$2"
                shift 2
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
    
    echo "$version|$target_platform|$p2_repo_url"
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
    IFS='|' read -r version target_platform p2_repo_url <<< "$args"
    
    log "Building P4Eclipse ${version} with Tycho ${TYCHO_VERSION}"
    log "Using target: $target_platform, P2 repo: $p2_repo_url"
    
    validate_version "$version"
    check_prerequisites
    set_version "${version}"
    process_sources
    build_project "$target_platform" "$p2_repo_url"
    run_tests
    
    success "Build completed! Artifacts in target directories"
}

# Entry point
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    [[ $# -eq 0 ]] && { show_usage; exit 0; }
    main "$@"
fi