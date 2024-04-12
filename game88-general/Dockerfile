FROM 77tv/openjdk-21-jre:latest

RUN mkdir -p /opt/logs/ /opt/tmp/

COPY app.jar /opt/app.jar

WORKDIR /opt/

CMD ["java", \
	"-XX:+UseZGC", \
	"-XX:+ZGenerational", \
	"-Dspring.profiles.active=prod", \
	"-Djava.io.tmpdir=/opt/tmp/", \
	"-Djava.library.path=/usr/local/lib/", \
	"-Djava.security.egd=file:/dev/./urandom", \
	"-Dfile.encoding=UTF-8", \
	"-jar", "/opt/app.jar" \
]