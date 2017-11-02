import React from "react";
import GroupList from "../components/GroupList";
import LineChart from "../components/LineChart";
import ThreeDimensionalPlot from "../components/ThreeDimensionalPlot";
import "whatwg-fetch";

import {
	SelectField,
	GridList,
	GridTile,
	Card,
	MenuItem,
	TextField,
	Divider,
	IconButton,
	RaisedButton
} from "material-ui";
import ActionSearch from "material-ui/svg-icons/action/search";

// utils
import chartSampler from "../utils/chartSampler";

// config
import chartConfig from "../components/config/ChartConfig";

// application configuration
import config from "../app.config";
import DistributionTable from "./DistributionTable";
import CustomExpressionTable from "./CustomExpressionTable";

class FragilityExplorerPage extends React.Component {
	constructor(props) {
		super(props);

		this.state = {
			selectedInventory: null,
			selectedHazard: null,
			selectedDemand: null,
			selectedAuthor: null,
			fragility: null,
			data: [],
			chartConfig: chartConfig.FragilityConfig,
			plotData3d: [],
			is3dPlot: false
		};

		this.clickFragility = this.clickFragility.bind(this);
		this.handleAuthorSelection = this.handleAuthorSelection.bind(this);
		this.handleInventorySelection = this.handleInventorySelection.bind(this);
		this.handleHazardSelection = this.handleHazardSelection.bind(this);
		this.handleDemandSelection = this.handleDemandSelection.bind(this);

		this.handleKeyPressed = this.handleKeyPressed.bind(this);
		this.searchFragilities = this.searchFragilities.bind(this);

		this.exportJson = this.exportJson.bind(this);
	}

	async componentDidMount() {
		let host = config.fragilityService;
		let fragilityId = this.props.params.id;

		let url = host;
		if (fragilityId !== undefined) {
			url = `${host}/${fragilityId}`;
		}

		let response = await fetch(url, {method: "GET", mode: "cors"});

		if (response.ok) {
			let fragilities = await response.json();

			// By default select the first returned in the list of fragilities
			if (fragilities.length > 0) {
				await this.clickFragility(fragilities[0]);
			} else {
				await this.clickFragility(fragilities); // only one fragility (get by id called)
			}

			this.setState({
				data: fragilities
			});
		} else {
			this.setState({
				fragility: null,
				data: []
			});
		}
	}

	async handleInventorySelection(event, index, value) {
		await this.setState({selectedInventory: value});
		await this.queryFragilities();
	}

	async handleHazardSelection(event, index, value) {
		await this.setState({selectedHazard: value});
		await this.queryFragilities();
	}

	async handleDemandSelection(event, index, value) {
		await this.setState({selectedDemand: value});
		await this.queryFragilities();
	}

	async handleAuthorSelection(event, index, value) {
		await this.setState({selectedAuthor: value});
		await this.queryFragilities();
	}

	async handleKeyPressed(event) {
		if (event.charCode === 13) { // enter
			event.preventDefault();
			await this.searchFragilities();
		}
	}

	async searchFragilities() {
		let searchText = this.refs.searchBox.getValue();

		let host = config.fragilityService;

		let url = `${host}/search?text=${searchText}`;

		let response = await fetch(url, {method: "GET", mode: "cors"});

		if (response.ok) {
			let fragilities = await response.json();

			if (fragilities.length > 0) {
				// By default select the first returned in the list of fragilities
				await this.clickFragility(fragilities[0]);
				this.setState({
					data: fragilities
				});
			} else {
				this.setState({
					data: [],
					fragility: null
				});
			}
		} else {
			this.setState({
				data: [],
				fragility: null
			});
		}
	}

	async queryFragilities() {
		let host = config.fragilityService;

		let url = "";

		if (this.state.selectedInventory !== null && this.state.selectedHazard !== null) {
			url = `${host}/query?inventory=${this.state.selectedInventory}&hazard=${this.state.selectedHazard}`;
		} else if (this.state.selectedInventory !== null) {
			url = `${host}/query?inventory=${this.state.selectedInventory}`;
		} else if (this.state.selectedHazard !== null) {
			url = `${host}/query?hazard=${this.state.selectedHazard}`;
		} else {
			url = `${host}`;
		}

		let response = await fetch(url, {method: "GET", mode: "cors"});

		if (response.ok) {
			let fragilities = await response.json();

			if (fragilities.length > 0) {
				// By default select the first returned in the list of fragilities
				await this.clickFragility(fragilities[0]);

				this.setState({
					data: fragilities
				});
			} else {
				this.setState({
					data: [],
					fragility: null
				});
			}
		} else {
			this.setState({
				data: [],
				fragility: null
			});
		}
	}

	render() {
		return (
			<div style={{padding: "20px"}}>
				<div style={{display: "flex"}}>
					<h2>Fragility Function Viewer</h2>
					<div style={{marginLeft: "auto"}}>
						<RaisedButton primary={true} style={{display: "inline-block"}} label="Export to JSON"
									  onClick={this.exportJson} />
						{/*<RaisedButton primary={true} style={{display: "inline-block"}} label="Export to NHML" />*/}
					</div>
				</div>
				<GridList cols={12} cellHeight="auto">
					{/* Inventory Type */}
					<GridTile cols={2}>
						<SelectField hintText="Inventory Type" value={this.state.selectedInventory}
									 onChange={this.handleInventorySelection}>
							<MenuItem primaryText="Building" value="Building" />
							<MenuItem primaryText="Bridge" value="Bridge" />
							<Divider />
							<MenuItem primaryText="Roadway" value="Roadway" />
							<MenuItem primaryText="Railway" value="Railway" />
							<Divider />
							<MenuItem primaryText="Electric Power Network" value="Electrical Facility" />
							<MenuItem primaryText="Potable Water Network" value="Buried Pipeline" />
						</SelectField>
					</GridTile>

					{/* Hazard Type */}
					<GridTile cols={2}>
						<SelectField hintText="Hazard Type" value={this.state.selectedHazard}
									 onChange={this.handleHazardSelection}>
							<MenuItem primaryText="Earthquake" value="Seismic" />
							<MenuItem primaryText="Tornado" value="Tornado" />
							<MenuItem primaryText="Tsunami" value="Tsunami" />
						</SelectField>
					</GridTile>

					<GridTile cols={8} style={{float: "right"}}>
						<TextField ref="searchBox" hintText="Search Fragilities" onKeyPress={this.handleKeyPressed} />
						<IconButton iconStyle={{position: "absolute", left: 0, bottom: 5, width: 30, height: 30}}
									onClick={this.searchFragilities}>
							<ActionSearch />
						</IconButton>
					</GridTile>
				</GridList>

				<GridList cols={12} style={{paddingTop: "10px"}} cellHeight="auto">
					<GridTile cols={6}>
						<GroupList id="fragility-list" onClick={this.clickFragility} height="800px"
								   data={this.state.data} displayField="author" />
					</GridTile>

					{/* TODO replace with new panel component, should take in fragility parameter, replace click with state */}
					<GridTile cols={6} rows={2}>
						{this.state.fragility !== null ?
							<div>
								<Card>
									{this.state.is3dPlot ?
										<ThreeDimensionalPlot plotId="3dplot" data={this.state.plotData3d}
															  xLabel={this.state.fragility.demandType} yLabel="Y"
															  zLabel={this.state.fragility.fragilityCurves[0].description}
															  width="100%" height="400px" style="surface" />
										:
										<LineChart chartId="chart" configuration={this.state.chartConfig} />}
								</Card>
								{this.state.fragility.fragilityCurves[0].className.includes("CustomExpressionFragilityCurve") ?
									<CustomExpressionTable fragility={this.state.fragility} />
									:
									<DistributionTable fragility={this.state.fragility} />}
							</div>
							:
							<div></div>
						}
					</GridTile>
				</GridList>
			</div>
		);
	}

	async clickFragility(fragility) {
		let is3dPlot = this.is3dFragility(fragility);

		let plotData3d = [];
		let plotConfig2d = {};

		if (is3dPlot) {
			plotData3d = await this.generate3dPlotData(fragility);
		} else {
			plotConfig2d = this.generate2dPlotData(fragility);
		}

		this.setState({
			chartConfig: plotConfig2d,
			plotData3d: plotData3d,
			is3dPlot: is3dPlot,
			fragility: fragility
		});
	}

	generate2dPlotData(fragility) {
		let updatedChartConfig = Object.assign({}, chartConfig.FragilityConfig);

		let demandType = fragility.demandType !== null ? fragility.demandType : "";
		let demandUnit = fragility.demandUnits !== null ? fragility.demandUnits : "";
		let description = fragility.description !== null ? fragility.description : "";
		let authors = fragility.authors.join(", ");

		updatedChartConfig.xAxis.title.text = `${demandType} (${demandUnit})`;
		updatedChartConfig.title.text = `${description} [${authors}]`;

		updatedChartConfig.series = [];

		for (let i = 0; i < fragility.fragilityCurves.length; i++) {
			let curve = fragility.fragilityCurves[i];

			let plotData;

			if (curve.className.includes("CustomExpressionFragilityCurve")) {
				plotData = chartSampler.computeExpressionSamples(0, 1.0, 90, curve.expression);
			} else if (curve.className.includes("StandardFragilityCurve")) {
				if (curve.curveType === "Normal") { // Actually Log Normal
					plotData = chartSampler.sampleLogNormalCdf(0, 0.999, 1000, curve.median, curve.beta);
				}

				if (curve.curveType === "StandardNormal") {
					plotData = chartSampler.sampleNormalCdf(0, 0.999, 1000, curve.median, curve.beta);
				}

				if (curve.curveType === "LogNormal") { // Log Normal with Normal mean and Normal variance
					plotData = chartSampler.sampleLogNormalAlternate(0, 0.999, 1000, curve.median, curve.beta);
				}
			} else if (curve.className.includes("periodStandardFragilityCurve")) {

			} else if (curve.className.includes("buildingPeriodStandardFragilityCurve")) {

			} else {

			}

			let series = {
				name: curve.description,
				data: plotData
			};

			updatedChartConfig.series.push(series);
		}

		return updatedChartConfig;
	}

	async generate3dPlotData(fragility) {
		let curve = fragility.fragilityCurves[0];

		let plotData = await chartSampler.computeExpressionSamples3d(0.001, 1.0, 50, 0.001, 1.0, 50, curve.expression);

		return plotData;
	}

	is3dFragility(fragility) {
		let curves = fragility.fragilityCurves;

		for (let i = 0; i < curves.length; i++) {
			let curve = curves[i];

			if (curve.className.includes("CustomExpressionFragilityCurve") && curve.expression.includes("y")) {
				return true;
			}
		}

		return false;
	}

	exportJson() {
		let fragilityJSON = JSON.stringify(this.state.fragility, null, 4);
		let blob = new Blob([fragilityJSON], {type: "application/json"});

		const filename = `${this.state.fragility.id}.json`;

		if (window.navigator.msSaveOrOpenBlob) {
			window.navigator.msSaveBlob(blob, filename);
		} else {
			let anchor = window.document.createElement("a");
			anchor.href = window.URL.createObjectURL(blob);
			anchor.download = filename;
			document.body.appendChild(anchor);
			anchor.click();
			document.body.removeChild(anchor);
		}
	}
}

FragilityExplorerPage.propTypes = {};

export default FragilityExplorerPage;
