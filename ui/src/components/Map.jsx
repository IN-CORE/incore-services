import React, {Component} from "react";
import config from "../app.config";
import {getHeader} from "../actions";

let ol = require("openlayers");
require("openlayers/css/ol.css");

let tileAttribution = "Tiles © <a href=\"https://services.arcgisonline.com/ArcGIS/" +
	"rest/services/NatGeo_World_Map/MapServer\">ArcGIS</a> &mdash; National Geographic, Esri, DeLorme, NAVTEQ, " +
	"UNEP-WCMC, USGS, NASA, ESA, METI, NRCAN, GEBCO, NOAA, iPC";

async function fetchExtent(name: string) {

	let parser = new ol.format.WMSCapabilities();
	try {
		const extentRequest = await fetch(`${config.geoServer}?SERVICE=WMS&REQUEST=GetCapabilities`,
			{method: "GET", mode: "cors", headers: getHeader()});
		const text = await extentRequest.text();
		let result = parser.read(text);
		let extent = result.Capability.Layer.Layer.find(l => l.Name === name).EX_GeographicBoundingBox;

		return extent;

	} catch (err) {
		console.log(err);
		return err;
	}
}

async function customLoader(tile, src) {

	let response = await fetch(src, {method: "GET", mode: "cors", headers: getHeader()});
	if (response.ok) {
		let blob = await response.blob();
		let urlCreator = window.URL || window.webkitURL;
		let imageUrl = urlCreator.createObjectURL(blob);
		tile.getImage().src = imageUrl;
	}
}

class Map extends Component {

	constructor(props) {
		super(props);
		this.state = {
			map: new ol.Map({
				view: new ol.View(),
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
				<div id="map" style={{width: "100%", height: "100%", position: "absolute", className: "root"}}/>
			</div>
		);

	}

	async componentDidUpdate() {
		const theMap = this.state.map;
		theMap.setLayerGroup(new ol.layer.Group());

		let sourceTiled = new ol.source.TileWMS({
			visible: false,
			url: config.geoServer,
			tileLoadFunction: customLoader,
			params: {
				"FORMAT": "image/png",
				"VERSION": "1.1.1",
				tiled: true,
				name: "tiledLayer",
				STYLES: "",
				LAYERS: `incore:${this.props.datasetId}`
			}
		});

		let layerTiled = new ol.layer.Tile({
			source: sourceTiled,
			opacity: 0.7
		});

		let mapTile = new ol.layer.Tile({
			source: new ol.source.XYZ({
				attribution: tileAttribution,
				url: "https://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}"
			})
		});
		theMap.addLayer(mapTile);
		theMap.addLayer(layerTiled);

		// snap the map to the hazard bounding box
		// default using the bounding box within dataset
		// if absent, then query geoserver
		if (this.props.boundingBox !== undefined
			&& this.props.boundingBox !== null
			&& this.props.boundingBox.length === 4
			&& this.props.boundingBox[2] > this.props.boundingBox[0]
			&& this.props.boundingBox[3] > this.props.boundingBox[1]
		) {
			let extent = this.props.boundingBox;
			theMap.getView().fit(extent, theMap.getSize());
		}
		else {
			let extent = await fetchExtent(this.props.datasetId);
			theMap.getView().fit(extent, theMap.getSize());
		}
	}

	async componentDidMount() {
		let sourceTiled = new ol.source.TileWMS({
			visible: false,
			url: config.geoServer,
			tileLoadFunction: customLoader,
			params: {
				FORMAT: "image/png",
				VERSION: "1.1.1",
				tiled: true,
				name: "tiledLayer",
				STYLES: "",
				LAYERS: `incore:${this.props.datasetId}`
			}
		});

		let layerTiled = new ol.layer.Tile({
			source: sourceTiled,
			opacity: 0.7
		});

		let mapTile = new ol.layer.Tile({
			source: new ol.source.XYZ({
				attribution: tileAttribution,
				url: "https://server.arcgisonline.com/ArcGIS/rest/services/NatGeo_World_Map/MapServer/tile/{z}/{y}/{x}",
			})
		});

		let layers;
		layers = [
			mapTile,
			layerTiled
		];

		const projection = new ol.proj.Projection({
			code: "EPSG:4326",
			units: "degrees",
			axisOrientation: "neu",
			global: true
		});


		const view = new ol.View({
			projection: projection,
			minZoom: 3,
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

		// snap the map to the hazard bounding box
		// default using the bounding box within dataset
		// if absent, then query geoserver
		if (this.props.boundingBox !== undefined
			&& this.props.boundingBox !== null
			&& this.props.boundingBox.length === 4
			&& this.props.boundingBox[2] > this.props.boundingBox[0]
			&& this.props.boundingBox[3] > this.props.boundingBox[1]
		) {
			let extent = this.props.boundingBox;
			theMap.getView().fit(extent, theMap.getSize());
		}
		else {
			let extent = await fetchExtent(this.props.datasetId);
			theMap.getView().fit(extent, theMap.getSize());
		}

		this.setState({map: theMap});
	}

}

export default Map;
