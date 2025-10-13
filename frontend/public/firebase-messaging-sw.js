// This file is registered in the browser and stays active even if the page is closed.
// The browser has to stay active to pick up the notifications..

// Force the new SW to become active immediately…
self.addEventListener("install", (e) => {
    self.skipWaiting();
});

self.addEventListener("push", function (event) {
    let title = "Surf's up";
    let body = "There are updates from magicswissweed";

    if (event.data) {
        const data = JSON.parse(event.data.text()).data;
        title = data.title;
        body = data.body;
    }

    event.waitUntil(
        self.registration.showNotification(title, {body: body,})
    );
});

self.addEventListener("notificationclick", function (event) {
    event.notification.close(); // Close the notification when clicked

    self.clients.openWindow(self.location.origin);
});
