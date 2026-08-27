# MagicSwissWeed

## Initial Setup

The repository only contains `*-TEMPLATE` files with placeholder values. Real secrets are **not** committed — copy
each template to its real filename (the real names are git-ignored) and fill in the values as described below.

### 1. Backend config — `application.properties`

Copy [application.properties-TEMPLATE](backend%2Fsrc%2Fmain%2Fresources%2Fapplication.properties-TEMPLATE) to
`backend/src/main/resources/application.properties`.

This is the backend's Spring configuration: local database connection plus the **Firebase service account** the backend
uses to verify users' auth tokens and send push notifications.

- Fill in the `firebase.*` values from a Firebase **service account** key:
  [Firebase Console](https://console.firebase.google.com/) → project `magicswissweed-293e2` → ⚙ **Project settings** →
  **Service accounts** → **Generate new private key**. The downloaded JSON contains `project_id`, `private_key_id`,
  `private_key`, `client_email`, `client_id` and `token_uri`.
- The database values already match the local Docker setup below and don't need changing.

### 2. Frontend env — `.env.local`

Copy [.env.local-TEMPLATE](frontend%2F.env.local-TEMPLATE) to `frontend/.env.local`.

These are build-time variables baked into the React app (Create React App only exposes vars prefixed with `REACT_APP_`).

- `REACT_APP_APIKEY`, `REACT_APP_AUTHDOMAIN`, `REACT_APP_PROJECTID`, `REACT_APP_STORAGEBUCKET`,
  `REACT_APP_MESSAGINGSENDERID`, `REACT_APP_APPID` — the Firebase **web app** config (used for client-side login).
  Get them from [Firebase Console](https://console.firebase.google.com/) → **Project settings** → **General** → **Your
  apps** → the web app → **SDK setup and configuration**.
- `REACT_APP_GOOGLE_MAPS_API_KEY` — the key for the Google Maps window (station/spot maps). Get it from the maintainers. This is a public client-side key (it ships in the browser bundle), so it should be restricted by
  HTTP referrer in the Cloud Console rather than kept secret.

### 3. HTTP client secrets — `http-client.private.env.json`

The `.http` files in [http-client/](http-client/) are used to manually call APIs from the IDE
(IntelliJ / VS Code REST Client). They are grouped per API, each directory with its own environments:

- [http-client/msw/](http-client/msw/) — our own backend (`local` / `dev` / `prd`).
  Copy [http-client.private.env.json-TEMPLATE](http-client%2Fmsw%2Fhttp-client.private.env.json-TEMPLATE) to
  `http-client/msw/http-client.private.env.json`.
    - `firebaseApiKey` — same Firebase web API key as `REACT_APP_APIKEY` above.
    - `email` / `password` — a test user that exists in **Firebase Console → Authentication**; used to obtain a login
      token.
- [http-client/rivermap/](http-client/rivermap/) — the external [Rivermap API](https://api.rivermap.org/) (`prod` only).
  Copy [http-client.private.env.json-TEMPLATE](http-client%2Frivermap%2Fhttp-client.private.env.json-TEMPLATE) to
  `http-client/rivermap/http-client.private.env.json`.
    - `rivermapApiKey` — the Rivermap API key. Get it from the maintainers.

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
