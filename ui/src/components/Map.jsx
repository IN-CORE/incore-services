import React, {Component} from "react";
let ol = require("openlayers");
require("openlayers/css/ol.css");

class Map extends Component {

	constructor(props) {
		super(props);
		this.state = {
			map: new ol.Map({
				view: new ol.View({
					center: [0, 0],
					zoom: 1
				}),
				layers: [
					new ol.layer.Tile({
						source: new ol.source.OSM()
					})
				],
				target: "map"
			})
		};
	}

	render() {
		return (
			<div>
				<div id="map" className="root"/>
			</div>
		);
	}

	componentDidMount() {

		let layers;
		layers = [
			new ol.layer.Tile({
				source: new ol.source.XYZ({
					attribution: [new ol.Attribution({
						html: "Tiles © <a href=\"https://services.arcgisonline.com/ArcGIS/" +
						"rest/services/NatGeo_World_Map/MapServer\">ArcGIS</a> &mdash; National Geographic, Esri, DeLorme, NAVTEQ, " +
						"UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC"
					})],
					url: "https://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}"
				})
			})
		];

		let view = new ol.View({
			projection: "EPSG:4326",
			center:  [-84.44799549, 38.9203417],
			zoom: 5.5,
			minZoom: 5.5,
			maxZoom: 12
		});

		let theMap;
		theMap = new ol.Map({
			target: "map",
			layers: layers,
			view: view,
			controls: ol.control.defaults({
				attributionOptions: ({
					collapsible: false
				})
			})
		});

		this.setState({map: theMap});
	}

}

export default Map;
