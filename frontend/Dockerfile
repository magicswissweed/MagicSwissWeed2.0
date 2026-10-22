# node 20 is end-of-life (2026-04); 24 is the current LTS
FROM node:24 AS build

# Set the working directory
WORKDIR /msw-frontend/

# Copy project files
COPY public/ public
COPY src/ src
COPY package.json package.json
COPY package-lock.json package-lock.json
COPY tsconfig.json tsconfig.json
COPY .env.production .env.production

# Replace localhost with the production URL
ARG API_BASE_URL=https://api.msw.ponteshare.ch
ENV API_BASE_URL=${API_BASE_URL}
RUN sh -c 'sed -i "s|http://localhost:8080|${API_BASE_URL}|g" src/gen/msw-api-ts/base.ts'

# no source maps in the published bundle
ENV GENERATE_SOURCEMAP=false

# ci = install exactly the versions of the lockfile (reproducible, same as locally)
RUN npm ci
RUN npm run build

# 1.25.3 was two years without security fixes; follow the stable 1.30 line
FROM nginx:1.30-alpine
COPY --from=build /msw-frontend/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
# default port for nginx is 80, but we overwrite it to 3000 in nginx.conf
EXPOSE 3000
COPY env.sh /docker-entrypoint.d/env.sh
RUN chmod +x /docker-entrypoint.d/env.sh
CMD [ "nginx","-g","daemon off;" ]
