#!/bin/bash
/opt/perforce/swarm/sbin/configure-swarm.sh --non-interactive \
	--p4port ${P4PORT} \
	--swarm-user ${SWARMUSER} --swarm-passwd ${SWARMPASSWD} \
	--swarm-host ${SWARMHOST} --email-host ${MAILHOST} \
	--super-user ${P4USER} --super-passwd ${P4PASSWD} 
if [ -x ${SWARMHOME}/sbin/redis-server-swarm ]
then
  ${SWARMHOME}/sbin/redis-server-swarm $SWARMETC/redis-server.conf --daemonize yes
fi
apachectl start &&
service cron start &&
echo "Running..."