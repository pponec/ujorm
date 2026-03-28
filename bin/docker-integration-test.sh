#/bin/sh
# =====================================================================
# Ujorm Integration Tests
# =====================================================================
# Note: Downloading the required database Docker images will
#       take up to 6 GB of your local disk space.
#
# Testcontainers automatically remove running containers after tests.
# To clean up and delete ALL unused Docker images from your system, run:
# $ docker image prune -a
# =====================================================================

set -e
readonly PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
mvn() { bash "$PROJECT_ROOT/mvnw" "$@"; }
mvn -version
cd "$PROJECT_ROOT"

# Check if Docker is installed, running, and accessible by the current user
if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker is either not installed, not running, or the current user lacks permissions." >&2
    exit 1
fi

echo "Install modules..."
mvn clean install -DskipTests

echo "Starting the integration docker tests..."
OPTS="-P docker-it -pl ujorm3-integration-tests"
mvn verify $OPTS -Dit.test=PostgresTutorialIT
mvn verify $OPTS -Dit.test=MySqlTutorialIT
mvn verify $OPTS -Dit.test=MariaDbTutorialIT
mvn verify $OPTS -Dit.test=MsSqlTutorialIT
mvn verify $OPTS -Dit.test=OracleTutorialIT
