import React, {Component} from "react";
let ol = require("openlayers");
require("openlayers/css/ol.css");

class Map extends Component {

	constructor(props) {
		super(props);
		this.state = {
			map: new ol.Map()
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

		let layers = [
			new ol.layer.Tile({
				source: new ol.source.XYZ({
					attributions: [new ol.Attribution({
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
			center: this.state.center,
			view: view,
			zoom: this.state.currentZoom,
			minZoom: 5.5,
			maxZoom: this.state.maxZoom
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
