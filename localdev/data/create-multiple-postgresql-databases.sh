#!/bin/bash

set -o errexit
set -o errtrace
set -o pipefail
set -o nounset

#set -x

function create_user_and_database() {
  local rawString=$1
	local user=$(echo $rawString | tr ':' ' ' | awk '{print $1}')
	local password=$(echo $rawString | tr ':' ' ' | awk '{print $2}')
	local database=$(echo $rawString | tr ':' ' ' | awk '{print $3}')
	echo "  Creating user '$user' with password '$password' for db '$database'"
	psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
	    CREATE USER $user WITH PASSWORD '$password';
	    CREATE DATABASE $database OWNER $user;
	    GRANT ALL PRIVILEGES ON DATABASE $database TO $user;
EOSQL
}

if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
	echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
	for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
		create_user_and_database $db
	done
	echo "Multiple databases created"
fi
