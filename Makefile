
COMPONENT=sc-service
FULL_COMPONENT=hsn2-sc-service

all:	${FULL_COMPONENT}-package

clean:	${FULL_COMPONENT}-package-clean

${FULL_COMPONENT}-package:
	mvn clean install -U -Pbundle -Dmaven.test.skip
	mkdir -p build/shell-scdbg
	tar xzf target/${FULL_COMPONENT}-1.0.0-SNAPSHOT.tar.gz -C build/shell-scdbg
	cp scdbg-amd64 build/shell-scdbg/lib/scdbg
	chmod +x build/shell-scdbg/lib/scdbg

${FULL_COMPONENT}-package-clean:
	rm -rf build

build-local:
	mvn clean install -U -Pbundle