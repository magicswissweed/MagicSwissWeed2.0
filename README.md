# MagicSwissWeed

## Initial Setup

1. Copy [application.properties-TEMPLATE](backend%2Fsrc%2Fmain%2Fresources%2Fapplication.properties-TEMPLATE) and rename
   to `application.properties`
    - replace the firebase keys
2. Copy [.env.local-TEMPLATE](frontend%2F.env.local-TEMPLATE) and rename it to `.env.local`
    - replace the secret values
3. Copy [http-client.private.env.json-TEMPLATE](http-client%2Fhttp-client.private.env.json-TEMPLATE) and rename to '
   http-client.private.env.json'
    - replace the secret values

## Run the backend

1. Run the [docker-compose.yml](docker-compose.yml) to start the database.

    ```bash
      docker compose up
    ```

2. Connect to database via plugin (psql) with
    - name: msw
    - username: develop
    - password: develop

    Or psql (type `exit` to exit.):

    ```bash
    PGPASSWORD=develop psql -d msw -U develop -h localhost
    ```

   and run the following commands to populate the database:

    ```sql
      INSERT INTO public.spot_table (id, type, stationid, name, measurement_type, min_value, max_value, ispublic) VALUES ('815cf49f-8c7c-4801-8b2d-62fb874486dd', 'BUNGEE_SURF', 2473, 'St. Gallen', 'FLOW', 130, 1300, true);
      INSERT INTO public.spot_table (id, type, stationid, name, measurement_type, min_value, max_value, ispublic) VALUES ('02dd6d9d-1a1c-4835-b81c-1a038aacd9ab', 'BUNGEE_SURF', 2243, 'Zürich', 'FLOW', 75, 350, true);
      INSERT INTO public.spot_table (id, type, stationid, name, measurement_type, min_value, max_value, ispublic) VALUES ('76e9b6c6-ea60-4769-aa6b-1c4262fbf883', 'BUNGEE_SURF', 2152, 'Luzern', 'FLOW', 80, 350, true);
      INSERT INTO public.spot_table (id, type, stationid, name, measurement_type, min_value, max_value, ispublic) VALUES ('f0a29af1-4e12-4431-974c-f2e39e42ff51', 'BUNGEE_SURF', 2091, 'Basel', 'FLOW', 850, 2500, true);
      INSERT INTO public.spot_table (id, type, stationid, name, measurement_type, min_value, max_value, ispublic) VALUES ('134463b8-1c0d-43d6-be3f-8693d283a418', 'BUNGEE_SURF', 2135, 'Bern', 'FLOW', 80, 360, true);
      INSERT INTO public.spot_table (id, type, stationid, name, measurement_type, min_value, max_value, ispublic) VALUES ('134463b8-1c0d-43d6-be3f-8693d283a419', 'RIVER_SURF', 2018, 'Your own surf spot', 'FLOW', 200, 400, true);
    ```

3. Run the [backend](backend/src/main/java/com/aa/msw/MswApplication.java) using the Run Configuration (built in feature
   of IntelliJ IDEA), for VS Code,
   check [this Java extension](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack).

## Start the frontend

```bash
  cd frontend
  npm install
  npm start
```

## More Information

- [Frontend](frontend/README.md)
- [Backend](backend/README.md)
- [Deployment](deployment/README.md)
