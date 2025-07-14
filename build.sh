
VER=$1
TYCHO_VERSION="0.20.0"
CURRENT_DIR=$(pwd)
POM_PATH="$CURRENT_DIR/3.7/build/p4eclipse_parent"
BCHDIR="$CURRENT_DIR/3.7"
QUALIFIER=MAIN-TEST_ONLY-SNAPSHOT


cd $POM_PATH
echo "Current directory: $POM_PATH"


props="-DtargetPlatform=p4e-428 -Dp2repo.url=https://download.eclipse.org/releases/2023-06/ --batch-mode"
echo "Properties: $props"

mvn -Dtycho.mode=maven org.eclipse.tycho:tycho-versions-plugin:$TYCHO_VERSION:set-version -DnewVersion=$VER --batch-mode

mvn -f ${BCHDIR}/build/p4eclipse_parent/replacep4java.xml process-sources -Dtargetprj=${BCHDIR}/plugins/com.perforce.team.core --batch-mode


mvn -DforceContextQualifier=$QUALIFIER -P p4update,replace-help clean package ${props}

# Run test
echo "Properties: $props"
echo "Current directory before running test: $POM_PATH"
./build_linux.sh testAlone