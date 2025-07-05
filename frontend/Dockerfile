FROM node:20.1.0 AS build

# Set the working directory
WORKDIR /msw-frontend/

# Copy project files
COPY public/ public
COPY src/ src
COPY package.json package.json
COPY tsconfig.json tsconfig.json
COPY .env.production .env.production

# Replace localhost with the production URL
ARG API_BASE_URL=https://api.msw.ponteshare.ch
ENV API_BASE_URL=${API_BASE_URL}
RUN sed -i 's|http://localhost:8080|${API_BASE_URL}|g' src/gen/msw-api-ts/base.ts

RUN npm install
RUN npm run build

FROM nginx:1.25.3
COPY --from=build /msw-frontend/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
# default port for nginx is 80, but we overwrite it to 3000 in nginx.conf
EXPOSE 3000
COPY env.sh /docker-entrypoint.d/env.sh
RUN chmod +x /docker-entrypoint.d/env.sh
CMD [ "nginx","-g","daemon off;" ]
