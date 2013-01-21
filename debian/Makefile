#
# This Makefile is responsoble for build debian package.
# 4 variables must be provided:
#   HSN2_VER=2.X
#   BUILD_NUMBER=999
#   DEBIAN_DIST=(experimental|2.X)

DEBIAN_DIST=experimental
HSN2_COMPONENT=shell-scdbg

all:	hsn2-shell-scdbg-amd64	hsn2-shell-scdbg-i386

clean:	hsn2-shell-scdbg-clean

PKGi386=hsn2-$(HSN2_COMPONENT)_$(HSN2_VER)-$(BUILD_NUMBER)_i386
hsn2-shell-scdbg-i386: hsn2-shell-scdbg-clean
	mkdir -p $(PKGi386)/DEBIAN
	mkdir -p $(PKGi386)/etc/init.d
	mkdir -p $(PKGi386)/etc/hsn2
	mkdir -p $(PKGi386)/opt/hsn2/$(HSN2_COMPONENT)
	mkdir -p $(PKGi386)/var/log/hsn2
	tar -zxf ../target/hsn2-sc-service-*.tar.gz -C $(PKGi386)/opt/hsn2/$(HSN2_COMPONENT)
	cp shellcode.initd $(PKGi386)/etc/init.d/hsn2-$(HSN2_COMPONENT)
	cp control $(PKGi386)/DEBIAN
	cp scdbg-i386 $(PKGi386)/opt/hsn2/$(HSN2_COMPONENT)/lib/scdbg
	sed -i "s/{VER}/${HSN2_VER}-${BUILD_NUMBER}/" $(PKGi386)/DEBIAN/control
	sed -i "s/{DEBIAN_DIST}/${DEBIAN_DIST}/" $(PKGi386)/DEBIAN/control
	sed -i "s/amd64/i386/" $(PKGi386)/DEBIAN/control
	fakeroot dpkg -b $(PKGi386)

PKGamd64=hsn2-$(HSN2_COMPONENT)_$(HSN2_VER)-$(BUILD_NUMBER)_amd64
hsn2-shell-scdbg-amd64: hsn2-shell-scdbg-clean
	mkdir -p $(PKGamd64)/DEBIAN
	mkdir -p $(PKGamd64)/etc/init.d
	mkdir -p $(PKGamd64)/etc/hsn2
	mkdir -p $(PKGamd64)/opt/hsn2/$(HSN2_COMPONENT)
	mkdir -p $(PKGamd64)/var/log/hsn2
	tar -zxf ../target/hsn2-sc-service-*.tar.gz -C $(PKGamd64)/opt/hsn2/$(HSN2_COMPONENT)
	cp shellcode.initd $(PKGamd64)/etc/init.d/hsn2-$(HSN2_COMPONENT)
	cp control $(PKGamd64)/DEBIAN
	cp scdbg-amd64 $(PKGamd64)/opt/hsn2/$(HSN2_COMPONENT)/lib/scdbg
	sed -i "s/{VER}/${HSN2_VER}-${BUILD_NUMBER}/" $(PKGamd64)/DEBIAN/control
	sed -i "s/{DEBIAN_DIST}/${DEBIAN_DIST}/" $(PKGamd64)/DEBIAN/control
	fakeroot dpkg -b $(PKGamd64)

hsn2-shell-scdbg-clean:
	rm -rf hsn2-$(HSN2_COMPONENT)*
