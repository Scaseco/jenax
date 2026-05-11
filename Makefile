CWD = $(shell pwd)

POM = -f pom.xml
# Maven Clean Install Skip ; skip tests, javadoc, scaladoc, etc
MS = mvn -DskipTests -Dmaven.javadoc.skip=true -Dskip
MCIS = $(MS) clean install
MCCS = $(MS) clean compile

VER = $(error specify VER=releasefile-name e.g. VER=1.9.7-rc2)
loud = echo "@@" $(1);$(1)

# Source: https://stackoverflow.com/questions/4219255/how-do-you-get-the-list-of-targets-in-a-makefile
.PHONY: help

.ONESHELL:
help:   ## Show these help instructions
	@sed -rn 's/^([a-zA-Z_-]+):.*?## (.*)$$/"\1" "\2"/p' < $(MAKEFILE_LIST) | xargs printf "make %-20s# %s\n"

arq-plugin-bundle: ## Create the self-contained JenaX Plugin Bundle JAR (ARQ and Fuseki)
	$(MCCS) $(POM) package -Pbundle -pl :jenax-arq-plugins-bundle -am $(ARGS)
	file=`find '$(CWD)/jenax-arq-parent/jenax-arq-plugins-parent/jenax-arq-plugins-bundle/target' -name '*-arq-plugin*.jar'`
	printf '\nCreated package:\n\n%s\n\n' "$$file"

arq-plugin-serviceenhancer: ## Create the self-contained Service Enhancer Preview Plugin JAR (ARQ and Fuseki)
	$(MCCS) $(POM) package -Pbundle -pl :jenax-serviceenhancer-preview -am $(ARGS)
	file=`find '$(CWD)/jenax-serviceenhancer-preview/target' -name '*-arq-plugin*.jar'`
	printf '\nCreated package:\n\n%s\n\n' "$$file"

release-github: SHELL:=/bin/bash
release-github: ## Create files for Github upload
	@set -eu
	ver=$(VER)
	$(call loud,$(MAKE) arq-plugin-bundle)
	file1=`find '$(CWD)/jenax-arq-parent/jenax-arq-plugins-parent/jenax-arq-plugins-bundle/target' -name '*-arq-plugin*.jar'`
	mkdir -p staging
	$(call loud,cp "$$file1" "staging/jenax-arq-plugins-bundle-$$ver.jar")
	$(call loud,$(MAKE) arq-plugin-serviceenhancer)
	file2=`find '$(CWD)/jenax-serviceenhancer-preview/target' -name '*-arq-plugin*.jar'`
	$(call loud,cp "$$file2" "staging/jenax-serviceenhancer-preview-plugin-$$ver.jar")
	$(call loud,gh release create v$$ver "staging/jenax-arq-plugins-bundle-$$ver.jar" "staging/jenax-serviceenhancer-preview-plugin-$$ver.jar")

