FROM modulator:latest

MAINTAINER Fenglin Chen <f73chen@uwaterloo.ca>

# packages should already be set up in modulator:latest
USER root

# move in the yaml to build modulefiles from
COPY bcl2fastq_recipe.yaml /modulator/code/gsi/recipe.yaml

# install the programs required for the yaml build
RUN apt-get -m update && apt-get install -y rpm2cpio cpio

# build the modules and set folder / file permissions
RUN ./build-local-code /modulator/code/gsi/recipe.yaml --initsh /usr/share/modules/init/sh --output /modules && \
	find /modules -type d -exec chmod 777 {} \; && \
	find /modules -type f -exec chmod 777 {} \;

RUN cat /modules/gsi/modulator/sw/Ubuntu18.04/bcl2fastq-2.20.0.4.buildlog

# add the user
RUN groupadd -r -g 1000 ubuntu && useradd -r -g ubuntu -u 1000 ubuntu
USER ubuntu

# copy the setup file to load the modules at startup
 COPY .bashrc /home/ubuntu/.bashrc

CMD /bin/bash
