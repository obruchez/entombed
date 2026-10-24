# Entombed in the browser: the optimised Scala.js build, served as static files by nginx
# (unprivileged: listens on port 8080 as a non-root user).
#
# Build the site first, then the image:
#   sbt site
#   docker buildx build --platform linux/amd64,linux/arm64 -t obruchez/entombed:<version> --push .
FROM nginxinc/nginx-unprivileged:1.27-alpine

COPY docker/nginx.conf /etc/nginx/conf.d/default.conf
COPY docs/ /usr/share/nginx/html/

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/healthz || exit 1
