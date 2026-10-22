#!/bin/sh

# This script is used to replace the environment-variables in the docker-image
# Otherwise we would have to embed the environment-variables during build-time.
# This would be bad, because sensitive information would be stored in the docker image.

for i in $(env | grep MY_APP_)
do
    key=$(echo $i | cut -d '=' -f 1)
    value=$(echo $i | cut -d '=' -f 2-)
    # only the key: the values must not end up in the container log
    echo "substituting $key"
    # sed All files
    # find /usr/share/nginx/html -type f -exec sed -i "s|${key}|${value}|g" '{}' +

    # sed JS and CSS only
    find /usr/share/nginx/html -type f \( -name '*.js' -o -name '*.css' \) -exec sed -i "s|${key}|${value}|g" '{}' +
done