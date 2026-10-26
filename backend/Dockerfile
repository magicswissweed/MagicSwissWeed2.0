FROM amazoncorretto:21
# AL2023 pins the repo release: without --releasever=latest the upgrade is a no-op
RUN dnf -y upgrade --refresh --releasever=latest && dnf clean all && rm -rf /var/cache/dnf
VOLUME /tmp
ADD app.jar app.jar
RUN mkdir /server
ENTRYPOINT ["java", "-jar","/app.jar"]
