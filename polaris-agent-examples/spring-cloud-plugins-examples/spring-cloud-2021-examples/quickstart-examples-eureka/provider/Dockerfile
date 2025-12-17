FROM alpine:3.19.1

ARG file_name
ARG java_version

COPY ./target/${file_name} /app/main.jar

WORKDIR /app

RUN sed -i 's!http://dl-cdn.alpinelinux.org/!https://mirrors.tencent.com/!g' /etc/apk/repositories

RUN set -eux && \
    apk update && \
    apk add --no-cache openjdk${java_version} && \
    apk add --no-cache bind-tools && \
    apk add --no-cache busybox-extras && \
    apk add --no-cache findutils && \
    apk add --no-cache tcpdump && \
    apk add --no-cache tzdata && \
    apk add --no-cache curl && \
    apk add --no-cache bash && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    date

RUN chmod 777 /app/

RUN ls -la /app/

ENTRYPOINT ["java", "-jar", "/app/main.jar"]
