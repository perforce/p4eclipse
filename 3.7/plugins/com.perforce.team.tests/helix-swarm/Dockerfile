# --------------------------------------------------------------------------------
# Docker configuration for SWARM
# --------------------------------------------------------------------------------

FROM ubuntu:20.04

MAINTAINER Paul Allen (pallen@perforce.com)

# Update Ubuntu
RUN \
    apt-get update -y && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y  wget sudo gnupg netcat php-redis  && \
	wget -qO - https://package.perforce.com/perforce.pubkey | sudo apt-key add - && \
    echo "deb http://package.perforce.com/apt/ubuntu focal release" > /etc/apt/sources.list.d/perforce.list && \
    apt-get update -y

ENV SWARMHOME=/opt/perforce/swarm
ENV SWARMETC=/opt/perforce/etc

# Create P4 Code Review user and add to perforce group
RUN useradd --home-dir /home/swarm --create-home --uid 1000 swarm
RUN usermod -G sudo -a swarm
RUN echo swarm:swarm | /usr/sbin/chpasswd
RUN chown -R swarm:swarm /home/swarm

# Install Perforce Client and Swarm
RUN apt-get install -y helix-cli
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y helix-swarm


# Configure Swarm
ENV P4PORT ${P4PORT}
ENV P4USER ${P4USER}
ENV P4PASSWD ${P4PASSWD}
ENV SWARMHOST ${SWARMHOST}
ENV SWARMUSER ${SWARMUSER}
ENV SWARMPASSWD ${SWARMPASSWD}
ENV MAILHOST ${MAILHOST}

RUN chown -R www-data:www-data /opt/perforce/swarm/data
RUN a2dissite 000-default

COPY files/start_swarm.sh /usr/local/bin
RUN chmod +x /usr/local/bin/start_swarm.sh

ENTRYPOINT \
	until nc -zw 1 p4.helix 1666; do sleep 1; done && \
    cron start && \
	usr/local/bin/start_swarm.sh && \
    echo swarm ready && \
	bash