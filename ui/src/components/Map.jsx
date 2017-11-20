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
		const outputLayerUntiled= new ol.layer.Image({
			source: new ol.source.ImageWMS({
				ratio: 1,
				url: "http://incore2-geoserver.ncsa.illinois.edu:9999/geoserver/incore/wms",
				params: {"FORMAT": "image/png",
					"VERSION": "1.1.1",
					STYLES: "",
					LAYERS: "incore:59f8d08dc7d30d278f25095d",
				}
			})
		});
		let outputLayerTiled = new ol.layer.Tile({
			source: new ol.source.TileWMS({
				visible: false,
				url: "http://incore2-geoserver.ncsa.illinois.edu:9999/geoserver/incore/wms",
				params: {"FORMAT": "image/png",
					"VERSION": "1.1.1",
					tiled: true,
					STYLES: "",
					LAYERS: "incore:59f8d08dc7d30d278f25095d", // TODO: Use this.props.outputDatasetId
					tilesOrigin: `${-90.07376669874641  },${  35.03298062856903}` //TODO: How are we going to get this center
				}
			})
		});

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
			}),
			outputLayerUntiled,
			outputLayerTiled
		];


		const projection = new ol.proj.Projection({
			code: "EPSG:4326",
			units: "degrees",
			axisOrientation: "neu",
			global: true
		});

		const view = new ol.View({
			projection: projection,
			center:  [-90.07376669874641, 35.03298062856903],
			zoom: 10,
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
