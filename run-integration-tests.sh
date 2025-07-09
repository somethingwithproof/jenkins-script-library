#!/bin/bash
# Jenkins Script Library Integration Test Runner
# This script provides an easy way to run integration tests with Docker

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
cd "$SCRIPT_DIR"

# Default values
ACTION="test"
KEEP_RUNNING=false
VERBOSE=false
SPECIFIC_TEST=""

# Help function
show_help() {
    cat << EOF
Jenkins Script Library Integration Test Runner

Usage: $0 [OPTIONS] [ACTION]

Actions:
    test        Run all integration tests (default)
    start       Start the integration environment
    stop        Stop the integration environment
    logs        Show logs from all containers
    shell       Open shell in Jenkins container
    status      Show status of all containers
    clean       Stop environment and clean up volumes

Options:
    -h, --help      Show this help message
    -k, --keep      Keep environment running after tests
    -v, --verbose   Show verbose output
    -t, --test      Run specific test class (e.g., CloudNodeManagerIntegrationTest)

Examples:
    $0                      # Run all integration tests
    $0 start                # Start environment only
    $0 -k test             # Run tests and keep environment running
    $0 -t JobManagement    # Run specific test class

EOF
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        -k|--keep)
            KEEP_RUNNING=true
            shift
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        -t|--test)
            SPECIFIC_TEST="$2"
            shift 2
            ;;
        start|stop|test|logs|shell|status|clean)
            ACTION="$1"
            shift
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Logging functions
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        log_error "Docker daemon is not running"
        exit 1
    fi
    
    log_info "Prerequisites check passed"
}

# Start integration environment
start_environment() {
    log_info "Starting integration environment..."
    
    if [ "$VERBOSE" = true ]; then
        docker-compose -f docker-compose.integration.yml up -d
    else
        docker-compose -f docker-compose.integration.yml up -d > /dev/null 2>&1
    fi
    
    log_info "Waiting for Jenkins to be ready..."
    local max_attempts=60
    local attempt=0
    
    while [ $attempt -lt $max_attempts ]; do
        if curl -s -f http://localhost:8080/login > /dev/null 2>&1; then
            log_info "Jenkins is ready!"
            break
        fi
        
        attempt=$((attempt + 1))
        if [ $((attempt % 10)) -eq 0 ]; then
            log_info "Still waiting for Jenkins... ($attempt/$max_attempts)"
        fi
        sleep 2
    done
    
    if [ $attempt -eq $max_attempts ]; then
        log_error "Jenkins failed to start within timeout"
        docker-compose -f docker-compose.integration.yml logs jenkins-integration
        exit 1
    fi
    
    log_info "Integration environment is ready"
    log_info "Jenkins UI: http://localhost:8080 (user: test, password: test123)"
}

# Stop integration environment
stop_environment() {
    log_info "Stopping integration environment..."
    docker-compose -f docker-compose.integration.yml down
    log_info "Environment stopped"
}

# Clean up everything
clean_environment() {
    log_info "Cleaning up integration environment..."
    docker-compose -f docker-compose.integration.yml down -v --remove-orphans
    
    # Clean up test results
    if [ -d "build/integration-test-results" ]; then
        rm -rf build/integration-test-results
    fi
    
    log_info "Cleanup completed"
}

# Show container status
show_status() {
    log_info "Integration environment status:"
    docker-compose -f docker-compose.integration.yml ps
}

# Show logs
show_logs() {
    if [ "$VERBOSE" = true ]; then
        docker-compose -f docker-compose.integration.yml logs -f
    else
        docker-compose -f docker-compose.integration.yml logs -f --tail=100
    fi
}

# Open shell in Jenkins container
open_shell() {
    log_info "Opening shell in Jenkins container..."
    docker-compose -f docker-compose.integration.yml exec jenkins-integration bash
}

# Run integration tests
run_tests() {
    log_info "Running integration tests..."
    
    # Ensure environment is running
    if ! docker-compose -f docker-compose.integration.yml ps | grep -q "jenkins-integration.*Up"; then
        start_environment
    fi
    
    # Build test command
    local test_cmd="./gradlew integrationTest"
    
    if [ -n "$SPECIFIC_TEST" ]; then
        test_cmd="$test_cmd --tests '*${SPECIFIC_TEST}*'"
        log_info "Running specific test: $SPECIFIC_TEST"
    fi
    
    if [ "$VERBOSE" = true ]; then
        test_cmd="$test_cmd --info"
    fi
    
    # Run tests
    if $test_cmd; then
        log_info "Integration tests passed!"
        
        # Show test report location
        if [ -f "build/reports/tests/integrationTest/index.html" ]; then
            log_info "Test report: file://${SCRIPT_DIR}/build/reports/tests/integrationTest/index.html"
        fi
    else
        log_error "Integration tests failed!"
        
        # Show Jenkins logs on failure
        log_warn "Showing recent Jenkins logs:"
        docker-compose -f docker-compose.integration.yml logs --tail=50 jenkins-integration
        
        exit 1
    fi
}

# Main execution
main() {
    check_prerequisites
    
    case $ACTION in
        start)
            start_environment
            ;;
        stop)
            stop_environment
            ;;
        clean)
            clean_environment
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
            ;;
        shell)
            open_shell
            ;;
        test)
            run_tests
            if [ "$KEEP_RUNNING" = false ]; then
                stop_environment
            else
                log_info "Environment kept running. Use '$0 stop' to shut down."
            fi
            ;;
        *)
            log_error "Unknown action: $ACTION"
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main