// TODO: use https://snazzymaps.com/explore to handle styles
export const lightMapStyle = [];

export const darkMapStyle = [
    {elementType: "geometry", stylers: [{color: "#1d1f23"}]},
    {elementType: "labels.text.fill", stylers: [{color: "#8a8a8a"}]},
    {elementType: "labels.text.stroke", stylers: [{color: "#1d1f23"}]},

    {
        featureType: "water",
        elementType: "geometry",
        stylers: [{color: "#0f252e"}],
    },
    {
        featureType: "road",
        elementType: "geometry",
        stylers: [{color: "#2c2f36"}],
    },
];
