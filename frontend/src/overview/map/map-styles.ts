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
];

export const darkMapStyle = [
    {elementType: "geometry", stylers: [{color: "#111111"}]},
    {elementType: "labels.text.fill", stylers: [{color: "#c8c7c7"}]},
    {elementType: "labels.text.stroke", stylers: [{color: "#3c4049"}]},

    {
        featureType: "water",
        elementType: "geometry",
        stylers: [{color: mswBlue}],
    },
    {
        featureType: "road",
        elementType: "geometry",
        stylers: [{color: "#626978"}],
    },
];
