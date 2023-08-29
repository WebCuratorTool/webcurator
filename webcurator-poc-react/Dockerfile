# Proxies to resolve CORS concerns.
FROM alpine:3

ARG NGINX_MAJOR_VERSION=1
ARG GETTEXT_MAJOR_VERSION=0

RUN apk update && apk add --no-cache \
    nginx=~${NGINX_MAJOR_VERSION} \
    gettext=~${GETTEXT_MAJOR_VERSION}

WORKDIR /etc/nginx/
COPY nginx.conf .
ENTRYPOINT ["nginx"]