// TODO: use https://snazzymaps.com/explore to handle styles
export const lightMapStyle = [];

export const darkMapStyle = [
    {elementType: "geometry", stylers: [{color: "#111111"}]},
    {elementType: "labels.text.fill", stylers: [{color: "#c8c7c7"}]},
    {elementType: "labels.text.stroke", stylers: [{color: "#3c4049"}]},

    {
        featureType: "water",
        elementType: "geometry",
        stylers: [{color: "#4097bc"}],
    },
    {
        featureType: "road",
        elementType: "geometry",
        stylers: [{color: "#626978"}],
    },
];
