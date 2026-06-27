// use https://snazzymaps.com/explore to handle styles

const mswBlue = getComputedStyle(document.documentElement)
    .getPropertyValue('--msw-blue')
    .trim();

export const lightMapStyle = [
    {
        featureType: "water",
        elementType: "geometry",
        stylers: [{color: mswBlue}],
    },
    {
        "featureType": "landscape",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#f5f5f5"
            },
            {
                "lightness": 20
            }
        ]
    },
    {
        "featureType": "road.highway",
        "elementType": "geometry.fill",
        "stylers": [
            {
                "color": "#d1d1d1"
            },
            {
                "lightness": 17
            }
        ]
    },
    {
        "featureType": "road.highway",
        "elementType": "geometry.stroke",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "lightness": 29
            },
            {
                "weight": 0.2
            }
        ]
    },
    {
        "featureType": "road.arterial",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "lightness": 18
            }
        ]
    },
    {
        "featureType": "road.local",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#ffffff"
            },
            {
                "lightness": 16
            }
        ]
    },
    {
        "featureType": "poi",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#f5f5f5"
            },
            {
                "lightness": 21
            }
        ]
    },
    {
        "featureType": "poi.park",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#dedede"
            },
            {
                "lightness": 21
            }
        ]
    },
    {
        "featureType": "poi.park",
        "elementType": "labels",
        "stylers": [
            {
                "visibility": "off"
            }
        ]
    },
    {
        "elementType": "labels.text.stroke",
        "stylers": [
            {
                "visibility": "on"
            },
            {
                "color": "#ffffff"
            },
            {
                "lightness": 16
            }
        ]
    },
    {
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "saturation": 36
            },
            {
                "color": "#333333"
            },
            {
                "lightness": 40
            }
        ]
    },
    {
        "elementType": "labels.icon",
        "stylers": [
            {
                "visibility": "off"
            }
        ]
    },
    {
        "featureType": "transit",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#d1d1d1"
            },
            {
                "lightness": 19
            }
        ]
    },
    {
        "featureType": "administrative",
        "elementType": "geometry.stroke",
        "stylers": [
            {
                "color": "#7c7c7c"
            },
            {
                "weight": 1.2
            }
        ]
    },
    {
        "featureType": "road.highway",
        "elementType": "labels",
        "stylers": [
            {
                "visibility": "off"
            }
        ]
    }
];

export const darkMapStyle = [
    {elementType: "geometry", stylers: [{color: "#111111"}]},
    {elementType: "labels.text.fill", stylers: [{color: "#cccccc"}]},
    {elementType: "labels.text.stroke", stylers: [{visibility: "on"}, {color: "#000000"}]},
    {elementType: "labels.icon", stylers: [{visibility: "off"}]},
    {
        featureType: "water",
        elementType: "geometry",
        stylers: [{color: mswBlue}],
    },
    {
        featureType: "water",
        elementType: "labels.text.fill",
        stylers: [{color: "#ffffff"}],
    },
    {
        featureType: "water",
        elementType: "labels.text.stroke",
        stylers: [{color: "#000000"}, {weight: 3}],
    },
    {
        featureType: "landscape",
        elementType: "geometry",
        stylers: [{color: "#0a0a0a"}],
    },
    {
        featureType: "road.highway",
        elementType: "geometry.fill",
        stylers: [{color: "#2e2e2e"}],
    },
    {
        featureType: "road.highway",
        elementType: "geometry.stroke",
        stylers: [{color: "#000000"}, {weight: 0.2}],
    },
    {
        featureType: "road.arterial",
        elementType: "geometry",
        stylers: [{color: "#1a1a1a"}],
    },
    {
        featureType: "road.local",
        elementType: "geometry",
        stylers: [{color: "#1a1a1a"}],
    },
    {
        featureType: "poi",
        elementType: "geometry",
        stylers: [{color: "#0a0a0a"}],
    },
    {
        featureType: "poi.park",
        elementType: "geometry",
        stylers: [{color: "#212121"}],
    },
    {
        featureType: "poi.park",
        elementType: "labels",
        stylers: [{visibility: "off"}],
    },
    {
        featureType: "transit",
        elementType: "geometry",
        stylers: [{color: "#2e2e2e"}],
    },
    {
        featureType: "administrative",
        elementType: "geometry.stroke",
        stylers: [{color: "#7c7c7c"}, {weight: 1.2}],
    },
    {
        featureType: "road.highway",
        elementType: "labels",
        stylers: [{visibility: "off"}],
    },
];
